package com.example.filemanager.Utils;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.filemanager.R;

import java.io.File;
import java.util.ArrayList;

public class FileAdapter extends ArrayAdapter<File> {

    private static Context context;
    private ArrayList<File> files;

    public FileAdapter(Context context, ArrayList<File> files) {
        super(context, 0, files);
        this.context = context;
        this.files = files;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.file_list_item, parent, false);
        }

        File file = getItem(position);

        TextView fileName = convertView.findViewById(R.id.file_name);

        // Set the file name
        fileName.setText(file.getName());

        return convertView;
    }

    // Method to handle file deletion
    private void deleteFile(File file, int position) {
        if (file.delete()) {
            Toast.makeText(context, "Deleted: " + file.getName(), Toast.LENGTH_SHORT).show();
            files.remove(position);  // Remove from list
            notifyDataSetChanged();  // Notify adapter of data change
        } else {
            Toast.makeText(context, "Failed to delete: " + file.getName(), Toast.LENGTH_SHORT).show();
        }
    }

    // Simulate file upload
    private static void uploadFile(File file) {
        // Placeholder for upload logic
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                // Simulate upload delay
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return false;
                }
                return true;  // Simulate successful upload
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if (success) {
                    Toast.makeText(context, "Uploaded: " + file.getName(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Upload failed for: " + file.getName(), Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }
}
