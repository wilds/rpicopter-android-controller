package org.wilds.quadcontroller.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;

public class OverlayView extends View implements SharedPreferences.OnSharedPreferenceChangeListener {

    private boolean hudEnable = true;

    private Paint paint;
    private Paint paintBigText;
    private Paint paintRed;

    protected int throttle = 0;
    protected float yaw = 0;      //degree
    protected float roll = 0;     //degree
    protected float pitch = 0;    //degree
    protected int altitude = 0; //cm
    protected int altitude_target = 0; //cm

    protected int recording = 0;

    protected static final float PITCH_TRANSLATE_FACTOR = 16.0f;     // from degree to dpi
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

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        hudEnable = sharedPreferences.getBoolean("hud_enable", true);
        int color = sharedPreferences.getInt("hud_color", 0x645CFF7B);

        paint = new Paint();
        paint.setColor(color);
        paint.setAntiAlias(true);
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(12);
        paint.setStrokeWidth(2);
        paint.setStyle(Paint.Style.STROKE);

        paintBigText = new Paint();
        paintBigText.setColor(color);
        paintBigText.setAntiAlias(true);
        paintBigText.setFlags(Paint.ANTI_ALIAS_FLAG);
        paintBigText.setTextAlign(Paint.Align.CENTER);
        paintBigText.setTextSize(18);
        paintBigText.setStrokeWidth(2);
        paintBigText.setStyle(Paint.Style.STROKE);

        paintRed = new Paint();
        paintRed.setColor(Color.RED);
        paintRed.setAntiAlias(true);
        paintRed.setFlags(Paint.ANTI_ALIAS_FLAG);
        paintRed.setTextAlign(Paint.Align.CENTER);
        paintRed.setTextSize(18);
        paintRed.setStrokeWidth(2);
        paintRed.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        hudEnable = sharedPreferences.getBoolean("hud_enabled", true);
        int color = sharedPreferences.getInt("hud_color", 0x645CFF7B);
        paint.setColor(color);
        paintBigText.setColor(color);
        this.invalidate();
    }

    public void setData(int throttle, int yaw, int pitch, int roll, int altitude, int altitude_target, int recording) {
        this.throttle = throttle;
        this.yaw = yaw;
        this.pitch = pitch;
        this.roll = roll;
        this.altitude = altitude;
        this.altitude_target = altitude_target;
        this.recording = recording;
        this.invalidate();
    }

    public void setData(int throttle, float yaw, float pitch, float roll, int altitude, int altitude_target, int recording) {
        this.throttle = throttle;

        // convert in degree
        /*
        this.yaw = (int) (yaw * 180 / Math.PI);
        this.pitch = (int) (pitch * 180 / Math.PI);
        this.roll = (int) (roll * 180 / Math.PI);
        */

        this.yaw = yaw;
        this.pitch = pitch;
        this.roll = roll;
        this.altitude = altitude;
        this.altitude_target = altitude_target;
        this.recording = recording;
        this.invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (hudEnable) {
            int paddingLeft = getPaddingLeft();
            int paddingTop = getPaddingTop();
            int paddingRight = getPaddingRight();
            int paddingBottom = getPaddingBottom();

            int contentWidth = getWidth() - paddingLeft - paddingRight;
            int contentHeight = getHeight() - paddingTop - paddingBottom;

            // Record status
            if (recording == 1) {
                Rect rect = new Rect();
                paintRed.getTextBounds("REC", 0, 3, rect);
                canvas.drawText("REC", getWidth() - rect.width(), rect.height(), paintRed);
                int radius = contentWidth / 20 / 3;
                canvas.drawCircle(getWidth() - rect.width() - radius * 4, rect.height() - radius, radius, paintRed);
            } else if (recording == 2) {
                Rect rect = new Rect();
                paintRed.getTextBounds("PAUSE", 0, 3, rect);
                canvas.drawText("PAUSE", getWidth() - rect.width(), rect.height(), paintRed);
            }

            // Throttle
            drawIndicator(canvas, paddingLeft, contentHeight * 2 / 10, contentWidth * 1 / 20, contentHeight * 6 / 10, THROTTLE_STEPS, THROTTLE_STEP_VALUE, THROTTLE_STEPS_DISPLAY_VALUE, throttle, paint, paintBigText, false, -1);

            // Altitude
            drawIndicator(canvas, paddingLeft + contentWidth * 19 / 20, contentHeight * 2 / 10, contentWidth * 1 / 20, contentHeight * 6 / 10, ALTITUDE_STEPS, ALTITUDE_STEP_VALUE, ALTITUDE_STEPS_DISPLAY_VALUE, altitude, paint, paintBigText, true, altitude_target);

            paint.setTextAlign(Paint.Align.CENTER);

            // Pitch and roll
            int offsetx = contentHeight / 2;
            canvas.rotate(roll, paddingLeft + contentWidth / 2, paddingTop + contentHeight / 2);

            /*
            Path t = new Path();
            t.moveTo(contentWidth * 1 / 20 / 2, 0);
            t.lineTo(contentWidth * 1 / 20, contentWidth * 1 / 20);
            t.lineTo(0, contentWidth * 1 / 20);
            t.close();
            t.offset( paddingLeft + contentWidth * 10 / 20 - contentWidth * 1 / 20 / 2, contentHeight * 1/20);
            canvas.drawPath(t, paint);
            */

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
    }

    protected void drawIndicator(Canvas canvas, int offsetX, int offsetY, int width, int height, int stepCount, int stepValue, int stepDisplay, int value, Paint paint, Paint paintBigText, boolean flip, int target) {
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
        float target_y = -1;
        float zero_y = -1;
        int step_distance = height / stepCount;
        float thr_off = (float) (value % stepValue) / stepValue;
        int startval = (int) Math.floor(value / stepValue) * stepValue - (stepCount / 2) * stepValue;
        int stepSize = width * 1 / 2;
        for (int i = 0; i < stepCount; ++i) {
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
                if (target > 0) {
                    if (Math.abs(val - target) < stepValue) {
                        float target_off = thr_off + (float) ((val - target) % stepValue) / stepValue;
                        float target_x = !flip ? offsetX + (width - stepSize * 2) : offsetX;
                        target_y = offsetY + step_distance * i + step_distance * target_off;
                        //canvas.drawLine(target_x, target_y, target_x + stepSize * 2, target_y, paint);
                    }
                }
                if (val == 0)
                    zero_y = offsetY + step_distance * i + step_distance * thr_off;
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

        if (target > 0) {
            if (target_y < 0) {
                if (value > target)
                    target_y = offsetY + height;
                else
                    target_y = offsetY;
            }
            if (zero_y < 0) {
                zero_y = offsetY + height;
            }
            canvas.drawLine(offsetX + stepSize, target_y, offsetX + stepSize, offsetY + step_distance * (int) Math.floor(stepCount / 2) - h / 2 + h * 1 / 2, paint);

            canvas.drawLine(offsetX + stepSize / 2 * (!flip ? +1 : -1), zero_y, offsetX + stepSize / 2 * (!flip ? +1 : -1), target_y, paint);
            canvas.drawLine(offsetX, target_y, offsetX + stepSize / 2 * (!flip ? +1 : -1), target_y, paint);
            canvas.drawLine(offsetX, zero_y, offsetX + stepSize / 2 * (!flip ? +1 : -1), zero_y, paint);
        }
    }

}
