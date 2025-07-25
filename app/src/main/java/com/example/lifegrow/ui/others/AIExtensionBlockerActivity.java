package com.example.lifegrow.ui.others;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lifegrow.R;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public class AIExtensionBlockerActivity extends AppCompatActivity {

    private EditText paragraphInput;
    private Button parseButton;

    private FirebaseFirestore db;
    private String userId;
    private static final String TAG = "AIBlocker";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_extension_blocker);

        paragraphInput = findViewById(R.id.paragraphInput);
        parseButton = findViewById(R.id.parseButton);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", "_");

        parseButton.setOnClickListener(v -> parseAndSaveTask());
    }

    private void parseAndSaveTask() {
        String inputText = paragraphInput.getText().toString().trim();

        if (inputText.isEmpty()) {
            Toast.makeText(this, "Please enter a task description", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] tasks = inputText.split("\\.\\s*");

        for (String taskInput : tasks) {
            taskInput = taskInput.trim();
            if (taskInput.isEmpty()) continue;

            String taskName = extractTaskName(taskInput);
            Date startDate = extractDateUsingNatty(taskInput);
            Date endDate = generateEndDate(startDate);
            String category = inferCategory(taskInput);
            String color = assignColor(category);

            if (taskName.isEmpty() || startDate == null) {
                Toast.makeText(this, "Couldn't extract task name or start time for: " + taskInput, Toast.LENGTH_SHORT).show();
                continue;
            }

            // Notification setup
            boolean notificationEnabled = true;
            String notificationTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(startDate);

            saveTaskToFirestore(taskName, taskInput, category, color, startDate, endDate, notificationEnabled, notificationTime);
        }
    }

    private Date extractDateUsingNatty(String inputText) {
        Parser parser = new Parser();
        List<DateGroup> groups = parser.parse(inputText);
        if (!groups.isEmpty()) {
            List<Date> dates = groups.get(0).getDates();
            if (!dates.isEmpty()) {
                return dates.get(0);
            }
        }
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, 1);
        return cal.getTime();
    }

    private String extractTaskName(String inputText) {
        String normalized = inputText.trim().replaceAll("\\s+", " ");

        String[] commonTaskPhrases = {
                "remind me to", "i need to", "don't forget to", "make sure to", "i have to",
                "i should", "remember to", "set a reminder for", "please remind me",
                "please help me with", "set a task for", "help me with this task"
        };

        for (String phrase : commonTaskPhrases) {
            normalized = normalized.replaceFirst("(?i)^" + Pattern.quote(phrase) + "\\s*", "");
        }

        String[] simpleWords = {
                "is", "at", "on", "for", "by", "to", "with", "of", "and", "the", "a", "an", "in"
        };

        for (String word : simpleWords) {
            normalized = normalized.replaceAll("(?i)\\b" + Pattern.quote(word) + "\\b", "");
        }

        String[] words = normalized.split(" ");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < Math.min(words.length, 6); i++) {
            builder.append(words[i]).append(" ");
        }
        return builder.toString().trim();
    }

    private void saveTaskToFirestore(String taskName, String description, String category, String color,
                                     Date startDate, Date endDate, boolean notificationEnabled, String notificationTime) {
        Map<String, Object> task = new HashMap<>();
        task.put("name", taskName);
        task.put("description", description);
        task.put("category", category);
        task.put("repeat", "None");
        task.put("notificationEnabled", notificationEnabled);
        task.put("notificationTime", notificationTime);
        task.put("status", "Pending");
        task.put("createdAt", new Timestamp(new Date()));
        task.put("updatedAt", "none");
        task.put("pomodoroCount", 0);
        task.put("tag", "");
        task.put("color", color);
        task.put("startdatetime", new Timestamp(startDate));
        task.put("enddatetime", new Timestamp(endDate));

        db.collection("users")
                .document(userId)
                .collection("tasks")
                .add(task)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(this, "Task saved successfully!", Toast.LENGTH_SHORT).show();

                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving task", e);
                    Toast.makeText(this, "Failed to save task", Toast.LENGTH_SHORT).show();
                });
    }

    private Date generateEndDate(Date startDate) {
        if (startDate == null) return null;
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        cal.add(Calendar.HOUR_OF_DAY, 1);
        return cal.getTime();
    }

    private String inferCategory(String text) {
        text = text.toLowerCase();
        if (text.contains("urgent") && text.contains("important")) return "Urgent Important";
        if (text.contains("important")) return "Not Urgent Important";
        if (text.contains("urgent")) return "Urgent Unimportant";
        return "Not Urgent Unimportant";
    }

    private String assignColor(String category) {
        switch (category) {
            case "Urgent Important":
                return "#FF7043";
            case "Not Urgent Important":
                return "#66BB6A";
            case "Urgent Unimportant":
                return "#FFCA28";
            case "Not Urgent Unimportant":
                return "#29B6F6";
            default:
                return "#BDBDBD";
        }
    }
}
