package com.example.filemanager;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.filemanager.Utils.VolleyMultipartRequest;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class Upload extends AppCompatActivity {

    // Constants
    private static final int PICK_FILE_REQUEST = 1;
    private Uri fileUri;
    private static final String UPLOAD_URL = "https://skcalamba.scarlet2.io/android_api/upload.php";

    // UI elements
    private TextView uploadText;

    ImageView uploadBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        // Initialize views
        LinearLayout selectFile = findViewById(R.id.selectFile);
        uploadText = findViewById(R.id.selectFileText);
        uploadBack = findViewById(R.id.uploadBack);

        // Handle back button click
        uploadBack.setOnClickListener(v -> {
            Intent intent = new Intent(Upload.this, MainActivity.class);
            startActivity(intent);
        });

        selectFile.setOnClickListener(view -> openFileChooser());

        // Handle upload button click
        Button uploadButton = findViewById(R.id.btn_upload_file);
        uploadButton.setOnClickListener(v -> {
            if (fileUri != null) {
                uploadText.setText(getFileName(fileUri));
                uploadFile(fileUri);

            } else {
                Toast.makeText(Upload.this, "Please select a file first", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Handle file selection
    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }

    // Handle result of file selection
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null) {
            fileUri = data.getData();

        }
    }

    // Upload file to the server
    private void uploadFile(Uri uri) {
        String fileName = getFileName(uri);
        RequestQueue queue = Volley.newRequestQueue(this);

        // Custom multipart request to handle file upload
        VolleyMultipartRequest uploadRequest = new VolleyMultipartRequest(Request.Method.POST, UPLOAD_URL,
                response -> {
                    Toast.makeText(Upload.this, "Upload Successful", Toast.LENGTH_SHORT).show();
                    Log.d("Upload", "Response: " + new String(response.data));
                },
                error -> Toast.makeText(Upload.this, "Upload Failed: " + error.getMessage(), Toast.LENGTH_SHORT).show()) {

            // Add any additional parameters if needed
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                // Add any additional parameters if needed
                return params;
            }

            // Add the file to the request
            @Override
            protected Map<String, DataPart> getByteData() {
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
