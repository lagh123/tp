package com.laghridat.opencv;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        long student = getIntent().getLongExtra("studentid", -1);

        Log.d("home3", String.valueOf(student));
        Button tp = findViewById(R.id.tp);
        Button profil = findViewById(R.id.profil);

        // Set click listeners for the buttons
        tp.setOnClickListener(v -> {
            Intent tpIntent = new Intent(HomeActivity.this, TPActivity.class);
            tpIntent.putExtra("studentid",student);
            startActivity(tpIntent);
        });
        profil.setOnClickListener(v -> {
            Intent profilIntent = new Intent(HomeActivity.this, ProfilActivity.class);
            profilIntent.putExtra("studentid",student);
            startActivity(profilIntent);
        });
    }


}
