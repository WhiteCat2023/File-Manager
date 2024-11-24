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

import com.google.android.material.imageview.ShapeableImageView;
import com.squareup.picasso.Picasso;

public class Profile extends AppCompatActivity {

    ImageView back;

    private static final String SHARED_PREF_NAME = "session";
    private static final String SESSION_EMAIL = "user_email";
    private static final String SESSION_POSITION = "user_position";
    private static final String SESSION_NAME = "user_name";
    private static final String SESSION_PROFILE_PICTURE = "user_profile_picture";
    private static final String SESSION_PROFILE_PICTURE_URL = "user_profile_picture_url";

    private TextView profileName, profileEmail, profilePosition;

    private Button logoutBtn, feedbackBtn;

    private ShapeableImageView profilePic;

    private ProgressDialog progressDialog;

    private String profile_url = "https://skcalamba.scarlet2.io/profile/";
    private String profilePicture = "";

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
        profilePic = findViewById(R.id.profileId);

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

        if (savedInstanceState != null) {
            String email = savedInstanceState.getString(SESSION_EMAIL);
            String name = savedInstanceState.getString(SESSION_NAME);
            String position = savedInstanceState.getString(SESSION_POSITION);
            String profilePictureUrl = savedInstanceState.getString(SESSION_PROFILE_PICTURE_URL);

            // Restore the values to the UI
            profileEmail.setText(email);
            profileName.setText(name);
            profilePosition.setText(position);

            // Load the profile picture using Picasso
            if (profilePictureUrl != null) {
                Picasso.get()
                        .load(profilePictureUrl)
                        .into(profilePic);
            }
        }
    }
    private void loadSessionData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String email = sharedPreferences.getString(SESSION_EMAIL, ""); // Default to empty string if not found
        String name = sharedPreferences.getString(SESSION_NAME, "");
        String position = sharedPreferences.getString(SESSION_POSITION, "");
        profilePicture = sharedPreferences.getString(SESSION_PROFILE_PICTURE, ""); // Default to empty string if not found

        // Set the retrieved data to the TextViews
        profileEmail.setText("Email: " + email);
        profileName.setText("Name: " + name);
        profilePosition.setText("Position: " + position);
        if(profilePicture != null){
            Picasso.get().load(profile_url + profilePicture ).into(profilePic);
        }

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
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SESSION_EMAIL, profileEmail.getText().toString());
        outState.putString(SESSION_NAME, profileName.getText().toString());
        outState.putString(SESSION_POSITION, profilePosition.getText().toString());
        outState.putString(SESSION_PROFILE_PICTURE_URL, profile_url + profilePicture); // Save the URL
    }
}