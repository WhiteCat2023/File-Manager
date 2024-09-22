package com.example.filemanager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class Front extends AppCompatActivity {
    //Initializing variables
    Button signIn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_front);

        //Assigning component to variables
        signIn = findViewById(R.id.splash_signin);

        //Command for proceeding to login page
        signIn.setOnClickListener(view -> {
            Intent intent = new Intent(Front.this, Login.class);
            startActivity(intent);
            finish();
        });
    }
}