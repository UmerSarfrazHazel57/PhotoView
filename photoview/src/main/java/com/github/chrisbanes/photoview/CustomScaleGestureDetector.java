package com.github.chrisbanes.photoview;

import android.util.Log;
import android.view.MotionEvent;

import java.util.Timer;

public class CustomScaleGestureDetector {
    private static final int INVALID_POINTER_ID = -1;

    private float focusX;
    private float focusY;

    private int primaryPointerId;
    private int secondaryPointerId;
    private boolean isInProgress ;
    private float initialDistance;
    private float scaleFactor;
    private OnScaleGestureListener listener;
    private float sensitivity =0.5f;
    public CustomScaleGestureDetector( OnScaleGestureListener listener) {
        primaryPointerId = INVALID_POINTER_ID;
        secondaryPointerId = INVALID_POINTER_ID;
        isInProgress = false;
        initialDistance = 0f;
        scaleFactor = 1f;
        this.listener = listener;
    }

    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();

        Log.d("NEW_IMPL","Called");

        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN: {
                if (!isInProgress && event.getPointerCount() >= 2) {



                    primaryPointerId = event.getPointerId(0);
                    secondaryPointerId = event.getPointerId(1);

                    focusX = (event.getX(event.findPointerIndex(primaryPointerId)) +
                            event.getX(event.findPointerIndex(secondaryPointerId))) / 2f;
                    focusY = (event.getY(event.findPointerIndex(primaryPointerId)) +
                            event.getY(event.findPointerIndex(secondaryPointerId))) / 2f;
                    initialDistance = getFingerDistance(event, primaryPointerId, secondaryPointerId);
                    scaleFactor = 1f;
                    isInProgress = true;
                    listener.onScaleBegin(scaleFactor, getFocusX(event), getFocusY(event));
                    return true;
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (isInProgress && event.getPointerCount() >= 2) {
                    float currentDistance = getFingerDistance(event, primaryPointerId, secondaryPointerId);
                    float newScaleFactor = currentDistance / initialDistance;

                    // Calculate the focus point
                    float focusX = getFocusX(event);
                    float focusY = getFocusY(event);

                    // Apply sensitivity to adjust the scaling speed
                    newScaleFactor = (float) Math.pow(newScaleFactor, sensitivity);

                    // Calculate the scaling delta
                    float scaleFactorDelta = newScaleFactor / scaleFactor;

                    // Update the scale factor
                    scaleFactor = newScaleFactor;

                    // Invoke the listener with the custom data
                    listener.onScale(scaleFactorDelta, focusX, focusY);

                    return true;
                }
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP: {
                if (isInProgress && (event.getPointerId(event.getActionIndex()) == primaryPointerId
                        || event.getPointerId(event.getActionIndex()) == secondaryPointerId)) {
                    primaryPointerId = INVALID_POINTER_ID;
                    secondaryPointerId = INVALID_POINTER_ID;
                    isInProgress = false;
                    listener.onScaleEnd(scaleFactor, getFocusX(event), getFocusY(event));
                    return true;
                }
                break;
            }
        }

        return false;
    }

    private float getFingerDistance(MotionEvent event, int pointerId1, int pointerId2) {
        int pointerIndex1 = event.findPointerIndex(pointerId1);
        int pointerIndex2 = event.findPointerIndex(pointerId2);

        float x = event.getX(pointerIndex1) - event.getX(pointerIndex2);
        float y = event.getY(pointerIndex1) - event.getY(pointerIndex2);
        return (float) Math.sqrt(x * x + y * y);
    }

    private float getFocusX(MotionEvent event) {
        return (event.getX(event.findPointerIndex(primaryPointerId)) +
                event.getX(event.findPointerIndex(secondaryPointerId))) / 2f;
    }

    private float getFocusY(MotionEvent event) {
        return (event.getY(event.findPointerIndex(primaryPointerId)) +
                event.getY(event.findPointerIndex(secondaryPointerId))) / 2f;
    }

    public boolean isInProgress() {
        return isInProgress;
    }

    public interface OnScaleGestureListener {
        void onScale(float scaleFactor, float focusX, float focusY);

        void onScaleBegin(float scaleFactor, float focusX, float focusY);

        void onScaleEnd(float scaleFactor, float focusX, float focusY);
    }
}