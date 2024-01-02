package com.laghridat.opencv;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.laghridat.opencv.entities.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{
    EditText cpswd,username;


    TextView forgot;
    Button add;

    String userName,password;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);



        cpswd=findViewById(R.id.password);
        add=findViewById(R.id.login);
        username=findViewById(R.id.username);




        add.setOnClickListener(this);
    }




    @Override
    public void onClick(View v) {
        userName = username.getText().toString();
        password = cpswd.getText().toString();
        System.out.println(userName+password);
        String endpoint = "/api/auth/login";
        String url = ApiConfig.BASE_URL + endpoint;
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("userName", userName);
            jsonBody.put("password", password);
            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    jsonBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            try {
                                Intent hintent = new Intent(LoginActivity.this, HomeActivity.class);
                                Intent intent = new Intent(LoginActivity.this, TPActivity.class);
                                Intent pintent = new Intent(LoginActivity.this, ProfilActivity.class);

                                JSONObject jsonObject= response;
                                long id = jsonObject.getInt("id");
                                String username=jsonObject.getString("userName");
                                //String nom=jsonObject.getString("lastName");
                                //String prenom=jsonObject.getString("firstName");


                                intent.putExtra("studentid",id);
                                hintent.putExtra("studentid",id);
                                pintent.putExtra("studentid",id);
                                //intent.putExtra("code", code);
                                //intent.putExtra("nom", nom);
                                //intent.putExtra("prenom", prenom);
                                intent.putExtra("username", username);
                                startActivity(hintent);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(LoginActivity.this, "Erreur! Vérifiez vos informations", Toast.LENGTH_SHORT).show();
                        }
                    }

            );


            Volley.newRequestQueue(this).add(request);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
