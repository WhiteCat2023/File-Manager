package com.example.filemanager;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.filemanager.Fragments.Announcements;
import com.example.filemanager.Fragments.Files;
import com.example.filemanager.Tabs.InternalStorage;
import com.example.filemanager.Tabs.ServerStorage;
import com.example.filemanager.Fragments.ToDoList;
import com.google.android.material.imageview.ShapeableImageView;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    // Initializing variables
    private ProgressDialog progressDialog;
    private ChipNavigationBar chipNavigationBar;
    private ShapeableImageView profile;

    private TextView title;

    private static final String SHARED_PREF_NAME = "session";
    private static final String SESSION_EMAIL = "user_email";
    private static final String SESSION_POSITION = "user_position";
    private static final String SESSION_NAME = "user_name";
    private static final String SESSION_PROFILE_PICTURE = "user_profile_picture";

    private String profile_url = "https://skcalamba.scarlet2.io/profile/";


//    TextView headerName, headerEmail, headerPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chipNavigationBar = findViewById(R.id.chipNaviagation);
        title = findViewById(R.id.title);
        profile = findViewById(R.id.shapeableImageView);

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String profilePicture = sharedPreferences.getString(SESSION_PROFILE_PICTURE, ""); // Default to empty string if not found
        Picasso.get().load(profile_url + profilePicture ).into(profile);

        //Chip Navigation
        chipNavigationBar.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int itemId) {
                if (itemId == R.id.nav_announcements) {
                    title.setText("SK Calamba");
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Announcements()).commit();
                } else if (itemId == R.id.nav_files) {
                    title.setText("Files");
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Files()).commit();
                } else if (itemId == R.id.nav_todolist) {
                    title.setText("To Do");
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ToDoList()).commit();
                } else if (itemId == R.id.nav_trash) {
                    title.setText("Trash");
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Trash()).commit();
                }
            }
        });

        profile.setOnClickListener(view -> {
            Intent intent = new Intent(this, Profile.class);
            startActivity(intent);
            finish();
        });


        //Progressbar
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setCancelable(false);

        if (savedInstanceState == null) {

            chipNavigationBar.setItemSelected(R.id.nav_announcements, true);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Announcements()).commit();
        }
    }

//    @Override
//    public void onTrimMemory(int level) {
//        super.onTrimMemory(level);
//        if (level == TRIM_MEMORY_UI_HIDDEN) {
//            // App is finishing
//            clearSharedPreferences();
//        }
//    }
//    private void clearSharedPreferences() {
//        SharedPreferences preferences = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = preferences.edit();
//        editor.clear();
//        editor.apply();
//    }
    // Handles the back button
    @Override
    public void onBackPressed() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

        if (currentFragment instanceof InternalStorage) {
            InternalStorage internalStorageFragment = (InternalStorage) currentFragment;
            internalStorageFragment.goBack();
        } else if (currentFragment instanceof ServerStorage) {
            ServerStorage serverStorageFragment = (ServerStorage) currentFragment;
            serverStorageFragment.goBack();
        } else{
            // Dismiss the progress dialog if it's showing
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            super.onBackPressed();
        }
    }
//    // Removes the session and logs the user out
//    private void logout() {
//        // Clear session data
//        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.clear();
//        editor.apply();
//
//        // Dismiss the progress dialog if it's showing
//        if (progressDialog != null && progressDialog.isShowing()) {
//            progressDialog.dismiss();
//        }
//
//        // Redirect to login activity
//        Intent intent = new Intent(MainActivity.this, Login.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear backstack
//        startActivity(intent);
//        finish();
//    }
    // Returns a short toast
    public void messageShort(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    // Returns a long toast
    public void messageLong(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    @Override
    protected void onStop() {
        super.onStop();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
//        outState.putString(SESSION_EMAIL, headerEmail.getText().toString());
//        outState.putString(SESSION_NAME, headerName.getText().toString());
//        outState.putString(SESSION_POSITION, headerPosition.getText().toString());
        if (title != null){
            String text = title.getText().toString().trim();
            outState.putString("title", text);
        }

    }
}
