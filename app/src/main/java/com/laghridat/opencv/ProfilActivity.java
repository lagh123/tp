package com.laghridat.opencv;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

public class ProfilActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil3);
        long student = getIntent().getLongExtra("studentid",-1);
        String endpoint = "/api/students/"+student;
        String url = ApiConfig.BASE_URL + endpoint;

        Log.d("student3", String.valueOf(student));

    }
}