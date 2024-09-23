package com.example.filemanager;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.android.volley.toolbox.StringRequest;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class Upload extends AppCompatActivity {

    private static final int PICK_FILE_REQUEST = 1;
    private Uri fileUri;
    private static final String UPLOAD_URL = "https://skcalamba.scarlet2.io/android_api/upload.php"; // Update with your secure upload URL

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        Button uploadButton = findViewById(R.id.btn_upload_file);
        uploadButton.setOnClickListener(v -> openFileChooser());
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*"); // Choose any file type
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null) {
            fileUri = data.getData();
            if (fileUri != null) {
                uploadFile(fileUri);
            }
        }
    }

    private void uploadFile(Uri uri) {
        String fileName = getFileName(uri);
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest uploadRequest = new StringRequest(Request.Method.POST, UPLOAD_URL,
                response -> {
                    try {
                        // Parse the response here if needed
                        Toast.makeText(Upload.this, "Upload Successful: " + response, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(Upload.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(Upload.this, "Upload Failed: " + error.getMessage(), Toast.LENGTH_SHORT).show()) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                // Add any additional parameters needed for the server
                return params;
            }

            public Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                try {
                    InputStream inputStream = getContentResolver().openInputStream(uri);
                    byte[] bytes = new byte[inputStream.available()];
                    inputStream.read(bytes);
                    params.put("file", new DataPart(fileName, bytes));
                } catch (Exception e) {
                    Log.e("Upload", "Error reading file: " + e.getMessage());
                }
                return params;
            }
        };

        // Optionally set a timeout
        uploadRequest.setRetryPolicy(new DefaultRetryPolicy(30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(uploadRequest);
    }

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
