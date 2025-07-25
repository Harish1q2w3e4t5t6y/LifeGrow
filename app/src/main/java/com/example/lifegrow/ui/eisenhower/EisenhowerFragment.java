package com.example.lifegrow.ui.eisenhower;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.lifegrow.R;
import com.example.lifegrow.databinding.FragmentEisenhowerBinding;
import com.example.lifegrow.model.TaskModel;
import com.example.lifegrow.repository.TaskRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EisenhowerFragment extends Fragment {
    private FragmentEisenhowerBinding binding;
    private RecyclerView criticalTasksRecyclerView, importantTasksRecyclerView, urgentTasksRecyclerView, lowPriorityTasksRecyclerView;
    private TaskAdapter criticalAdapter, importantAdapter, urgentAdapter, lowPriorityAdapter;
    private FirebaseAuth auth;
    private Map<String, String> taskIdMap;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEisenhowerBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        auth = FirebaseAuth.getInstance();
        taskIdMap = new HashMap<>();

        // Initialize RecyclerViews
        criticalTasksRecyclerView = binding.criticalTasks;
        importantTasksRecyclerView = binding.importantTasks;
        urgentTasksRecyclerView = binding.urgentTasks;
        lowPriorityTasksRecyclerView = binding.lowPriorityTasks;

        criticalTasksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        importantTasksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        urgentTasksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        lowPriorityTasksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        loadTasks(); // Updated: Load tasks from cache or Firestore

        // Enable Drag and Drop for each RecyclerView
        enableDragAndDrop(criticalTasksRecyclerView, "Urgent Important");
        enableDragAndDrop(importantTasksRecyclerView, "Not Urgent Important");
        enableDragAndDrop(urgentTasksRecyclerView, "Urgent Unimportant");
        enableDragAndDrop(lowPriorityTasksRecyclerView, "Not Urgent Unimportant");

        return root;
    }
    public void onTaskUpdated() {
        refreshEisenhowerMatrix(); // ✅ Refresh the UI when a task is updated
    }

    @Override
    public void onResume() {
        super.onResume();
        // No need to call loadTasks() from cache since we want fresh data.
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        String userId = user.getEmail().replace(".", "_");

        // Fetch updated tasks from Firestore, ensuring any category changes are captured.
        TaskRepository.getInstance().prefetchTasks(userId, new TaskRepository.OnTaskPrefetchListener() {
            @Override
            public void onPrefetchSuccess(List<TaskModel> tasks) {
                processTasks(tasks); // Refresh UI with updated task list
            }

            @Override
            public void onPrefetchFailure(String errorMessage) {
                Toast.makeText(getContext(), "Failed to refresh tasks!", Toast.LENGTH_SHORT).show();
            }
        });
    }




    private void loadTasks() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        String userId = user.getEmail().replace(".", "_");
        List<TaskModel> cachedTasks = TaskRepository.getInstance().getCachedTasks();

        if (!cachedTasks.isEmpty()) {
            processTasks(cachedTasks);
        } else {
            // Prefetch tasks if cache is empty
            TaskRepository.getInstance().prefetchTasks(userId, new TaskRepository.OnTaskPrefetchListener() {
                @Override
                public void onPrefetchSuccess(List<TaskModel> tasks) {
                    processTasks(tasks);
                }
                @Override
                public void onPrefetchFailure(String errorMessage) {
                    Toast.makeText(getContext(), "Failed to load tasks: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void processTasks(List<TaskModel> tasks) {
        // Clear any existing task lists and mappings to prevent duplicates.
        List<String> criticalTasks = new ArrayList<>();
        List<String> importantTasks = new ArrayList<>();
        List<String> urgentTasks = new ArrayList<>();
        List<String> lowPriorityTasks = new ArrayList<>();
        taskIdMap.clear();

        // Categorize each task based on its (updated) category value.
        for (TaskModel task : tasks) {
            taskIdMap.put(task.getName(), task.getId());
            switch (task.getCategory()) {
                case "Urgent Important":
                    criticalTasks.add(task.getName());
                    break;
                case "Not Urgent Important":
                    importantTasks.add(task.getName());
                    break;
                case "Urgent Unimportant":
                    urgentTasks.add(task.getName());
                    break;
                case "Not Urgent Unimportant":
                    lowPriorityTasks.add(task.getName());
                    break;
            }
        }

        // Update adapters for each quadrant.
        criticalAdapter = new TaskAdapter(getContext(), criticalTasks, taskIdMap);
        importantAdapter = new TaskAdapter(getContext(), importantTasks, taskIdMap);
        urgentAdapter = new TaskAdapter(getContext(), urgentTasks, taskIdMap);
        lowPriorityAdapter = new TaskAdapter(getContext(), lowPriorityTasks, taskIdMap);

        criticalTasksRecyclerView.setAdapter(criticalAdapter);
        importantTasksRecyclerView.setAdapter(importantAdapter);
        urgentTasksRecyclerView.setAdapter(urgentAdapter);
        lowPriorityTasksRecyclerView.setAdapter(lowPriorityAdapter);
    }


    private void enableDragAndDrop(RecyclerView recyclerView, String targetCategory) {
        View parentCategory = (View) recyclerView.getParent();
        if (parentCategory.getTag() == null) {
            parentCategory.setTag(parentCategory.getBackground()); // Store original background in Tag
        }

        recyclerView.setOnDragListener((view, dragEvent) -> {
            switch (dragEvent.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    return true;
                case DragEvent.ACTION_DRAG_ENTERED:
                    parentCategory.setBackground(ContextCompat.getDrawable(view.getContext(), R.drawable.drag_highlight_border));
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                case DragEvent.ACTION_DRAG_ENDED:
                    parentCategory.setBackground((Drawable) parentCategory.getTag()); // Restore original background
                    return true;
                case DragEvent.ACTION_DROP:
                    parentCategory.setBackground((Drawable) parentCategory.getTag());
                    updateTaskCategory(dragEvent.getClipData().getItemAt(0).getText().toString(), targetCategory);
                    return true;
                default:
                    return false;
            }
        });
    }

    private void refreshEisenhowerMatrix() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        String userId = user.getEmail().replace(".", "_");

        TaskRepository.getInstance().prefetchTasks(userId, new TaskRepository.OnTaskPrefetchListener() {
            @Override
            public void onPrefetchSuccess(List<TaskModel> tasks) {
                processTasks(tasks); // This should update your UI
            }

            @Override
            public void onPrefetchFailure(String errorMessage) {
                Toast.makeText(getContext(), "Failed to refresh tasks!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateTaskCategory(String taskName, String newCategory) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        String userId = user.getEmail().replace(".", "_");
        String taskId = taskIdMap.get(taskName);
        if (taskId == null) {
            Toast.makeText(getContext(), "Task ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("tasks")
                .document(taskId)
                .update("category", newCategory)
                .addOnSuccessListener(aVoid -> {
                    // Update local cache immediately
                    TaskRepository.getInstance().updateTaskCategoryInCache(taskId, newCategory);

                    //Toast.makeText(getContext(), "Task moved to " + newCategory, Toast.LENGTH_SHORT).show();
                    removeTaskFromOldList(taskName);
                    addTaskToNewList(taskName, newCategory);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Update failed", Toast.LENGTH_SHORT).show()
                );
    }


    private void removeTaskFromOldList(String taskName) {
        List<TaskAdapter> adapters = List.of(criticalAdapter, importantAdapter, urgentAdapter, lowPriorityAdapter);
        for (TaskAdapter adapter : adapters) {
            if (adapter.removeTask(taskName)) {
                adapter.notifyDataSetChanged();
                break; // Stop loop after finding the task
            }
        }
    }

    private void addTaskToNewList(String taskName, String category) {
        String taskId = taskIdMap.get(taskName);
        if (taskId == null) return; // Ensure taskId exists

        switch (category) {
            case "Urgent Important":
                criticalAdapter.addTask(taskName, taskId);
                criticalAdapter.notifyDataSetChanged();
                break;
            case "Not Urgent Important":
                importantAdapter.addTask(taskName, taskId);
                importantAdapter.notifyDataSetChanged();
                break;
            case "Urgent Unimportant":
                urgentAdapter.addTask(taskName, taskId);
                urgentAdapter.notifyDataSetChanged();
                break;
            case "Not Urgent Unimportant":
                lowPriorityAdapter.addTask(taskName, taskId);
                lowPriorityAdapter.notifyDataSetChanged();
                break;
        }
        // Update taskIdMap to ensure correct ID is stored
        taskIdMap.put(taskName, taskId);
    }
}

