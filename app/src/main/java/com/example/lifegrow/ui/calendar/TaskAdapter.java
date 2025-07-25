package com.example.lifegrow.ui.calendar;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.lifegrow.R;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private List<Task> taskList;

    public TaskAdapter(List<Task> taskList) {
        this.taskList = taskList;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.taskName.setText(task.getName());

        if (task.getName().equals("No tasks available")) {
            holder.taskTime.setText("");
            holder.taskProgress.setText("");
            holder.itemView.setBackgroundColor(Color.LTGRAY);
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM d", Locale.getDefault());

            // Max visual width of the bar
            final int MAX_BAR_LENGTH = 20;

            long startMillis = task.getStartDateTime().toDate().getTime();
            long endMillis = task.getEndDateTime().toDate().getTime();
            long totalDuration = endMillis - startMillis;
            long totalDays = totalDuration / (1000 * 60 * 60 * 24) + 1;

            long today = System.currentTimeMillis();
            long elapsedMillis = today - startMillis;
            long progressDays = elapsedMillis / (1000 * 60 * 60 * 24);
            progressDays = Math.max(0, Math.min(progressDays, totalDays)); // Clamp to [0, totalDays]

// Scale the progress to MAX_BAR_LENGTH
            int filled = (int) ((progressDays * MAX_BAR_LENGTH) / totalDays);
            int empty = MAX_BAR_LENGTH - filled;

            StringBuilder progressBar = new StringBuilder();
            progressBar.append(getEmojiFromColor(task.getColor()));
            for (int i = 0; i < filled; i++) progressBar.append("■");
            for (int i = 0; i < empty; i++) progressBar.append("─");


            String dateRange = "(" + sdf.format(task.getStartDateTime().toDate()) + " to " +
                    sdf.format(task.getEndDateTime().toDate()) + ")";
            holder.taskTime.setText(dateRange);
            holder.taskProgress.setText(progressBar.toString() + " " + dateRange);

            String color = task.getColor();
            if (color == null || color.equals("NoColor")) {
                holder.itemView.setBackgroundColor(Color.TRANSPARENT);
            } else {
                try {
                    int taskColor = Color.parseColor(color);
                    int lighterColor = blendColor(taskColor, Color.WHITE, 0.5f);
                    holder.itemView.setBackgroundColor(lighterColor);
                } catch (IllegalArgumentException e) {
                    holder.itemView.setBackgroundColor(0x818181);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    private int blendColor(int color1, int color2, float ratio) {
        int r = (int) ((Color.red(color1) * (1 - ratio)) + (Color.red(color2) * ratio));
        int g = (int) ((Color.green(color1) * (1 - ratio)) + (Color.green(color2) * ratio));
        int b = (int) ((Color.blue(color1) * (1 - ratio)) + (Color.blue(color2) * ratio));
        return Color.rgb(r, g, b);
    }

    private String getEmojiFromColor(String color) {
        if (color == null) return "⬜";
        switch (color.toLowerCase()) {
            case "#66bb6a": return "🟩"; // Green
            case "#ffca28": return "🟨"; // Yellow
            case "#ff7043": return "🟥"; // Red
            case "#29b6f6": return "🟦"; // Blue
            default: return "⬜";
        }
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskName, taskTime, taskProgress;

        TaskViewHolder(View itemView) {
            super(itemView);
            taskName = itemView.findViewById(R.id.taskName);
            taskTime = itemView.findViewById(R.id.taskTime);
            taskProgress = itemView.findViewById(R.id.taskProgressBar);
        }
    }
}
