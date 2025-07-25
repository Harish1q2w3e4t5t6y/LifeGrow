package com.example.lifegrow;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddTask extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText etTaskName, etDescription, etNotificationTime, etStartDateTime, etEndDateTime;
    private Spinner spCategory, spRepeat, spinnerColorr;
    private Switch switchNotification;
    private Button btnSave;
    private String notificationTime = "None";
    private CardView cardNotificationTime, cardEndDateTime;
    private EditText etTagg;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addtask);
        setTitle(getString(R.string.title_addtask));

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Bind UI elements
        etTaskName = findViewById(R.id.etTaskName);
        etDescription = findViewById(R.id.etDescription);
        etNotificationTime = findViewById(R.id.etNotificationTime);
        etStartDateTime = findViewById(R.id.etStartDateTime);
        etEndDateTime = findViewById(R.id.etEndDateTime);
        spCategory = findViewById(R.id.spinnerCategory);
        spRepeat = findViewById(R.id.spinnerRepeat);
        switchNotification = findViewById(R.id.switchNotification);
        btnSave = findViewById(R.id.btnSave);
        cardNotificationTime = findViewById(R.id.cardNotificationTime);
        cardEndDateTime = findViewById(R.id.cardEndDateTime);
        ImageView btnClose = findViewById(R.id.btnClose);
        etTagg = findViewById(R.id.etTag);
        spinnerColorr = findViewById(R.id.spinnerColor);

        // Populate Spinners
        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(this, R.array.quadrants_array, android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(categoryAdapter);

        ArrayAdapter<CharSequence> repeatAdapter = ArrayAdapter.createFromResource(this, R.array.repeat_options_array, android.R.layout.simple_spinner_dropdown_item);
        spRepeat.setAdapter(repeatAdapter);

        ArrayAdapter<CharSequence> colorAdapter = ArrayAdapter.createFromResource(this, R.array.color_array, android.R.layout.simple_spinner_dropdown_item);
        spinnerColorr.setAdapter(colorAdapter);

        // Notification Toggle Logic
        switchNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                cardNotificationTime.setVisibility(View.VISIBLE);
                showTimePicker();
            } else {
                cardNotificationTime.setVisibility(View.GONE);
                notificationTime = "None";
                etNotificationTime.setText("");
            }
        });

        // Date-Time Pickers
        etStartDateTime.setOnClickListener(v -> showDateTimePicker(etStartDateTime, true));
        etEndDateTime.setOnClickListener(v -> showDateTimePicker(etEndDateTime, false));

        // Notification Time Picker
        etNotificationTime.setOnClickListener(v -> showTimePicker());

        btnSave.setOnClickListener(v -> saveTaskToFirestore());

        btnClose.setOnClickListener(v -> {
            finish(); // Close the current activity
        });
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            notificationTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
            etNotificationTime.setText(notificationTime);
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show();
    }

    private void showDateTimePicker(EditText editText, boolean isStartDateTime) {
        Calendar calendar = Calendar.getInstance();

        // Show Date Picker
        new DatePickerDialog(this, (dateView, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);

            // Show Time Picker after selecting date
            new TimePickerDialog(this, (timeView, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                String formattedDateTime = sdf.format(calendar.getTime());

                editText.setText(formattedDateTime);

                if (isStartDateTime) {
                    cardEndDateTime.setVisibility(View.VISIBLE);
                }
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show();

        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveTaskToFirestore() {
        // Validate Task Name
        String taskName = etTaskName.getText().toString().trim();
        if (taskName.isEmpty()) {
            Toast.makeText(this, "Task Name is required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check user authentication
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = user.getEmail().replace(".", "_");

        // Gather form values
        String category = spCategory.getSelectedItem().toString();
        String repeat = spRepeat.getSelectedItem().toString();
        String description = etDescription.getText().toString().trim();
        boolean notificationEnabled = switchNotification.isChecked();
        String tag = etTagg.getText().toString().trim();  // Get tag input from EditText
        String color = spinnerColorr.getSelectedItem().toString();  // Get selected color from Spinner

        // Parse Start and End DateTime
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        Date startDate = null, endDate = null;
        try {
            if (!etStartDateTime.getText().toString().isEmpty()) {
                startDate = sdf.parse(etStartDateTime.getText().toString());
            }
            if (!etEndDateTime.getText().toString().isEmpty()) {
                endDate = sdf.parse(etEndDateTime.getText().toString());
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Build the task map with Lifegrow memory task structure
        Map<String, Object> task = new HashMap<>();
        task.put("name", taskName);
        task.put("description", description.isEmpty() ? "none" : description);
        task.put("category", category);
        task.put("repeat", repeat);
        task.put("notificationEnabled", notificationEnabled);
        task.put("notificationTime", notificationTime);
        task.put("status", "Pending");
        task.put("createdAt", new Timestamp(new Date()));
        task.put("updatedAt", "none");
        task.put("pomodoroCount", 0);
        task.put("tag", tag);
        task.put("color", color);
        task.put("startdatetime", startDate != null ? new Timestamp(startDate) : null);
        task.put("enddatetime", endDate != null ? new Timestamp(endDate) : null);

        // Save the task to Firestore under the user's tasks collection
        db.collection("users").document(userId)
                .collection("tasks")
                .add(task)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Task saved successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e("FIREBASE_ERROR", "Error saving task", e); // ✅ Now this will work
                    Toast.makeText(this, "Failed to save task", Toast.LENGTH_SHORT).show();
                });
    }
}
