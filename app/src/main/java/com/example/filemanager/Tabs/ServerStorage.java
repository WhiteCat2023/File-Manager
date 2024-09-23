package com.example.filemanager.Tabs;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.filemanager.R;
import com.example.filemanager.Utils.MyAdapter;
import com.example.filemanager.Utils.RecyclerItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerStorage extends Fragment {
    // Initialize variables
    private List<RecyclerItem> recyclerItems;
    private MyAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    // Hostinger API endpoint (replace with your actual endpoint)
    private static final String HOSTINGER_API_URL = "https://skcalamba.scarlet2.io/android_api/hostinger_api.php";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Access this before initializing the id of the recycler view
        View view = inflater.inflate(R.layout.fragment_server_storage, container, false);
        // Check if the RecyclerView is null
        RecyclerView recyclerView = view.findViewById(R.id.serverRecyclerView);
        if (recyclerView == null) {
            Log.e("ServerStorage", "recyclerView is null, check if the ID is correct in the layout.");
            return view; // Avoid further errors
        }
        // Set up the RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerItems = new ArrayList<>();
        adapter = new MyAdapter(recyclerItems);
        recyclerView.setAdapter(adapter);
        swipeRefreshLayout = view.findViewById(R.id.serverRefreshLayout);

        fetchFilesFromHostinger();
        //refreshing the layout in the server tab
        swipeRefreshLayout.setOnRefreshListener(() -> {
            fetchFilesFromHostinger();
            swipeRefreshLayout.setRefreshing(false);
        });

        return view;
    }
    // Fetch files from the Hostinger API
    private void fetchFilesFromHostinger() {
        // Create a Volley request queue
        RequestQueue queue = Volley.newRequestQueue(requireContext());
        // Create a Volley request to fetch files from the Hostinger API
        StringRequest request = new StringRequest(Request.Method.POST, HOSTINGER_API_URL,
                response -> {
                    try {
                        // Parse the JSON response
                        Log.d("ServerResponse", "Response: " + response);
                        JSONObject jsonObject = new JSONObject(response);
                        // Check if the response is successful
                        if (jsonObject.getString("status").equals("success")) {
                            JSONArray filesArray = jsonObject.getJSONArray("files");
                            int currentSize = recyclerItems.size(); // Track the current size of the list

                            recyclerItems.clear(); // Clear the existing items if you expect new data
                            for (int i = 0; i < filesArray.length(); i++) {
                                JSONObject fileObject = filesArray.getJSONObject(i);
                                String name = fileObject.getString("name");
                                String size = fileObject.optString("size", "N/A"); // Add default if missing
                                String date = fileObject.optString("date", "N/A"); // Add default if missing
                                boolean isDirectory = fileObject.getBoolean("isDirectory");
                                // Add the file to the list
                                recyclerItems.add(new RecyclerItem(name, size, date, isDirectory));
                                // Notify that an item was inserted at the end of the list
                                adapter.notifyItemInserted(currentSize + i);
                            }
                        } else {
                            // Handle error
                            String errorMessage = jsonObject.optString("message", "Error fetching files");
                            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        // Handle JSON parsing error
                        Log.e("ServerStorage", "JSON parsing error: " + e.getMessage());
                        Toast.makeText(requireContext(), "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    // Handle network error
                    Log.e("ServerStorage", "Volley error: " + (error.getMessage() != null ? error.getMessage() : "Unknown error"));
                    Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show();
                }) {
            // Add any necessary parameters to the request
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("auth_token", "bf4edef043130d19e11048aab68d4c512b62d2de1d000514b65410876e9a96f2");
                params.put("path", "/public_html/myfolder");
                return params;
            }
        };
        // Add the request to the queue
        queue.add(request);
    }


}