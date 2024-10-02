package com.example.filemanager;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.filemanager.Utils.UploadItem;
import com.example.filemanager.Utils.UploadListAdapter;
import com.example.filemanager.Utils.VolleyMultipartRequest;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Upload extends AppCompatActivity {

    // Constants
    private static final int PICK_FILE_REQUEST = 0;
    private static final String UPLOAD_URL = "https://skcalamba.scarlet2.io/android_api/upload.php";

    // UI elements
    private RecyclerView uploadRecyclerView;
    private List<UploadItem> selectedUploadItems;
    private UploadListAdapter uploadAdapter;
    private List<Uri> uploadItemQueue;
    private boolean isUploading = false;

    ImageView uploadBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        // Initialize views
        LinearLayout selectFile = findViewById(R.id.selectFile);
        uploadBack = findViewById(R.id.uploadBack);
        uploadItemQueue = new ArrayList<>();
        uploadRecyclerView = findViewById(R.id.uploadRecyclerView);

        selectedUploadItems = new ArrayList<>();
        uploadAdapter = new UploadListAdapter(selectedUploadItems);
        uploadRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        uploadRecyclerView.setAdapter(uploadAdapter);

        // Handle back button click
        uploadBack.setOnClickListener(v -> {
            Intent intent = new Intent(Upload.this, MainActivity.class);
            startActivity(intent);
            finish(); // Close the current activity
        });

        selectFile.setOnClickListener(view -> openFileChooser());

        // Handle upload button click
        Button uploadButton = findViewById(R.id.btn_upload_file);
        uploadButton.setOnClickListener(v -> {
            if (!uploadItemQueue.isEmpty()) {
                uploadFiles(uploadItemQueue); // Start uploading all files in the queue
            } else {
                Toast.makeText(Upload.this, "Please select a file first", Toast.LENGTH_SHORT).show();
            }
        });
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
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
                uploadAdapter.notifyDataSetChanged();
            }
        }
    }

    // Helper method to add file to the upload list
    private void addFileToUploadList(Uri fileUri) {
        long fileSize = getFileSize(fileUri);
        selectedUploadItems.add(new UploadItem(getFileName(fileUri), formatFileSize(fileSize), 0));
        uploadItemQueue.add(fileUri);
    }

    private String formatFileSize(long size) {
        if (size <= 0) return "0 KB";
        String[] units = new String[]{"B", "KB", "MB", "GB"};
        int idx = (int) (Math.log(size) / Math.log(1024));
        return String.format("%.1f %s", size / Math.pow(1024, idx), units[idx]);
    }

    private long getFileSize(Uri uri) {
        long size = 0;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
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

//On progress pa ang progress bar para sa upload items
    // Upload multiple files to the server
    private void uploadFiles(List<Uri> uris) {
        if (isUploading) return;

        int position = uploadItemQueue.size();

        isUploading = true;
        RequestQueue queue = Volley.newRequestQueue(this);

        // Custom multipart request to handle file upload
        VolleyMultipartRequest uploadRequest = new VolleyMultipartRequest(Request.Method.POST, UPLOAD_URL,
                response -> {
                    runOnUiThread(() -> {
                        Toast.makeText(Upload.this, "Upload Successful", Toast.LENGTH_SHORT).show();
                        Log.d("Upload", "Response: " + new String(response.data));

                        // Clear the uploaded files from the queue
                        uploadItemQueue.clear();
                        selectedUploadItems.clear();
                        uploadAdapter.notifyDataSetChanged();
                        isUploading = false;
                    });
                },
                error -> {
                    runOnUiThread(() -> {
                        Toast.makeText(Upload.this, "Upload Failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        isUploading = false;
                    });
                }, position,
                (itemPosition, progress) -> {
                    // Ensure progress updates the UI on the main thread
                    Log.d("UploadProgress", "Item Position: " + itemPosition + " Progress: " + progress);
                    runOnUiThread(() -> uploadAdapter.updateProgress(itemPosition, progress));
                }) {

            @Override
            protected Map<String, String> getParams() {
                return new HashMap<>();
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                for (Uri uri : uris) {
                    String fileName = getFileName(uri);
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(uri);
                        byte[] bytes = new byte[inputStream.available()];
                        inputStream.read(bytes);
                        params.put("file[" + fileName + "]", new DataPart(fileName, bytes));
                    } catch (Exception e) {
                        Log.e("Upload", "Error reading file: " + e.getMessage());
                    }
                }
                return params;
            }
        };

        // Set a timeout
        uploadRequest.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        queue.add(uploadRequest);
    }


    // Get file name from URI
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
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

    private static class DataPart {
        String fileName;
        byte[] bytes;

        DataPart(String fileName, byte[] bytes) {
            this.fileName = fileName;
            this.bytes = bytes;
        }
    }
}
