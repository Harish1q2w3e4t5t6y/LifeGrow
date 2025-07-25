package com.example.lifegrow.ui.others.progress;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lifegrow.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Date;

public class ProgressActivity extends AppCompatActivity {

    private TextView taskCompletionText;
    private TextView pomodoroCountText;
    private TaskCompletionCircleView taskCompletionCircle;
    private BarGraphView pomodoroBar;
    private EisenhowerView eisenhowerView; // Add your EisenhowerView

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        taskCompletionText = findViewById(R.id.taskCompletionText);
        pomodoroCountText = findViewById(R.id.pomodoroCountText);
        taskCompletionCircle = findViewById(R.id.taskCompletionCircle);
        pomodoroBar = findViewById(R.id.pomodoroGraph);
        eisenhowerView = findViewById(R.id.eisenhowerView); // Make sure you have this in your XML

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        listenToProgressData();
    }

    private void listenToProgressData() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getEmail().replace(".", "_");
        CollectionReference taskRef = db.collection("users").document(userId).collection("tasks");

        taskRef.addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (e != null || queryDocumentSnapshots == null) {
                Log.e("ProgressActivity", "Error fetching tasks", e);
                return;
            }

            int done = 0, pending = 0;
            int pomodoroTotal = 0;
            int[] pomodoroByDay = new int[7];

            // Variables for Eisenhower progress (0 to 3)
            int[] eisenhowerCompleted = new int[4];
            int[] eisenhowerTotal = new int[4];

            Calendar today = Calendar.getInstance();

            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                String status = doc.getString("status");
                String category = doc.getString("category");
                Long pomodoro = doc.getLong("pomodoroCount");
                Date createdAt = doc.getDate("createdAt");

                // Overall task counts
                if ("Done".equalsIgnoreCase(status)) {
                    done++;
                } else {
                    pending++;
                }

                if (pomodoro != null) {
                    pomodoroTotal += pomodoro;
                }

                // Pomodoro count by day (for the last 7 days)
                if (pomodoro != null && createdAt != null) {
                    Calendar taskDate = Calendar.getInstance();
                    taskDate.setTime(createdAt);
                    int diff = (int) ((today.getTimeInMillis() - taskDate.getTimeInMillis()) / (1000 * 60 * 60 * 24));
                    if (diff >= 0 && diff < 7) {
                        pomodoroByDay[6 - diff] += pomodoro;
                    }
                }

                // Process Eisenhower categories.
                if (category != null) {
                    int index = getCategoryIndex(category);
                    if (index != -1) {
                        eisenhowerTotal[index]++;
                        if ("Done".equalsIgnoreCase(status)) {
                            eisenhowerCompleted[index]++;
                        }
                    }
                }
            }

            int totalTasks = done + pending;

            // Update overall progress view and Pomodoro graph.
            taskCompletionCircle.setProgress(done, totalTasks);
            pomodoroBar.setData(pomodoroByDay, getMax(pomodoroByDay));

            taskCompletionText.setText("Completed: " + done + ", Pending: " + pending);
            pomodoroCountText.setText("Pomodoros Completed: " + pomodoroTotal);

            // Debug: Show counts for Eisenhower categories.
            String debugMessage = "Eisenhower Totals:\n" +
                    "Urgent & Important: " + eisenhowerTotal[0] + "\n" +
                    "Not Urgent & Important: " + eisenhowerTotal[1] + "\n" +
                    "Urgent & Not Important: " + eisenhowerTotal[2] + "\n" +
                    "Not Urgent & Not Important: " + eisenhowerTotal[3];
            //Toast.makeText(ProgressActivity.this, debugMessage, Toast.LENGTH_LONG).show();

            // Update the Eisenhower view with the computed progress.
            if (eisenhowerView != null) {
                eisenhowerView.setEisenhowerProgress(eisenhowerCompleted, eisenhowerTotal);
            }
        });
    }


    // Maps a task's category string to an index (0-3).
    private int getCategoryIndex(String category) {
        switch (category) {
            case "Urgent Important":
                return 0;
            case "Not Urgent Important":
                return 1;
            case "Urgent Unimportant":
                return 2;
            case "Not Urgent Unimportant":
                return 3;
            default:
                return -1; // Unknown category
        }
    }



private int getMax(int[] data) {
    int max = 0;
    for (int value : data) {
        if (value > max) max = value;
    }
    return max > 0 ? max : 10;
}
}