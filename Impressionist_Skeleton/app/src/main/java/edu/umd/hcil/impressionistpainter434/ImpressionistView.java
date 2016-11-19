package edu.umd.hcil.impressionistpainter434;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.ImageView;

import java.text.MessageFormat;
import java.util.Random;

import static android.content.Context.SENSOR_SERVICE;

/**
 * Created by jon on 3/20/2016.
 */
public class ImpressionistView extends View {

    private ImageView _imageView;

    private Canvas _offScreenCanvas = null;
    private Bitmap _offScreenBitmap = null;
    private Paint _paint = new Paint();

    private int _alpha = 100;
    private float _defaultRadius = 30.0f;
    private Point _lastPoint = null;
    private long _lastPointTime = -1;
    private boolean _useMotionSpeedForBrushStrokeSize = true;
    private Paint _paintBorder = new Paint();
    private BrushType _brushType = BrushType.Square;
    private float _minBrushRadius = 5;

    // properties for tracking gesture (velocity)
    private VelocityTracker _velocityTracker = null;
    private int _touchPoints = 0;

    // properties for tracking gesture (path)
    private Path _touchPath = new Path();
    private Paint _touchPaint = new Paint();

    public ImpressionistView(Context context) {
        super(context);
        init(null, 0);
    }

    public ImpressionistView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ImpressionistView(Context context, AttributeSet attrs, int defStyle) {
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

        _paint.setColor(Color.RED);
        _paint.setAlpha(_alpha);
        _paint.setAntiAlias(true);
        _paint.setStyle(Paint.Style.FILL);
        _paint.setStrokeWidth(4);

        _paintBorder.setColor(Color.BLACK);
        _paintBorder.setStrokeWidth(3);
        _paintBorder.setStyle(Paint.Style.STROKE);
        _paintBorder.setAlpha(50);

        _touchPaint.setColor(Color.RED);
        _touchPaint.setAlpha(255);
        _touchPaint.setAntiAlias(true);
        _touchPaint.setStyle(Paint.Style.STROKE);
        _touchPaint.setStrokeWidth(3);

        //_paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));
    }

    @Override
    protected void onSizeChanged (int w, int h, int oldw, int oldh){

        Bitmap bitmap = getDrawingCache();
        Log.v("onSizeChanged", MessageFormat.format("bitmap={0}, w={1}, h={2}, oldw={3}, oldh={4}", bitmap, w, h, oldw, oldh));
        if(bitmap != null) {
            _offScreenBitmap = getDrawingCache().copy(Bitmap.Config.ARGB_8888, true);
            _offScreenCanvas = new Canvas(_offScreenBitmap);
            clearPainting();
        }
    }

    /**
     * Sets the ImageView, which hosts the image that we will paint in this view
     * @param imageView
     */
    public void setImageView(ImageView imageView){
        _imageView = imageView;
    }

    /**
     * Sets the brush type. Feel free to make your own and completely change my BrushType enum
     * @param brushType
     */
    public void setBrushType(BrushType brushType){
        _brushType = brushType;
    }

    /**
     * Clears the painting
     */
    public void clearPainting(){
        //TODO
        if(_offScreenCanvas != null) {
            Paint paint = new Paint();
            paint.setColor(Color.WHITE);
            paint.setStyle(Paint.Style.FILL);
            _offScreenCanvas.drawRect(0, 0, _offScreenCanvas.getWidth(), _offScreenCanvas.getHeight(), paint);
            if (_velocityTracker != null) {
                _velocityTracker.clear();
            }
            invalidate();
        }
    }

    /**
     * Draws the border of impressionist painting canvas and user's gesture path
     * @param canvas
     */
    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(_offScreenBitmap != null) {
            canvas.drawBitmap(_offScreenBitmap, 0, 0, _paint);
        }

        // Draw the border. Helpful to see the size of the bitmap in the ImageView
        canvas.drawRect(getBitmapPositionInsideImageView(_imageView), _paintBorder);

