package ichen.chu.drawableviewlibs;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.os.Build;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import java.util.ArrayList;

import ichen.chu.drawableviewlibs.draw.CanvasDrawer;
import ichen.chu.drawableviewlibs.draw.PathDrawer;
import ichen.chu.drawableviewlibs.draw.SerializablePath;
import ichen.chu.drawableviewlibs.gestures.creator.GestureCreator;
import ichen.chu.drawableviewlibs.gestures.creator.GestureCreatorListener;
import ichen.chu.drawableviewlibs.gestures.scale.GestureScaleListener;
import ichen.chu.drawableviewlibs.gestures.scale.GestureScaler;
import ichen.chu.drawableviewlibs.gestures.scale.ScalerListener;
import ichen.chu.drawableviewlibs.gestures.scroller.GestureScrollListener;
import ichen.chu.drawableviewlibs.gestures.scroller.GestureScroller;
import ichen.chu.drawableviewlibs.gestures.scroller.ScrollerListener;
import ichen.chu.simplefingergesturelibs.SimpleFingerGestures;


public class DrawableView extends View
    implements View.OnTouchListener, ScrollerListener, GestureCreatorListener, ScalerListener {

    private static final MLog mLog = new MLog(false);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

    public static boolean DEBUG_MODE = false;

    private final ArrayList<SerializablePath> paths = new ArrayList<>();

    // flag
    private boolean isDisabled = false;

    // Listener
    private DrawableViewListener mDrawableViewListener;

    // Gesture
    private SimpleFingerGestures simpleFingerGestures = new SimpleFingerGestures();
    private FingerGestureListener mFingerGestureListener = new FingerGestureListener();

    private GestureScroller gestureScroller;
    private GestureScaler gestureScaler;
    private GestureCreator gestureCreator;
    private int canvasHeight;
    private int canvasWidth;
    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleGestureDetector;
    private PathDrawer pathDrawer;
    private CanvasDrawer canvasDrawer;
    private SerializablePath currentDrawingPath;

    public DrawableView(Context context) {
    super(context);
        init();
    }

    public DrawableView(Context context, AttributeSet attrs) {
    super(context, attrs);
        init();
    }

    public DrawableView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP) public DrawableView(Context context, AttributeSet attrs,
                                                               int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        mLog.d(TAG, "init");
        gestureScroller = new GestureScroller(this);
        gestureDetector = new GestureDetector(getContext(), new GestureScrollListener(gestureScroller));
        gestureScaler = new GestureScaler(this);
        scaleGestureDetector =
              new ScaleGestureDetector(getContext(), new GestureScaleListener(gestureScaler));
        gestureCreator = new GestureCreator(this);
        pathDrawer = new PathDrawer();
        canvasDrawer = new CanvasDrawer();
        setOnTouchListener(this);
        simpleFingerGestures.setOnFingerGestureListener(mFingerGestureListener);
        simpleFingerGestures.setDebug(true);
        simpleFingerGestures.setConsumeTouchEvents(true);
        this.setOnTouchListener(simpleFingerGestures);
    }

    public void setConfig(DrawableViewConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("PaintDemoTabView configuration cannot be null");
        }
        canvasWidth = config.getCanvasWidth();
        canvasHeight = config.getCanvasHeight();
        gestureCreator.setConfig(config);
        gestureScaler.setZooms(config.getMinZoom(), config.getMaxZoom());
        gestureScroller.setCanvasBounds(canvasWidth, canvasHeight);
        canvasDrawer.setConfig(config);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        gestureScroller.setViewBounds(w, h);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
