package com.quentinjian.receiptlottery;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class OverlayView extends View {
    private Paint rectPaint, textPaint;
    private String detectedCode = "";

    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public OverlayView(Context context, @Nullable Rect rect, String text) {
        super(context);
        init();
    }

    private void init() {
        textPaint = new Paint();
        textPaint.setColor(Color.RED);
        textPaint.setTextSize(64f);
        textPaint.setTextAlign(Paint.Align.CENTER);

        rectPaint = new Paint();
        rectPaint.setColor(Color.GREEN);
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(5f);
    }

    public void updateDetectedCode(String code) {
        if (code != null && code.matches("^\\d{8}$")) {
            detectedCode = code;
        } else {
            detectedCode = null;
        }
        invalidate();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        if (detectedCode != null) {
            canvas.drawText("Detected number: " + detectedCode, getWidth() / 2f,
                    getHeight() / 2f, textPaint);

            float padding = 20f;
            canvas.drawRect(padding, padding, getWidth() - padding, getHeight() - padding,
                    rectPaint);
        }
    }
}