        // Draw gesture movement path
        canvas.drawPath(_touchPath, _touchPaint);
    }

    /**
     * Draws impressionist painting
     * Keeps track of past movements to display seamless drawing on slower devices and emulators
     * @param motionEvent
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent){

        //TODO
        //Basically, the way this works is to liste for Touch Down and Touch Move events and determine where those
        //touch locations correspond to the bitmap in the ImageView. You can then grab info about the bitmap--like the pixel color--
        //at that location

        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (_velocityTracker == null) {
                    _velocityTracker = VelocityTracker.obtain();
                } else {
                    _velocityTracker.clear();
                }
                _velocityTracker.addMovement(motionEvent);

                _touchPath.reset();
                _touchPath.moveTo(motionEvent.getX(), motionEvent.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                _velocityTracker.addMovement(motionEvent);
                _velocityTracker.computeCurrentVelocity(1000);

                int historySize = motionEvent.getHistorySize();
                for (int i = 0; i < historySize; i++) {
                    float historicalX = motionEvent.getHistoricalX(i);
                    float historicalY = motionEvent.getHistoricalY(i);
                    _paint.setColor(getPixelColor(historicalX, historicalY));
                    draw(historicalX, historicalY);
                }
                _touchPoints += historySize + 1;

                float touchX = motionEvent.getX();
                float touchY = motionEvent.getY();
                _paint.setColor(getPixelColor(touchX, touchY));
                draw(touchX, touchY);

                _touchPath.lineTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                _velocityTracker.recycle();
                _velocityTracker = null;

                _touchPath.lineTo(motionEvent.getX(), motionEvent.getY());
                _touchPath.reset();
                break;
        }

        invalidate();
        return true;
    }

    /**
     * Draw based on brush type
     * For CircleSplatter, brush size based on speed of user's gesture
     * @param touchX
     * @param touchY
     */
    private void draw(float touchX, float touchY) {
        switch(_brushType) {
            case Circle:
                _offScreenCanvas.drawCircle(touchX, touchY, _defaultRadius, _paint);
                break;
            case Square:
                _offScreenCanvas.drawRect(touchX - _defaultRadius, touchY - _defaultRadius, touchX + _defaultRadius, touchY + _defaultRadius, _paint);
                break;
            case CircleSplatter:
                float xVelocity = _velocityTracker.getXVelocity();
                float yVelocity = _velocityTracker.getYVelocity();
                double speed = Math.sqrt(Math.pow(xVelocity, 2) + Math.pow(yVelocity, 2));
                float radius = (float) Math.ceil(speed / 50);

                if (radius >= 150) {
                    radius = 150;
                }

                Random random = new Random();

                int[][] splatter = new int[][]{{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
                for (int i = 0; i < splatter.length; i++) {
                    float x = touchX + splatter[i][0] * (random.nextFloat() * _defaultRadius);
                    float y = touchY + splatter[i][1] * (random.nextFloat() * _defaultRadius);
                    _paint.setColor(getPixelColor(x, y));
                    _offScreenCanvas.drawCircle(x, y, radius, _paint);
                }
                break;
        }
    }

    /**
     * Gets pixel color of the image (algorithm similar to getBitmapPositionInsideImageView method)
     * @param touchX
     * @param touchY
     * @return
     */
    private int getPixelColor(float touchX, float touchY) {
        if(_imageView == null) {
            return Color.WHITE;
        }

        Rect rect = getBitmapPositionInsideImageView(_imageView);
        if(touchX < rect.left || touchX > rect.right || touchY < rect.top || touchY > rect.bottom) {
            return Color.WHITE;
        }

        BitmapDrawable bitmapDrawable = ((BitmapDrawable) _imageView.getDrawable());
        if(bitmapDrawable == null) {
            return Color.WHITE;
        }

        Bitmap bitmap = bitmapDrawable.getBitmap();

        float scaleX = ((float) bitmapDrawable.getIntrinsicWidth()) / rect.width();
        float scaleY = ((float) bitmapDrawable.getIntrinsicHeight()) / rect.height();

        int x = (int) Math.ceil((touchX - rect.left) * scaleX);
        int y = (int) Math.ceil((touchY - rect.top) * scaleY);

        if (x < 0 || x >= bitmapDrawable.getIntrinsicWidth() || y < 0 || y >= bitmapDrawable.getIntrinsicHeight()) {
            return Color.WHITE;
        }

        int color = bitmap.getPixel(x, y);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(_alpha, red, green, blue);
    }


    /**
     * This method is useful to determine the bitmap position within the Image View. It's not needed for anything else
     * Modified from:
     *  - http://stackoverflow.com/a/15538856
     *  - http://stackoverflow.com/a/26930938
     * @param imageView
     * @return
     */
    private static Rect getBitmapPositionInsideImageView(ImageView imageView){
        Rect rect = new Rect();

        if (imageView == null || imageView.getDrawable() == null) {
            return rect;
        }

        // Get image dimensions
        // Get image matrix values and place them in an array
        float[] f = new float[9];
        imageView.getImageMatrix().getValues(f);

        // Extract the scale values using the constants (if aspect ratio maintained, scaleX == scaleY)
        final float scaleX = f[Matrix.MSCALE_X];
        final float scaleY = f[Matrix.MSCALE_Y];

        // Get the drawable (could also get the bitmap behind the drawable and getWidth/getHeight)
        final Drawable d = imageView.getDrawable();
        final int origW = d.getIntrinsicWidth();
        final int origH = d.getIntrinsicHeight();

        // Calculate the actual dimensions
        final int widthActual = Math.round(origW * scaleX);
        final int heightActual = Math.round(origH * scaleY);

        // Get image position
        // We assume that the image is centered into ImageView
        int imgViewW = imageView.getWidth();
        int imgViewH = imageView.getHeight();

        int top = (int) (imgViewH - heightActual)/2;
        int left = (int) (imgViewW - widthActual)/2;

        // Log.d("Dimension", left + " " + top + " " + widthActual + " " + heightActual);
        rect.set(left, top, left + widthActual, top + heightActual);

        return rect;
    }
}

