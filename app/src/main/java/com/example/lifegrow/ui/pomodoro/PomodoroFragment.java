package com.example.lifegrow.ui.pomodoro;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.example.lifegrow.R;
import com.example.lifegrow.databinding.FragmentPomodoroBinding;
import android.media.MediaPlayer;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
public class PomodoroFragment extends Fragment implements LifecycleObserver {

    private long appLeftTimestamp = 0; // Track when the app was left
    private static final long RESET_THRESHOLD = 5000; // 5 seconds
    private MediaPlayer tickingSound; // Your ticking sound

    @Override
    public void onStart() {
        super.onStart();

        if (appLeftTimestamp > 0) {
            long timeGone = System.currentTimeMillis() - appLeftTimestamp;
            if (timeGone > RESET_THRESHOLD) {
                resetTimer(); // Reset the timer if gone for more than 5 sec
                stopSound(); // Stop sound immediately
                //Toast.makeText(requireContext(), "Timer reset due to inactivity", Toast.LENGTH_SHORT).show();
            } else {
                resumeSound(); // Resume sound if within 5 sec
            }
        }
        appLeftTimestamp = 0; // Reset timestamp
    }

    @Override
    public void onStop() {
        super.onStop();
        appLeftTimestamp = System.currentTimeMillis(); // Save timestamp when leaving
        pauseSound(); // Pause sound when leaving
    }

    private void pauseSound() {
        if (tickingSound != null && tickingSound.isPlaying()) {
            tickingSound.pause();
        }
    }

    private void stopSound() {
        if (tickingSound != null) {
            tickingSound.stop();
            tickingSound.release();
            tickingSound = null;
        }
    }

    private void resumeSound() {
        if (tickingSound == null) {
            // Reinitialize MediaPlayer if it was released
            tickingSound = MediaPlayer.create(requireContext(), R.raw.tic);
            tickingSound.setLooping(true);
        }

        if (!tickingSound.isPlaying()) {
            if(isTimerRunning){
                if(!isBreakTime) {
                    tickingSound.start();
                }
           }

        }
    }


    private FragmentPomodoroBinding binding;
    private TextView timerTextView;
    private ImageButton startPauseButton, resetButton, skipBreakButton;
    private CircularTimerView timerView;
    private CountDownTimer countDownTimer;
    private TextView focusTextView;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private static final long ONE_MINUTE = 60 * 1000;
    private static final long DEFAULT_WORK_TIME = 25 * ONE_MINUTE;
    private static final long SHORT_BREAK_TIME = 5 * ONE_MINUTE;
    private static final long LONG_BREAK_TIME = 15 * ONE_MINUTE;

    private long workTime = DEFAULT_WORK_TIME;
    private long timeLeftInMillis = workTime;
    private boolean isTimerRunning = false;
    private int sessionCount = 0;
    private boolean isBreakTime = false;

    private MediaPlayer bellSound;
    private String selectedTaskId = null;  // Store selected Task ID
    private GestureDetector gestureDetector;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPomodoroBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        focusTextView = binding.focusTextView;
        timerTextView = binding.timerTextView;
        timerView = binding.timerView;
        startPauseButton = binding.startPauseButton;
        resetButton = binding.resetButton;
        skipBreakButton = binding.skipBreakButton;

        View rootView = binding.getRoot();
        ViewGroup rootLayout = rootView.findViewById(R.id.pomodoro); // Ensure you have the correct parent layout

