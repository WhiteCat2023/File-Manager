package com.example.filemanager.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.filemanager.R;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

public class Feedback extends Fragment {

    private TextInputEditText feedbackTitle, feedbackDescription;
    private Button sendFeedback;
    private RequestQueue requestQueue;

    private String feedbackTitleString, feedbackDescriptionString;
    private final String FEEDBACK_URL = "https://skcalamba.scarlet2.io/sk_feedback.php";

    private static final String SHARED_PREF_NAME = "session";
    private static final String SESSION_EMAIL = "user_email";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_feedback, container, false);

        feedbackTitle = view.findViewById(R.id.feedbackTitle);
        feedbackDescription = view.findViewById(R.id.feedbackDescription);
        sendFeedback = view.findViewById(R.id.sendFeedback);

        requestQueue = Volley.newRequestQueue(getActivity());

        sendFeedback.setOnClickListener(view1 -> {
            sendFeedbackToAdmin();
        });


        return view;
    }

    private void sendFeedbackToAdmin(){
        feedbackTitleString = feedbackTitle.getText().toString().trim();
        feedbackDescriptionString = feedbackDescription.getText().toString().trim();

        if (feedbackTitleString.isEmpty() || feedbackDescriptionString.isEmpty()) {
            Toast.makeText(getActivity(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String feedback_by = sharedPreferences.getString(SESSION_EMAIL, "0");

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("feedback_title", feedbackTitleString);
            jsonBody.put("feedback_description", feedbackDescriptionString);
            jsonBody.put("feedback_by", feedback_by);

        } catch (JSONException e) {
            Log.e("Feedback", "JSON error: " + e.getMessage());
            Toast.makeText(getActivity(), "Error creating JSON", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(getActivity(), "Feedback sent successfully", Toast.LENGTH_SHORT).show();
                            feedbackTitle.setText("");
                            feedbackDescription.setText("");
                        }else{
                            String errorMessage = jsonResponse.optString("message", "Authentication failed");
                            Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("Feedback", "JSON parsing error: " + e.getMessage());
                        Toast.makeText(getActivity(), "Sending Feedback failed: Invalid server response", Toast.LENGTH_SHORT).show();
                    }

                },error -> {
                    Log.e("Feedback", "Volley error");
                    if (error.networkResponse != null) {
                        Log.e("Feedback", "Status Code: " + error.networkResponse.statusCode);
                        Log.e("Feedback", "Response Data: " + new String(error.networkResponse.data));
                    }
                    Toast.makeText(getActivity(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
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