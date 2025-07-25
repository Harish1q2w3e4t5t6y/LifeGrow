package com.example.lifegrow.ui.others.progress;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class EisenhowerView extends View {

    // Arrays to hold completed and total counts for each of the 4 categories.
    private int[] eisenhowerCompleted = new int[4];
    private int[] eisenhowerTotal = new int[4];

    // Unique colors for each Eisenhower category:
    // 0 - Urgent & Important, 1 - Not Urgent & Important,
    // 2 - Urgent & Not Important, 3 - Not Urgent & Not Important
    private int[] eisenhowerColors = {
            Color.parseColor("#FF7043"),  // Red: Urgent & Important
            Color.parseColor("#66BB6A"),  // Green: Not Urgent & Important
            Color.parseColor("#FFCA28"),  // Yellow: Urgent & Not Important
            Color.parseColor("#29B6F6")   // Blue: Not Urgent & Not Important
    };

    // Paint objects for drawing arcs and text.
    private Paint arcPaint;
    private Paint textPaint;

    public EisenhowerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // Initialize the arc paint used for drawing the arcs.
        arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeWidth(40f);  // Adjust the stroke width as needed

        // Initialize the text paint used for drawing the percentage text.
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(48f);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    /**
     * Sets the progress for each Eisenhower category.
     *
     * @param completed An array of completed tasks for each category (length must be 4).
     * @param total     An array of total tasks for each category (length must be 4).
     */
    public void setEisenhowerProgress(int[] completed, int[] total) {
        if (completed.length == 4 && total.length == 4) {
            this.eisenhowerCompleted = completed;
            this.eisenhowerTotal = total;
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Determine the center and maximum radius (with some padding).
        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        float radius = Math.min(centerX, centerY) - 20f;

        // Loop over 4 quadrants (each representing one Eisenhower category)
        for (int i = 0; i < 4; i++) {
            // Each quadrant covers 90 degrees. Calculate the starting angle.
            float startAngle = -90 + i * 90;

            // Draw a full quadrant background arc in light gray.
            arcPaint.setColor(Color.LTGRAY);
            canvas.drawArc(centerX - radius, centerY - radius,
                    centerX + radius, centerY + radius,
                    startAngle, 90, false, arcPaint);

            // Calculate the progress ratio for the category.
            int totalTasks = (eisenhowerTotal[i] == 0) ? 1 : eisenhowerTotal[i];  // Avoid division by zero.
            float progressRatio = (float) eisenhowerCompleted[i] / totalTasks;
            float sweepAngle = 90 * progressRatio;

            // Draw the progress arc with the unique color for this category.
            arcPaint.setColor(eisenhowerColors[i]);
            canvas.drawArc(centerX - radius, centerY - radius,
                    centerX + radius, centerY + radius,
                    startAngle, sweepAngle, false, arcPaint);

            // Compute the mid-angle of the quadrant for text placement.
            float midAngle = startAngle + 45;
            // Adjust the text radius slightly inside the arc.
            float textRadius = radius - 40f;
            float textX = centerX + (float) Math.cos(Math.toRadians(midAngle)) * textRadius;
            float textY = centerY + (float) Math.sin(Math.toRadians(midAngle)) * textRadius + 16; // +16 to adjust vertical centering

            // Convert progress ratio to a percentage.
            int percent = (int) (progressRatio * 100);
            canvas.drawText(percent + "%", textX, textY, textPaint);
        }
    }
}
