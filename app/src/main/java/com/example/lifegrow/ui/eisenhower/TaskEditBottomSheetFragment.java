package com.example.lifegrow.ui.eisenhower;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
//////////// ////////
import com.example.lifegrow.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class TaskEditBottomSheetFragment extends BottomSheetDialogFragment {

    private EditText etTaskName, etDescription, etStartDateTime, etEndDateTime, etNotificationTime;
    private Spinner spinnerCategory, spinnerRepeat, spinnerColor;
    private Switch switchNotification;
    private CardView cardNotificationTime, cardEndDateTime;
    private Button btnUpdate, btnDelete;
    private ImageView btnClose;

    private FirebaseFirestore db;
    private String userId;
    private String taskId;
    // taskName is passed as argument and shown read-only
    private String taskName;
    private EditText etTag; // Declare at the top


    private String notificationTime = "None";
    private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
    public interface OnTaskUpdatedListener {
        void onTaskUpdated(String taskName, String category);

        void onTaskUpdated();
    }


    private OnTaskUpdatedListener taskUpdatedListener;

    public static TaskEditBottomSheetFragment newInstance(String taskName, String taskId) {
        TaskEditBottomSheetFragment fragment = new TaskEditBottomSheetFragment();
        Bundle args = new Bundle();
        args.putString("taskName", taskName);
        args.putString("taskId", taskId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_modify_task, container, false);

        // Bind UI elements
        etTaskName = view.findViewById(R.id.etTaskName);
        etDescription = view.findViewById(R.id.etDescription);
        spinnerCategory = view.findViewById(R.id.spinnerCategory);
        spinnerRepeat = view.findViewById(R.id.spinnerRepeat);
        spinnerColor = view.findViewById(R.id.spinnerColor);
        switchNotification = view.findViewById(R.id.switchNotification);
        cardNotificationTime = view.findViewById(R.id.cardNotificationTime);
        etNotificationTime = view.findViewById(R.id.etNotificationTime);
        //cardStartDateTime = view.findViewById(R.id.cardStartDateTime);
        etStartDateTime = view.findViewById(R.id.etStartDateTime);
        cardEndDateTime = view.findViewById(R.id.cardEndDateTime);
        etEndDateTime = view.findViewById(R.id.etEndDateTime);
        btnUpdate = view.findViewById(R.id.btnUpdate);
        btnDelete = view.findViewById(R.id.btnDelete);
        btnClose = view.findViewById(R.id.btnClose);
        etTag = view.findViewById(R.id.etTag);


// Initialize the adapters for the Spinners
        setupSpinners();



        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", "_");

        if (getArguments() != null) {
            taskName = getArguments().getString("taskName");
            taskId = getArguments().getString("taskId");
        }

        // Set task name (read-only)
        etTaskName.setText(taskName);

        // Load task data from Firestore
        loadTaskData();

        // Set listeners for date/time pickers
        etStartDateTime.setOnClickListener(v -> showDateTimePicker(etStartDateTime, true));
        etEndDateTime.setOnClickListener(v -> showDateTimePicker(etEndDateTime, false));
        etNotificationTime.setOnClickListener(v -> showTimePicker());

        // Notification switch listener
        switchNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                cardNotificationTime.setVisibility(View.VISIBLE);
                //showTimePicker();
            } else {
                cardNotificationTime.setVisibility(View.GONE);
                notificationTime = "None";
                etNotificationTime.setText("");
            }
        });

        // Close button
        btnClose.setOnClickListener(v -> dismiss());

        btnUpdate.setOnClickListener(v -> updateTask());
        btnDelete.setOnClickListener(v -> deleteTask());

        // Note: You should initialize the spinners’ adapters (for category, repeat, color)
        // similar to your AddTask activity if not set elsewhere.

        return view;
    }

    // Full screen of fragment
    /*
    @Override
    public void onStart() {
        super.onStart();
        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        if (dialog != null) {
            View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                bottomSheet.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        }
    }
    */
    private void setupSpinners() {
        // Category Spinner
        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.quadrants_array, android.R.layout.simple_spinner_item);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        // Repeat Spinner
        ArrayAdapter<CharSequence> repeatAdapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.repeat_options_array, android.R.layout.simple_spinner_item);
        repeatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRepeat.setAdapter(repeatAdapter);

        // Color Spinner
        ArrayAdapter<CharSequence> colorAdapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.color_array, android.R.layout.simple_spinner_item);
        colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerColor.setAdapter(colorAdapter);
    }

    private void loadTaskData() {
        db.collection("users").document(userId)
                .collection("tasks").document(taskId)
                .get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        etDescription.setText(documentSnapshot.getString("description"));
                        etTag.setText(documentSnapshot.getString("tag"));

                        // Get Firestore values
                        String category = documentSnapshot.getString("category");
                        String repeat = documentSnapshot.getString("repeat");
                        String color = documentSnapshot.getString("color");

                        Boolean notifEnabled = documentSnapshot.getBoolean("notificationEnabled");
                        switchNotification.setChecked(notifEnabled != null && notifEnabled);
                        etNotificationTime.setText(documentSnapshot.getString("notificationTime"));

                        Timestamp startTimestamp = documentSnapshot.getTimestamp("startdatetime");
                        Timestamp endTimestamp = documentSnapshot.getTimestamp("enddatetime");
                        if (startTimestamp != null) {
                            etStartDateTime.setText(dateTimeFormat.format(startTimestamp.toDate()));
                        }
                        if (endTimestamp != null) {
                            etEndDateTime.setText(dateTimeFormat.format(endTimestamp.toDate()));
                            cardEndDateTime.setVisibility(View.VISIBLE);
                        }

                        // Set spinner selections dynamically
                        setSpinnerSelection(spinnerCategory, category);
                        setSpinnerSelection(spinnerRepeat, repeat);
                        setSpinnerSelection(spinnerColor, color);
                    }
                }).addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load task", Toast.LENGTH_SHORT).show());
    }
    private void setSpinnerSelection(Spinner spinner, String value) {
        if (value != null) {
            ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
            int position = adapter.getPosition(value);
            if (position >= 0) {
                spinner.setSelection(position);
            }
        }
    }


    private void updateTask() {
        Map<String, Object> updates = new HashMap<>();
        updates.put("description", etDescription.getText().toString().trim());
        updates.put("category", spinnerCategory.getSelectedItem() != null ? spinnerCategory.getSelectedItem().toString() : "");
        updates.put("repeat", spinnerRepeat.getSelectedItem() != null ? spinnerRepeat.getSelectedItem().toString() : "");
        updates.put("notificationEnabled", switchNotification.isChecked());
        updates.put("notificationTime", etNotificationTime.getText().toString().trim());
        updates.put("color", spinnerColor.getSelectedItem() != null ? spinnerColor.getSelectedItem().toString() : "");
        updates.put("tag", etTag.getText().toString().trim());

        try {
            if (!TextUtils.isEmpty(etStartDateTime.getText().toString())) {
                Date startDate = dateTimeFormat.parse(etStartDateTime.getText().toString());
                updates.put("startdatetime", new Timestamp(startDate));
            } else {
                updates.put("startdatetime", null);
            }
            if (!TextUtils.isEmpty(etEndDateTime.getText().toString())) {
                Date endDate = dateTimeFormat.parse(etEndDateTime.getText().toString());
                updates.put("enddatetime", new Timestamp(endDate));
            } else {
                updates.put("enddatetime", null);
            }
        } catch (ParseException e) {
            Toast.makeText(getContext(), "Invalid date format", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").document(userId)
                .collection("tasks").document(taskId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Task updated", Toast.LENGTH_SHORT).show();
                    if (taskUpdatedListener != null) {
                        String updatedCategory = spinnerCategory.getSelectedItem().toString();
                        taskUpdatedListener.onTaskUpdated(taskName, updatedCategory); // ✅ Pass taskName & category
                    }
                    dismiss();  // Close the bottom sheet
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to update task", Toast.LENGTH_SHORT).show()
                );
    }


    private void deleteTask() {
        db.collection("users").document(userId)
                .collection("tasks").document(taskId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Task deleted", Toast.LENGTH_SHORT).show();
                    dismiss();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to delete task", Toast.LENGTH_SHORT).show());
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        new TimePickerDialog(getContext(), (view, hourOfDay, minute) -> {
            notificationTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
            etNotificationTime.setText(notificationTime);
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show();
    }

    private void showDateTimePicker(final EditText editText, final boolean isStartDateTime) {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(getContext(), (datePicker, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            new TimePickerDialog(getContext(), (timePicker, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                editText.setText(dateTimeFormat.format(calendar.getTime()));
                if (isStartDateTime) {
                    cardEndDateTime.setVisibility(View.VISIBLE);
                }
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (taskUpdatedListener != null) {
            String updatedCategory = spinnerCategory.getSelectedItem().toString();
            taskUpdatedListener.onTaskUpdated(taskName, updatedCategory); // ✅ Pass required arguments
        }
    }


}
