package com.laghridat.opencv;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

public class AnglesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_angles);

        // Retrieve values and image path from the Intent
        double angleBetweenLines = getIntent().getDoubleExtra("angleBetweenLines", 0.0);
        double angleLeft = getIntent().getDoubleExtra("angleLeft", 0.0);
        double angleRight = getIntent().getDoubleExtra("angleRight", 0.0);
        double note = getIntent().getDoubleExtra("note", 0.0);
        String imagePath = getIntent().getStringExtra("imagePath");
        long student = getIntent().getLongExtra("studentId",-1);
        long pw = getIntent().getLongExtra("selectedpwId",-1);

        // Update TextViews with actual values
        TextView textViewAngleBetweenLines = findViewById(R.id.textViewAngleBetweenLines);
        TextView textViewAngleLeft = findViewById(R.id.textViewAngleLeft);
        TextView textViewAngleRight = findViewById(R.id.textViewAngleRight);
        TextView textViewNote = findViewById(R.id.textViewNote);

        textViewAngleBetweenLines.setText("Angle Between Lines: " + angleBetweenLines);
        textViewAngleLeft.setText("Angle Left: " + angleLeft);
        textViewAngleRight.setText("Angle Right: " + angleRight);
        textViewNote.setText("Note: " + note);

        // Load and display the image
        ImageView imageView = findViewById(R.id.imageView);
        if (imagePath != null) {
            File imgFile = new File(imagePath);
            if (imgFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                imageView.setImageBitmap(bitmap);
            }
        }
    }
}
