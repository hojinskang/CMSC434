package edu.umd.hcil.cmsc434drawtest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DrawTestView drawTestView1 = (DrawTestView)findViewById(R.id.viewDrawTest1);
        drawTestView1.setDrawMode(DrawMode.Objects);

        DrawTestView drawTestView2 = (DrawTestView)findViewById(R.id.viewDrawTest2);
        drawTestView2.setDrawMode(DrawMode.OffscreenBitmap);
    }
}
