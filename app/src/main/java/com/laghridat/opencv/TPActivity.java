package com.laghridat.opencv;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.laghridat.opencv.adapters.PWAdapter;
import com.laghridat.opencv.entities.PW;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;




public class TPActivity extends AppCompatActivity {

    private ListView listView;
    private List<PW> pws;
    private PWAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        listView = findViewById(R.id.listView);
        retrieveStudentsData();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Inside onItemClick method
                PW selected = pws.get(position);
                Intent intent = new Intent(TPActivity.this, MainActivity.class);
                intent.putExtra("studentid", getIntent().getLongExtra("studentid", 1));

                // Pass both student ID and selected PW ID as extras
                long ids = getIntent().getLongExtra("studentid", 1);
                long idp= selected.getId();
                intent.putExtra("idpw", idp);
                Log.d("STUDENT_PW",ids+""+idp);
                // Start the MainActivity
                startActivity(intent);
            }

        });
    }

    private void retrieveStudentsData() {
        String endpoint = "/api/pws";
        String loadUrl = ApiConfig.BASE_URL + endpoint;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, loadUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Parse the JSON response and display data in ListView
                        Log.d("response", response+"");
                        handleJsonResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("VolleyError", "Error fetching data: " + error.getMessage());

                    }
                });

        // Add the request to the RequestQueue
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void handleJsonResponse(String response) {
        try {
            JSONArray jsonArray = new JSONArray(response);
            pws = new ArrayList<>();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                PW pw = new PW(
                        jsonObject.getInt("id"),
                        jsonObject.getString("docs"),
                        jsonObject.getString("objectif"),
                        jsonObject.getString("title")
                );
                long idpw=jsonObject.getLong("id");
                getIntent().putExtra("idpw",idpw);
                long id = getIntent().getLongExtra("studentid", 1);
                Log.d("STUDENTPW", String.valueOf(idpw+""+id));
                pws.add(pw);
                System.out.println();
            }

            // Set up an adapter to display the list
            adapter = new PWAdapter(this, pws);
            listView.setAdapter((ListAdapter) adapter);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }











}