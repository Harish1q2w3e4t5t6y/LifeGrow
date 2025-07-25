package com.example.lifegrow.ui.calendar;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.lifegrow.databinding.FragmentCalendarBinding;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CalendarFragment extends Fragment {
    private FragmentCalendarBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TaskAdapter taskAdapter;
    private List<Task> taskList = new ArrayList<>();
    private Map<Long, String> deadlineColors = new HashMap<>(); // ✅ Store task dates & colors

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCalendarBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // ✅ Set up RecyclerView
        taskAdapter = new TaskAdapter(taskList);
        binding.taskRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.taskRecyclerView.setAdapter(taskAdapter);

        // Fetch all task deadlines once
        fetchAllTaskDeadlines();

        // Handle date selection in CalendarView
        binding.calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            long selectedDateMillis = getMillisForSelectedDate(year, month, dayOfMonth);

            // Load tasks for selected date
            Timestamp selectedDate = getTimestampForSelectedDate(year, month, dayOfMonth);
            fetchTasksForDate(selectedDate);
        });

        // ✅ Automatically load today's tasks on first load
        Calendar today = Calendar.getInstance();
        int year = today.get(Calendar.YEAR);
        int month = today.get(Calendar.MONTH); // ⚠️ zero-based
        int day = today.get(Calendar.DAY_OF_MONTH);

        long todayMillis = getMillisForSelectedDate(year, month, day);
        Timestamp todayTimestamp = getTimestampForSelectedDate(year, month, day);
        fetchTasksForDate(todayTimestamp);

// Optional: highlight today's date in CalendarView
        binding.calendarView.setDate(todayMillis, true, true);


        return root;
    }


    private void fetchAllTaskDeadlines() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Log.e("CalendarFragment", "User not logged in");
            return;
        }

        String userId = user.getEmail().replace(".", "_");
        CollectionReference tasksRef = db.collection("users").document(userId).collection("tasks");

        tasksRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            deadlineColors.clear();
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                Timestamp startdatetime = document.getTimestamp("startdatetime");
                String color = document.getString("color");

                if (startdatetime != null && color != null) {
                    long dateMillis = startdatetime.toDate().getTime();
                    deadlineColors.put(dateMillis, color); // ✅ Store date & color
                }
            }
        });
    }


    private void fetchTasksForDate(Timestamp selectedDate) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Log.e("CalendarFragment", "User not logged in");
            return;
        }

        String userId = user.getEmail().replace(".", "_");
        CollectionReference tasksRef = db.collection("users").document(userId).collection("tasks");

        Timestamp startOfDay = getStartOfDayTimestamp(selectedDate);
        Timestamp endOfDay = getEndOfDayTimestamp(selectedDate);

        tasksRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            taskList.clear();

            boolean taskFound = false;

            // Convert selectedDate, start, and end to just date (yyyyMMdd)
            SimpleDateFormat dateOnlyFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
            String selectedDateStr = dateOnlyFormat.format(selectedDate.toDate());

            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                String name = document.getString("name");
                String color = document.getString("color") != null ? document.getString("color") : "black";
                Timestamp startdatetime = document.getTimestamp("startdatetime");
                Timestamp enddatetime = document.getTimestamp("enddatetime");

                if (startdatetime != null) {
                    String startStr = dateOnlyFormat.format(startdatetime.toDate());
                    String endStr = (enddatetime != null)
                            ? dateOnlyFormat.format(enddatetime.toDate())
                            : startStr; // If no end, treat it as same as start

                    // ✅ Compare as integers to check if selected date falls in range
                    int selected = Integer.parseInt(selectedDateStr);
                    int start = Integer.parseInt(startStr);
                    int end = Integer.parseInt(endStr);

                    if (selected >= start && selected <= end) {
                        taskList.add(new Task(name, color, startdatetime, (enddatetime != null ? enddatetime : startdatetime)));
                    }
                }
            }

// 👻 Add placeholder if none matched
            if (taskList.isEmpty()) {
                taskList.add(new Task("No tasks available", "#CCCCCC", selectedDate, selectedDate));
            }

            taskAdapter.notifyDataSetChanged();
            binding.taskRecyclerView.setVisibility(View.VISIBLE);

        });
    }

        // ✅ Helper methods for timestamps
    private Timestamp getStartOfDayTimestamp(Timestamp timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(timestamp.toDate());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return new Timestamp(calendar.getTime());
    }

    private Timestamp getEndOfDayTimestamp(Timestamp timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(timestamp.toDate());
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return new Timestamp(calendar.getTime());
    }

    private Timestamp getTimestampForSelectedDate(int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return new Timestamp(calendar.getTime());
    }

    private long getMillisForSelectedDate(int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
