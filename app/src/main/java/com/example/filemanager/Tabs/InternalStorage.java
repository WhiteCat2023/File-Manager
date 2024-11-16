package com.example.filemanager.Tabs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.filemanager.ChooseDestinationActivity;
import com.example.filemanager.Fragments.Announcements;
import com.example.filemanager.MainActivity;
import com.example.filemanager.R;
import com.example.filemanager.Utils.InternalStorageAdapter;
import com.example.filemanager.Utils.RecyclerItem;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Stack;


public class InternalStorage extends Fragment {

    private static final String TRASH_FOLDER_NAME = "Trash";
    private RecyclerView recyclerView;
    private List<RecyclerItem> recyclerItems;
    private InternalStorageAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageView emptyStateImageView;
    private TextView emptyStateTextView;
    // Define the folder name for internal storage
    private static final String DOWNLOAD_FOLDER_NAME = "MyDownloads";
    private static final int REQUEST_CODE_COPY_TO = 0;
    private File currentDirectory;
    private List<RecyclerItem> deletedItems;
    private LinearLayout breadcrumbContainer;

    private TextView newLocalFolderTextView, importFilesTexView;
    private FloatingActionButton newLocalFolder, fabInternal, importFiles;
    private Boolean isAllVisible;

    private Stack<String> folderStack;

    private ActivityResultLauncher<Intent> filePickerLauncher;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout
        View view = inflater.inflate(R.layout.fragment_internal_storage, container, false);

        emptyStateImageView = view.findViewById(R.id.internalImageView);
        emptyStateTextView = view.findViewById(R.id.internalTextView);

        breadcrumbContainer = view.findViewById(R.id.breadcrumb_container);

        //Fab
        newLocalFolder = view.findViewById(R.id.newLocalFolder);
        importFiles = view.findViewById(R.id.importFiles);
        newLocalFolderTextView = view.findViewById(R.id.newLocalFolderTextView);
        importFilesTexView = view.findViewById(R.id.importTextView);
        fabInternal = view.findViewById(R.id.fabInternal);

        isAllVisible = false;

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.internalRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerItems = new ArrayList<>();

        folderStack = new Stack<>();
        // Register the file picker launcher
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        if (result.getData() != null) {
                            handleSelectedFiles(result.getData());
                        }
                    }
                }
        );

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
                    public void onDeleteClick(RecyclerItem item) {
                        // Handle delete
                        deleteInternalFile(item.getFileName());
                    }

                });
        //@Override
