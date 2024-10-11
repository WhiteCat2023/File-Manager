package com.example.filemanager;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.filemanager.Tabs.InternalStorage;
import com.example.filemanager.Utils.DeletedItemAdapter;
import com.example.filemanager.Utils.RecyclerItem;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class Trash extends Fragment {

    private RecyclerView recyclerView;
    private DeletedItemAdapter adapter;

    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView emptyStateTextView;
    private ImageView emptyStateImageView;

    private List<RecyclerItem> deletedItems;

    private static final String TRASH_FOLDER_NAME = "Trash";
    private static final String METADATA_FOLDER_NAME = "Metadata";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trash, container, false);

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.trashRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        emptyStateTextView = view.findViewById(R.id.trashText);
        emptyStateImageView = view.findViewById(R.id.trashImageView);
        swipeRefreshLayout = view.findViewById(R.id.trashSwipeRefreshLayout);

        // Initialize deletedItems
        deletedItems = new ArrayList<>();

        // Set up the adapter with deleted items
        adapter = new DeletedItemAdapter(deletedItems, new DeletedItemAdapter.OnItemActionListener() {
            @Override
            public void onRestoreClick(RecyclerItem item) {
                restoreDeletedFiles(item);  // Call the restore method within the Trash fragment
            }

            @Override
            public void onDeleteClick(RecyclerItem item) {
                deleteDeletedFile(item);
            }

            @Override
            public void onRestoreFolderClick(RecyclerItem item) {
                restoreFolder(item);
            }

            @Override
            public void onDeleteFolderClick(RecyclerItem item) {
                deleteFolder(item);
            }
        });
        recyclerView.setAdapter(adapter);

        // Create Trash folder if it doesn't exist
        createTrashFolderIfNeeded();

        // Load deleted items from the Trash folder
        loadDeletedItems();

        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadDeletedItems();
            swipeRefreshLayout.setRefreshing(false);
        });

        return view;
    }

    private void restoreFolder(RecyclerItem item) {
        // Define paths
        File trashFolder = new File(requireContext().getExternalFilesDir(null), TRASH_FOLDER_NAME);
        File folderToRestore = new File(trashFolder, item.getFileName());

        // Metadata folder
        File metadataFolder = new File(requireContext().getExternalFilesDir(null), METADATA_FOLDER_NAME);
        File metadataFile = new File(metadataFolder, item.getFileName() + ".json");

        // Load the original path from the metadata file
        String originalPath = null;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(metadataFile));
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }
            reader.close();

            JSONObject metadata = new JSONObject(json.toString());
            originalPath = metadata.getString("originalPath");
        } catch (Exception e) {
            Log.e("RestoreFolder", "Error reading metadata: " + e.getMessage());
            Toast.makeText(requireContext(), "Error reading metadata.", Toast.LENGTH_SHORT).show();
            return;
        }

        // New destination in MyDownloads
        File downloadsFolder = new File(requireContext().getExternalFilesDir(null), "MyDownloads");
        File restoredFolder = new File(downloadsFolder, folderToRestore.getName());

        // Log the paths for debugging
        Log.d("RestoreFolder", "Restoring folder to: " + restoredFolder.getAbsolutePath());

        if (restoredFolder.exists()) {
            Toast.makeText(requireContext(), "Folder already exists in MyDownloads.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Attempt to move the folder
        if (folderToRestore.renameTo(restoredFolder)) {
            Toast.makeText(requireContext(), "Folder restored successfully", Toast.LENGTH_SHORT).show();
            deletedItems.remove(item);
            adapter.notifyDataSetChanged();

            // Delete metadata after restoring
            if (metadataFile.exists()) {
                metadataFile.delete();
            }
        } else {
            Log.e("RestoreFolder", "Failed to restore folder");
            Toast.makeText(requireContext(), "Failed to restore folder", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteFolder(RecyclerItem item) {
        // Define paths
        File trashFolder = new File(requireContext().getExternalFilesDir(null), TRASH_FOLDER_NAME);
        File folderToDelete = new File(trashFolder, item.getFileName());

        // Metadata folder
        File metadataFolder = new File(requireContext().getExternalFilesDir(null), METADATA_FOLDER_NAME);
        File metadataFile = new File(metadataFolder, item.getFileName() + ".json");

        if (folderToDelete.exists()) {
            // Confirm deletion
            new AlertDialog.Builder(requireContext())
                    .setTitle("Delete Folder")
                    .setMessage("Are you sure you want to delete this folder and all its contents?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        deleteFolderRecursively(folderToDelete);

                        // Check if metadata exists and delete it
                        if (metadataFile.exists()) {
                            if (metadataFile.delete()) {
                                Log.d("Trash", "Deleted metadata for folder: " + item.getFileName());
                            } else {
                                Log.e("Trash", "Failed to delete metadata for folder: " + item.getFileName());
                            }
                        }

                        Toast.makeText(requireContext(), "Folder deleted successfully", Toast.LENGTH_SHORT).show();
                        deletedItems.remove(item);
                        adapter.notifyDataSetChanged();
                    })
                    .setNegativeButton("No", null)
                    .show();
        } else {
            Toast.makeText(requireContext(), "Folder does not exist", Toast.LENGTH_SHORT).show();
        }
    }

    // Recursive folder deletion method
    private void deleteFolderRecursively(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteFolderRecursively(file); // Recursive call for subdirectories
                }
                file.delete(); // Delete the file
            }
        }
        folder.delete(); // Finally, delete the folder itself
    }



    // Method to create Trash folder if it doesn't exist
    private void createTrashFolderIfNeeded() {
        File trashFolder = new File(requireContext().getExternalFilesDir(null), TRASH_FOLDER_NAME);

        if (!trashFolder.exists()) {
            boolean folderCreated = trashFolder.mkdir();  // Create the folder
            if (folderCreated) {
                Log.d("Trash", "Trash folder created.");
                Toast.makeText(requireContext(), "Trash folder created.", Toast.LENGTH_SHORT).show();
            } else {
                Log.e("Trash", "Failed to create Trash folder.");
                Toast.makeText(requireContext(), "Failed to create Trash folder.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.d("Trash", "Trash folder already exists.");
        }
    }

    // Method to load deleted items from the Trash folder
    private void loadDeletedItems() {
        File trashFolder = new File(requireContext().getExternalFilesDir(null), TRASH_FOLDER_NAME);

        deletedItems.clear();

        if (trashFolder.exists() && trashFolder.isDirectory()) {
            File[] files = trashFolder.listFiles();
            if (files != null && files.length > 0) {
                for (File file : files) {
                    updateEmptyStateVisibility();
                    boolean isDirectory = file.isDirectory();
                    deletedItems.add(new RecyclerItem(file.getName(), formatFileSize(file.length()), "", isDirectory, file.getAbsolutePath()));
                }
            } else {
                updateEmptyStateVisibility();
                Log.e("Trash", "No files found in Trash folder.");
                Toast.makeText(requireContext(), "No items in Trash.", Toast.LENGTH_SHORT).show();
            }
            adapter.notifyDataSetChanged();
        } else {
            updateEmptyStateVisibility();
            Log.e("Trash", "Trash folder does not exist.");
            Toast.makeText(requireContext(), "Trash folder does not exist.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateEmptyStateVisibility() {
        if (deletedItems.isEmpty()) {
            emptyStateImageView.setVisibility(View.VISIBLE);
            emptyStateTextView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyStateImageView.setVisibility(View.GONE);
            emptyStateTextView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    // Method to format file size into a readable format
    private String formatFileSize(long size) {
        if (size <= 0) return "0 KB";
        String[] units = new String[]{"B", "KB", "MB", "GB"};
        int idx = (int) (Math.log(size) / Math.log(1024));
        return String.format("%.1f %s", size / Math.pow(1024, idx), units[idx]);
    }

    // Method to restore deleted files
    private void restoreDeletedFiles(RecyclerItem item) {
        File trashFolder = new File(requireContext().getExternalFilesDir(null), TRASH_FOLDER_NAME);
        File restoredFile = new File(trashFolder, item.getFileName());

        // Load the original path from the metadata file inside the Metadata folder
        File metadataFolder = new File(requireContext().getExternalFilesDir(null), "Metadata");
        File metadataFile = new File(metadataFolder, item.getFileName() + ".json");
        String originalPath = null;

        try {
            BufferedReader reader = new BufferedReader(new FileReader(metadataFile));
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }
            reader.close();

            // Parse the JSON data to extract the original path
            JSONObject metadata = new JSONObject(json.toString());
            originalPath = metadata.getString("originalPath");

        } catch (Exception e) {
            Log.e("RestoreFiles", "Error reading metadata: " + e.getMessage());
            Toast.makeText(requireContext(), "Error reading metadata.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new File object for the original path
        File originalFile = new File(originalPath);

        // Log the paths for debugging
        Log.d("RestoreFiles", "Restoring from: " + restoredFile.getAbsolutePath() + " to: " + originalFile.getAbsolutePath());

        try {
            // Check if the original file already exists
            if (originalFile.exists()) {
                Toast.makeText(requireContext(), "Original file already exists: " + originalFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                return;
            }

            // Attempt to rename the file back to its original location
            if (restoredFile.renameTo(originalFile)) {
                Toast.makeText(requireContext(), "Restored " + item.getFileName(), Toast.LENGTH_SHORT).show();
                deletedItems.remove(item);
                adapter.notifyDataSetChanged();

                // Delete the metadata file after restoring
                if (metadataFile.exists()) {
                    metadataFile.delete();
                }

            } else {
                Log.e("RestoreFiles", "Failed to restore " + item.getFileName());
                Toast.makeText(requireContext(), "Failed to restore " + item.getFileName(), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("RestoreFiles", "Error restoring file: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Error occurred while restoring file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }



    // Method to delete files from Trash permanently
    private void deleteDeletedFile(RecyclerItem item) {
        File trashFolder = new File(requireContext().getExternalFilesDir(null), TRASH_FOLDER_NAME);
        File fileToDelete = new File(trashFolder, item.getFileName());

        File metadataFolder = new File(requireContext().getExternalFilesDir(null), METADATA_FOLDER_NAME);
        File metadataFile = new File(metadataFolder, item.getFileName() + ".json");

        if (fileToDelete.delete()) {
           if (metadataFile.exists()){
               if (metadataFile.delete()) {
                   Log.d("Trash", "Deleted metadata for " + item.getFileName());
               } else {
                   Log.e("Trash", "Failed to delete metadata for " + item.getFileName());
               }
           }
            Toast.makeText(requireContext(), "Deleted " + item.getFileName(), Toast.LENGTH_SHORT).show();
            deletedItems.remove(item);
            adapter.notifyDataSetChanged();
        } else {
            Toast.makeText(requireContext(), "Failed to delete " + item.getFileName(), Toast.LENGTH_SHORT).show();
        }
    }
}
