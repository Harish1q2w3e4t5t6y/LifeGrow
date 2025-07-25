package com.example.lifegrow.ui.others.progress;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class BarGraphView extends View {

    private int[] data = new int[]{};
    private int maxValue = 10;

    private Paint barPaint;
    private Paint bgPaint;

    public BarGraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        barPaint.setColor(Color.parseColor("#29B6F6")); // blue
        barPaint.setStyle(Paint.Style.FILL);

        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(Color.LTGRAY);
        bgPaint.setStyle(Paint.Style.FILL);
    }

    public void setData(int[] values, int maxValue) {
        this.data = values;
        this.maxValue = maxValue == 0 ? 1 : maxValue;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (data == null || data.length == 0) return;

        float barWidth = getWidth() / (float) data.length;
        float chartHeight = getHeight();

        for (int i = 0; i < data.length; i++) {
            float valueHeight = (data[i] / (float) maxValue) * chartHeight;

            // Background
            canvas.drawRect(i * barWidth, 0, (i + 1) * barWidth, chartHeight, bgPaint);

            // Foreground bar
            canvas.drawRect(i * barWidth, chartHeight - valueHeight,
                    (i + 1) * barWidth, chartHeight, barPaint);
        }
    }
}
