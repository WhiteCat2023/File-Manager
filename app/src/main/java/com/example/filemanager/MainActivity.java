package com.example.filemanager;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
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

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.filemanager.Fragments.Files;
import com.example.filemanager.Fragments.Todo;
import com.example.filemanager.Trash;
import com.example.filemanager.Tabs.InternalStorage;
import com.example.filemanager.Tabs.ServerStorage;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // Initializing variables
    DrawerLayout drawerLayout;
    BottomNavigationView bottomNavigationView;
    Toolbar toolbar;
    FragmentManager fragmentManager;

    private static final String DOWNLOAD_FOLDER_NAME = "MyDownloads";

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

        View headerView = navigationView.getHeaderView(0);
        headerName = headerView.findViewById(R.id.headerName);
        headerEmail = headerView.findViewById(R.id.headerEmail);
        headerPosition = headerView.findViewById(R.id.headerPosition);

        // Load the initial fragment only if savedInstanceState is null
        if (savedInstanceState == null) {
            openFragment(new Files());
            navigationView.setCheckedItem(R.id.nav_files);
        }

        loadUserData();

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

//    private void showBottomSheetDialog() {
//        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(MainActivity.this);
//        View options = LayoutInflater.from(MainActivity.this).inflate(R.layout.bottomsheet_nav, null);
//        bottomSheetDialog.setContentView(options);
//        bottomSheetDialog.show();
//
//        LinearLayout newTask = options.findViewById(R.id.newTask);
//        LinearLayout uploadFile = options.findViewById(R.id.uploadFile);
//        LinearLayout newFolder = options.findViewById(R.id.newFolder);
//
//        // Directs you to the new task page
//        newTask.setOnClickListener(view -> {
//            Intent intent = new Intent(MainActivity.this, NewTask.class);
//            startActivity(intent);
//        });
//
//        // Directs you to upload page
//        uploadFile.setOnClickListener(view -> {
//            Intent intent = new Intent(MainActivity.this, Upload.class);
//            startActivity(intent);
//        });
//
//        // Directs you to the new folder page
//        newFolder.setOnClickListener(view -> {
//            BottomSheetDialog newFolderDialog = new BottomSheetDialog(MainActivity.this);
//            View newFolderOptions = LayoutInflater.from(MainActivity.this).inflate(R.layout.bottomsheet_new_folder, null);
//            newFolderDialog.setContentView(newFolderOptions);
//            bottomSheetDialog.dismiss(); // Close the first bottom sheet
//            newFolderDialog.show();
//
//            LinearLayout internalNewFolder = newFolderOptions.findViewById(R.id.internalNewFolder);
//            LinearLayout serverNewFolder = newFolderOptions.findViewById(R.id.serverNewFolder);
//
//            internalNewFolder.setOnClickListener(v -> newFolderInInternalStorage());
//            serverNewFolder.setOnClickListener(v -> newFolderInServerStorage());
//        });
//    }

//    public void newFolderInServerStorage() {
//        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.alertdialog_input_folder, null);
//        TextInputEditText newFolderName = view.findViewById(R.id.folderNameInput);
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
//                .setTitle("Create Folder on Server")
//                .setView(view)
//                .setPositiveButton("Create", (dialog, which) -> {
//                    String folderName = newFolderName.getText().toString().trim();
//                    if (!folderName.isEmpty()) {
//                        createFolderOnServer(folderName);
//                    } else {
//                        Toast.makeText(MainActivity.this, "Please enter a folder name", Toast.LENGTH_SHORT).show();
//                    }
//                })
//                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
//        builder.show();
//    }

//    private void createFolderOnServer(String folderName) {
//        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
//        String currentPath = sharedPreferences.getString(SERVER_STORAGE_CURRENT_PATH, MY_FOLDER_PATH);
//
//        JSONObject jsonBody = new JSONObject();
//        try {
//            jsonBody.put("folder_name", folderName);
//            jsonBody.put("current_path", currentPath);
//        } catch (JSONException e) {
//            e.printStackTrace();
//            return;
//        }
//
//        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
//
//        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
//                Request.Method.POST,
//                CREATE_FOLDER_API,
//                jsonBody,
//                response -> {
//                    try {
//                        JSONObject responses = new JSONObject(response.toString());
//                        if (responses.equals("success")){
//                            Toast.makeText(MainActivity.this, "Folder created successfully", Toast.LENGTH_SHORT).show();
//                        }else{
//                            Toast.makeText(MainActivity.this, "Failed to create folder", Toast.LENGTH_SHORT).show();
//                        }
//                    } catch (JSONException e) {
//                        Toast.makeText(MainActivity.this, "Response parsing error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                },
//                error -> {
//                    String errorMsg;
//                    if (error.networkResponse != null) {
//                        errorMsg = "Error code: " + error.networkResponse.statusCode + "\nResponse: " + new String(error.networkResponse.data);
//                    } else {
//                        errorMsg = "Error: " + error.getMessage();
//                    }
//                    Log.e("MainActivity", "Error creating folder: " + errorMsg);
//                    Toast.makeText(MainActivity.this, "Failed to create folder: " + errorMsg, Toast.LENGTH_LONG).show();
//                }
//
//        ) {
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String, String> headers = new HashMap<>();
//                headers.put("auth_token", AUTH_TOKEN);  // Set the correct auth token
//                headers.put("Content-Type", "application/json");
//                return headers;
//            }
//        };
//
//        // Add the request to the Volley queue
//        requestQueue.add(jsonObjectRequest);
//    }

//    public void newFolderInInternalStorage() {
//        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.alertdialog_input_folder, null);
//        TextInputEditText newFolderName = view.findViewById(R.id.folderNameInput);
//        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
//                .setTitle("Create Folder")
//                .setView(view)
//                .setPositiveButton("Create", (dialog, which) -> {
//                    String newFolderNameString = newFolderName.getText().toString().trim();
//
//                    if (!newFolderNameString.isEmpty()) {
//                        File directory = new File(MainActivity.this.getExternalFilesDir(null), DOWNLOAD_FOLDER_NAME);
//                        File newFolder = new File(directory, newFolderNameString);
//
//                        if (!newFolder.exists()) {
//                            boolean createdNewFolder = newFolder.mkdir();
//                            if (createdNewFolder) {
//                                Toast.makeText(MainActivity.this, "Folder created successfully", Toast.LENGTH_SHORT).show();
//                            } else {
//                                Toast.makeText(MainActivity.this, "Failed to create folder", Toast.LENGTH_SHORT).show();
//                            }
//                        } else {
//                            Toast.makeText(MainActivity.this, "Folder already exists", Toast.LENGTH_SHORT).show();
//                        }
//                    } else {
//                        Toast.makeText(MainActivity.this, "Please enter a folder name", Toast.LENGTH_SHORT).show();
//                    }
//                })
//                .setNegativeButton("Cancel", (dialog, which) -> {
//                    dialog.dismiss();
//                });
//        builder.show();
//    }FA

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
        ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setCancelable(false);

        int itemId = item.getItemId();
        if (itemId == R.id.nav_files) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Files()).commit();
        } else if (itemId == R.id.nav_todo) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Todo()).commit();
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
            super.onBackPressed();
        }

        // Check if the navigation drawer is open
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    // Removes the session and logs the user out
    private void logout() {
        // Clear session data
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

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
}
