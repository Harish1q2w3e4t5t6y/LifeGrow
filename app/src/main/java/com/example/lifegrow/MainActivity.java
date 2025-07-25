package com.example.lifegrow;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.lifegrow.databinding.ActivityMainBinding;
import com.example.lifegrow.notification.ToastReceiver;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TaskUpdater.updateTaskDates(this);
        scheduleTaskNotificationsForToday();  // Schedule alarm once app is launched

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().show();

        BottomNavigationView navView = findViewById(R.id.nav_view);
        FloatingActionButton fabAddTask = findViewById(R.id.fab_add_task);

        // Setting up navigation
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_pomodoro, R.id.navigation_eisenhower, R.id.navigation_kanban,
                R.id.navigation_calendar, R.id.navigation_others)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        // Hide FloatingActionButton in specific fragments
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.navigation_others || destination.getId() == R.id.navigation_pomodoro) {
                fabAddTask.setVisibility(View.GONE);
            } else {
                fabAddTask.setVisibility(View.VISIBLE);
            }
        });

        // Floating Action Button to Add Task
        fabAddTask.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AddTask.class);
            startActivity(intent);
        });


    }
    private void scheduleTaskNotificationsForToday() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String userId = user.getEmail().replace(".", "_");
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(userId).collection("tasks")
                .whereNotEqualTo("status", "Done")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Calendar today = Calendar.getInstance();
                        today.set(Calendar.HOUR_OF_DAY, 0);
                        today.set(Calendar.MINUTE, 0);
                        today.set(Calendar.SECOND, 0);
                        today.set(Calendar.MILLISECOND, 0);
                        Date todayDate = today.getTime();

                        // Replace 'this' with your actual context if it's in a different class
                        Context context = getApplicationContext();  // Or 'getContext()' if in Fragment

                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Boolean notificationEnabled = doc.getBoolean("notificationEnabled");
                            if (notificationEnabled == null || !notificationEnabled) continue;

                            String taskName = doc.getString("name");
                            String notifTime = doc.getString("notificationTime");  // format: "HH:mm"

                            Timestamp startTs = doc.getTimestamp("startdatetime");
                            Timestamp endTs = doc.getTimestamp("enddatetime");

                            boolean isToday = false;
                            if (startTs != null && endTs != null) {
                                Date startDate = stripTime(startTs.toDate());
                                Date endDate = stripTime(endTs.toDate());
                                if (!todayDate.before(startDate) && !todayDate.after(endDate)) isToday = true;
                            } else if (startTs != null) {
                                Date startDate = stripTime(startTs.toDate());
                                if (todayDate.equals(startDate)) isToday = true;
                            } else {
                                isToday = true; // No date? Show today.
                            }

                            if (isToday && notifTime != null && taskName != null) {
                                // Log to see if the task is being fetched correctly
                                Log.d("TaskNotification", "Fetched task: " + taskName);

                                // Show a Toast with the task name
                                //Toast.makeText(context, "Task: " + taskName, Toast.LENGTH_SHORT).show();

                                // Proceed with scheduling the toast for the task
                                scheduleToastForTask(taskName, notifTime);
                            }
                        }
                    } else {
                        //Log.e("TaskNotification", "Error fetching tasks: " + task.getException());
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

    private void scheduleToastForTask(String taskName, String notifTime) {
        String[] parts = notifTime.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // Skip past time
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) return;

        Intent intent = new Intent(this, ToastReceiver.class);
        intent.putExtra("task_name", taskName);

        int requestCode = (taskName + hour + minute).hashCode();  // unique per task-time
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );
        } else {
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );
        }
    }


    public void setFullScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
            getWindow().getInsetsController().hide(
                    WindowInsets.Type.statusBars());
        }
    }
}
