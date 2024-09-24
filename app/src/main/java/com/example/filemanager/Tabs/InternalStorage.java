package com.example.filemanager.Tabs;

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

import com.example.filemanager.R;
import com.example.filemanager.Utils.MyAdapter;
import com.example.filemanager.Utils.RecyclerItem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class InternalStorage extends Fragment {

    private RecyclerView recyclerView;
    private List<RecyclerItem> recyclerItems;
    private MyAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    // Define the folder name for internal storage
    private static final String DOWNLOAD_FOLDER_NAME = "MyDownloads";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout
        View view = inflater.inflate(R.layout.fragment_internal_storage, container, false);

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.internalRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerItems = new ArrayList<>();

        // Initialize adapter
        adapter = new MyAdapter(recyclerItems,
                item -> {
                    // Handle item click
                    Toast.makeText(requireContext(), "Clicked: " + item.getFileName(), Toast.LENGTH_SHORT).show();
                },
                new MyAdapter.OnItemActionListener() {
                    @Override
                    public void onDownloadClick(RecyclerItem item) {
                        // Handle download
                    }

                    @Override
                    public void onDeleteClick(RecyclerItem item) {
                        // Handle delete
                    }
                });
        recyclerView.setAdapter(adapter);

        // Initialize SwipeRefreshLayout
        swipeRefreshLayout = view.findViewById(R.id.internalRefreshLayout);

        // Load files from internal storage
        loadDownloadedFiles();

        // Set up refresh listener
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadDownloadedFiles();
            swipeRefreshLayout.setRefreshing(false);
        });

        return view;
    }

    // Method to load downloaded files from internal storage
    private void loadDownloadedFiles() {
        recyclerItems.clear(); // Clear the list to avoid duplicates

        // Use the app-specific directory for the downloads folder
        File directory = new File(requireContext().getExternalFilesDir(null), DOWNLOAD_FOLDER_NAME);
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null && files.length > 0) {
                for (File file : files) {
                    // Add each file to the recyclerItems list
                    recyclerItems.add(new RecyclerItem(file.getName(), formatFileSize(file.length()), "", false));
                }
                adapter.notifyDataSetChanged(); // Notify adapter of new data
            } else {
                Log.e("InternalStorage", "No files found in the directory.");
                Toast.makeText(requireContext(), "No files found in the download folder.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e("InternalStorage", "Download folder does not exist.");
            Toast.makeText(requireContext(), "Download folder does not exist.", Toast.LENGTH_SHORT).show();
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
