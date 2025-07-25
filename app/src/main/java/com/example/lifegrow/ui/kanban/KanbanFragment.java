package com.example.lifegrow.ui.kanban;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.lifegrow.R;
import com.example.lifegrow.model.TaskModel;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KanbanFragment extends Fragment {
    private RecyclerView todoRecyclerView, inProgressRecyclerView, doneRecyclerView;
    private TaskAdapterKanban todoAdapter, inProgressAdapter, doneAdapter;
    private List<TaskModel> todoList, inProgressList, doneList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String userId;
    private ListenerRegistration taskListener;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_kanban, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        todoRecyclerView = root.findViewById(R.id.todoRecyclerView);
        inProgressRecyclerView = root.findViewById(R.id.inProgressRecyclerView);
        doneRecyclerView = root.findViewById(R.id.doneRecyclerView);

        todoRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        inProgressRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        doneRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        todoList = new ArrayList<>();
        inProgressList = new ArrayList<>();
        doneList = new ArrayList<>();

        todoAdapter = new TaskAdapterKanban(getContext(), todoList);
        inProgressAdapter = new TaskAdapterKanban(getContext(), inProgressList);
        doneAdapter = new TaskAdapterKanban(getContext(), doneList);

        todoRecyclerView.setAdapter(todoAdapter);
        inProgressRecyclerView.setAdapter(inProgressAdapter);
        doneRecyclerView.setAdapter(doneAdapter);

        fetchTasksFromFirestore(); // Load tasks initially

        enableSwipe(todoRecyclerView, todoAdapter, "Progress");
        enableSwipe(inProgressRecyclerView, inProgressAdapter, "Done");
        enableSwipe(doneRecyclerView, doneAdapter, "Pending"); // Move back to To-Do

        return root;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (taskListener != null) {
            taskListener.remove();
        }
    }


    private void fetchTasksFromFirestore() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        String userId = user.getEmail().replace(".", "_");
        CollectionReference tasksRef = db.collection("users").document(userId).collection("tasks");

        taskListener = tasksRef.addSnapshotListener((querySnapshot, error) -> {
            if (error != null || querySnapshot == null) return;

            todoList.clear();
            inProgressList.clear();
            doneList.clear();

            // Get today's date
            Calendar todayCal = Calendar.getInstance();
            long todayMillis = todayCal.getTimeInMillis();
            int todayYear = todayCal.get(Calendar.YEAR);
            int todayMonth = todayCal.get(Calendar.MONTH);
            int todayDay = todayCal.get(Calendar.DAY_OF_MONTH);

            for (QueryDocumentSnapshot document : querySnapshot) {
                TaskModel task = document.toObject(TaskModel.class);
                task.setId(document.getId());

                String repeat = document.getString("repeat");
                Timestamp startTimestamp = document.getTimestamp("startdatetime");
                Timestamp endTimestamp = document.getTimestamp("enddatetime");

                boolean isTaskDue = false;

                if (startTimestamp != null) {
                    long startMillis = startTimestamp.toDate().getTime();
                    long endMillis = (endTimestamp != null) ? endTimestamp.toDate().getTime() : startMillis;

                    // ✅ Main addition: check if today is between start and end datetime
                    if (todayMillis >= startMillis && todayMillis <= endMillis) {
                        isTaskDue = true;
                    }

                    // ✅ Also retain repeat-type logic if needed
                    if (!isTaskDue) {
                        Calendar taskCal = Calendar.getInstance();
                        taskCal.setTime(startTimestamp.toDate());
                        int taskYear = taskCal.get(Calendar.YEAR);
                        int taskMonth = taskCal.get(Calendar.MONTH);
                        int taskDay = taskCal.get(Calendar.DAY_OF_MONTH);

                        if ("Monthly".equals(repeat)) {
                            if (taskYear == todayYear && taskDay == todayDay) {
                                isTaskDue = true;
                            }
                        } else {
                            if (taskYear == todayYear && taskMonth == todayMonth && taskDay == todayDay) {
                                isTaskDue = true;
                            }
                        }
                    }
                } else {
                    // No startdatetime, treat as due
                    isTaskDue = true;
                }

                if (isTaskDue) {
                    switch (task.getStatus()) {
                        case "Pending":
                            todoList.add(task);
                            break;
                        case "Progress":
                            inProgressList.add(task);
                            break;
                        case "Done":
                            doneList.add(task);
                            break;
                    }
                }
            }

            requireActivity().runOnUiThread(() -> {
                todoAdapter.notifyDataSetChanged();
                inProgressAdapter.notifyDataSetChanged();
                doneAdapter.notifyDataSetChanged();
            });
        });
    }

    private void enableSwipe(RecyclerView recyclerView, TaskAdapterKanban adapter, String progress) {
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                TaskModel task = adapter.getTaskAt(position);

                String currentStatus = task.getStatus();
                String newStatus = "";

                if (direction == ItemTouchHelper.RIGHT) {
                    // Move Forward
                    switch (currentStatus) {
                        case "Pending":
                            newStatus = "Progress";
                            break;
                        case "Progress":
                            newStatus = "Done";
                            break;
                    }
                } else if (direction == ItemTouchHelper.LEFT) {
                    // Move Backward
                    switch (currentStatus) {
                        case "Progress":
                            newStatus = "Pending";
                            break;
                        case "Done":
                            newStatus = "Progress";
                            break;
                    }
                }

                if (!newStatus.isEmpty()) {
                    updateTaskStatus(task, newStatus);
                    adapter.removeTask(position); // Remove from UI immediately
                } else {
                    adapter.notifyItemChanged(position); // Prevent swipe if no valid status
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;
                ColorDrawable background;
                //Drawable icon = ContextCompat.getDrawable(getContext(), R.drawable.ic_skip);
                Drawable icon;
                if (dX > 0) { // Swiping Right - Move Forward
                    background = new ColorDrawable(Color.parseColor("#FFFFFF"));
                    icon = ContextCompat.getDrawable(getContext(), R.drawable.ic_arrowright);
                } else { // Swiping Left - Move Backward
                    background = new ColorDrawable(Color.parseColor("#FFFFFF"));
                    icon = ContextCompat.getDrawable(getContext(), R.drawable.ic_arrowleft);
                }

                background.setBounds(
                        (int) (dX > 0 ? itemView.getLeft() : itemView.getRight() + dX),
                        itemView.getTop(),
                        (int) (dX > 0 ? itemView.getLeft() + dX : itemView.getRight()),
                        itemView.getBottom()
                );

                background.draw(c);

                if (icon != null) {
                    int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
                    int iconTop = itemView.getTop() + iconMargin;
                    int iconBottom = iconTop + icon.getIntrinsicHeight();
                    int iconLeft, iconRight;

                    if (dX > 0) { // Swiping Right
                        iconLeft = itemView.getLeft() + iconMargin;
                        iconRight = iconLeft + icon.getIntrinsicWidth();
                    } else { // Swiping Left
                        iconRight = itemView.getRight() - iconMargin;
                        iconLeft = iconRight - icon.getIntrinsicWidth();
                    }

                    icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                    icon.draw(c);
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        });

        itemTouchHelper.attachToRecyclerView(recyclerView);
    }


    private void updateTaskStatus(TaskModel task, String newStatus) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        String userId = user.getEmail().replace(".", "_");
        DocumentReference taskRef = db.collection("users").document(userId)
                .collection("tasks").document(task.getId());

        // Only update updatedAt for "Pending" → "Progress" or "Progress" → "Done"
        boolean shouldUpdateTimestamp =
                ("Pending".equals(task.getStatus()) && "Progress".equals(newStatus)) ||
                        ("Progress".equals(task.getStatus()) && "Done".equals(newStatus));

        // Build update map
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", newStatus);
        if (shouldUpdateTimestamp) {
            updates.put("updatedAt", new Timestamp(new java.util.Date()));
        }

        taskRef.update(updates)
                .addOnSuccessListener(aVoid -> Log.d("KanbanFragment", "Task updated to " + newStatus))
                .addOnFailureListener(e -> Log.e("KanbanFragment", "Task update failed", e));
    }

}
