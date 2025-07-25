package com.example.lifegrow.ui.kanban;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.example.lifegrow.R;
import com.example.lifegrow.model.TaskModel;
import com.example.lifegrow.ui.eisenhower.TaskEditBottomSheetFragment;

import java.util.List;

public class TaskAdapterKanban extends RecyclerView.Adapter<TaskAdapterKanban.TaskViewHolder> {
    private final Context context;
    private final List<TaskModel> taskList;

    public TaskAdapterKanban(Context context, List<TaskModel> taskList) {
        this.context = context;
        this.taskList = taskList;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.task_item_kanban, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        TaskModel task = taskList.get(position);
        holder.taskTitle.setText(task.getName());

        holder.itemView.setOnClickListener(v -> {
            AppCompatActivity activity = (AppCompatActivity) context;
            TaskEditBottomSheetFragment bottomSheet =
                    TaskEditBottomSheetFragment.newInstance(task.getName(), task.getId());
            bottomSheet.show(activity.getSupportFragmentManager(), "TaskEditBottomSheet");
        });
    }


    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public void removeTask(int position) {
        if (position >= 0 && position < taskList.size()) {
            taskList.remove(position);
            notifyItemRemoved(position);
        }
    }

    public TaskModel getTaskAt(int position) {
        if (position >= 0 && position < taskList.size()) {
            return taskList.get(position);
        }
        return null; // Return null if position is out of bounds
    }

    public Context getContext() {
        return context;
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskTitle;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskTitle = itemView.findViewById(R.id.taskTitle);
        }
    }
}
