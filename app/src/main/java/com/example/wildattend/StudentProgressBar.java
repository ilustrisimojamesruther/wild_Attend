package com.example.wildattend;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class StudentProgressBar extends View {
    private Paint paint;
    private RectF rectF;
    private int strokeWidth = 50; // example value
    private int progress = 0;
    private static final String TAG = "StudentProgressBar";

    public StudentProgressBar(Context context) {
        super(context);
        init();
    }

    public StudentProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public StudentProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(Color.RED); // Base color
        rectF = new RectF();
    }

    public void setProgress(int progress) {
        this.progress = progress;
        invalidate(); // Force redraw
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        int radius = Math.min(width, height) / 2;

        rectF.set(strokeWidth / 2, strokeWidth / 2, width - strokeWidth / 2, height - strokeWidth / 2);

        // Draw background circle
        paint.setColor(Color.parseColor("#800000"));
        canvas.drawArc(rectF, -90, 360, false, paint);

        // Draw progress
        paint.setColor(Color.parseColor("#FFBF00"));
        canvas.drawArc(rectF, -90, 360 * progress / 100, false, paint);

    }
}
