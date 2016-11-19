package edu.umd.hcil.cmsc434drawtest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by jon on 3/30/2016.
 */
public class DrawTestView extends View {
    private static final int DEFAULT_BRUSH_RADIUS = 25;

    private Canvas _offScreenCanvas = null;
    private Bitmap _offScreenBitmap = null;
    private DrawMode _drawMode = DrawMode.OffscreenBitmap;

    private Paint _paint = new Paint();
    private Paint _paintText = new Paint();
    private int _alpha = 128;

    private ArrayList<PaintPoint> _listPaintPoints = new ArrayList<PaintPoint>();

    // You can ignore this stuff. It's simply for measuring performance
    private long _touchPoints = 0;
    private long _drawingCalls = 0;
    private long _elapsedTimeDrawingInMs = 0;
    private long _touchEventCalls = 0;
    private long _elapsedTimeProcessingTouchEventsInMs = 0;
    private long _lastTimeDrawingInMs = 0;
    private long _maxOnDrawInMs = 0;
    private int _numDrawingCallsToMeasure = 100;


    public DrawTestView(Context context) {
        super(context);
        init(null, 0);
    }

    public DrawTestView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public DrawTestView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    /**
     * Because we have more than one constructor (i.e., overloaded constructors), we use
     * a separate initialization method
     * @param attrs
     * @param defStyle
     */
    private void init(AttributeSet attrs, int defStyle){

        // Set setDrawingCacheEnabled to true to support generating a bitmap copy of the view (for saving)
        // See: http://developer.android.com/reference/android/view/View.html#setDrawingCacheEnabled(boolean)
        //      http://developer.android.com/reference/android/view/View.html#getDrawingCache()
        this.setDrawingCacheEnabled(true);

        // Setup main paint object
        _paint.setColor(Color.BLUE);
        _paint.setAlpha(_alpha);
        _paint.setAntiAlias(true);
        _paint.setStyle(Paint.Style.FILL);
        _paint.setStrokeWidth(4);

        // Setup paint object for painting text
        _paintText.setColor(Color.BLACK);
        _paintText.setTextSize(36f);
        _paintText.setAntiAlias(true);
    }

    public void setDrawMode(DrawMode drawMode){
        _drawMode = drawMode;
        clearDrawing();
    }

    public void clearDrawing(){
        _listPaintPoints.clear();

        if(_offScreenCanvas != null) {
            Paint paint = new Paint();
            paint.setColor(Color.WHITE);
            paint.setStyle(Paint.Style.FILL);
            _offScreenCanvas.drawRect(0, 0, this.getWidth(), this.getHeight(), paint);
        }
    }

