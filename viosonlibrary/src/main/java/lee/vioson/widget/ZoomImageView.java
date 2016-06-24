package lee.vioson.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

/**
 * 图片展示
 * 实现手势缩放， 双击放大缩小 拖拽移动
 */
public class ZoomImageView extends ImageView implements ScaleGestureDetector.OnScaleGestureListener, View.OnTouchListener, ViewTreeObserver.OnGlobalLayoutListener {
    private static final float SCALE_MAX = 4.0f;
    private static final float SCALE_MID = 2.0f;
    private static final String TAG = "ZoomImageView";
    /**
     * 初始化时的缩放比例，如果图片宽或高大于屏幕，此值将小于0
     */
    private float initScale = 1.0f;
    /**
     * 用于存放矩阵的9个值
     */
    private final float[] matrixValues = new float[9];

    private boolean once = true;

    private ScaleGestureDetector mScaleGestureDetector = null;

    private Matrix mScaleMatrix = new Matrix();

    private GestureDetector mGestureDetector;

    private int lastPointerCount = 0;
    private boolean isCanDrag = false;
    private float mLastX = 0;
    private float mLastY = 0;
    private boolean isCheckTopAndBottom = false;
    private boolean isCheckLeftAndRight = false;
    private boolean isAutoScale = false;

    private float mWidth = 0;
    private float mHeight = 0;

    public ZoomImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ZoomImageView(Context context) {
        this(context, null);
    }

