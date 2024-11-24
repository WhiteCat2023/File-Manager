package com.example.filemanager;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class Feedback extends AppCompatActivity {

    private TextInputEditText  feedbackDescription;
    private RequestQueue requestQueue;
    private RatingBar rating;
    private final String FEEDBACK_URL = "https://skcalamba.scarlet2.io/sk_feedback.php";

    private static final String SHARED_PREF_NAME = "session";
    private static final String SESSION_TOKEN = "user_token";

    private int userRate;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        feedbackDescription = findViewById(R.id.feedbackDescription);
        Button sendFeedback = findViewById(R.id.sendFeedback);
        ImageView feedbackBack = findViewById(R.id.feedbackBack);
        rating = findViewById(R.id.ratingBar);

        feedbackBack.setOnClickListener(v -> {
            Intent intent = new Intent(Feedback.this, Profile.class);
            startActivity(intent);
            finish();
        });



        requestQueue = Volley.newRequestQueue(this);

        sendFeedback.setOnClickListener(view1 -> {
            sendFeedbackToAdmin(rating);
            progressDialog = new ProgressDialog(Feedback.this);
            progressDialog.setMessage("Sending Feedback...");
            progressDialog.show();
        });
    }

    private void sendFeedbackToAdmin(RatingBar rating){
        String feedbackDescriptionString = Objects.requireNonNull(feedbackDescription.getText()).toString().trim();

        userRate += (int) rating.getRating();
        if (feedbackDescriptionString.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences sharedPreferences = this.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String feedback_by = sharedPreferences.getString(SESSION_TOKEN, "0");

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("feedback_rate", userRate);
            jsonBody.put("feedback_description", feedbackDescriptionString);
            jsonBody.put("feedback_by", feedback_by);

        } catch (JSONException e) {
            Log.e("Feedback", "JSON error: " + e.getMessage());
            Toast.makeText(this, "Error creating JSON", Toast.LENGTH_SHORT).show();
            return;
        }

        StringRequest request = new StringRequest(Request.Method.POST, FEEDBACK_URL,
                response -> {
                    Log.d("Login", "Response: " + response); // Log the raw response for debugging
                    JSONObject jsonResponse = null;
                    try {
                        jsonResponse = new JSONObject(response);
                        String status = jsonResponse.getString("status");
                        if (status.equals("success")){
                            Toast.makeText(this, "Feedback sent successfully", Toast.LENGTH_SHORT).show();
                            feedbackDescription.setText("");
                            rating.setRating(0);
                            userRate = 0;
                            progressDialog.dismiss();
                        }else{
                            String errorMessage = jsonResponse.optString("message", "Authentication failed");
                            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    } catch (JSONException e) {
                        Log.e("Feedback", "JSON parsing error: " + e.getMessage());
                        Toast.makeText(this, "Sending Feedback failed: Invalid server response", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }

                },error -> {
            Log.e("Feedback", "Volley error");
            if (error.networkResponse != null) {
                Log.e("Feedback", "Status Code: " + error.networkResponse.statusCode);
                Log.e("Feedback", "Response Data: " + new String(error.networkResponse.data));
            }
            Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }
        ){
            @Override
            public byte[] getBody() {
                return jsonBody.toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };
        requestQueue.add(request);
    }
}