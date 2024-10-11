package com.example.filemanager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class Login extends AppCompatActivity {
    EditText etEmail, etPassword;
    Button btnLogin;
    RequestQueue requestQueue;
    ProgressDialog progressDialog;

    private static final String SHARED_PREF_NAME = "session";
    private static final String SESSION_EMAIL = "user_email";
    private static final String SESSION_TOKEN = "user_token";
    private static final String SESSION_POSITION = "user_position";
    private static final String SESSION_NAME = "user_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.login_email);
        etPassword = findViewById(R.id.login_password);
        btnLogin = findViewById(R.id.login_btn);

        requestQueue = Volley.newRequestQueue(this);

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            login(email, password);
            progressDialog = new ProgressDialog(Login.this);
            progressDialog.setMessage("Logging in...");
            progressDialog.show();
        });
    }

    private void login(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            progressDialog.dismiss();
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("email", email);
            jsonBody.put("password", password);
        } catch (JSONException e) {
            Log.e("Login", "JSON error: " + e.getMessage());
            progressDialog.dismiss();
            Toast.makeText(this, "Error creating JSON", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "https://skcalamba.scarlet2.io/sk_login.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        Log.d("Login", "Response: " + response); // Log the raw response for debugging
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.getString("status");
                        String userToken = jsonResponse.getString("user_id");
                        String userPosition = jsonResponse.getString("user_position");
                        String userName = jsonResponse.getString("user_name");
                        String userEmail = jsonResponse.getString("user_email");

                        if (status.equals("success")) {
                            saveInstanceState(userEmail, userToken, userPosition, userName);

                            progressDialog.dismiss();
                            Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(Login.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            progressDialog.dismiss();
                            String errorMessage = jsonResponse.optString("message", "Authentication failed");
                            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        progressDialog.dismiss();
                        Log.e("Login", "JSON parsing error: " + e.getMessage());
                        Toast.makeText(this, "Login failed: Invalid server response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    Log.e("Login", "Volley error: " + error.toString());
                    if (error.networkResponse != null) {
                        Log.e("Login", "Status Code: " + error.networkResponse.statusCode);
                        Log.e("Login", "Response Data: " + new String(error.networkResponse.data));
                    }
                    Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public byte[] getBody() {
                return jsonBody.toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };

        requestQueue.add(stringRequest);
    }

    private void saveInstanceState(String email, String token, String position, String name) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SESSION_EMAIL, email);
        editor.putString(SESSION_TOKEN, token);
        editor.putString(SESSION_POSITION, position);
        editor.putString(SESSION_NAME, name);
        editor.apply();
    }
}
