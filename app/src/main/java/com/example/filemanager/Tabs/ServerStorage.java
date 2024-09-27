package com.example.filemanager.Tabs;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
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

    // Hostinger API endpoint (replace with your actual endpoint)
    private static final String HOSTINGER_API_URL = "https://skcalamba.scarlet2.io/android_api/hostinger_api.php";
    private static final String DOWNLOAD_URL = "https://skcalamba.scarlet2.io/android_api/public_html/myfolder/";
    private static final String FILE_DELETE_URL = "https://skcalamba.scarlet2.io/android_api/delete_file.php";

    // Define the folder name for internal storage
    private static final String DOWNLOAD_FOLDER_NAME = "MyDownloads";

    private static final String MY_FOLDER_PATH = "./android_api/public_html/myfolder/";

    private String currentPath = "";
    private Stack<String> folderStack;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout
        View view = inflater.inflate(R.layout.fragment_server_storage, container, false);

        // Initialize RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.serverRecyclerView);
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
                        // Handle delete
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
            loadFolder(currentPath); // Load the previous folder
        } else {
            // Handle the case when there are no previous folders (e.g., show a message)
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
        swipeRefreshLayout.setRefreshing(true);

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

                                // Add the file to the list
                                if (!name.equals("..") && !name.equals(".")){
                                    recyclerItems.add(new RecyclerItem(name, formatFileSize(size.length()), date, isDirectory));
                                }
                            }

                            adapter.notifyDataSetChanged(); // Notify adapter of new data
                        } else {
                            // Handle error
                            String errorMessage = jsonObject.optString("message", "Error fetching files");
                            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        // Handle JSON parsing error
                        Log.e("ServerStorage", "JSON parsing error: " + e.getMessage());
                        Toast.makeText(requireContext(), "Error parsing response", Toast.LENGTH_SHORT).show();
                    } finally {
                        // Stop refresh animation
                        swipeRefreshLayout.setRefreshing(false);
                    }
                },
                error -> {
                    // Handle network error
                    Log.e("ServerStorage", "Volley error: " + (error.getMessage() != null ? error.getMessage() : "Unknown error"));
                    Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false); // Stop refresh animation in case of error
                }) {
            // Add parameters to the request
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("auth_token", "bf4edef043130d19e11048aab68d4c512b62d2de1d000514b65410876e9a96f2"); // Replace with your actual token
                params.put("path", MY_FOLDER_PATH  + folderPath);
                return params;
            }
        };

        // Add the request to the queue
        queue.add(request);
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
                        currentDownloads.add(new RecyclerItem(title, "Downloading...", "", false)); // Assuming isDirectory is false for files
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
    // Method to format file size into a readable format
    private String formatFileSize(long size) {
        if (size <= 0) return "0 KB";
        String[] units = new String[]{"B", "KB", "MB", "GB"};
        int idx = (int) (Math.log(size) / Math.log(1024));
        return String.format("%.1f %s", size / Math.pow(1024, idx), units[idx]);
    }
}
