package com.example.hojinskang.doodle;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by hojinskang on 11/2/16.
 */

public class DoodleView extends View {

    private Paint _paintDoodle = new Paint();
    private Path _path = new Path();

    public DoodleView (Context context) {
        super(context);
        init(null, 0);
    }

    public DoodleView (Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public DoodleView (Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    public void init(AttributeSet attrs, int defStyle) {
        _paintDoodle.setColor(Color.RED);
        _paintDoodle.setAntiAlias(true);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // canvas.drawLine(0, 0, getWidth(), getHeight(), _paintDoodle);
        canvas.drawPath(_path, _paintDoodle);
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        float touchX = motionEvent.getX();
        float touchY = motionEvent.getY();

        switch(motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                _path.moveTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_MOVE:
                _path.lineTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_UP:
                break;
        }

        invalidate();
        return true;
    }
}