    public ZoomImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        super.setScaleType(ScaleType.MATRIX);
        mScaleGestureDetector = new ScaleGestureDetector(context, this);
        this.setOnTouchListener(this);
        mGestureDetector = new GestureDetector(context,
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onDoubleTap(MotionEvent e) {
                        if (isAutoScale)
                            return true;

                        float x = e.getX();
                        float y = e.getY();
//                        Log.e("DoubleTap", getScale() + " , " + initScale);
                        if (getScale() < SCALE_MID) {
                            ZoomImageView.this.postDelayed(
                                    new AutoScaleRunnable(SCALE_MID, x, y), 16);
                            isAutoScale = true;
                        } else if (getScale() >= SCALE_MID
                                && getScale() < SCALE_MAX) {
                            ZoomImageView.this.postDelayed(
                                    new AutoScaleRunnable(SCALE_MAX, x, y), 16);
                            isAutoScale = true;
                        } else {
                            ZoomImageView.this.postDelayed(
                                    new AutoScaleRunnable(initScale, x, y), 16);
                            isAutoScale = true;
                        }
                        return true;
                    }


                });
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        float scale = getScale();
        float scaleFactor = detector.getScaleFactor();
        if (getDrawable() == null)
            return true;

        /**
         *缩放范围控制
         */
        if ((scale < SCALE_MAX && scaleFactor > 1.0f) || (scale > initScale && scaleFactor < 1.0f)) {
            //最大最小值判断
            if (scaleFactor * scale < initScale) {
                scaleFactor = initScale / scale;
            }
            if (scaleFactor * scale > SCALE_MAX) {
                scaleFactor = SCALE_MAX / scale;
            }

            //设置缩放比例
//            mScaleMatrix.postScale(scaleFactor, scaleFactor, getWidth() / 2,
//                    getHeight() / 2);
            mScaleMatrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(),
                    detector.getFocusY());
            checkBorderAndCenterWhenScale();
            setImageMatrix(mScaleMatrix);
        }
        return true;
    }

    /**
     * 在缩放时，进行图片显示范围的控制
     */
    private void checkBorderAndCenterWhenScale() {

        RectF rect = getMatrixRectF();
        float deltaX = 0;
        float deltaY = 0;

        int width = getWidth();
        int height = getHeight();

        // 如果宽或高大于屏幕，则控制范围
        if (rect.width() >= width) {
            if (rect.left > 0) {
                deltaX = -rect.left;
            }
            if (rect.right < width) {
                deltaX = width - rect.right;
            }
        }
        if (rect.height() >= height) {
            if (rect.top > 0) {
                deltaY = -rect.top;
            }
            if (rect.bottom < height) {
                deltaY = height - rect.bottom;
            }
        }
        // 如果宽或高小于屏幕，则让其居中
        if (rect.width() < width) {
            deltaX = width * 0.5f - rect.right + 0.5f * rect.width();
        }
        if (rect.height() < height) {
            deltaY = height * 0.5f - rect.bottom + 0.5f * rect.height();
        }

        mScaleMatrix.postTranslate(deltaX, deltaY);

    }

    /**
     * 根据当前图片的Matrix获得图片的范围
     *
     * @return
     */
    private RectF getMatrixRectF() {
        Matrix matrix = mScaleMatrix;
        RectF rect = new RectF();
        Drawable d = getDrawable();
        if (null != d) {
            rect.set(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
            matrix.mapRect(rect);
        }
        return rect;
    }

    private float getScale() {
        mScaleMatrix.getValues(matrixValues);
        return matrixValues[Matrix.MSCALE_X];
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (mGestureDetector.onTouchEvent(event))
            return true;
        float x = 0, y = 0;
        // 拿到触摸点的个数
        final int pointerCount = event.getPointerCount();
        // 得到多个触摸点的x与y均值
        for (int i = 0; i < pointerCount; i++) {
            x += event.getX(i);
            y += event.getY(i);
        }
        x = x / pointerCount;
        y = y / pointerCount;

        /**
         * 每当触摸点发生变化时，重置mLasX , mLastY
         */
        if (pointerCount != lastPointerCount) {
            isCanDrag = false;
            mLastX = x;
            mLastY = y;
        }

        lastPointerCount = pointerCount;

        RectF rectF = getMatrixRectF();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (rectF.width() > getWidth() || rectF.height() > getHeight()) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                break;
            case MotionEvent.ACTION_MOVE:
//                Log.e(TAG, "ACTION_MOVE");
                float dx = x - mLastX;
                float dy = y - mLastY;
                if (!isCanDrag) {
                    isCanDrag = isCanDrag(dx, dy);
                }

                if (isCanDrag) {
                    if (getDrawable() != null) {
                        if (getMatrixRectF().left == 0 && dx > 0) {
                            getParent().requestDisallowInterceptTouchEvent(false);
                        }

                        if (getMatrixRectF().right == getWidth() && dx < 0) {
                            getParent().requestDisallowInterceptTouchEvent(false);
                        }

                        isCheckLeftAndRight = isCheckTopAndBottom = true;
                        // 如果宽度小于屏幕宽度，则禁止左右移动
                        if (rectF.width() < getWidth()) {
                            dx = 0;
                            isCheckLeftAndRight = false;
                        }
                        // 如果高度小雨屏幕高度，则禁止上下移动
                        if (rectF.height() < getHeight()) {
                            dy = 0;
                            isCheckTopAndBottom = false;
                        }
                        mScaleMatrix.postTranslate(dx, dy);
                        checkMatrixBounds();
                        setImageMatrix(mScaleMatrix);
                    }
                }
                mLastX = x;
                mLastY = y;
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
//                Log.e(TAG, "ACTION_UP");
                lastPointerCount = 0;
                break;
        }

        mScaleGestureDetector.onTouchEvent(event);

        return false;
    }

    /**
     * 移动时，进行边界判断，主要判断宽或高大于屏幕的
     */
    private void checkMatrixBounds() {
        RectF rect = getMatrixRectF();

        float deltaX = 0, deltaY = 0;
        final float viewWidth = getWidth();
        final float viewHeight = getHeight();
        // 判断移动或缩放后，图片显示是否超出屏幕边界
        if (rect.top > 0 && isCheckTopAndBottom) {
            deltaY = -rect.top;
        }
        if (rect.bottom < viewHeight && isCheckTopAndBottom) {
            deltaY = viewHeight - rect.bottom;
        }
        if (rect.left > 0 && isCheckLeftAndRight) {
            deltaX = -rect.left;
        }
        if (rect.right < viewWidth && isCheckLeftAndRight) {
            deltaX = viewWidth - rect.right;
        }
        mScaleMatrix.postTranslate(deltaX, deltaY);
    }

    /**
     * 是否是推动行为
     *
     * @param dx
     * @param dy
     * @return
     */
    private boolean isCanDrag(float dx, float dy) {
        double mTouchSlop = 15;//识别拉拽值
        return Math.sqrt((dx * dx) + (dy * dy)) >= mTouchSlop;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getViewTreeObserver().removeOnGlobalLayoutListener(this);
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        int drawableWidth = drawable.getIntrinsicWidth();
        int drawableHeight = drawable.getIntrinsicHeight();
        float scale = computeScale(drawableWidth, drawableHeight);
//        Log.e(TAG, "setImageDrawable");
        mScaleMatrix.postScale(scale / getScale(), scale / getScale(), drawableWidth / 2, drawableHeight / 2);
        checkBorderAndCenterWhenScale();
        setImageMatrix(mScaleMatrix);
    }

    /**
     * 计算缩放比例
     *
     * @param drawableWidth
     * @param drawableHeight
     * @return
     */
    private float computeScale(int drawableWidth, int drawableHeight) {
        float scale = 1.0f;
        if (mWidth != 0 && mHeight != 0) {
            if (drawableWidth > mWidth && drawableHeight <= mHeight) {
                //宽大高小
                scale = mWidth * 1.0f / drawableWidth;
            } else if (drawableHeight > mHeight && drawableWidth <= mWidth) {
                //高大宽小
                scale = mHeight * 1.0f / drawableHeight;
            } else {
                //高大宽大，高小宽小
                scale = Math.min(mWidth * 1.0f / drawableWidth, mHeight * 1.0f / drawableHeight);
            }
        }
        return scale;
    }

    @Override
    public void onGlobalLayout() {
        mHeight = getHeight();
        mWidth = getWidth();
//        Log.e(TAG, "onGlobalLayout");
        if (once) {
            Drawable drawable = getDrawable();
            if (drawable == null)
                return;
            final int width = getWidth();
            final int height = getHeight();
            //拿到图片的宽高
            final int drawableWidth = drawable.getIntrinsicWidth();
            final int drawableHeight = drawable.getIntrinsicHeight();
            float scale = computeScale(drawableWidth, drawableHeight);
//            if (drawableWidth > width && drawableHeight <= height) {
//                //宽大高小
//                scale = width * 1.0f / drawableWidth;
//            } else if (drawableHeight > height && drawableWidth <= width) {
//                //高大宽小
//                scale = height * 1.0f / drawableHeight;
//            } else {
//                //高大宽大，高小宽小
//                scale = Math.min(width * 1.0f / drawableWidth, height * 1.0f / drawableHeight);
//            }
            initScale = scale;
            //图片移至屏幕中央
            mScaleMatrix.postTranslate((width - drawableWidth) / 2, (height - drawableHeight) / 2);
            mScaleMatrix.postScale(scale, scale, getWidth() / 2, getHeight() / 2);
            setImageMatrix(mScaleMatrix);
            once = false;
        }
    }


    /**
     * 自动缩放的任务
     *
     * @author zhy
     */
    private class AutoScaleRunnable implements Runnable {
        static final float BIGGER = 1.07f;
        static final float SMALLER = 0.93f;
        private float mTargetScale;
        private float tmpScale;

        /**
         * 缩放的中心
         */
        private float x;
        private float y;

        /**
         * 传入目标缩放值，根据目标值与当前值，判断应该放大还是缩小
         *
         * @param targetScale
         */
        public AutoScaleRunnable(float targetScale, float x, float y) {
            this.mTargetScale = targetScale;
            this.x = x;
            this.y = y;
            if (getScale() < mTargetScale) {
                tmpScale = BIGGER;
            } else {
                tmpScale = SMALLER;
            }

        }

        @Override
        public void run() {
            // 进行缩放
            mScaleMatrix.postScale(tmpScale, tmpScale, x, y);
            checkBorderAndCenterWhenScale();
            setImageMatrix(mScaleMatrix);

            final float currentScale = getScale();
            //如果值在合法范围内，继续缩放
            if (((tmpScale > 1f) && (currentScale < mTargetScale))
                    || ((tmpScale < 1f) && (mTargetScale < currentScale))) {
                ZoomImageView.this.postDelayed(this, 16);
            } else {//设置为目标的缩放比例
                final float deltaScale = mTargetScale / currentScale;
                mScaleMatrix.postScale(deltaScale, deltaScale, x, y);
                checkBorderAndCenterWhenScale();
                setImageMatrix(mScaleMatrix);
                isAutoScale = false;
            }

        }
    }
}
