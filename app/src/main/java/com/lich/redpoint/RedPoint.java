package com.lich.redpoint;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Administrator on 2018/2/1.
 */

public class RedPoint extends View {
    private Paint mPaint;
    private int mOriginRadius = 40;

    private Point mOrigin, mCurrent;

    private int mNewRadius;
    private boolean isTouch = false;
    private boolean isAutoMove = false;
    private Point mEndPoint;

    public RedPoint(Context context) {
        super(context);
        init();
    }

    public RedPoint(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RedPoint(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.RED);
        mOrigin = new Point();
        mCurrent = new Point();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mOrigin.X = getWidth() / 2;
        mOrigin.Y = getHeight() / 2;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawTouchCircle(mCurrent.X, mCurrent.Y, canvas);
        drawOriginCircle(canvas);
//        int crossX = calCrossPointX();
//        int crossY = calCrossPointY();

        if (isTouch || isAutoMove) {
//            canvas.drawLine(mOriginX, mOriginY, mCurrentX, mCurrentY, bluePaint);
            Point point = getCircleAndLinesCrossPoint(mOrigin.X, mOrigin.Y, mNewRadius, mOrigin.X, mOrigin.Y, mCurrent.X, mCurrent.Y);
            Point tanPoint1 = getTangentCrossPoint(mOrigin.X, mOrigin.Y, mNewRadius, point.X, point.Y, 80);
            Point tanPoint2 = getTangentCrossPoint(mOrigin.X, mOrigin.Y, mNewRadius, point.X, point.Y, -80);

//            Paint greenPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//            greenPaint.setColor(Color.GREEN);
//            canvas.drawCircle(point.X, point.Y, 3, greenPaint);
//            canvas.drawCircle(tanPoint1.X, tanPoint1.Y, 3, greenPaint);
//            canvas.drawCircle(tanPoint2.X, tanPoint2.Y, 3, greenPaint);


            Point tpoint = getCircleAndLinesCrossPoint(mCurrent.X, mCurrent.Y, mOriginRadius, mOrigin.X, mOrigin.Y, mCurrent.X, mCurrent.Y);
            Point ttanPoint1 = getTangentCrossPoint(mCurrent.X, mCurrent.Y, mOriginRadius, tpoint.X, tpoint.Y, 85);
            Point ttanPoint2 = getTangentCrossPoint(mCurrent.X, mCurrent.Y, mOriginRadius, tpoint.X, tpoint.Y, -85);


//            canvas.drawCircle(tpoint.X, tpoint.Y, 3, greenPaint);
//            canvas.drawCircle(ttanPoint1.X, ttanPoint1.Y, 3, greenPaint);
//            canvas.drawCircle(ttanPoint2.X, ttanPoint2.Y, 3, greenPaint);
//
//            canvas.drawLine(tanPoint1.X, tanPoint1.Y, ttanPoint1.X, ttanPoint1.Y, mPaint);
//            canvas.drawLine(tanPoint2.X, tanPoint2.Y, ttanPoint2.X, ttanPoint2.Y, mPaint);

            Point middlePoint = getLinesCross(tanPoint1.X, tanPoint1.Y, ttanPoint2.X, ttanPoint2.Y, tanPoint2.X, tanPoint2.Y, ttanPoint1.X, ttanPoint1.Y);
            canvas.drawCircle(middlePoint.X, middlePoint.Y, 3, mPaint);

            Path p1 = new Path();
            p1.reset();
            p1.moveTo(tanPoint1.X, tanPoint1.Y);
            p1.quadTo(middlePoint.X, middlePoint.Y, ttanPoint1.X, ttanPoint1.Y);
            p1.lineTo(ttanPoint2.X, ttanPoint2.Y);
            p1.quadTo(middlePoint.X, middlePoint.Y, tanPoint2.X, tanPoint2.Y);
            p1.lineTo(tanPoint1.X, tanPoint1.Y);
            canvas.drawPath(p1, mPaint);

//            Path p2 = new Path();
//            p2.reset();
//            p2.moveTo(tanPoint2.X, tanPoint2.Y);
//            p2.quadTo(middlePoint.X, middlePoint.Y, ttanPoint2.X, ttanPoint2.Y);
//            canvas.drawPath(p2, mPaint);
        }
//        canvas.drawPoint(crossX, crossY, bluePaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        Log.d("LICH","event:" + event.getX() + "  :" + event.getY());
        if (event.getAction() == MotionEvent.ACTION_UP) {
            isTouch = false;
            mEndPoint = mCurrent;

            handlePointBack();
        } else {
            isTouch = true;
            mCurrent.X = (int) event.getX();
            mCurrent.Y = (int) event.getY();

            if (mCurrent.X < 0) {
                mCurrent.X = 0;
            } else if (mCurrent.X > getWidth()) {
                mCurrent.X = getWidth();
            }

            if (mCurrent.Y < 0) {
                mCurrent.Y = 0;
            } else if (mCurrent.Y > getHeight()) {
                mCurrent.Y = getHeight();
            }
        }
        invalidate();
        return true;
    }

