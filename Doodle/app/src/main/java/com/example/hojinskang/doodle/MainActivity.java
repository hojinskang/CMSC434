package com.example.hojinskang.doodle;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import java.util.Vector;

public class MainActivity extends AppCompatActivity {
    private DoodleView doodleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.doodleView = (DoodleView) findViewById(R.id.doodleView);
    }

    public void onClickSetSize(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        ViewGroup mainView = (ViewGroup)findViewById(R.id.activity_main);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.size_layout, null);

        final SeekBar sizeSeekBar = (SeekBar)dialogView.findViewById(R.id.size_seekBar);
        sizeSeekBar.setProgress(doodleView.getSize());

        sizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        builder.setView(dialogView)
                // Add action buttons
                .setPositiveButton("OK!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        doodleView.setSize(sizeSeekBar.getProgress());
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // nothing done
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    int alpha = 255, red = 0, green = 0, blue = 0;
    int color = Color.argb(alpha, red, green, blue);
    private SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {}

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            switch (seekBar.getId()) {
                case R.id.opacity_seekBar:
                    alpha = progress;
                    break;
                case R.id.red_seekBar:
                    red = progress;
                    break;
                case R.id.green_seekBar:
                    green = progress;
                    break;
                case R.id.blue_seekBar:
                    blue = progress;
                    break;
            }

            ViewGroup colorView = (ViewGroup)findViewById(R.id.activity_main);
            color = Color.argb(alpha, red, green, blue);
            colorView.setBackgroundColor(color);
        }
    };

    public void onClickSetColor(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        ViewGroup mainView = (ViewGroup)findViewById(R.id.activity_main);

        Drawable background = mainView.getBackground();

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.color_layout, null);

        SeekBar redSeekBar = (SeekBar)dialogView.findViewById(R.id.red_seekBar);
        SeekBar greenSeekBar = (SeekBar)dialogView.findViewById(R.id.green_seekBar);
        SeekBar blueSeekBar = (SeekBar)dialogView.findViewById(R.id.blue_seekBar);
        SeekBar opacitySeekBar = (SeekBar)dialogView.findViewById(R.id.opacity_seekBar);

        redSeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        greenSeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        blueSeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        opacitySeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);

        redSeekBar.setProgress(doodleView.getColor('R'));
        greenSeekBar.setProgress(doodleView.getColor('G'));
        blueSeekBar.setProgress(doodleView.getColor('B'));
        opacitySeekBar.setProgress(doodleView.getColor('A'));

        builder.setView(dialogView)
                // Add action buttons
                .setPositiveButton("OK!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        ViewGroup mainView = (ViewGroup)findViewById(R.id.activity_main);
                        mainView.setBackgroundColor(Color.WHITE);
                        doodleView.setColor(alpha, 'A');
                        doodleView.setColor(red, 'R');
                        doodleView.setColor(green, 'G');
                        doodleView.setColor(blue, 'B');
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ViewGroup mainView = (ViewGroup)findViewById(R.id.activity_main);
                        mainView.setBackgroundColor(Color.WHITE);
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void onClickUndo(View v) {
        doodleView.onClickUndo(v);
    }

    public void onClickRedo(View v) {
        doodleView.onClickRedo(v);
    }

    public void onClickClear(View v) {
        doodleView.onClickClear(v);
    }

}
