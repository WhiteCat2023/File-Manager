package com.example.filemanager.Tabs;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.filemanager.R;
import com.example.filemanager.Utils.ServerStorageAdapter;
import com.example.filemanager.Utils.RecyclerItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class ServerStorage extends Fragment {
    // Initialize variables
    private List<RecyclerItem> recyclerItems;
    private ServerStorageAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageView emptyStateImageView;
    private TextView emptyStateTextView;
    private RecyclerView recyclerView;

    // Hostinger API endpoint (replace with your actual endpoint)
    private static final String HOSTINGER_API_URL = "https://skcalamba.scarlet2.io/android_api/hostinger_api.php";
    private static final String DOWNLOAD_URL = "https://skcalamba.scarlet2.io/android_api/public_html/myfolder/";
    private static final String FILE_DELETE_URL = "https://skcalamba.scarlet2.io/android_api/delete_file.php";

    // Define the folder name for internal storage
    private static final String DOWNLOAD_FOLDER_NAME = "MyDownloads";

    private static final String MY_FOLDER_PATH = "./android_api/public_html/myfolder/";

    private String auth = "bf4edef043130d19e11048aab68d4c512b62d2de1d000514b65410876e9a96f2";
    private String currentPath = "";
    private Stack<String> folderStack;

    private static final String SHARED_PREF_NAME = "session";
    private static final String SESSION_EMAIL = "user_email";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout
        View view = inflater.inflate(R.layout.fragment_server_storage, container, false);

        emptyStateImageView = view.findViewById(R.id.externalImageView);
        emptyStateTextView = view.findViewById(R.id.externalTextView);

        folderStack = new Stack<>();
        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.serverRecyclerView);
        if (recyclerView == null) {
            Log.e("ServerStorage", "RecyclerView is null, check the ID in the layout.");
            return view; // Prevent further errors
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerItems = new ArrayList<>();
        adapter = new ServerStorageAdapter(recyclerItems,
                item -> {
                    if (item.isDirectory()) {
                        // If the clicked item is a folder, fetch its contents
                        currentPath += "/" + item.getFileName();
                        fetchFilesFromHostinger(currentPath);
                    }
                },
                new ServerStorageAdapter.OnItemActionListener() {
                    @Override
                    public void onDownloadClick(RecyclerItem item) {
                        downloadFile(item);  // Handle download
                    }

                    @Override
                    public void onDeleteClick(RecyclerItem item) {
                        new AlertDialog.Builder(requireContext())
                                .setTitle("Delete Confirmation")
                                .setMessage("Are you sure you want to delete this file?" + item.getFileName())
                                .setPositiveButton("Yes", (dialog, which) -> {
                                    fileDeletionRequest(item.getFileName());
                                }).setNegativeButton("No", null)
                                .show();
                    }
                });
        recyclerView.setAdapter(adapter);

        // Initialize SwipeRefreshLayout
        swipeRefreshLayout = view.findViewById(R.id.serverRefreshLayout);

        // Create the download folder in internal storage
        createDownloadFolder();

        // Fetch initial files
        fetchFilesFromHostinger("");
        fetchCurrentDownloads(); // Fetch current downloads

        // Set up refresh listener
        swipeRefreshLayout.setOnRefreshListener(() -> {
            fetchFilesFromHostinger(currentPath);
            fetchCurrentDownloads(); // Refresh current downloads
        });


        return view;
    }
    public void goBack() {
        if (!folderStack.isEmpty()) {
            currentPath = folderStack.pop(); // Go back to the previous folder
            loadFolder(currentPath);
            emptyStateImageView.setVisibility(View.VISIBLE);
            emptyStateTextView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE); // Load the previous folder
        } else {
            // Handle the case when there are no previous folders (e.g., show a message)
            Toast.makeText(requireContext(), "No more folders to go back to", Toast.LENGTH_SHORT).show();
        }
    }

    public void loadFolder(String folder) {
        // Only add to the stack if we're not going back to the root
        if (!currentPath.isEmpty()) {
            folderStack.push(currentPath);
        }
        currentPath = folder;

        // Fetch the contents of the new folder
        fetchFilesFromHostinger(currentPath); // Load the folder's contents
    }



    // Method to create the download folder in internal storage
    private void createDownloadFolder() {
        File directory = new File(requireContext().getExternalFilesDir(null), DOWNLOAD_FOLDER_NAME);
        if (!directory.exists()) {
            boolean isCreated = directory.mkdirs(); // Create the directory if it doesn't exist
            if (isCreated) {
                Log.d("ServerStorage", "Download folder created: " + directory.getAbsolutePath());
            } else {
                Log.e("ServerStorage", "Failed to create download folder.");
            }
        }
    }


    // Fetch files from the Hostinger API
    private void fetchFilesFromHostinger(String folderPath) {
        // Show refresh animation
        if (swipeRefreshLayout != null){
            swipeRefreshLayout.setRefreshing(true);
        }

        // Create a Volley request queue
        RequestQueue queue = Volley.newRequestQueue(requireContext());
        // Create a request to fetch files from the API
        StringRequest request = new StringRequest(Request.Method.POST, HOSTINGER_API_URL,
                response -> {
                    try {
                        // Parse the JSON response
                        Log.d("ServerResponse", "Response: " + response);
                        JSONObject jsonObject = new JSONObject(response);

                        // Check if the response is successful
                        if (jsonObject.getString("status").equals("success")) {
                            JSONArray filesArray = jsonObject.getJSONArray("files");

                            recyclerItems.clear(); // Clear old data

                            for (int i = 0; i < filesArray.length(); i++) {
                                JSONObject fileObject = filesArray.getJSONObject(i);
                                String name = fileObject.getString("name");
                                String size = fileObject.optString("size", "N/A"); // Add default if missing
                                String date = fileObject.optString("date", "N/A"); // Add default if missing
                                boolean isDirectory = fileObject.getBoolean("isDirectory");

                                String fullPath = folderPath.isEmpty() ? name : folderPath + "/" + name;

                                // Add the file to the list
                                if (!name.equals("..") && !name.equals(".")){
                                    recyclerItems.add(new RecyclerItem(name, formatFileSize(size.length()), date, isDirectory, fullPath));
                                }
                            }

                            adapter.notifyDataSetChanged(); // Notify adapter of new data
                            updateEmptyStateVisibility();
                        } else {
                            // Handle error
                            String errorMessage = jsonObject.optString("message", "Error fetching files");
                            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
                            emptyStateImageView.setVisibility(View.VISIBLE);
                            emptyStateImageView.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        }
                    } catch (JSONException e) {
                        // Handle JSON parsing error
                        handleError("Error parsing JSON" + e.getMessage());
                    } finally {
                        // Stop refresh animation
                        swipeRefreshLayout.setRefreshing(false);
                    }
                },
                error -> {
                    // Handle network error
                    swipeRefreshLayout.setRefreshing(false);
                    handleError("Network Error" + error.getMessage());
                }) {
            // Add parameters to the request
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("auth_token", auth); // Replace with your actual token
                params.put("path", MY_FOLDER_PATH  + folderPath);
                return params;
            }
        };

        // Add the request to the queue
        queue.add(request);
    }
    private void updateEmptyStateVisibility() {
        if (recyclerItems.isEmpty()) {
            emptyStateImageView.setVisibility(View.VISIBLE);
            emptyStateTextView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyStateImageView.setVisibility(View.GONE);
            emptyStateTextView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
        Log.d("Todo", "Item Count: " + recyclerItems.size());
    }

    private void handleError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        Log.e("Todo", message);
        emptyStateImageView.setVisibility(View.VISIBLE);
        emptyStateTextView.setVisibility(View.VISIBLE);
        emptyStateTextView.setText(message);
        recyclerView.setVisibility(View.GONE);
    }

    // Method to download the file
    private void downloadFile(RecyclerItem item) {
        if (!item.isDirectory()) {
            String fileUrl = DOWNLOAD_URL + item.getFileName();
            Log.d("ServerStorage", "Download URL: " + fileUrl); // Log the URL

            // Use the app-specific directory for downloads
            File downloadDir = new File(requireContext().getExternalFilesDir(null), DOWNLOAD_FOLDER_NAME);

            // Create a unique file name to avoid overwriting existing files
            File downloadFile = new File(downloadDir, item.getFileName());
            if (downloadFile.exists()) {
                String uniqueFileName = getUniqueFileName(downloadFile);
                downloadFile = new File(downloadDir, uniqueFileName);
            }

            // Set up the download request
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(fileUrl));
            request.setTitle(item.getFileName());
            request.setDescription("Downloading file...");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationUri(Uri.fromFile(downloadFile)); // Save to the download directory

            DownloadManager downloadManager = (DownloadManager) requireContext().getSystemService(Context.DOWNLOAD_SERVICE);
            if (downloadManager != null) {
                long downloadId = downloadManager.enqueue(request); // Store the download ID
                Log.d("ServerStorage", "Download started for: " + item.getFileName() + " at " + downloadFile.getAbsolutePath());
                Toast.makeText(requireContext(), "Downloading: " + downloadFile.getName(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Download Manager not available", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(requireContext(), "This is a directory, not a file.", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to create a unique file name if a file with the same name exists
    private String getUniqueFileName(File file) {
        String fileName = file.getName();
        String filePath = file.getParent();
        String uniqueFileName = fileName;
        int counter = 1;

        // Loop to create a unique filename
        while (file.exists()) {
            int dotIndex = fileName.lastIndexOf('.');
            String baseName = (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
            String extension = (dotIndex == -1) ? "" : fileName.substring(dotIndex);

            uniqueFileName = baseName + "_" + counter + extension; // Append counter to the base name
            file = new File(filePath, uniqueFileName);
            counter++;
        }

        return uniqueFileName; // Return the unique file name
    }

    // Method to fetch currently downloading files
    private void fetchCurrentDownloads() {
        DownloadManager downloadManager = (DownloadManager) requireContext().getSystemService(Context.DOWNLOAD_SERVICE);
        if (downloadManager != null) {
            // Query for active downloads
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterByStatus(DownloadManager.STATUS_RUNNING | DownloadManager.STATUS_PAUSED);
            Cursor cursor = downloadManager.query(query);

            // Clear the existing items in the list to avoid duplicates
            List<RecyclerItem> currentDownloads = new ArrayList<>();

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int titleIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TITLE);
                    int uriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_URI);
                    int idIndex = cursor.getColumnIndex(DownloadManager.COLUMN_ID);

                    // Check if the indices are valid
                    if (titleIndex != -1 && uriIndex != -1 && idIndex != -1) {
                        String title = cursor.getString(titleIndex);
                        String uri = cursor.getString(uriIndex);
                        long id = cursor.getLong(idIndex);

                        // Create a RecyclerItem for the current download
                        currentDownloads.add(new RecyclerItem(title, "Downloading...", "", false, ""));
                    } else {
                        Log.e("ServerStorage", "Column index not found. Check column names.");
                    }
                }
                cursor.close();
            } else {
                Log.e("ServerStorage", "Cursor is null. No current downloads found.");
            }

            // Add the current downloads to the recyclerItems list
            recyclerItems.addAll(currentDownloads);
            adapter.notifyDataSetChanged(); // Notify the adapter to refresh the view
        } else {
            Toast.makeText(requireContext(), "Download Manager not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void fileDeletionRequest(String fileName) {
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String userEmail = sharedPreferences.getString(SESSION_EMAIL, "0");

        String path = MY_FOLDER_PATH + currentPath + "/" + fileName;
        JSONObject itemParams = new JSONObject();

        try {
            itemParams.put("auth_token", auth);
            itemParams.put("file_name", fileName);
            itemParams.put("action", "request_permission");
            itemParams.put("current_path", path);
            itemParams.put("user_email", userEmail);
        } catch (JSONException e) {
            Log.e("ServerStorage", "Error creating JSON object for deletion request: " + e.getMessage());
            Toast.makeText(requireContext(), "Error preparing deletion request", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("ServerStorage", "Sending deletion request with parameters: " + itemParams.toString());

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                FILE_DELETE_URL,
                itemParams,
                response -> {
                    if (response != null) {
                        try {
                            Toast.makeText(requireContext(), "File deletion request sent", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Log.e("ServerStorage", "Error parsing response: " + e.getMessage());
                            Toast.makeText(requireContext(), "Error processing response", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(requireContext(), "No response from server", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("ServerStorage", "Error sending deletion request: " + error.getMessage());
                    Toast.makeText(requireContext(), "Error Sending Deletion Request", Toast.LENGTH_SHORT).show();
                }
        );

        queue.add(request);
    }

    // Method to format file size into a readable format
    private String formatFileSize(long size) {
        if (size <= 0) return "0 KB";
        String[] units = new String[]{"B", "KB", "MB", "GB"};
        int idx = (int) (Math.log(size) / Math.log(1024));
        return String.format("%.1f %s", size / Math.pow(1024, idx), units[idx]);
    }
}
