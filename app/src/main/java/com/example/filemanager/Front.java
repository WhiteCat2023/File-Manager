package com.example.filemanager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class Front extends AppCompatActivity {
    //Initializing variables
    Button signIn;

    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String SHARED_PREF_NAME = "session";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_front);

        //Assigning component to variables
        signIn = findViewById(R.id.splash_signin);

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);

        if (isLoggedIn) {
            // User is not logged in, redirect to Login Activity
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish(); // Close the Main Activity
        } else if(!isLoggedIn){
            Intent intent = new Intent(this, Login.class);
            startActivity(intent);
            finish(); // Close the Main Activity
        }else {
            //Command for proceeding to login page
            signIn.setOnClickListener(view -> {
                Intent intent = new Intent(Front.this, Login.class);
                startActivity(intent);
                finish();
            });
        }


    }
}