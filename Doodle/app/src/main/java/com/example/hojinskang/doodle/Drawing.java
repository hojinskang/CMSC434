package com.example.hojinskang.doodle;

import android.graphics.Paint;
import android.graphics.Path;

/**
 * Created by hojinskang on 11/8/16.
 */

public class Drawing {
    private Paint paint;
    private Path path;

    Drawing(int color, int size) {
        this.paint = new Paint();
        this.path = new Path();
        this.paint.setColor(color);
        this.paint.setAntiAlias(true);
        this.paint.setStyle(Paint.Style.STROKE);
        this.paint.setStrokeWidth(size);
    }

    Drawing(Drawing d) {
        this.paint = d.getPaint();
        this.path = d.getPath();
    }

    Paint getPaint() {
        return this.paint;
    }

    Path getPath() {
        return this.path;
    }
}