    private void drawTouchCircle(float x, float y, Canvas canvas) {
        if (!isTouch && !isAutoMove) {
            return;
        }
        canvas.drawCircle(x, y, mOriginRadius, mPaint);
    }

    private void drawOriginCircle(Canvas canvas) {
        if (!isTouch && !isAutoMove) {
            canvas.drawCircle(mOrigin.X, mOrigin.Y, mOriginRadius, mPaint);
            return;
        }
        double distance = Math.sqrt(Math.pow((mOrigin.X - mCurrent.X), 2) + Math.pow((mOrigin.Y - mCurrent.Y), 2));
        double ra = distance == 0 ? 1 : distance / getHeight() * 2;

//        Log.d("LICH", "Height:" + getHeight());

        if (ra > 0 && ra < 0.7) {
            mNewRadius = (int) (mOriginRadius * (1 - ra));
        } else if (ra > 0) {
            mNewRadius = (int) (mOriginRadius * 0.3);
        } else {
            mNewRadius = mOriginRadius;
        }
        canvas.drawCircle(mOrigin.X, mOrigin.Y, mNewRadius, mPaint);
    }

    private Point getCircleAndLinesCrossPoint(float cx, float cy, float r, float stx, float sty, float edx, float edy) {
        // 求直线
        float k, b;
        float x1, y1, x2, y2;
        if (Math.abs(edx - stx) > 1) {
            k = (edy - sty) / (edx - stx);

            b = edy - k * edx;

            float c = cx * cx + (b - cy) * (b - cy) - r * r;
            float a = (1 + k * k);
            float b1 = (2 * cx - 2 * k * (b - cy));

            float d = b1 * b1 - 4 * a * c;
            if (d > 0) {

                float tmp = (float) Math.sqrt(b1 * b1 - 4 * a * c);
                x1 = (b1 + tmp) / (2 * a);
                y1 = k * x1 + b;
                x2 = (b1 - tmp) / (2 * a);
                y2 = k * x2 + b;
            } else {
                x1 = cx;
                y1 = cy + r;
                x2 = cx;
                y2 = cy - r;
            }
        } else {
            x1 = cx;
            y1 = cy + r;
            x2 = cx;
            y2 = cy - r;
        }

        Log.d("LICH", "x1:" + x1 + " y1:" + y1 + " x2:" + x2 + " y2:" + y2);
        //判断求出的点是否在圆上
/*        float res = (x1 - cx) * (x1 - cx) + (y1 - cy) * (y1 - cy);
        if (res == r*r){
            return new Point(x1, y1);
        } else {
            return new Point(x2, y2);
        }*/
        if (isBetween(stx, edx, x1) && isBetween(sty, edy, y1)) {
            return new Point(x1, y1);
        } else {
            return new Point(x2, y2);
        }
    }

//    private Point getCircleAndLinesCrossPoint(float cx, float cy, float r, float stx, float sty, float edx, float edy) {
//        // 求直线
//        float k, b;
//        int edxI = (int) edx;
//        int stxI = (int) stx;
//        if (edxI != stxI) {
//            k = (edy - sty) / (edx - stx);
//        } else {
//            k = 1;
//        }
//        Log.d("LICH", "k:" + k);
//        b = edy - k * edx;
//
//        //列方程
//        float tmp = (float) Math.sqrt((r * r - cx * cx - b * b - 2 * b * cy + cy * cy) + (k * b - cx + cy * k) * (k * b - cx + cy * k) / (1 + k * k));
//
//        float x1 = (float) (tmp - (k * b - cx + cy * k) / Math.sqrt(1 + k * k)) / (1 + k * k);
//        float y1 = k * x1 + b;
//        float x2 = (float) (-tmp - (k * b - cx + cy * k) / Math.sqrt(1 + k * k)) / (1 + k * k);
//        float y2 = k * x2 + b;
//        Log.d("LICH","x1:" + x1 + " y1:" + y1 + " x2:" + x2 + " y2:" + y2);
//
//        if (isBetween(stx, edx, x1) && isBetween(sty, edy, y1)) {
//            return new Point(x1, y1);
//        } else {
//            return new Point(x2, y2);
//        }
//    }

    private boolean isBetween(float start, float end, float target) {
        return (target >= start && target <= end) || (target >= end && target <= start);
    }

    private boolean isBetween(float x1, float y1, float x2, float y2, float tx, float ty) {
        return isBetween(x1, x2, tx) && isBetween(y1, y2, ty);
    }

