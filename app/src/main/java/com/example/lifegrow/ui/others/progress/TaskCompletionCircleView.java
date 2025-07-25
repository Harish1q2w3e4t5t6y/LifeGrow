package com.example.lifegrow.ui.others.progress;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class TaskCompletionCircleView extends View {

    private int completed = 0;
    private int total = 1;

    private Paint circlePaint;
    private Paint arcPaint;
    private Paint textPaint;

    private int circleColor = Color.LTGRAY; // Default color for the background circle
    private int arcColor = Color.parseColor("#66BB6A"); // Default color for the arc (green)

    // Eisenhower categories
    private int[] eisenhowerCompleted = new int[4];  // Completed tasks for 4 Eisenhower categories
    private int[] eisenhowerTotal = new int[4];      // Total tasks for 4 Eisenhower categories
    private int[] eisenhowerColors = {
            Color.parseColor("#FF7043"),  // Urgent & Important - Red
            Color.parseColor("#66BB6A"),  // Not Urgent & Important - Green
            Color.parseColor("#FFCA28"),  // Urgent & Not Important - Yellow
            Color.parseColor("#29B6F6")   // Not Urgent & Not Important - Blue
    };

    public TaskCompletionCircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // Circle paint (background)
        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setColor(circleColor);
        circlePaint.setStrokeWidth(20f);

        // Arc paint (progress)
        arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setColor(arcColor);
        arcPaint.setStrokeWidth(20f);
        arcPaint.setStrokeCap(Paint.Cap.ROUND);

        // Text paint (percentage text)
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(48f);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    // Set the progress values (completed, total) for the main circle
    public void setProgress(int completed, int total) {
        this.completed = completed;
        this.total = total == 0 ? 1 : total; // Prevent division by zero
        invalidate();
    }

    // Set the progress values (completed, total) for the Eisenhower categories
    public void setEisenhowerProgress(int[] completed, int[] total) {
        if (completed.length == 4 && total.length == 4) {
            this.eisenhowerCompleted = completed;
            this.eisenhowerTotal = total;
            invalidate();
        }
    }

    // Method to set the circle's background color
    public void setCircleColor(int color) {
        this.circleColor = color;
        circlePaint.setColor(circleColor); // Update the color of the circle paint
        invalidate(); // Redraw the view with the new color
    }

    // Method to set the arc's color (progress)
    public void setArcColor(int color) {
        this.arcColor = color;
        arcPaint.setColor(arcColor); // Update the color of the arc paint
        invalidate(); // Redraw the view with the new color
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        float radius = Math.min(centerX, centerY) - 30f;

        // Draw the main background circle (center circle)
        canvas.drawCircle(centerX, centerY, radius, circlePaint);

        // Draw the main arc
        float sweepAngle = (360f * completed) / total;
        canvas.drawArc(centerX - radius, centerY - radius,
                centerX + radius, centerY + radius,
                -90, sweepAngle, false, arcPaint);

        // Draw percentage text
        int percent = (int) ((completed * 100f) / total);
        canvas.drawText(percent + "%", centerX, centerY + 20f, textPaint);

        // Draw the four Eisenhower circles around the main circle (2x2 grid)
        float offsetX = radius * 2 + 50f; // Spacing between circles
        float offsetY = radius * 2 + 50f;

        for (int i = 0; i < 4; i++) {
            float angle = (360f * eisenhowerCompleted[i]) / eisenhowerTotal[i];
            float circleOffsetX = (i % 2 == 0) ? -offsetX : offsetX; // Positioning circles
            float circleOffsetY = (i < 2) ? -offsetY : offsetY;

            // Draw Eisenhower category background circle
            circlePaint.setColor(Color.LTGRAY);
            canvas.drawCircle(centerX + circleOffsetX, centerY + circleOffsetY, radius, circlePaint);

            // Draw progress arc for Eisenhower circle
            arcPaint.setColor(eisenhowerColors[i]);
            canvas.drawArc(centerX + circleOffsetX - radius, centerY + circleOffsetY - radius,
                    centerX + circleOffsetX + radius, centerY + circleOffsetY + radius,
                    -90, angle, false, arcPaint);

            // Draw percentage text for Eisenhower category
            int eisenhowerPercent = (int) ((eisenhowerCompleted[i] * 100f) / eisenhowerTotal[i]);
            canvas.drawText(eisenhowerPercent + "%", centerX + circleOffsetX, centerY + circleOffsetY + 20f, textPaint);
        }
    }
}
