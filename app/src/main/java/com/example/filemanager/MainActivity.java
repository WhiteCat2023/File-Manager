package com.example.filemanager;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.filemanager.Fragments.Announcements;
import com.example.filemanager.Fragments.Files;
import com.example.filemanager.Tabs.InternalStorage;
import com.example.filemanager.Tabs.ServerStorage;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // Initializing variables
    DrawerLayout drawerLayout;
    Toolbar toolbar;
    ProgressDialog progressDialog;
    FragmentManager fragmentManager;

    private static final String SHARED_PREF_NAME = "session";
    private static final String SESSION_EMAIL = "user_email";
    private static final String SESSION_POSITION = "user_position";
    private static final String SESSION_NAME = "user_name";


    TextView headerName, headerEmail, headerPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);

        // Adds the hamburger icon
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Adds the navigation drawer
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        fragmentManager = getSupportFragmentManager();

        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setCancelable(false);


        View headerView = navigationView.getHeaderView(0);
        headerName = headerView.findViewById(R.id.headerName);
        headerEmail = headerView.findViewById(R.id.headerEmail);
        headerPosition = headerView.findViewById(R.id.headerPosition);

        loadUserData();

        if (savedInstanceState == null) {
            openFragment(new Announcements());
            navigationView.setCheckedItem(R.id.nav_announcements);
        } else {
            String userEmail = savedInstanceState.getString(SESSION_EMAIL);
            String userName = savedInstanceState.getString(SESSION_NAME);
            String userPosition = savedInstanceState.getString(SESSION_POSITION);

            headerEmail.setText(userEmail);
            headerName.setText(userName);
            headerPosition.setText(userPosition);
        }
    }

    private void loadUserData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
        String userEmail = sharedPreferences.getString(SESSION_EMAIL, "Email not found");
        String userName = sharedPreferences.getString(SESSION_NAME, "Name not found");
        String userPosition = sharedPreferences.getString(SESSION_POSITION, "Position not found");

        headerName.setText(userName);
        headerEmail.setText(userEmail);
        headerPosition.setText(userPosition);
    }
    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (level == TRIM_MEMORY_UI_HIDDEN) {
            // App is finishing
            clearSharedPreferences();
        }
    }
    private void clearSharedPreferences() {
        SharedPreferences preferences = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }
    // Handles the navigation drawer
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.nav_announcements) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Announcements()).commit();
        } else if (itemId == R.id.nav_files) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Files()).commit();
        } else if (itemId == R.id.nav_todolist) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ToDoList()).commit();
        } else if (itemId == R.id.nav_trash) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Trash()).commit();
        } else if (itemId == R.id.nav_feedback) {
            Intent intent = new Intent(this, Feedback.class);
            startActivity(intent);
        } else if (itemId == R.id.nav_signout) {
            progressDialog.setMessage("Signing Out...");
            progressDialog.show();
            messageShort("Signing Out...");
            logout();
        }

        drawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }
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
        } else {
            // Dismiss the progress dialog if it's showing
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

            super.onBackPressed();
        }

        // Check if the navigation drawer is open
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }
    // Removes the session and logs the user out
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
        Intent intent = new Intent(MainActivity.this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear backstack
        startActivity(intent);
        finish();
    }
    // Opens a fragment
    private void openFragment(Fragment fragment) {
        if (fragment != null) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }
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
        outState.putString(SESSION_EMAIL, headerEmail.getText().toString());
        outState.putString(SESSION_NAME, headerName.getText().toString());
        outState.putString(SESSION_POSITION, headerPosition.getText().toString());

    }



}
