package com.example.filemanager.Fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.example.filemanager.R;
import com.example.filemanager.Utils.FileAdapter;

import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Files extends Fragment {

    private ListView listView;
    private FileAdapter adapter;
    private ArrayList<File> fileList = new ArrayList<>();

    private AppCompatButton btnInternal, btnExternal;

    // FTP server credentials
    private static final String FTP_HOST = "ftp://scarlet2.io";
    private static final String FTP_USER = "u843230181.skcalamba.scarlet2.io";
    private static final String FTP_PASS = "WhiteCat@2004";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_files, container, false);

        listView = view.findViewById(R.id.file_list_view);
        adapter = new FileAdapter(getContext(), fileList);  // Using custom FileAdapter
        listView.setAdapter(adapter);

        btnInternal = view.findViewById(R.id.internalBtn);
        btnExternal = view.findViewById(R.id.externalBtn);

        // Set click listeners for buttons
        btnInternal.setOnClickListener(View -> {
            listFilesFromInternalStorage();
        });

        btnExternal.setOnClickListener(View -> {
            new FTPListFilesTask().execute();
        });

        return view;
    }

    //Internal storage
    private void listFilesFromInternalStorage() {
        File internalStorageDir = getContext().getFilesDir();  // Internal storage directory
        File[] files = internalStorageDir.listFiles();  // List all files in the directory

        if (files != null) {
            fileList.clear();
            for (File file : files) {
                fileList.add(file);  // Add file to the list
            }
        } else {
            Toast.makeText(getContext(), "No files found in internal storage", Toast.LENGTH_SHORT).show();
        }

        // Notify adapter of data changes
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    //External storage (FTP)
    private class FTPListFilesTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            FTPClient ftpClient = new FTPClient();

            try {
                // Connect to FTP server
                ftpClient.connect(FTP_HOST);
                ftpClient.login(FTP_USER, FTP_PASS);

                // Get list of files and directories
                String[] files = ftpClient.listNames();

                if (files != null) {
                    fileList.clear();
                    for (String file : files) {
                        fileList.add(new File(file));  // Add file/directory names to list
                    }
                }

                ftpClient.logout();
                ftpClient.disconnect();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(getContext(), "Error retrieving files from server", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
