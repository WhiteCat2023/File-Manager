package com.example.filemanager;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
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

import com.example.filemanager.Fragments.Dashboard;
import com.example.filemanager.Fragments.Files;
import com.example.filemanager.Fragments.Todo;
import com.example.filemanager.Tabs.ServerStorage;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // Initializing variables
    DrawerLayout drawerLayout;
    FloatingActionButton fab;
    BottomNavigationView bottomNavigationView;
    Toolbar toolbar;
    FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Assigning components to variables
        fab = findViewById(R.id.fab);
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



        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setBackground(null);

        if (savedInstanceState == null) {
            // Load the initial fragment
            openFragment(new Dashboard());
            bottomNavigationView.setSelectedItemId(R.id.bottom_dashboard);
        }

        // Handles the bottom navigation bar
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.bottom_dashboard){
                    openFragment(new Dashboard());
                    return true;
                } else if (itemId == R.id.bottom_files) {
                    openFragment(new Files());
                    return true;
                } else if (itemId == R.id.bottom_todo) {
                    openFragment(new Todo());
                    return true;
                }
                return false;
            }
        });

        // Floating Action Button logic
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBottomSheetDialog();
            }
        });
    }

    private void showBottomSheetDialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(MainActivity.this);
        View options = LayoutInflater.from(MainActivity.this).inflate(R.layout.bottomsheet_nav, null);
        bottomSheetDialog.setContentView(options);
        bottomSheetDialog.show();

        LinearLayout newTask = options.findViewById(R.id.newTask);
        LinearLayout uploadFile = options.findViewById(R.id.uploadFile);
        LinearLayout newFolder = options.findViewById(R.id.newFolder);

        // Directs you to the new task page
        newTask.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, NewTask.class);
            startActivity(intent);
        });

        // Directs you to upload page
        uploadFile.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, Upload.class);
            startActivity(intent);
        });

        // Directs you to the new folder page
        newFolder.setOnClickListener(view -> {
            BottomSheetDialog newFolderDialog = new BottomSheetDialog(MainActivity.this);
            View newFolderOptions = LayoutInflater.from(MainActivity.this).inflate(R.layout.bottomsheet_new_folder, null);
            newFolderDialog.setContentView(newFolderOptions);
            bottomSheetDialog.dismiss(); // Close the first bottom sheet
            newFolderDialog.show();
        });
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
        SharedPreferences preferences = getSharedPreferences("your_preferences_name", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }

    // Handles the navigation drawer
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);

        int itemId = item.getItemId();
        if (itemId == R.id.nav_trash){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Trash()).commit();
        } else if (itemId == R.id.nav_signout) {
            progressDialog.setMessage("Signing Out...");
            progressDialog.show();
            messageShort("Signing Out");
            logout();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    // Handles the back button
    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (fragment instanceof ServerStorage) {
            ((ServerStorage) fragment).goBack(); // Call goBack method in ServerStorage
        } else {
            super.onBackPressed(); // Use default back action
        }

        // Check if the drawer is open and close it
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }


    // Removes the session and logs the user out
    private void logout() {
        SharedPreferences sharedPreferences = getSharedPreferences("session", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(MainActivity.this, Login.class);
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
}
