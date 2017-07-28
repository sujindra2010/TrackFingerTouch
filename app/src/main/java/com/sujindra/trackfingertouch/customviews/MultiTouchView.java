package com.sujindra.trackfingertouch.customviews;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.sujindra.trackfingertouch.utils.AppUtil;

/**
 * Created by Sujindra-PC on 7/28/2017.
 */

public class MultiTouchView extends View {

    private static final float CIRCLE_RADIUS = 60;
    private static final float TEXT_SIZE = 48;
    private static final String DISPLAY_TEXT = "Touch on the screen";
    private FirebaseAnalytics firebaseAnalytics;
    private SparseArray<PointF> activePointersSparseArray;
    private Paint circlePaint, textPaint;
    private int[] paintColors = {Color.GREEN, Color.BLUE, Color.RED,
            Color.YELLOW, Color.MAGENTA, Color.GRAY};

    public MultiTouchView(Context context) {
        super(context);
        initUi();
        firebaseAnalytics = FirebaseAnalytics.getInstance(context);

    }

    public MultiTouchView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initUi();
        firebaseAnalytics = FirebaseAnalytics.getInstance(context);

    }

    public MultiTouchView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initUi();
        firebaseAnalytics = FirebaseAnalytics.getInstance(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MultiTouchView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initUi();
        firebaseAnalytics = FirebaseAnalytics.getInstance(context);
    }

    private void initUi() {
        activePointersSparseArray = new SparseArray<>();
        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setColor(Color.BLUE);
        circlePaint.setStrokeWidth(16);
        circlePaint.setStyle(Paint.Style.STROKE);
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(TEXT_SIZE);
        textPaint.setColor(Color.GREEN);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        // get pointer index from the event object
        int pointerIndex = event.getActionIndex();

        // get pointer ID
        int pointerId = event.getPointerId(pointerIndex);

        // get masked (not specific to a pointer) action
        int maskedAction = event.getActionMasked();

        switch (maskedAction) {

            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN: {
                trackEvent(event.getX(pointerIndex), event.getY(pointerIndex));
                PointF pointF = new PointF();
                pointF.x = event.getX(pointerIndex);
                pointF.y = event.getY(pointerIndex);
                activePointersSparseArray.put(pointerId, pointF);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                Log.d("pointer count::", String.valueOf(event.getPointerCount()));
                for (int i = 0; i < event.getPointerCount(); i++) {
                    PointF pointF = activePointersSparseArray.get(event.getPointerId(i));
                    if (pointF != null) {
                        pointF.x = event.getX(i);
                        pointF.y = event.getY(i);
                    }
                }
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL: {
                trackEvent(event.getX(pointerIndex), event.getY(pointerIndex));
                activePointersSparseArray.remove(pointerId);
                break;
            }
        }
        invalidate();
        return true;
    }

    private void trackEvent(float x, float y) {
        Bundle params = new Bundle();
        params.putString("x", String.valueOf(x));
        params.putString("y", String.valueOf(y));
        firebaseAnalytics.logEvent("touch_event", params);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // draw all pointers
        if (activePointersSparseArray != null) {
            for (int i = 0; i < activePointersSparseArray.size(); i++) {
                PointF pointF = activePointersSparseArray.valueAt(i);
                if (pointF != null) {
                    circlePaint.setColor(paintColors[activePointersSparseArray.keyAt(i) % 6]);
                    canvas.drawCircle(pointF.x, pointF.y, CIRCLE_RADIUS, circlePaint);
                }
            }
            if (activePointersSparseArray.size() == 0) {
                float textWidth = textPaint.measureText(DISPLAY_TEXT);
                canvas.drawText(DISPLAY_TEXT, ((AppUtil.getScreenWidth(getContext()) - textWidth) / 2), AppUtil.getScreenHeight(getContext()) / 2, textPaint);
            }
        }

    }
}
