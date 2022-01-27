package com.example.tocheeeeck;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

public class TestSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    DrawingThread drawingThread;

    public TestSurfaceView(Context context) {
        super(context);
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        drawingThread = new DrawingThread(getContext(), getHolder());
        drawingThread.start();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        drawingThread.setTouchXY(event.getX(), event.getY());
        return false;
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        drawingThread.requestStop();
        boolean retry = true;
        while (retry) {
            try {
                drawingThread.join();
                retry = false;
            } catch (InterruptedException e) {
                // ignore e
            }
        }
    }
    private static class DrawingThread extends Thread {
        private final SurfaceHolder surfaceHolder;
        private volatile boolean running = true;
        private Rect buttonCircle = new Rect(0, 0, 100, 100);
        private Rect buttonRect = new Rect(100, 0, 200, 100);
        private final Paint foregroundPaint;
        {
            foregroundPaint = new Paint();
            foregroundPaint.setColor(Color.YELLOW);
        }
        public enum Type {
            CIRCLE,
            RECT
        }
        private Type type = Type.CIRCLE;
        private float touchX = 10;
        private float touchY = 10;
        public DrawingThread(Context context, SurfaceHolder holder) {
            surfaceHolder = holder;
        }

        public void requestStop() {
            running = false;
        }

        public void setTouchXY(float touchX, float touchY) {
            this.touchX = touchX;
            this.touchY = touchY;
        }
        @Override
        public void run() {
            while (running) {
                Canvas canvas = surfaceHolder.lockCanvas();
                if (canvas != null) {
                    try {
                        foregroundPaint.setColor(Color.RED);
                        canvas.drawRect(buttonRect, foregroundPaint);
                        foregroundPaint.setColor(Color.GREEN);
                        canvas.drawRect(buttonCircle, foregroundPaint);
                        foregroundPaint.setColor(Color.YELLOW);
                        if (touchX < 100 && touchX > 0 && touchY > 0 && touchY < 100) {
                            type = Type.CIRCLE;
                        }
                        else if (touchX < 200 && touchX > 100 && touchY > 0 && touchY < 100) {
                            type = Type.RECT;
                        }
                        else {
                            switch (type) {
                                case RECT:
                                    canvas.drawRect(touchX - 75, touchY - 50, touchX + 75, touchY + 50, foregroundPaint);
                                    break;
                                case CIRCLE:
                                    canvas.drawCircle(touchX, touchY, 60, foregroundPaint);
                                    break;
                                default:
                                    break;
                            }
                        }
                    } catch (Exception e) {
                        //ignore e
                    } finally {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }
                }
            }
        }
    }
}
