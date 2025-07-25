package com.example.lifegrow;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TaskUpdater {

    public static void updateTaskDates(Context context) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert email to Firestore-friendly user ID
        String userId = user.getEmail().replace(".", "_");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SharedPreferences sharedPrefs = context.getSharedPreferences("TaskPrefs", Context.MODE_PRIVATE);

        // Get today's date
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Check if tasks were already updated today
        if (todayDate.equals(sharedPrefs.getString("last_update_date", ""))) {
            Toast.makeText(context, "Tasks already updated today!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Fetch tasks from Firestore
        db.collection("users").document(userId).collection("tasks")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> processTasks(db, userId, queryDocumentSnapshots, todayDate, sharedPrefs, context))
                .addOnFailureListener(e -> Toast.makeText(context, "Failed to fetch tasks!", Toast.LENGTH_SHORT).show());
    }

    private static void processTasks(FirebaseFirestore db, String userId, QuerySnapshot documents, String todayDate, SharedPreferences sharedPrefs, Context context) {
        boolean updateConfirmed = false; // Temporary variable to confirm the process

        for (DocumentSnapshot document : documents) {
            String taskId = document.getId();
            String repeat = document.getString("repeat") != null ? document.getString("repeat") : "None";

            if ("Daily".equals(repeat)) {
                // Prepare the update map
                Map<String, Object> updatedTask = new HashMap<>();
                updatedTask.put("startdatetime", new Timestamp(new Date())); // Set new date as Timestamp

                db.collection("users").document(userId).collection("tasks").document(taskId)
                        .update(updatedTask);

                updateConfirmed = true; // Confirm at least one update happened
            }
        }

        // Save today's update date only if updates were performed
        if (updateConfirmed) {
            sharedPrefs.edit().putString("last_update_date", todayDate).apply();
            Toast.makeText(context, "Daily tasks updated successfully!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "No tasks needed updating.", Toast.LENGTH_SHORT).show();
        }
    }
}