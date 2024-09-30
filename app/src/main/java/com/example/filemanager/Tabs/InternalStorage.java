package com.example.filemanager.Tabs;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.filemanager.R;
import com.example.filemanager.Utils.InternalStorageAdapter;
import com.example.filemanager.Utils.RecyclerItem;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class InternalStorage extends Fragment {

    private RecyclerView recyclerView;
    private List<RecyclerItem> recyclerItems;
    private InternalStorageAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageView emptyStateImageView;
    private TextView emptyStateTextView;
    // Define the folder name for internal storage
    private static final String DOWNLOAD_FOLDER_NAME = "MyDownloads";
    private File currentDirectory;

    private Stack<String> folderStack;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout
        View view = inflater.inflate(R.layout.fragment_internal_storage, container, false);

        emptyStateImageView = view.findViewById(R.id.internalImageView);
        emptyStateTextView = view.findViewById(R.id.internalTextView);
        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.internalRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerItems = new ArrayList<>();

        folderStack = new Stack<>();

        // Initialize adapter
        adapter = new InternalStorageAdapter(recyclerItems,
                item -> {
                    // Check if the item is a directory or a file
                    if (item.isDirectory()) {
                        openDirectory(item.getFileName()); // Call openDirectory if it's a directory
                    } else {
                        openFile(item); // Call openFile if it's a file
                    }
                },
                new InternalStorageAdapter.OnItemActionListener() {
                    @Override
                    public void onRenameClick(RecyclerItem item) {
                        //Handle Rename
                        renameFile(item.getFileName());
                    }

                    @Override
                    public void onMoveToClick(RecyclerItem item) {

                    }

                    @Override
                    public void onDeleteClick(RecyclerItem item) {
                        // Handle delete
                        deleteInternalFile(item.getFileName());
                    }
                });
        recyclerView.setAdapter(adapter);

        // Initialize SwipeRefreshLayout
        swipeRefreshLayout = view.findViewById(R.id.internalRefreshLayout);

        // Load files from internal storage
        currentDirectory = new File(requireContext().getExternalFilesDir(null), DOWNLOAD_FOLDER_NAME);
        loadFilesFromDirectory(currentDirectory);


        // Set up refresh listener
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadFilesFromDirectory(currentDirectory);
            swipeRefreshLayout.setRefreshing(false);
        });

        return view;
    }
    // Method to navigate into a directory
    private void openDirectory(String directoryName) {
        File newDirectory = new File(currentDirectory, directoryName);
        if (newDirectory.exists() && newDirectory.isDirectory()) {
            folderStack.push(currentDirectory.getAbsolutePath());
            currentDirectory = newDirectory;
            loadFilesFromDirectory(currentDirectory);
        } else {
            Toast.makeText(requireContext(), "Cannot open directory.", Toast.LENGTH_SHORT).show();
        }
    }
    // Method to load files and directories from the specified directory
    private void loadFilesFromDirectory(File directory) {
        recyclerItems.clear();

        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null && files.length > 0) {
                for (File file : files) {
                    emptyStateImageView.setVisibility(View.GONE);
                    emptyStateTextView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    boolean isDirectory = file.isDirectory();
                    recyclerItems.add(new RecyclerItem(file.getName(), formatFileSize(file.length()), "", isDirectory));

                }

                // Notify adapter of new data
                adapter.notifyDataSetChanged();
            } else {
                Log.e("InternalStorage", "No files found in the directory.");
                emptyStateImageView.setVisibility(View.VISIBLE);
                emptyStateTextView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                adapter.notifyDataSetChanged();
            }
        } else {
            emptyStateImageView.setVisibility(View.VISIBLE);
            emptyStateTextView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            Log.e("InternalStorage", "Directory does not exist.");
            Toast.makeText(requireContext(), "Directory does not exist.", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to format file size into a readable format
    private String formatFileSize(long size) {
        if (size <= 0) return "0 KB";
        String[] units = new String[]{"B", "KB", "MB", "GB"};
        int idx = (int) (Math.log(size) / Math.log(1024));
        return String.format("%.1f %s", size / Math.pow(1024, idx), units[idx]);
    }

    // Method to open the file with the appropriate viewer
    private void openFile(RecyclerItem item) {
        if (item == null || item.getFileName() == null) {
            Toast.makeText(requireContext(), "Invalid file.", Toast.LENGTH_SHORT).show();
            return;
        }

        String fileName = item.getFileName();
        String fileExtension = getFileExtension(fileName);

        // Determine the appropriate file reader based on the file type
        switch (fileExtension) {
            case "pdf":
                openFileReader("application/pdf", fileName);
                break;
            case "txt":
                openFileReader("text/plain", fileName);
                break;
            case "doc":
            case "docx":
                openFileReader("application/msword", fileName);
                break;
            case "xls":
            case "xlsx":
                openFileReader("application/vnd.ms-excel", fileName);
                break;
            case "ppt":
            case "pptx":
                openFileReader("application/vnd.ms-powerpoint", fileName);
                break;
            case "jpg":
            case "jpeg":
            case "png":
                openFileReader("image/*", fileName);
                break;
            case "mp3":
                openFileReader("audio/*", fileName);
                break;
            case "mp4":
                openFileReader("video/*", fileName);
                break;
            default:
                Toast.makeText(requireContext(), "No application available to view this file type.", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    // Method to get the file extension
    private String getFileExtension(String fileName) {
        if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) {
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        }
        return "";
    }

    // Method to open the file with the appropriate viewer using FileProvider
    private void openFileReader(String mimeType, String fileName) {
        File file = new File(requireContext().getExternalFilesDir(DOWNLOAD_FOLDER_NAME), fileName);
        if (file.exists()) {
            Uri uri = FileProvider.getUriForFile(requireContext(), requireContext().getPackageName() + ".provider", file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, mimeType);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Check if there is an application to handle the intent
            if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(requireContext(), "No application available to view this file.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(requireContext(), "File does not exist.", Toast.LENGTH_SHORT).show();
        }
    }
    private void deleteInternalFile(String fileName) {
        File file = new File(currentDirectory, fileName);

        if (file.exists()) {
            boolean deleted = file.delete();
            if (deleted) {
                Toast.makeText(requireContext(), "File deleted successfully.", Toast.LENGTH_SHORT).show();
                loadFilesFromDirectory(currentDirectory); // Update here
            } else {
                Toast.makeText(requireContext(), "Failed to delete file.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(requireContext(), "File does not exist.", Toast.LENGTH_SHORT).show();
        }
    }

    private void renameFile(String filename){
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.alertdialog_input, null);
        TextInputEditText rename = view.findViewById(R.id.renameInternalItem);
        rename.setText(filename);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext())
                .setTitle("Rename File")
                .setView(view)
                .setPositiveButton("Rename", (dialog, which) -> {
                    String newFileName = rename.getText().toString();
                    if (!newFileName.isEmpty()){
                        renameInternalFile(filename, newFileName);
                    }else {
                        Toast.makeText(requireContext(), "Please enter a new name.", Toast.LENGTH_SHORT).show();
                    }
                }).setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                });
        builder.show();
    }
    private void renameInternalFile(String oldName, String newName) {
        File oldFile = new File(currentDirectory, oldName);
        File newFile = new File(currentDirectory, newName);

        if (oldFile.exists()) {
            boolean renamed = oldFile.renameTo(newFile);
            if (renamed) {
                Toast.makeText(requireContext(), "File renamed successfully.", Toast.LENGTH_SHORT).show();
                loadFilesFromDirectory(currentDirectory); // Update here
            } else {
                Toast.makeText(requireContext(), "Failed to rename file.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(requireContext(), "File does not exist.", Toast.LENGTH_SHORT).show();
        }
    }
    public void goBack() {
        if (!folderStack.isEmpty()) {
           String previousPath = folderStack.pop();
           currentDirectory = new File(previousPath);
           loadFilesFromDirectory(currentDirectory);
        } else {
            // Handle the case when there are no previous folders (e.g., show a message)
            Log.e("InternalStorage", "No previous folders to go back to.");
        }
    }
    @Override
    public void onResume() {
        super.onResume();

        // Handle back press specifically in the fragment
        requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (!folderStack.isEmpty()) {
                    goBack();
                } else {
                    requireActivity().onBackPressed();
                }
            }
        });
    }

}