    @Override
    protected void onSizeChanged (int w, int h, int oldw, int oldh){

        Bitmap bitmap = getDrawingCache();
        Log.v("onSizeChanged", MessageFormat.format("bitmap={0}, w={1}, h={2}, oldw={3}, oldh={4}", bitmap, w, h, oldw, oldh));
        if(bitmap != null) {
            _offScreenBitmap = getDrawingCache().copy(Bitmap.Config.ARGB_8888, true);
            _offScreenCanvas = new Canvas(_offScreenBitmap);
            clearDrawing();
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        long startTime = SystemClock.elapsedRealtime();

        if(_drawMode == DrawMode.OffscreenBitmap) {
            if(_offScreenBitmap != null) {
                // TODO: draw the offscreen bitmap to the screen
                // Insert one line of code here
                canvas.drawBitmap(_offScreenBitmap, 0, 0, _paint);
            }
        }else if(_drawMode == DrawMode.Objects){
            // TODO: iterate through _listPaintPoints and draw each one to the screen
            // This will take a few lines of code
            for (PaintPoint p : _listPaintPoints) {
                canvas.drawPoint(p.getX(), p.getY(), p.getPaint());
            }
        }

        // You can ignore everything past this point. Again, simply for measuring performance
        canvas.drawText("DrawMode: " + _drawMode.name(), 10, 40, _paintText);
        canvas.drawText("Points: " + _touchPoints, 10, 80, _paintText);

        if(_touchEventCalls > 0) {
            canvas.drawText(MessageFormat.format("Avg Time Processing Touch Events: {0} ms", _elapsedTimeProcessingTouchEventsInMs / _touchEventCalls), 10, 120, _paintText);
        }

        if(_drawingCalls > 0) {

            if(_lastTimeDrawingInMs > _maxOnDrawInMs){
                _maxOnDrawInMs = _lastTimeDrawingInMs;
            }

            canvas.drawText(MessageFormat.format("Avg Time Drawing (Last {0} Calls): {1} ms", _numDrawingCallsToMeasure, _elapsedTimeDrawingInMs / _drawingCalls), 10, 160, _paintText);
            canvas.drawText(MessageFormat.format("Last onDraw: {0} ms | Max: {1} ms", _lastTimeDrawingInMs, _maxOnDrawInMs), 10, 200, _paintText);

            if(_drawingCalls > _numDrawingCallsToMeasure){
                _drawingCalls = 0;
                _elapsedTimeDrawingInMs = 0;
            }
        }

        _drawingCalls++;
        long endTime = SystemClock.elapsedRealtime();
        _lastTimeDrawingInMs = endTime - startTime;
        _elapsedTimeDrawingInMs += endTime - startTime;
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        long startTime = SystemClock.elapsedRealtime();

        float curTouchX = motionEvent.getX();
        float curTouchY = motionEvent.getY();
        int curTouchXRounded = (int) curTouchX;
        int curTouchYRounded = (int) curTouchY;
        float brushRadius = DEFAULT_BRUSH_RADIUS;

        switch(motionEvent.getAction()){
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                _touchEventCalls++;

                // For efficiency, motion events with ACTION_MOVE may batch together multiple movement samples within a single object.
                // The most current pointer coordinates are available using getX(int) and getY(int).
                // Earlier coordinates within the batch are accessed using getHistoricalX(int, int) and getHistoricalY(int, int).
                // See: http://developer.android.com/reference/android/view/MotionEvent.html
                int historySize = motionEvent.getHistorySize();
                _paint.setColor(getRandomColor());

                if(_drawMode == DrawMode.OffscreenBitmap) {
                    for (int i = 0; i < historySize; i++) {

                        float touchX = motionEvent.getHistoricalX(i);
                        float touchY = motionEvent.getHistoricalY(i);

                        // TODO: draw to the offscreen bitmap for historical x,y points
                        // Insert one line of code here
                    }

                    // TODO: draw to the offscreen bitmap for current x,y point.
                    // Insert one line of code here

                }else if(_drawMode == DrawMode.Objects){
                    for (int i = 0; i < historySize; i++) {
                        float touchX = motionEvent.getHistoricalX(i);
                        float touchY = motionEvent.getHistoricalY(i);

                        // TODO: Make a new PaintPoint object and insert the historical points into _listPaintPoints
                    }
                    // TODO: Make a new PaintPoint object and insert the current point into _listPaintPoints
                }

                _touchPoints += historySize + 1;
                invalidate();

                long endTime = SystemClock.elapsedRealtime();
                _elapsedTimeProcessingTouchEventsInMs += endTime - startTime;
                break;
            case MotionEvent.ACTION_UP:
                break;
        }

        return true;
    }

    private static final Random _random = new Random();
    protected int getRandomColor(){
        int r = _random.nextInt(255);
        int g = _random.nextInt(255);
        int b = _random.nextInt(255);
        //int b = 50 + (int)(_random.nextFloat() * (255-50));
        return Color.argb(_alpha, r, g, b);
    }

    private class PaintPoint {
        private Paint _paint = new Paint();
        private PointF _point;
        private float _brushRadius;

        public PaintPoint(float x, float y, float brushRadius, Paint paintSrc){
            // Copy the fields from paintSrc into this paint
            _paint.set(paintSrc);
            _point = new PointF(x, y);
            _brushRadius = brushRadius;
        }

        public Paint getPaint(){
            return _paint;
        }

        public float getX(){
            return _point.x;
        }

        public float getY(){
            return _point.y;
        }

        public float getBrushRadius(){
            return _brushRadius;
        }
    }
}
