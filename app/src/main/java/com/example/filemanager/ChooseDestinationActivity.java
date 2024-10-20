package com.example.filemanager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filemanager.Utils.DirectoryAdapter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class ChooseDestinationActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DirectoryAdapter directoryAdapter;
    private List<File> directories;
    private String itemName;
    private File currentDir;
    private ImageView back;
    private Button copyToButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_destination);

        itemName = getIntent().getStringExtra("itemName");

        // Set currentDir to the Downloads directory
        currentDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        recyclerView = findViewById(R.id.directoryRecyclerView);
        directories = new ArrayList<>();

        // Set up the RecyclerView
        directoryAdapter = new DirectoryAdapter(directories, this::onDirectorySelected);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(directoryAdapter);
        back = findViewById(R.id.backArrowButton);
        copyToButton = findViewById(R.id.btn); // Assume you have this button in the layout

        back.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });

        // Copy to button logic
        copyToButton.setOnClickListener(v -> copyFiles());

        // Load the initial directories
        loadDirectories(currentDir);
    }

    private void loadDirectories(File dir) {
        directories.clear();
        File[] files = dir.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    directories.add(file);
                }
            }
        }

        directoryAdapter.notifyDataSetChanged();
    }

    private void onDirectorySelected(File directory) {
        if (directory.getName().equals("..")) {
            currentDir = currentDir.getParentFile();
        } else {
            currentDir = directory;
        }

        loadDirectories(currentDir);
    }

    private void copyFiles() {
        File selectedDirectory = directoryAdapter.getSelectedDirectory();
        if (selectedDirectory == null) {
            Toast.makeText(this, "Please select a destination folder", Toast.LENGTH_SHORT).show();
            return;
        }

        File sourceFile = new File(Environment.getExternalStorageDirectory(), itemName);
        File destFile = new File(selectedDirectory, itemName);

        try {
            Files.copy(sourceFile.toPath(), destFile.toPath());
            Toast.makeText(this, "File copied successfully", Toast.LENGTH_SHORT).show();
            finish();
        } catch (IOException e) {
            Toast.makeText(this, "Failed to copy file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (currentDir.getParentFile() != null) {
            currentDir = currentDir.getParentFile();
            loadDirectories(currentDir);
        } else {
            super.onBackPressed();
        }
    }
}