    private Point getTangentCrossPoint(float cx, float cy, float r, float crossX, float crossY, float angle) {
        float disX = cx - crossX;
        float disY = cy - crossY;
        float originTan;
        if (disX == 0) {
            originTan = 1;
        } else {
            originTan = disY / disX;
        }
        float originAngle = (float) Math.toDegrees(Math.atan(originTan));
        float x1 = (float) (cx + r * Math.cos((originAngle + angle) * Math.PI / 180));
        float y1 = (float) (cy + r * Math.sin((originAngle + angle) * Math.PI / 180));
        return new Point(x1, y1);
    }

    private Point getLinesCross(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4) {
/*        float a = (y1 - y2) / (x1 - x2);
        float b = (x1 * y2 - x2 * y1) / (x1 - x2);

        float c = (y3 - y4) / (x3 - x4);
        float d = (x3 * y4 - x4 * y3) / (x3 - x4);*/

        float x = ((x1 - x2) * (x3 * y4 - x4 * y3) - (x3 - x4) * (x1 * y2 - x2 * y1))
                / ((x3 - x4) * (y1 - y2) - (x1 - x2) * (y3 - y4));

        float y = ((y1 - y2) * (x3 * y4 - x4 * y3) - (x1 * y2 - x2 * y1) * (y3 - y4))
                / ((y1 - y2) * (x3 - x4) - (x1 - x2) * (y3 - y4));

        return new Point(x, y);
    }

    private void handlePointBack() {
        final float a, b;
        if (mOrigin.X != mEndPoint.X) {
            a = (mEndPoint.Y - mOrigin.Y) / (mEndPoint.X - mOrigin.X);
        } else {
            a = 1;
        }
        b = mEndPoint.Y - (mOrigin.Y - mEndPoint.Y) / (mOrigin.X - mEndPoint.X) * mEndPoint.X;

//        float dis = (float) Math.sqrt((mEndPoint.X - mOrigin.X) * (mEndPoint.X - mOrigin.X) + (mEndPoint.Y - mOrigin.Y) * (mEndPoint.Y - mOrigin.Y));
//        final float speedX = 50;
//        final float speedY = a * speedX + b;
//        float speed = (float) Math.sqrt(speedX * speedX + speedY * speedY);

        ValueAnimator valueAnimator = ValueAnimator.ofObject(new TypeEvaluator() {
            @Override
            public Object evaluate(float v, Object o, Object t1) {
                Point target = new Point();
                if (v <= 0.6) {
                    if (mEndPoint.X > mOrigin.X) {
                        target.X = mEndPoint.X - v / 0.7f * (mEndPoint.X - mOrigin.X);
                        if (target.X < mOrigin.X) {
                            target.X = mOrigin.X;
                        }
                    } else {
                        target.X = mEndPoint.X + v / 0.7f * (mOrigin.X - mEndPoint.X);
                        if (target.X > mOrigin.X) {
                            target.X = mOrigin.X;
                        }
                    }

                    if (mEndPoint.Y > mOrigin.Y) {
                        target.Y = mEndPoint.Y - v / 0.7f * (mEndPoint.Y - mOrigin.Y);
                        if (target.Y < mOrigin.Y) {
                            target.Y = mOrigin.Y;
                        }
                    } else {
                        target.Y = mEndPoint.Y + v / 0.7f * (mOrigin.Y - mEndPoint.Y);
                        if (target.Y > mOrigin.Y) {
                            target.Y = mOrigin.Y;
                        }
                    }
                } else {
                    if (mEndPoint.X > mOrigin.X) {
                        target.X = mOrigin.X + getOffset(v) * mOriginRadius;
                    } else if (mEndPoint.X < mOrigin.X) {
                        target.X = mOrigin.X - getOffset(v) * mOriginRadius;
                    } else {
                        target.X = mOrigin.X;
                    }
                    target.Y = a * target.X + b;
                }
                return target;
            }
        }, mEndPoint, mOrigin);
        valueAnimator.setDuration(130);

        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                isAutoMove = true;
                mCurrent = (Point) valueAnimator.getAnimatedValue();
                invalidate();
            }
        });

        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                isAutoMove = false;
            }
        });

        valueAnimator.start();
    }

    public float getOffset(float v) {
        if (v > 0.6 && v <= 0.7) {
            return -1 * (v - 0.6f) * 10;
        } else if (v > 0.7 && v <= 0.8) {
            return 1 * (v - 0.7f) * 10;
        } else if (v > 0.8 && v <= 0.9) {
            return -1 * (v - 0.8f) * 10;
        } else if (v > 0.9 && v <= 1.0f) {
            return 1 * (v - 0.9f) * 10;
        }
        return 0;
    }

    class Point {
        public float X;
        public float Y;

        public Point(float x, float y) {
            this.X = x;
            this.Y = y;
        }

        public Point() {
        }
    }
}
