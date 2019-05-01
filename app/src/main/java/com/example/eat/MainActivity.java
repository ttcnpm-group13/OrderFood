package com.example.eat;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {
    Button  btndangnhap,btndangky;
    TextView txtChao,txtApp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btndangnhap = (Button)findViewById(R.id.btndangnhap);
        btndangky = (Button)findViewById(R.id.btndangky);
        txtChao = (TextView)findViewById(R.id.txtChao);
        txtApp = (TextView)findViewById(R.id.txtApp);
        Typeface face1 = Typeface.createFromAsset(getAssets(),"fonts/NABILA.TTF");
        Typeface face2 = Typeface.createFromAsset(getAssets(),"fonts/VNI-Trung Kien.TTF");
        txtChao.setTypeface(face1);
        txtApp.setTypeface(face1);

        btndangky.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent dangky = new Intent(MainActivity.this,DangKy.class);
                startActivity(dangky);
            }
        });
        btndangnhap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent dangnhap = new Intent(MainActivity.this,Dangnhap.class);
                startActivity(dangnhap);
            }
        });
    }
}
