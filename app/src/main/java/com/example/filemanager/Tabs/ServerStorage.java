package com.example.filemanager.Tabs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.filemanager.R;
import com.example.filemanager.Utils.ServerStorageAdapter;
import com.example.filemanager.Utils.RecyclerItem;
import com.example.filemanager.Utils.UploadItem;
import com.example.filemanager.Utils.VolleyMultipartRequest;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
    private LinearLayout breadcrumbContainerServer;
    private HorizontalScrollView horizontalScrollView;

    private FloatingActionButton addFabServer, uploadFilesFab, newServerFolderFab ;
    private TextView uploadFilesFabTextView, newServerFolderFabTextView;
    private Boolean isAllVisible;



    // Hostinger API endpoint (replace with your actual endpoint)
    private static final String HOSTINGER_API_URL = "https://skcalamba.scarlet2.io/android_api/hostinger_api.php";
    private static final String DOWNLOAD_URL = "https://skcalamba.scarlet2.io/android_api/uploads/";
    private static final String FILE_DELETE_URL = "https://skcalamba.scarlet2.io/android_api/delete_file.php";
    private static final String CREATE_FOLDER_API = "https://skcalamba.scarlet2.io/createFolder.php";

    // Define the folder name for internal storage
    private static final String DOWNLOAD_FOLDER_NAME = "MyDownloads";

    private static final String MY_FOLDER_PATH = "./android_api/uploads/";
    private static final String ROOT_FOLDER_PATH = "./";

    private String auth = "bf4edef043130d19e11048aab68d4c512b62d2de1d000514b65410876e9a96f2";
    private String uploadAuth = "Ramsey";
    private String currentPath = "";
    private Stack<String> folderStack;

    private static final String SHARED_PREF_NAME = "session";
    private static final String SESSION_TOKEN = "user_token";
    private static final String SERVER_STORAGE_CURRENT_PATH = "server_storage_current_path";

    private static final int PICK_FILE_REQUEST = 0;
    private static final String UPLOAD_URL = "https://skcalamba.scarlet2.io/android_api/upload.php";

    private boolean isUploading = false;
    private ProgressDialog progressDialog;
    private RequestQueue requestQueue;

    private List<UploadItem> selectedUploadItems;
    private List<Uri> uploadItemQueue;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout
        View view = inflater.inflate(R.layout.fragment_server_storage, container, false);

        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setCancelable(false);

        selectedUploadItems = new ArrayList<>();
        uploadItemQueue = new ArrayList<>();

        emptyStateImageView = view.findViewById(R.id.externalImageView);
        emptyStateTextView = view.findViewById(R.id.externalTextView);

        horizontalScrollView = view.findViewById(R.id.serverHorizontalScrollView);

        breadcrumbContainerServer = view.findViewById(R.id.breadcrumb_container_server);
        requestQueue = Volley.newRequestQueue(this.requireContext());

