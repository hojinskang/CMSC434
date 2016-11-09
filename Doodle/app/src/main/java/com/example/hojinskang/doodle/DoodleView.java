package com.example.hojinskang.doodle;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

/**
 * Created by hojinskang on 11/2/16.
 */

public class DoodleView extends View {

    private Drawing drawing;
    private Vector<Drawing> drawings, undoneDrawings;
    private int brushA, brushR, brushG, brushB, brushColor, brushSize;
    private Canvas cv;

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
        brushA = 255;
        brushR = 0;
        brushG = 0;
        brushB = 0;
        brushColor = Color.BLACK;
        brushSize = 5;
        drawings = new Vector<Drawing>();
        undoneDrawings = new Vector<Drawing>();
        drawing = new Drawing(brushColor, brushSize);
        cv = new Canvas();
    }

    public void setColor(int color) {
        brushColor = color;
    }

    public void setSize(int size) {
        brushSize = size;
    }

    public void setColor(int num, char c) {
        switch(c) {
            case 'A':
                brushA = num;
                break;
            case 'R':
                brushR = num;
                break;
            case 'G':
                brushG = num;
                break;
            case 'B':
                brushB = num;
                break;
        }
        brushColor = Color.argb(brushA, brushR, brushG, brushB);
    }

    public int getColor() {
        return brushColor;
    }

    public int getSize() {
        return brushSize;
    }

    public int getColor(char c) {
        switch(c) {
            case 'A':
                return brushA;
            case 'R':
                return brushR;
            case 'G':
                return brushG;
            case 'B':
                return brushB;
        }
        return -1; // this should never occur
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (Drawing d : drawings) {
            canvas.drawPath(d.getPath(), d.getPaint());
        }
        drawing.getPaint().setColor(brushColor);
        drawing.getPaint().setStrokeWidth(brushSize);
        canvas.drawPath(drawing.getPath(), drawing.getPaint());
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        float touchX = motionEvent.getX();
        float touchY = motionEvent.getY();

        switch(motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //drawing = new Drawing(brushColor, brushSize);
                drawing.getPath().reset();
                drawing.getPath().moveTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_MOVE:
                drawing.getPath().lineTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_UP:
                drawing.getPath().lineTo(touchX, touchY);
                cv.drawPath(drawing.getPath(), drawing.getPaint());
                drawings.add(drawing);
                drawing = new Drawing(brushColor, brushSize);
                break;
        }

        invalidate();
        return true;
    }

    public void onClickUndo(View v) {
        if (!drawings.isEmpty()) {
            Drawing d = drawings.remove(drawings.size() - 1);
            Log.d("UNDO", d.toString());
            undoneDrawings.add(d);
            invalidate();
        }
    }

    public void onClickRedo(View v) {
        if (!undoneDrawings.isEmpty()) {
            Drawing d = undoneDrawings.remove(undoneDrawings.size() - 1);
            Log.d("REDO", d.toString());
            drawings.add(d);
            invalidate();
        }
    }

    public void onClickClear(View v) {
        drawing.getPath().reset();
        drawings.clear();
        undoneDrawings.clear();
        invalidate();
    }
}