        gestureDetector = new GestureDetector(requireContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                Log.d("Gesture", "Double-tap detected");
                rootLayout.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.black));
                return true;
            }
        });

        rootLayout.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

        focusTextView.setOnClickListener(v -> fetchPendingTasks());
        timerTextView.setOnClickListener(v -> showTimerSelectionDialog());
        startPauseButton.setOnClickListener(v -> toggleTimer());
        resetButton.setOnClickListener(v -> resetTimer());
        skipBreakButton.setOnClickListener(v -> skipBreak());

        bellSound = MediaPlayer.create(requireContext(), R.raw.bell);

        // Load saved duration
        workTime = getSavedWorkDuration();
        timeLeftInMillis = workTime;
        timerView.setTotalTime((int) (workTime / 1000));

        updateTimerUI();
        return root;
    }

    // Function to retrieve saved work duration
    private long getSavedWorkDuration() {
        return requireActivity().getSharedPreferences("PomodoroPrefs", 0)
                .getLong("work_duration", DEFAULT_WORK_TIME);
    }

    private void toggleTimer() {
        if (isTimerRunning) {
            pauseTimer();
        } else {
            startTimer();
        }
    }

    private void fetchPendingTasks() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Log.e("Firestore", "User not authenticated.");
            return;
        }

        String userId = user.getEmail().replace(".", "_");

        db.collection("users").document(userId).collection("tasks")
                .whereNotEqualTo("status", "Done")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<String> taskNames = new ArrayList<>();
                        List<String> taskIds = new ArrayList<>();

                        // Get today's date without time
                        Calendar todayCal = Calendar.getInstance();
                        todayCal.set(Calendar.HOUR_OF_DAY, 0);
                        todayCal.set(Calendar.MINUTE, 0);
                        todayCal.set(Calendar.SECOND, 0);
                        todayCal.set(Calendar.MILLISECOND, 0);
                        Date todayDate = todayCal.getTime();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String taskName = document.getString("name");
                            if (taskName != null) {
                                Timestamp startTs = document.getTimestamp("startdatetime");
                                Timestamp endTs = document.getTimestamp("enddatetime");

                                boolean include = false;

                                if (startTs != null && endTs != null) {
                                    Date startDate = stripTime(startTs.toDate());
                                    Date endDate = stripTime(endTs.toDate());
                                    if (!todayDate.before(startDate) && !todayDate.after(endDate)) {
                                        include = true;
                                    }
                                } else if (startTs != null) {
                                    Date startDate = stripTime(startTs.toDate());
                                    if (todayDate.equals(startDate)) {
                                        include = true;
                                    }
                                } else {
                                    // Include tasks with no startdatetime
                                    include = true;
                                }

                                if (include) {
                                    taskNames.add(taskName);
                                    taskIds.add(document.getId());
                                }
                            }
                        }

                        if (taskNames.isEmpty()) {
                            Toast.makeText(requireContext(), "No pending tasks", Toast.LENGTH_SHORT).show();
                        } else {
                            showTaskSelectionDialog(taskNames, taskIds);
                        }
                    } else {
                        Log.e("Firestore", "Error fetching tasks", task.getException());
                        Toast.makeText(requireContext(), "Failed to fetch tasks", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private Date stripTime(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    // Reuse this utility method


    private void showTaskSelectionDialog(List<String> taskNames, List<String> taskIds) {
        if (taskNames.isEmpty()) {
            Toast.makeText(requireContext(), "No pending tasks", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Select a Task")
                .setItems(taskNames.toArray(new String[0]), (dialog, which) -> {
                    selectedTaskId = taskIds.get(which); // Store selected task ID
                    focusTextView.setText(taskNames.get(which)+" >");
                })
                .show();

    }


    private void startTimer() {
        if (tickingSound == null) {
            tickingSound = MediaPlayer.create(requireContext(), R.raw.tic);
            tickingSound.setLooping(true); // Loop the ticking sound
        }
        tickingSound.start(); // Start ticking sound

        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateTimerUI();
            }

            @Override
            public void onFinish() {
                stopTickingSound(); // Stop ticking sound when the timer ends
                onSessionComplete();
            }
        }.start();

        isTimerRunning = true;
        startPauseButton.setImageResource(R.drawable.ic_pause);
    }

    private void pauseTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        stopTickingSound(); // Stop sound when paused
        isTimerRunning = false;
        startPauseButton.setImageResource(R.drawable.ic_play);
    }


    private void resetTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        stopTickingSound(); // Stop sound when reset

        timeLeftInMillis = isBreakTime
                ? ((sessionCount % 4 == 0) ? LONG_BREAK_TIME : SHORT_BREAK_TIME)
                : workTime;
        isTimerRunning = false;
        updateTimerUI();
        startPauseButton.setImageResource(R.drawable.ic_play);
    }

    private void stopTickingSound() {
        if (tickingSound != null && tickingSound.isPlaying()) {
            tickingSound.pause();
            tickingSound.seekTo(0);
        }
    }




    // When a session (work or break) finishes:
    private void onSessionComplete() {
        if (!isBreakTime) {
            // Play bell sound when work session ends
            if (bellSound != null) {
                bellSound.start();
            }

            // Check if it's time for a long break (every 4th session)
            if ((sessionCount + 1) % 4 == 0) {  // Check next session count before incrementing
                timeLeftInMillis = LONG_BREAK_TIME;
            } else {
                timeLeftInMillis = SHORT_BREAK_TIME;
            }

            sessionCount++;  // Increment session count
            isBreakTime = true;
            Toast.makeText(requireContext(), "Work session complete! " + sessionCount, Toast.LENGTH_SHORT).show();
            startPauseButton.setImageResource(R.drawable.ic_play);

            // Increment Firestore task counter if applicable
            if (selectedTaskId != null) {
                incrementPomodoroCount(selectedTaskId);
            }

            // Update the circular timer's total time to match the new break duration
            timerView.setTotalTime((int)(timeLeftInMillis / 1000));
        } else {
            isBreakTime = false;
            timeLeftInMillis = workTime;
            Toast.makeText(requireContext(), "Break complete!", Toast.LENGTH_SHORT).show();
            bellSound.start();
            startPauseButton.setImageResource(R.drawable.ic_play);

            // Reset circular timer to the selected work duration
            timerView.setTotalTime((int)(workTime / 1000));
        }

        updateTimerUI();
    }


    private void incrementPomodoroCount(String taskId) {
        FirebaseUser user = auth.getCurrentUser();
        String userId = user.getEmail().replace(".", "_");
        DocumentReference taskRef = db.collection("users").document(userId)
                .collection("tasks").document(taskId);

        taskRef.update("pomodoroCount", FieldValue.increment(1))
                .addOnSuccessListener(aVoid -> Toast.makeText(requireContext(), "Pomodoro count updated!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to update task", Toast.LENGTH_SHORT).show());
        selectedTaskId=null;
    }


    // Skip break button action:
    private void skipBreak() {
        if (isBreakTime) {
            if (countDownTimer != null) {
                countDownTimer.cancel();  // Stop current timer
            }
            isBreakTime = false;
            timeLeftInMillis = workTime; // Reset time to work session
            stopTickingSound();
            // Update circular timer's total time to the work session duration
            timerView.setTotalTime((int)(workTime / 1000));

            updateTimerUI();

            startPauseButton.setImageResource(R.drawable.ic_play);
            isTimerRunning = false; // Ensure the timer is not running

            Toast.makeText(requireContext(), "Break skipped! Back to work.", Toast.LENGTH_SHORT).show();
        }
    }


    private void updateTimerUI() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        String timeFormatted = String.format("%02d:%02d", minutes, seconds);
        timerTextView.setText(timeFormatted);
        int remainingSeconds = (int) (timeLeftInMillis / 1000);
        timerView.setTimeLeft(remainingSeconds);

        // Show skip button only during break sessions.
        skipBreakButton.setVisibility(isBreakTime ? View.VISIBLE : View.GONE);
    }

    private void showTimerSelectionDialog() {
        String[] timeOptions = {"1:00", "10:00", "25:00", "45:00"};
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Select Work Duration")
                .setItems(timeOptions, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            workTime = ONE_MINUTE;
                            break;
                        case 1:
                            workTime = 10 * ONE_MINUTE;
                            break;
                        case 2:
                            workTime = 25 * ONE_MINUTE;
                            break;
                        case 3:
                            workTime = 45 * ONE_MINUTE;
                            break;
                    }

                    // Save to SharedPreferences
                    saveWorkDuration(workTime);

                    timeLeftInMillis = workTime;
                    timerView.setTotalTime((int) (workTime / 1000));
                    if (isTimerRunning) {
                        resetTimer();  // Ensure this stops the countdown
                    }

                    //showTimerSelectionDialog(); // Allow user to select new timer

                    // Ensure the timer does not auto-start after selection
                    isTimerRunning = false;
                    updateTimerUI();
                });
        builder.show();
    }

    // Function to save work duration
    private void saveWorkDuration(long duration) {
        requireActivity().getSharedPreferences("PomodoroPrefs", 0)
                .edit()
                .putLong("work_duration", duration)
                .apply();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (tickingSound != null) {
            tickingSound.release();
            tickingSound = null;
        }

        if (bellSound != null) {
            bellSound.release();
            bellSound = null;
        }

        binding = null;
    }

}