//        mLog.d(TAG, "onTouch");
        scaleGestureDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);
        gestureCreator.onTouchEvent(event);
        invalidate();
        return true;
    }

    public void undo() {
        if (paths.size() > 0) {
            paths.remove(paths.size() - 1);
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
//        mLog.d(TAG, "onDraw");
        super.onDraw(canvas);
        canvasDrawer.onDraw(canvas);
        pathDrawer.onDraw(canvas, currentDrawingPath, paths);
    }

    public void clear() {
        paths.clear();
        invalidate();
    }

    public Bitmap obtainBitmap(Bitmap createdBitmap) {
        return pathDrawer.obtainBitmap(createdBitmap, paths);
    }

    public void setDisabled(boolean flag) {
        isDisabled = flag;
    }

    public Bitmap obtainBitmap() {
        return obtainBitmap(Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888));
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        DrawableViewSaveState state = new DrawableViewSaveState(super.onSaveInstanceState());
        state.setPaths(paths);
        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof DrawableViewSaveState)) {
            super.onRestoreInstanceState(state);
        } else {
            DrawableViewSaveState ss = (DrawableViewSaveState) state;
            super.onRestoreInstanceState(ss.getSuperState());
            paths.addAll(ss.getPaths());
        }
    }

    @Override
    public void onViewPortChange(RectF currentViewport) {
        gestureCreator.onViewPortChange(currentViewport);
        canvasDrawer.onViewPortChange(currentViewport);
    }

    @Override
    public void onCanvasChanged(RectF canvasRect) {
        gestureCreator.onCanvasChanged(canvasRect);
        canvasDrawer.onCanvasChanged(canvasRect);
    }

    @Override
    public void onGestureCreated(SerializablePath serializablePath) {
        mLog.d(TAG, "onGestureCreated");
        paths.add(serializablePath);
    }

    @Override
    public void onCurrentGestureChanged(SerializablePath currentDrawingPath) {
//        mLog.d(TAG, "onCurrentGestureChanged, currentDrawingPath= " + currentDrawingPath);
        this.currentDrawingPath = currentDrawingPath;
    }

    @Override
    public void onScaleChange(float scaleFactor) {
        mLog.d(TAG, "onScaleChange");
        gestureScroller.onScaleChange(scaleFactor);
        gestureCreator.onScaleChange(scaleFactor);
        canvasDrawer.onScaleChange(scaleFactor);
    }

    private class FingerGestureListener implements SimpleFingerGestures.OnFingerGestureListener, OnTouchListener {

        @Override
        public boolean onSwipeUp(int fingers, long gestureDuration, double gestureDistance) {
            mLog.d(TAG, "swiped " + fingers + " up");
            mDrawableViewListener.onSwipeUp(fingers, gestureDuration, gestureDistance);
            return false;
        }

        @Override
        public boolean onSwipeDown(int fingers, long gestureDuration, double gestureDistance) {
            mLog.d(TAG, "swiped " + fingers + " down");
            mDrawableViewListener.onSwipeDown(fingers, gestureDuration, gestureDistance);
            return false;
        }

        @Override
        public boolean onSwipeLeft(int fingers, long gestureDuration, double gestureDistance) {
            mLog.d(TAG, "swiped " + fingers + " left");
            mDrawableViewListener.onSwipeLeft(fingers, gestureDuration, gestureDistance);
            return false;
        }

        @Override
        public boolean onSwipeRight(int fingers, long gestureDuration, double gestureDistance) {
            mLog.d(TAG, "swiped " + fingers + " right");
            mDrawableViewListener.onSwipeRight(fingers, gestureDuration, gestureDistance);
            return false;
        }

        @Override
        public boolean onPinch(int fingers, long gestureDuration, double gestureDistance) {
            mLog.d(TAG, "pinch");
            mDrawableViewListener.onPinch(fingers, gestureDuration, gestureDistance);
            return false;
        }

        @Override
        public boolean onUnpinch(int fingers, long gestureDuration, double gestureDistance) {
            mLog.d(TAG, "unpinch");
            mDrawableViewListener.onUnpinch(fingers, gestureDuration, gestureDistance);
            return false;
        }

        @Override
        public boolean onDoubleTap(int fingers) {
            mLog.d(TAG, "onDoubleTap");
            mDrawableViewListener.onDoubleTap(fingers);
            return false;
        }


        @Override
        public boolean onTouch(View v, MotionEvent event) {
//            mLog.d(TAG, "onTouch");
            if (!isDisabled) {
                scaleGestureDetector.onTouchEvent(event);
                gestureDetector.onTouchEvent(event);
                gestureCreator.onTouchEvent(event);
                invalidate();
            }
            return true;
        }
    }

    public void setSimpleFingerGesturesListener (DrawableViewListener listener) {
        mDrawableViewListener = listener;
    }

    public interface DrawableViewListener {
        /**
         * Called when user swipes <b>up</b> with two fingers
         *
         * @param fingers         number of fingers involved in this gesture
         * @param gestureDuration duration in milliSeconds
         * @return
         */
        public boolean onSwipeUp(int fingers, long gestureDuration, double gestureDistance);

        /**
         * Called when user swipes <b>down</b> with two fingers
         *
         * @param fingers         number of fingers involved in this gesture
         * @param gestureDuration duration in milliSeconds
         * @return
         */
        public boolean onSwipeDown(int fingers, long gestureDuration, double gestureDistance);

        /**
         * Called when user swipes <b>left</b> with two fingers
         *
         * @param fingers         number of fingers involved in this gesture
         * @param gestureDuration duration in milliSeconds
         * @return
         */
        public boolean onSwipeLeft(int fingers, long gestureDuration, double gestureDistance);

        /**
         * Called when user swipes <b>right</b> with two fingers
         *
         * @param fingers         number of fingers involved in this gesture
         * @param gestureDuration duration in milliSeconds
         * @return
         */
        public boolean onSwipeRight(int fingers, long gestureDuration, double gestureDistance);

        /**
         * Called when user <b>pinches</b> with two fingers (bring together)
         *
         * @param fingers         number of fingers involved in this gesture
         * @param gestureDuration duration in milliSeconds
         * @return
         */
        public boolean onPinch(int fingers, long gestureDuration, double gestureDistance);

        /**
         * Called when user <b>un-pinches</b> with two fingers (take apart)
         *
         * @param fingers         number of fingers involved in this gesture
         * @param gestureDuration duration in milliSeconds
         * @return
         */
        public boolean onUnpinch(int fingers, long gestureDuration, double gestureDistance);

        public boolean onDoubleTap(int fingers);

        boolean onTouch(View v, MotionEvent event);
    }
}
