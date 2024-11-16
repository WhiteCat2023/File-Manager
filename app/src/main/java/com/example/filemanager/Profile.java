package com.example.filemanager;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Profile extends AppCompatActivity {

    ImageView back;

    private static final String SHARED_PREF_NAME = "session";
    private static final String SESSION_EMAIL = "user_email";
    private static final String SESSION_POSITION = "user_position";
    private static final String SESSION_NAME = "user_name";

    TextView profileName, profileEmail, profilePosition;

    Button logoutBtn, feedbackBtn;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profileName = findViewById(R.id.profileName);
        profileEmail = findViewById(R.id.profileEmail);
        profilePosition = findViewById(R.id.profilePosition);

        logoutBtn = findViewById(R.id.userSignOut);
        feedbackBtn = findViewById(R.id.userFeedback);
        back = findViewById(R.id.profile_back_btn);

        progressDialog = new ProgressDialog(Profile.this);
        progressDialog.setCancelable(false);

        //Back function
        back.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        //Feedback function
        feedbackBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, Feedback.class);
            startActivity(intent);
            finish();
        });

        //Logout function
        logoutBtn.setOnClickListener(v -> {
            logout();
        });

        loadSessionData();
    }
    private void loadSessionData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String email = sharedPreferences.getString(SESSION_EMAIL, ""); // Default to empty string if not found
        String name = sharedPreferences.getString(SESSION_NAME, "");
        String position = sharedPreferences.getString(SESSION_POSITION, "");

        // Set the retrieved data to the TextViews
        profileEmail.setText("Email: " + email);
        profileName.setText("Name: " + name);
        profilePosition.setText("Position: " + position);
    }

    private void logout() {
        // Clear session data
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        // Dismiss the progress dialog if it's showing
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        // Redirect to login activity
        Intent intent = new Intent(Profile.this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear backstack
        startActivity(intent);
        finish();
    }
}