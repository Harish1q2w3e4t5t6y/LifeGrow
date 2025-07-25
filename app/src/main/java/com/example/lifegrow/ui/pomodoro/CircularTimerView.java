package com.example.lifegrow.ui.pomodoro;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class CircularTimerView extends View {

    private Paint progressPaint;
    private Paint backgroundPaint;
    private float progress = 360f;  // Full circle (in degrees)
    private int totalTimeInSeconds = 1500;  // Default: 25 min
    private int timeLeftInSeconds = 1500;

    public CircularTimerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        progressPaint.setColor(0xFF000D54); // Light Red Color
        progressPaint.setStrokeWidth(8f); // Arc Thickness

        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setColor(Color.WHITE);
        backgroundPaint.setStrokeWidth(2f); // Background Thickness
    }

    // Update total time (in seconds) for the session
    public void setTotalTime(int seconds) {
        this.totalTimeInSeconds = seconds;
        this.timeLeftInSeconds = seconds;
        this.progress = 360f; // Reset to full circle
        invalidate();
    }

    // Update remaining time (in seconds) while timer is running
    public void setTimeLeft(int seconds) {
        this.timeLeftInSeconds = seconds;
        updateProgress();
    }

    private void updateProgress() {
        this.progress = ((float) timeLeftInSeconds / totalTimeInSeconds) * 360f;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float size = Math.min(getWidth(), getHeight());
        float radius = size / 2f - progressPaint.getStrokeWidth();
        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;

        // Draw background circle
        canvas.drawCircle(centerX, centerY, radius, backgroundPaint);

        // Draw progress arc
        RectF rectF = new RectF(
                centerX - radius, centerY - radius,
                centerX + radius, centerY + radius
        );
        canvas.drawArc(rectF, -90f, progress, false, progressPaint);
    }
}