//                    public void onMoveToClick(RecyclerItem item) {
//
//                    }
//
//                    @Override
//                    public void onCopyToClick(RecyclerItem item) {
//                        copyTo(item);
//                    }
        recyclerView.setAdapter(adapter);

        // Initialize SwipeRefreshLayout
        swipeRefreshLayout = view.findViewById(R.id.internalRefreshLayout);

        // Load files from internal storage
        currentDirectory = new File(requireContext().getExternalFilesDir(null), DOWNLOAD_FOLDER_NAME);
        loadFilesFromDirectory(currentDirectory);
        updateBreadcrumbs();



        // Set up refresh listener
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadFilesFromDirectory(currentDirectory);
            swipeRefreshLayout.setRefreshing(false);
        });
        fabInternal.setOnClickListener(v -> {
            if (!isAllVisible){
                newLocalFolder.show();
                newLocalFolderTextView.setVisibility(View.VISIBLE);
                importFiles.show();
                importFilesTexView.setVisibility(View.VISIBLE);
                isAllVisible = true;
                // Trigger folder creation
                newLocalFolder.setOnClickListener(v1 -> {
                    createFolderDialog();  // Open dialog to create a folder
                });
                importFiles.setOnClickListener(v1 -> {
                    importFileOrFolder();
                });
            }else{
                newLocalFolder.hide();
                importFiles.hide();
                importFilesTexView.setVisibility(View.GONE);
                newLocalFolderTextView.setVisibility(View.GONE);
                isAllVisible = false;
            }
        });

        return view;
    }

    private void copyTo(RecyclerItem item) {
        Intent intent = new Intent(requireContext(), ChooseDestinationActivity.class);
        intent.putExtra("itemName", item.getFileName());
        startActivity(intent);
    }



    // Method to open file picker for file or folder import
    private void importFileOrFolder() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // Enable multiple file selection
        filePickerLauncher.launch(intent);
    }
    // Method to handle the selected files or folders
    private void handleSelectedFiles(Intent data) {
        if (data != null) {
            if (data.getClipData() != null) {
                // Multiple files selected
                ClipData clipData = data.getClipData();
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    Uri fileUri = clipData.getItemAt(i).getUri();
                    importSingleFile(fileUri);
                }
            } else if (data.getData() != null) {
                // Single file selected
                Uri fileUri = data.getData();
                importSingleFile(fileUri);
            }
        }
    }
    // Method to handle a single file import
    private void importSingleFile(Uri uri) {
        try {
            // Get file name and copy the file to internal storage
            String fileName = getFileNameFromUri(uri);
            File destinationFile = new File(currentDirectory, fileName);

            // Copy the file to internal storage
            copyUriToFile(uri, destinationFile);

            // Refresh the file list
            loadFilesFromDirectory(currentDirectory);
            updateBreadcrumbs();

            Toast.makeText(requireContext(), "File imported: " + fileName, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Failed to import file", Toast.LENGTH_SHORT).show();
        }
    }
    // Helper method to get file name from Uri
    private String getFileNameFromUri(Uri uri) {
        Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            String name = cursor.getString(nameIndex);
            cursor.close();
            return name;
        }
        return "unknown_file";
    }
    // Helper method to copy Uri data to a file
    private void copyUriToFile(Uri uri, File destinationFile) throws IOException {
        try (InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
             OutputStream outputStream = new FileOutputStream(destinationFile)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        }
    }
    // Create a dialog for folder name input and create the folder in the current directory
    private void createFolderDialog() {
        // Inflate a custom view with an input for the folder name
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.alertdialog_input_folder, null);
        TextInputEditText folderNameInput = view.findViewById(R.id.folderNameInput);

        // Create an alert dialog to input the folder name
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext())
                .setTitle("Create New Folder")
                .setView(view)
                .setPositiveButton("Create", (dialog, which) -> {
                    String folderName = folderNameInput.getText().toString().trim();
                    if (!folderName.isEmpty()) {
                        createFolderInCurrentDirectory(folderName);  // Create the folder
                    } else {
                        Toast.makeText(requireContext(), "Please enter a folder name", Toast.LENGTH_SHORT).show();
                    }
                }).setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }
    // Method to create a folder inside the current directory
    private void createFolderInCurrentDirectory(String folderName) {
        // Define the new folder path
        File newFolder = new File(currentDirectory, folderName);

        // Check if the folder already exists
        if (!newFolder.exists()) {
            boolean isCreated = newFolder.mkdir();  // Attempt to create the new folder
            if (isCreated) {
                Toast.makeText(requireContext(), "Folder created successfully", Toast.LENGTH_SHORT).show();
                loadFilesFromDirectory(currentDirectory);  // Refresh the current directory view
            } else {
                Toast.makeText(requireContext(), "Failed to create folder", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(requireContext(), "Folder already exists", Toast.LENGTH_SHORT).show();
        }
    }
    private void updateBreadcrumbs() {
        breadcrumbContainer.removeAllViews(); // Clear the existing breadcrumbs

        // Split the current path into folders
        String[] folders = currentDirectory.getAbsolutePath().split("/");

        // Find the index of the "MyDownloads" folder
        int startIndex = 0;
        for (int i = 0; i < folders.length; i++) {
            if (folders[i].equals(DOWNLOAD_FOLDER_NAME)) {
                startIndex = i; // Start breadcrumb from "MyDownloads"
                break;
            }
        }

        // Create breadcrumbs for each folder starting from "MyDownloads"
        StringBuilder currentPathBuilder = new StringBuilder();
        for (int i = startIndex; i < folders.length; i++) {
            final int index = i;
            String folderName = folders[i];

            if (!folderName.isEmpty()) {
                currentPathBuilder.append("/").append(folderName);

                // Create a TextView for each folder
                TextView breadcrumbTextView = new TextView(requireContext());
                breadcrumbTextView.setText(folderName);
                breadcrumbTextView.setPadding(8, 20, 8, 20); // Add some padding for better visibility
                breadcrumbTextView.setTextSize(16);
                breadcrumbTextView.setTextColor(getResources().getColor(R.color.purple));

                // Set the TextView to be clickable
                breadcrumbTextView.setOnClickListener(v -> {
                    // Build the path up to this breadcrumb
                    File targetPath = new File("/"); // Start from root and build path
                    for (int j = 0; j <= index; j++) {
                        targetPath = new File(targetPath, folders[j]);
                    }
                    navigateToBreadcrumb(targetPath);
                });

                // Add the TextView to the breadcrumb container
                breadcrumbContainer.addView(breadcrumbTextView);

                // Add a ">" separator between breadcrumb items (except the last one)
                if (i < folders.length - 1 && !folders[i + 1].equals(DOWNLOAD_FOLDER_NAME)) {
                    TextView separator = new TextView(requireContext());
                    separator.setText(" > ");
                    separator.setPadding(4, 8, 4, 8);
                    breadcrumbContainer.addView(separator);
                }
            }
        }
    }
    // Method to navigate to a folder when a breadcrumb is clicked
    private void navigateToBreadcrumb(File targetDirectory) {
        if (targetDirectory.exists() && targetDirectory.isDirectory()) {
            // Update the current directory
            currentDirectory = targetDirectory;

            // Clear folderStack and rebuild it up to the clicked breadcrumb
            folderStack.clear();
            String[] folders = currentDirectory.getAbsolutePath().split("/");
            for (int i = 0; i < folders.length - 1; i++) { // Rebuild the stack excluding the current folder
                folderStack.push(currentDirectory.getParentFile().getAbsolutePath());
            }

            // Load files and update breadcrumbs
            loadFilesFromDirectory(currentDirectory);
            updateBreadcrumbs();
        } else {
            Toast.makeText(requireContext(), "Unable to navigate to folder.", Toast.LENGTH_SHORT).show();
        }
    }
    // Method to navigate into a directory
    private void openDirectory(String directoryName) {
        File newDirectory = new File(currentDirectory, directoryName);
        if (newDirectory.exists() && newDirectory.isDirectory()) {
            folderStack.push(currentDirectory.getAbsolutePath());
            currentDirectory = newDirectory;
            loadFilesFromDirectory(currentDirectory);
            updateBreadcrumbs();
        } else {
            AlertDialog alertDialog = new AlertDialog.Builder(requireContext())
                    .setTitle("Error")
                    .setMessage("Cannot open directory.")
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .create();
            alertDialog.show();
            Log.e("InternalStorage", "Cannot open directory.");
        }
    }
    // Method to load files and directories from the specified directory
//    private void loadFilesFromDirectory(File directory) {
//        recyclerItems.clear();
//
//        if (directory.exists() && directory.isDirectory()) {
//            File[] files = directory.listFiles();
//            if (files != null && files.length > 0) {
//                for (File file : files) {
//                    emptyStateImageView.setVisibility(View.GONE);
//                    emptyStateTextView.setVisibility(View.GONE);
//                    recyclerView.setVisibility(View.VISIBLE);
//                    boolean isDirectory = file.isDirectory();
//                    recyclerItems.add(new RecyclerItem(file.getName(), formatFileSize(file.length()), "", isDirectory, file.getAbsolutePath()));
//
//                }
//
//                // Notify adapter of new data
//                adapter.notifyDataSetChanged();
//            } else {
//                Log.e("InternalStorage", "No files found in the directory.");
//                emptyStateImageView.setVisibility(View.VISIBLE);
//                emptyStateTextView.setVisibility(View.VISIBLE);
//                recyclerView.setVisibility(View.GONE);
//                adapter.notifyDataSetChanged();
//            }
//        } else {
//            emptyStateImageView.setVisibility(View.VISIBLE);
//            emptyStateTextView.setVisibility(View.VISIBLE);
//            recyclerView.setVisibility(View.GONE);
//            Log.e("InternalStorage", "Directory does not exist.");
//            Toast.makeText(requireContext(), "Directory does not exist.", Toast.LENGTH_SHORT).show();
//        }
//    }
    // Method to format file size into a readable format

    private void loadFilesFromDirectory(File directory) {
        recyclerItems.clear();

        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null && files.length > 0) {
                for (File file : files) {
                    emptyStateImageView.setVisibility(View.GONE);
                    emptyStateTextView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);

                    // Check if it's a directory or file
                    boolean isDirectory = file.isDirectory();

                    // Get the last modified date of the file/folder
                    long lastModified = file.lastModified();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a"); // Format the date
                    String formattedDate = dateFormat.format(new Date(lastModified));

                    // Add the file to recyclerItems
                    recyclerItems.add(new RecyclerItem(
                            file.getName(),
                            formatFileSize(file.length()),
                            formattedDate,  // Add the formatted date here
                            isDirectory,
                            file.getAbsolutePath()
                    ));
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
    // Method to move a file to the Trash folder along with its original path
    private void deleteInternalFile(String fileName) {
        File file = new File(currentDirectory, fileName);

        if (file.exists()) {

            Log.d("Move to trash", "Moving " + file.getAbsolutePath() + " to Trash");

            // Move the file to the Trash folder
            File trashFolder = new File(requireContext().getExternalFilesDir(null), "Trash");
            if (!trashFolder.exists()) {
                boolean folderCreated = trashFolder.mkdir();  // Create Trash folder if it doesn't exist
                if (!folderCreated) {
                    Toast.makeText(requireContext(), "Failed to create Trash folder.", Toast.LENGTH_SHORT).show();
                    return; // Stop if the Trash folder couldn't be created
                }
            }

            // Create a new File object for the destination in the Trash folder
            File destination = new File(trashFolder, file.getName());

            // Create the Metadata folder if it doesn't exist
            File metadataFolder = new File(requireContext().getExternalFilesDir(null), "Metadata");
            if (!metadataFolder.exists()) {
                boolean metadataFolderCreated = metadataFolder.mkdir();  // Create Metadata folder if it doesn't exist
                if (!metadataFolderCreated) {
                    Toast.makeText(requireContext(), "Failed to create Metadata folder.", Toast.LENGTH_SHORT).show();
                    return; // Stop if the Metadata folder couldn't be created
                }
            }

            // Store the original file path in a metadata file inside the Metadata folder
            File metadataFile = new File(metadataFolder, file.getName() + ".json");
            try {
                FileWriter writer = new FileWriter(metadataFile);
                writer.write("{\"originalPath\":\"" + file.getAbsolutePath() + "\"}");
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(requireContext(), "Failed to save metadata.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Attempt to move the file to the Trash folder
            boolean moved = file.renameTo(destination);
            if (moved) {
                Toast.makeText(requireContext(), "File moved to Trash.", Toast.LENGTH_SHORT).show();
                loadFilesFromDirectory(currentDirectory); // Reload files after moving
            } else {
                Toast.makeText(requireContext(), "Failed to move file to Trash.", Toast.LENGTH_SHORT).show();
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
            updateBreadcrumbs();
        }else{
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
            AlertDialog alertDialog = new AlertDialog.Builder(requireContext())
                    .setTitle("Error")
                    .setMessage("No previous folders to go back to.")
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .create();
            alertDialog.show();
            // Handle the case when there are no previous folders (e.g., show a message)
            Log.e("InternalStorage", "No previous folders to go back to.");
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, new Announcements()) // Replace with your fragment container ID
                    .addToBackStack(null) // Optional: Add to back stack if needed
                    .commit();
        }
    }
//    public void goBack() {
//        if (!folderStack.isEmpty()) {
//           String previousPath = folderStack.pop();
//           currentDirectory = new File(previousPath);
//           loadFilesFromDirectory(currentDirectory);
//           updateBreadcrumbs();
//            requireActivity().getOnBackPressedDispatcher().addCallback(this.requireActivity(),
//                    new OnBackPressedCallback(true) {
//                        @Override
//                        public void handleOnBackPressed() {
//                           goBack();
//                        }
//                    });
//        } else {
////            AlertDialog alertDialog = new AlertDialog.Builder(requireContext())
////                    .setTitle("Error")
////                    .setMessage("No previous folders to go back to.")
////                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
////                    .create();
////            alertDialog.show();
////            // Handle the case when there are no previous folders (e.g., show a message)
////            Log.e("InternalStorage", "No previous folders to go back to.");
//            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
//            fragmentManager.beginTransaction()
//                    .replace(R.id.fragment_container, new Announcements()) // Replace with your fragment container ID
//                    .addToBackStack(null) // Optional: Add to back stack if needed
//                    .commit();
//
//        }
//
//
//    }
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