//        Fabs
        addFabServer = view.findViewById(R.id.fabServer);
        uploadFilesFab = view.findViewById(R.id.uploadFiles);
        newServerFolderFab = view.findViewById(R.id.newServerFolder);

        uploadFilesFabTextView = view.findViewById(R.id.uploadFilesTextView);
        newServerFolderFabTextView = view.findViewById(R.id.newServerFolderTextView);

        isAllVisible = false;


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
                        folderStack.push(currentPath); // Save current path
                        currentPath = currentPath.isEmpty() ? item.getFileName() : currentPath + "/" + item.getFileName(); // Update current path
                        fetchFilesFromHostinger(currentPath); // Fetch new folder contents
                    }else{
                        previewFile(item);
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
                                    View views = LayoutInflater.from(requireContext()).inflate(R.layout.alertdialog_input_reason, null);
                                    TextInputEditText reasonInput = views.findViewById(R.id.reasonInput);
                                    new AlertDialog.Builder(requireContext())
                                            .setTitle("Reason")
                                            .setView(views)
                                            .setPositiveButton("Create", (dialogs, whichs) -> {
                                                String reason = reasonInput.getText().toString().trim();
                                                if (!reason.isEmpty()) {
                                                    fileDeletionRequest(item.getFileName(), reason);
                                                }else{
                                                    Toast.makeText(requireContext(), "Please enter a reason", Toast.LENGTH_SHORT).show();
                                                }
                                            })
                                            .setNegativeButton("Cancel", (dialogs, whichs) -> dialogs.dismiss())
                                            .show();

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

        addFabServer.setOnClickListener(v -> {
            if (!isAllVisible){
                uploadFilesFab.show();
                newServerFolderFab.show();
                uploadFilesFabTextView.setVisibility(View.VISIBLE);
                newServerFolderFabTextView.setVisibility(View.VISIBLE);
                isAllVisible = true;

                uploadFilesFab.setOnClickListener(v1 -> {
                   openFileChooser();
                });
                newServerFolderFab.setOnClickListener(v1 -> {

                    View views = LayoutInflater.from(this.getContext()).inflate(R.layout.alertdialog_input_folder, null);
                    TextInputEditText newFolderName = views.findViewById(R.id.folderNameInput);
                    AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext())
                        .setTitle("Create Folder on Server")
                        .setView(views)
                        .setPositiveButton("Create", (dialog, which) -> {
                            String folderName = newFolderName.getText().toString().trim();
                            if (!folderName.isEmpty()) {
                                createFolder(folderName);
                            } else {
                                Toast.makeText(this.getContext(), "Please enter a folder name", Toast.LENGTH_SHORT).show();
                            }
                        }).setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
                    builder.show();
                });
            }else{
                uploadFilesFab.hide();
                newServerFolderFab.hide();
                uploadFilesFabTextView.setVisibility(View.GONE);
                newServerFolderFabTextView.setVisibility(View.GONE);
                isAllVisible = false;
            }
        });


        return view;
    }

    // Handle file selection
    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Select File"), PICK_FILE_REQUEST);
    }
    // Handle result of file selection
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    for (int i = 0; i < count; i++) {
                        Uri fileUri = data.getClipData().getItemAt(i).getUri();
                        addFileToUploadList(fileUri);
                    }
                } else if (data.getData() != null) {
                    Uri fileUri = data.getData();
                    addFileToUploadList(fileUri);
                }
            }
            // After adding files to the upload list, trigger the upload
            if (!uploadItemQueue.isEmpty()) {
                uploadFiles(uploadItemQueue); // Call uploadFiles here
            } else {
                Toast.makeText(requireContext(), "No files selected for upload", Toast.LENGTH_SHORT).show();
            }
        }
    }
    //    // Upload multiple files to the server
    //    private void uploadFiles(List<Uri> uris) {
    //        if (isUploading) return;
    //
    //        isUploading = true;
    //        progressDialog.setTitle("Uploading Files");
    //        progressDialog.setMessage("Please wait...");
    //        progressDialog.show();
    //
    //        VolleyMultipartRequest uploadRequest = new VolleyMultipartRequest(Request.Method.POST, UPLOAD_URL,
    //                response -> {
    //                    progressDialog.dismiss();
    //                    String responseString = new String(response.data);
    //                    Log.d("Upload", "Response: " + responseString);
    //
    //                    try {
    //                        JSONObject jsonResponse = new JSONObject(responseString);
    //                        String status = jsonResponse.getString("status");
    //                        String message = jsonResponse.getString("message");
    //                        if (status.equals("success")) {
    //                            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    //                        } else {
    //                            Toast.makeText(requireContext(), "Upload Failed: " + message, Toast.LENGTH_SHORT).show();
    //                        }
    //                    } catch (JSONException e) {
    //                        Log.e("Upload", "JSON Error: " + e.getMessage());
    //                    }
    //
    //                    uploadItemQueue.clear();
    //                    selectedUploadItems.clear();
    //                    isUploading = false;
    //                },
    //                error -> {
    //                    progressDialog.dismiss();
    //                    Toast.makeText(requireContext(), "Upload Failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
    //                    Log.e("Upload", "Error: " + error.getMessage());
    //                    isUploading = false;
    //                }) {
    //
    //            @Override
    //            protected Map<String, String> getParams() {
    //                Map<String, String> params = new HashMap<>();
    //                params.put("folder_path", currentPath.isEmpty() ? MY_FOLDER_PATH : currentPath);
    //                params.put("auth_token", uploadAuth);
    //                return params;
    //            }
    //
    //            @Override
    //            protected Map<String, DataPart> getByteData() {
    //                Map<String, DataPart> params = new HashMap<>();
    //                for (Uri uri : uris) {
    //                    String fileName = getFileName(uri);
    //                    try (InputStream inputStream = requireContext().getContentResolver().openInputStream(uri)) {
    //                        if (inputStream != null) {
    //                            byte[] bytes = new byte[inputStream.available()];
    //                            inputStream.read(bytes);
    //                            Log.d("Upload", "Uploading file: " + fileName + " Size: " + bytes.length + " bytes");
    //                            params.put("file[]", new DataPart(fileName, bytes));  // Use "file[]" as the key
    //                        } else {
    //                            Log.e("Upload", "InputStream is null for URI: " + uri.toString());
    //                        }
    //                    } catch (Exception e) {
    //                        Log.e("Upload", "Error reading file: " + e.getMessage());
    //                    }
    //                }
    //                return params;
    //            }
    //
    //        };
    //
    //        uploadRequest.setRetryPolicy(new DefaultRetryPolicy(
    //                30000,
    //                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
    //                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
    //        ));
    //
    //        requestQueue.add(uploadRequest);
    //    }
    private void uploadFiles(List<Uri> uris) {
        if (isUploading) return;

        isUploading = true;
        progressDialog.setTitle("Uploading Files");
        progressDialog.setMessage("Please wait...");
        progressDialog.show();

        // Prepare the JSON object to send
        JSONObject jsonRequest = new JSONObject();
        JSONArray filesArray = new JSONArray();  // To hold the file data

        try {
            jsonRequest.put("folder_path", currentPath.isEmpty() ? ROOT_FOLDER_PATH : currentPath);
            jsonRequest.put("auth_token", uploadAuth);

            for (Uri uri : uris) {
                String fileName = getFileName(uri);

                // Convert file to Base64
                try (InputStream inputStream = requireContext().getContentResolver().openInputStream(uri)) {
                    if (inputStream != null) {
                        byte[] bytes = new byte[inputStream.available()];
                        inputStream.read(bytes);

                        // Encode file to Base64 string
                        String encodedFile = Base64.encodeToString(bytes, Base64.DEFAULT);

                        // Create JSON object for each file
                        JSONObject fileObject = new JSONObject();
                        fileObject.put("file_name", fileName);
                        fileObject.put("file_data", encodedFile);

                        // Add file object to files array
                        filesArray.put(fileObject);

                        Log.d("Upload", "Prepared file: " + fileName + " Size: " + bytes.length + " bytes");
                    } else {
                        Log.e("Upload", "InputStream is null for URI: " + uri.toString());
                    }
                } catch (Exception e) {
                    Log.e("Upload", "Error reading file: " + e.getMessage());
                }
            }

            // Attach files array to the main JSON request
            jsonRequest.put("files", filesArray);

        } catch (JSONException e) {
            Log.e("Upload", "JSON Error: " + e.getMessage());
        }

        // Create a JsonObjectRequest to send the request
        JsonObjectRequest uploadRequest = new JsonObjectRequest(Request.Method.POST, UPLOAD_URL, jsonRequest,
                response -> {
                    progressDialog.dismiss();
                    try {
                        String status = response.getString("status");
                        String message = response.getString("message");
                        if (status.equals("success")) {
                            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), "Upload Failed: " + message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("Upload", "JSON Error: " + e.getMessage());
                    }

                    uploadItemQueue.clear();
                    selectedUploadItems.clear();
                    isUploading = false;
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(requireContext(), "Upload Failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("Upload", "Error: " + error.getMessage());
                    isUploading = false;
                });

        uploadRequest.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        requestQueue.add(uploadRequest);
    }
    // Helper method to add file to the upload list
    private void addFileToUploadList(Uri fileUri) {
        long fileSize = getFileSize(fileUri);
        selectedUploadItems.add(new UploadItem(getFileName(fileUri), formatFileSize(fileSize)));
        uploadItemQueue.add(fileUri);
    }
    private long getFileSize(Uri uri) {
        long size = 0;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                    if (sizeIndex != -1) {
                        size = cursor.getLong(sizeIndex);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return size;
    }
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result != null ? result : "unknown_file";
    }
    // Handle result of file selection
    private void createFolder(String folderName) {
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        JSONObject folderParams = new JSONObject();

        try {
            folderParams.put("auth_token", auth);
            folderParams.put("folder_name", folderName);
            folderParams.put("current_path", MY_FOLDER_PATH + currentPath);
        } catch (JSONException e) {
            Log.e("ServerStorage", "Error creating JSON object for folder creation: " + e.getMessage());
            Toast.makeText(requireContext(), "Error preparing folder creation request", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("ServerStorage", "Sending folder creation request with parameters: " + folderParams.toString());

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                CREATE_FOLDER_API,
                folderParams,
                response -> {
                    if (response != null) {
                        try {
                            Toast.makeText(requireContext(), "Folder creation request sent", Toast.LENGTH_SHORT).show();
                            fetchFilesFromHostinger(currentPath); // Refresh the folder contents
                        } catch (Exception e) {
                            Log.e("ServerStorage", "Error parsing response: " + e.getMessage());
                            Toast.makeText(requireContext(), "Error processing response", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(requireContext(), "No response from server", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("ServerStorage", "Error sending folder creation request: " + error.getMessage());
                    Toast.makeText(requireContext(), "Error Sending Folder Creation Request", Toast.LENGTH_SHORT).show();
                }
        );

        queue.add(request);
    }
    public void goBack() {
        if (!folderStack.isEmpty()) {
            currentPath = folderStack.pop(); // Go back to the previous folder
            loadFolder(currentPath);
            updateBreadcrumbs();
        } else {
            // Handle the case when there are no previous folders (e.g., show a message)
            Toast.makeText(requireContext(), "No more folders to go back to", Toast.LENGTH_SHORT).show();
        }
    }

    public void updateBreadcrumbs(){
        breadcrumbContainerServer.removeAllViews();

        // Ensure currentPath is not null or empty
        if (currentPath == null || currentPath.isEmpty()) {
            return;
        }

        // Split the current path into parts
        String[] pathParts = currentPath.split("/");

        TextView rootBreadcrumb = new TextView(requireContext());
        rootBreadcrumb.setText("Root"); // Display "/" for the root
        rootBreadcrumb.setPadding(8, 20, 8, 20);
        rootBreadcrumb.setTextSize(16);
        rootBreadcrumb.setTextColor(ContextCompat.getColor(getContext(), R.color.purple));
        rootBreadcrumb.setOnClickListener(v -> {
            // Navigate to root when clicked
            navigateToBreadcrumb(-1, pathParts); // Assuming index 0 represents root
        });
        breadcrumbContainerServer.addView(rootBreadcrumb);
        TextView separators = new TextView(requireContext());
        separators.setText(" > ");
        separators.setPadding(4, 8, 4, 8);
        breadcrumbContainerServer.addView(separators);

        StringBuilder currentPathBuilder = new StringBuilder();
        for (int i = 0; i < pathParts.length; i++ ){
            final int index = i;
            String folderName = pathParts[i];
// Add the root folder breadcrumb

            if (!folderName.isEmpty()){
                    currentPathBuilder.append("/").append(folderName);

                    TextView breadcrumb = new TextView(requireContext());
                    breadcrumb.setText(folderName);
                    breadcrumb.setPadding(8, 20, 8, 20);  // Add some padding
                    breadcrumb.setTextSize(16);
                    breadcrumb.setTextColor(ContextCompat.getColor(getContext(), R.color.purple));

                    breadcrumb.setOnClickListener(v -> {
                        navigateToBreadcrumb(index, pathParts);

                    });

                    breadcrumbContainerServer.addView(breadcrumb);

                    if (i < pathParts.length - 1 && !pathParts[i + 1].equals(ROOT_FOLDER_PATH)){
                        TextView separator = new TextView(requireContext());
                        separator.setText(" > ");
                        separator.setPadding(4, 8, 4, 8);
                        breadcrumbContainerServer.addView(separator);
                    }
            }
        }
    }
    private void navigateToBreadcrumb(int index, String[] pathParts) {
        folderStack.clear();
        StringBuilder newPath = new StringBuilder();
        for (int j = 0; j <= index; j++) {
            if (!pathParts[j].isEmpty()) {
                newPath.append(pathParts[j]).append("/");
            }
        }
        currentPath = newPath.toString();
        loadFolder(currentPath);  // Load the folder corresponding to the clicked breadcrumb
    }

    private void previewFile(RecyclerItem item) {
        if (!item.isDirectory()) {
            String filePath = DOWNLOAD_URL + item.getFileName();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(filePath), getMimeType(item.getFileName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(requireContext(), "No app found to open this file type", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(requireContext(), "This is a directory, not a file.", Toast.LENGTH_SHORT).show();
        }
    }
    // Method to get the MIME type of a file
    private String getMimeType(String fileName) {
        String mimeType = null;
        if (fileName.endsWith(".pdf")) {
            mimeType = "application/pdf";
        } else if (fileName.endsWith(".docx")) {
            mimeType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        } else if (fileName.endsWith(".pptx")) {
            mimeType = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
        } else if (fileName.endsWith(".doc")) {
            mimeType = "application/msword";
        } else if (fileName.endsWith(".ppt")) {
            mimeType = "application/vnd.ms-powerpoint";
        } else if (fileName.endsWith(".xls")) {
            mimeType = "application/vnd.ms-excel";
        } else if (fileName.endsWith(".xlsx")) {
            mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        } else if (fileName.endsWith(".txt")) {
            mimeType = "text/plain";
        } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png")) {
            mimeType = "image/*";
        } else if (fileName.endsWith(".mp3") || fileName.endsWith(".wav")) {
            mimeType = "audio/*";
        } else if (fileName.endsWith(".mp4") || fileName.endsWith(".avi") || fileName.endsWith(".mov")) {
            mimeType = "video/*";
        }
        return mimeType;
    }
    public void loadFolder(String folder) {
        // Only add to the stack if we're not going back to the root
        if (!currentPath.isEmpty()) {
            folderStack.push(currentPath);
        }
        currentPath = folder;

        saveCurrentPath(currentPath);
        Log.d("ServerStorage", "Loading folder: " + currentPath);
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
        if (swipeRefreshLayout != null) {
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
                                saveCurrentPath(fullPath);
                                // Add the file or directory to the list
                                if (!name.equals("..") && !name.equals(".")) {
                                    recyclerItems.add(new RecyclerItem(name, formatFileSize(size.length()), date, isDirectory, fullPath));
                                }
                            }

                            updateBreadcrumbs();
                            adapter.notifyDataSetChanged(); // Notify adapter of new data
                            updateEmptyStateVisibility();
                        } else {
                            // Handle error
                            String errorMessage = jsonObject.optString("message", "Error fetching files");
                            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
                            emptyStateImageView.setVisibility(View.VISIBLE);
                            emptyStateTextView.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        }
                    } catch (JSONException e) {
                        // Handle JSON parsing error
                        handleError("Error parsing JSON: " + e.getMessage());
                    } finally {
                        // Stop refresh animation
                        swipeRefreshLayout.setRefreshing(false);
                    }
                },
                error -> {
                    // Handle network error
                    swipeRefreshLayout.setRefreshing(false);
                    handleError("Network Error: " + error.getMessage());
                }) {
            // Add parameters to the request
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("auth_token", auth); // Replace with your actual token
                params.put("path", MY_FOLDER_PATH + folderPath);
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
        Log.d("SharedTask", "Item Count: " + recyclerItems.size());
    }
    private void handleError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        Log.e("SharedTask", message);
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
    private void fileDeletionRequest(String fileName, String reason) {
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String userToken = sharedPreferences.getString(SESSION_TOKEN, "0");
        String path;
        if(currentPath.length() == 0){
            path = MY_FOLDER_PATH + fileName;
        }else {
            path = MY_FOLDER_PATH + currentPath + "/" + fileName;
        }
        JSONObject itemParams = new JSONObject();

        try {
            itemParams.put("auth_token", auth);
            itemParams.put("file_name", fileName);
            itemParams.put("action", "request_permission");
            itemParams.put("current_path", path);
            itemParams.put("user_id", userToken);
            itemParams.put("reason", reason);
        } catch (JSONException e) {
            Log.e("ServerStorage", "Error creating JSON object for deletion request: " + e.getMessage());
            Toast.makeText(requireContext(), "Error preparing deletion request", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("ServerStorage", "Sending deletion request with parameters: " + itemParams.toString());

        StringRequest request = new StringRequest(Request.Method.POST, FILE_DELETE_URL,
                response -> {
                    try{
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.getString("status");
                        String message = jsonResponse.getString("message");
                        if (status.equals("success")) {
                            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                        }
                    }catch (JSONException e){

                    }
                }, error -> {
                    if (error.networkResponse != null) {
                        Log.e("Deletion Request", "Status Code: " + error.networkResponse.statusCode);
                        Log.e("Deletion Request", "Response Data: " + new String(error.networkResponse.data));
                    }
                    errorShortMessage("Error: ", error.getMessage());
                }){
                    @Override
                    public byte[] getBody() {
                        return itemParams.toString().getBytes();
                    }

                    @Override
                    public String getBodyContentType() {
                        return "application/json";
                    }
                };
                queue.add(request);
    }
    private void saveCurrentPath(String path) {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SERVER_STORAGE_CURRENT_PATH, path);
        editor.apply();
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
    // Method to format file size into a readable format
    private String formatFileSize(long size) {
        if (size <= 0) return "0 KB";
        String[] units = new String[]{"B", "KB", "MB", "GB"};
        int idx = (int) (Math.log(size) / Math.log(1024));
        return String.format("%.1f %s", size / Math.pow(1024, idx), units[idx]);
    }
    private void errorShortMessage(String type, String message) {
        Toast.makeText(requireContext(), type + message, Toast.LENGTH_SHORT).show();
    }

}
