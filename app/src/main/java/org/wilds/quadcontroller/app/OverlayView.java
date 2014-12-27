package org.wilds.quadcontroller.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class OverlayView extends View {

    private Paint paint;
    private Paint paintBigText;

    protected int throttle = 0;
    protected int yaw = 0;      //degree
    protected int roll = 0;     //degree
    protected int pitch = 0;    //degree
    protected int altitude = 0; //cm

    protected static final float PITCH_TRANSLATE_FACTOR = 15.0f;     // from degree to dpi
    protected static final float PITCH_STEP = PITCH_TRANSLATE_FACTOR * 5;

    protected static final int THROTTLE_STEPS = 12;
    protected static final int THROTTLE_STEP_VALUE = 10;
    protected static final int THROTTLE_STEPS_DISPLAY_VALUE = 20;

    protected static final int ALTITUDE_STEPS = 8;
    protected static final int ALTITUDE_STEP_VALUE = 100;
    protected static final int ALTITUDE_STEPS_DISPLAY_VALUE = 200;

    public OverlayView(Context context) {
        super(context);
        init(null, 0);
    }

    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public OverlayView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setAlpha(100);
        paint.setAntiAlias(true);
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(12);
        paint.setStrokeWidth(2);
        paint.setStyle(Paint.Style.STROKE);

        paintBigText = new Paint();
        paintBigText.setColor(Color.GREEN);
        paintBigText.setAlpha(100);
        paintBigText.setAntiAlias(true);
        paintBigText.setFlags(Paint.ANTI_ALIAS_FLAG);
        paintBigText.setTextAlign(Paint.Align.CENTER);
        paintBigText.setTextSize(18);
        paintBigText.setStrokeWidth(2);
        paintBigText.setStyle(Paint.Style.STROKE);
    }

    public void setData(int throttle, int yaw, int roll, int pitch, int altitude) {
        this.throttle = throttle;
        this.yaw = yaw;     // TODO convert in degree
        this.roll = roll;
        this.pitch = pitch;
        this.altitude = altitude;
        this.invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

        int contentWidth = getWidth() - paddingLeft - paddingRight;
        int contentHeight = getHeight() - paddingTop - paddingBottom;


        // Throttle
        drawIndicator(canvas, paddingLeft, contentHeight * 2 / 10, contentWidth * 1 / 20, contentHeight * 6 / 10, THROTTLE_STEPS, THROTTLE_STEP_VALUE, THROTTLE_STEPS_DISPLAY_VALUE, throttle, paint, paintBigText, false);

        // Altitude
        drawIndicator(canvas, paddingLeft + contentWidth * 19 / 20, contentHeight * 2 / 10, contentWidth * 1 / 20, contentHeight * 6 / 10, ALTITUDE_STEPS, ALTITUDE_STEP_VALUE, ALTITUDE_STEPS_DISPLAY_VALUE, altitude, paint, paintBigText, true);

        paint.setTextAlign(Paint.Align.CENTER);

        // Pitch and roll
        int offsetx = contentHeight / 2;
        canvas.rotate(roll, paddingLeft + contentWidth / 2, paddingTop + contentHeight / 2);
        canvas.translate(0, -pitch * PITCH_TRANSLATE_FACTOR);
        canvas.drawLine(paddingLeft + contentWidth * 2 / 10, offsetx, paddingLeft + contentWidth * 8 / 10, offsetx, paint); // main
        for (int i = -36; i < 36; ++i) {
            if (i != 0) {
                String text = "" + (i * 5);
                Rect rect = new Rect();
                paint.getTextBounds(text, 0, text.length(), rect);
                canvas.drawText(text, paddingLeft + contentWidth * 7 / 20 - rect.width(), offsetx - i * PITCH_STEP + rect.height() / 2, paint);
                canvas.drawText(text, paddingLeft + contentWidth * 13 / 20 + rect.width(), offsetx - i * PITCH_STEP + rect.height() / 2, paint);


                canvas.drawLine(paddingLeft + contentWidth * 7 / 20, offsetx - i * PITCH_STEP + contentWidth * 2 / 20 / 5, paddingLeft + contentWidth * 7 / 20, offsetx - i * PITCH_STEP, paint);
                canvas.drawLine(paddingLeft + contentWidth * 7 / 20, offsetx - i * PITCH_STEP, paddingLeft + contentWidth * 9 / 20, offsetx - i * PITCH_STEP, paint);

                canvas.drawLine(paddingLeft + contentWidth * 13 / 20, offsetx - i * PITCH_STEP + contentWidth * 2 / 20 / 5, paddingLeft + contentWidth * 13 / 20, offsetx - i * PITCH_STEP, paint);
                canvas.drawLine(paddingLeft + contentWidth * 11 / 20, offsetx - i * PITCH_STEP, paddingLeft + contentWidth * 13 / 20, offsetx - i * PITCH_STEP, paint);
            }
        }
    }

    protected void drawIndicator(Canvas canvas, int offsetX, int offsetY, int width, int height, int stepCount, int stepValue, int stepDisplay, int value, Paint paint, Paint paintBigText, boolean flip) {
        // vertical indicator
        Path path = new Path();
        if (!flip) {
            path.moveTo(0, 0);
            path.lineTo(width, 0);
            path.lineTo(width, height);
            path.lineTo(0, height);
        } else {
            path.moveTo(width, 0);
            path.lineTo(0, 0);
            path.lineTo(0, height);
            path.lineTo(width, height);
        }
        path.offset(offsetX, offsetY);
        canvas.drawPath(path, paint);

        // steps
        int step_distance = height / stepCount;
        float thr_off = (float) (value % stepValue) / stepValue;
        int startval = (int) Math.floor(value / stepValue) * stepValue - (stepCount / 2) * stepValue;
        int stepSize = width * 1 / 2;
        for (int i = 1; i < stepCount; ++i) {
            int val = startval + (stepCount - i) * stepValue;      // stepCount - i for mirror the value in y
            if (val >= 0) {
                if (!flip)
                    canvas.drawLine(offsetX + (width - stepSize), offsetY + step_distance * i + step_distance * thr_off, offsetX + width, offsetY + step_distance * i + step_distance * thr_off, paint);
                else
                    canvas.drawLine(offsetX, offsetY + step_distance * i + step_distance * thr_off, offsetX + stepSize, offsetY + step_distance * i + step_distance * thr_off, paint);
                if (val % stepDisplay == 0 && Math.abs(val - value) > stepValue / 2) {
                    String text = "" + val;
                    Rect rect = new Rect();
                    paint.getTextBounds(text, 0, text.length(), rect);
                    if (!flip) {
                        paint.setTextAlign(Paint.Align.RIGHT);
                        canvas.drawText(text, offsetX + (width - stepSize) - 1, offsetY + step_distance * i + step_distance * thr_off + rect.height() / 2, paint);
                    } else {
                        paint.setTextAlign(Paint.Align.LEFT);
                        canvas.drawText(text, offsetX + stepSize + 1, offsetY + step_distance * i + step_distance * thr_off + rect.height() / 2, paint);
                    }
                }
            }
        }

        // value
        String value_str = "" + value;
        Rect valueRect = new Rect();
        paintBigText.getTextBounds(value_str, 0, value_str.length(), valueRect);

        Rect rect = new Rect();
        paintBigText.getTextBounds("888888888888888", 0, ("" + stepCount * stepValue).length() + 1, rect);
        Path pathValue = new Path();
        int h = rect.height() + 10;
        int w = rect.width() + 6;
        int arrowSize = stepSize;
        if (!flip) {
            pathValue.moveTo(0, 0);
            pathValue.lineTo(w, 0);
            pathValue.lineTo(w, h * 1 / 2 - arrowSize * 1 / 2);
            pathValue.lineTo(w + arrowSize, h * 1 / 2);
            pathValue.lineTo(w, h * 1 / 2 + arrowSize * 1 / 2);
            pathValue.lineTo(w, h);
            pathValue.lineTo(0, h);
            pathValue.close();
            pathValue.offset(offsetX + (width - stepSize) - (w + arrowSize), offsetY + step_distance * (int) Math.floor(stepCount / 2) - h / 2);
            paintBigText.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(value_str, offsetX + (width - stepSize) - arrowSize - 3, offsetY + step_distance * (int) Math.floor(stepCount / 2) + valueRect.height() / 2, paintBigText);
        } else {
            pathValue.moveTo(0, 0);
            pathValue.lineTo(w, 0);
            pathValue.lineTo(w, h);
            pathValue.lineTo(0, h);
            pathValue.lineTo(0, h * 1 / 2 + arrowSize * 1 / 2);
            pathValue.lineTo(-arrowSize, h * 1 / 2);
            pathValue.lineTo(0, h * 1 / 2 - arrowSize * 1 / 2);
            pathValue.close();
            pathValue.offset(offsetX + stepSize + arrowSize, offsetY + step_distance * (int) Math.floor(stepCount / 2) - h / 2);
            paintBigText.setTextAlign(Paint.Align.LEFT);
            canvas.drawText(value_str, offsetX + stepSize + arrowSize + 3, offsetY + step_distance * (int) Math.floor(stepCount / 2) + valueRect.height() / 2, paintBigText);
        }
        canvas.drawPath(pathValue, paint);
    }

}
