package com.example.lifegrow.ui.eisenhower;

import android.content.ClipData;
import android.content.Context;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.example.lifegrow.R;
import com.example.lifegrow.repository.TaskRepository;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private final List<String> taskList;
    private final Map<String, String> taskIdMap; // Using Map for flexibility
    private final Context context;
    private final FirebaseFirestore db;
    private final String userId;

    public TaskAdapter(Context context, List<String> tasks, Map<String, String> taskIdMap) {
        this.context = context;
        this.taskList = tasks != null ? tasks : new ArrayList<>();
        this.taskIdMap = taskIdMap != null ? taskIdMap : new HashMap<>();
        this.db = FirebaseFirestore.getInstance();
        this.userId = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", "_");
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_item, parent, false);
        return new TaskViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        String taskName = taskList.get(position);
        holder.taskName.setText(taskName);

        // Enable Drag-and-Drop (long press)
        holder.itemView.setOnLongClickListener(v -> {
            ClipData data = ClipData.newPlainText("task", taskName);
            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
            v.startDragAndDrop(data, shadowBuilder, v, 0);
            return true;
        });

        // OnClick listener to open the Bottom Sheet for editing/deleting the task
        holder.itemView.setOnClickListener(v -> {
            String taskId = taskIdMap.get(taskName);
            if (taskId != null) {
                // Cast context to AppCompatActivity to get supportFragmentManager
                AppCompatActivity activity = (AppCompatActivity) context;
                TaskEditBottomSheetFragment bottomSheet =
                        TaskEditBottomSheetFragment.newInstance(taskName, taskId);
                bottomSheet.show(activity.getSupportFragmentManager(), "TaskEditBottomSheet");
            } else {
                Toast.makeText(context, "Task ID not found", Toast.LENGTH_SHORT).show();
            }
        });

        // Checkbox Logic: Mark as Done
        holder.taskCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    String taskId = taskIdMap.get(taskName);
                    if (taskId != null) {
                        markTaskAsDone(taskName, taskId, adapterPosition, holder);
                    } else {
                        Toast.makeText(context, "Task ID not found", Toast.LENGTH_SHORT).show();
                        holder.taskCheckbox.setChecked(false);
                    }
                }
            }
        });
    }

    private void markTaskAsDone(String taskName, String taskId, int position, TaskViewHolder holder) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "Done");
        updates.put("updatedAt", new Timestamp(new Date()));

        db.collection("users").document(userId)
                .collection("tasks").document(taskId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Task completed", Toast.LENGTH_SHORT).show();

                    // ✅ Play sound when task is marked as done
                    MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.taskdone);
                    mediaPlayer.setOnCompletionListener(MediaPlayer::release);
                    mediaPlayer.start();

                    // Remove task from cache and UI
                    TaskRepository.getInstance().removeTaskFromCache(taskId);
                    removeTask(taskName);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Task update failed", Toast.LENGTH_SHORT).show();
                    holder.taskCheckbox.setChecked(false);
                });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public void addTask(String task, String taskId) {
        if (!taskList.contains(task)) {
            taskList.add(task);
            taskIdMap.put(task, taskId);
            notifyItemInserted(taskList.size() - 1);
        }
    }

    public boolean removeTask(String taskName) {
        int position = taskList.indexOf(taskName);
        if (position >= 0) {
            taskList.remove(position);
            // Optionally remove the taskId from taskIdMap if not used elsewhere:
            // taskIdMap.remove(taskName);
            notifyItemRemoved(position);
            return true;
        }
        return false;
    }

    public int getItemPosition(String taskName) {
        if (taskList != null) {
            return taskList.indexOf(taskName);
        }
        return -1;
    }



    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskName;
        CheckBox taskCheckbox;

        public TaskViewHolder(View itemView) {
            super(itemView);
            taskName = itemView.findViewById(R.id.task_name);
            taskCheckbox = itemView.findViewById(R.id.task_checkbox);
        }
    }
}
