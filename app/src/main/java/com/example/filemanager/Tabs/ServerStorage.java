package com.example.filemanager.Tabs;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.filemanager.MainActivity;
import com.example.filemanager.R;
import com.example.filemanager.Utils.MyAdapter;
import com.example.filemanager.Utils.RecyclerItem;

import org.apache.commons.net.ftp.FTPClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerStorage extends Fragment {

    private RecyclerView recyclerView;
    private List<RecyclerItem> recyclerItems;
    private RecyclerItem recyclerItem;
    private MyAdapter adapter;

    // Hostinger API endpoint (replace with your actual endpoint)
    private static final String HOSTINGER_API_URL = "https://skcalamba.scarlet2.io/android_api/hostinger_api.php";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        recyclerView = recyclerView.findViewById(R.id.serverRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        recyclerItems = new ArrayList<>();
        adapter = new MyAdapter(recyclerItems);
        recyclerView.setAdapter(adapter);

        fetchFilesFromHostinger();

        return inflater.inflate(R.layout.fragment_server_storage, container, false);
    }
    private void fetchFilesFromHostinger() {
        RequestQueue queue = Volley.newRequestQueue(getContext());

        StringRequest request = new StringRequest(Request.Method.POST, HOSTINGER_API_URL,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getString("status").equals("success")) {
                            JSONArray filesArray = jsonObject.getJSONArray("files");
                            for (int i = 0; i < filesArray.length(); i++) {
                                JSONObject fileObject = filesArray.getJSONObject(i);
                                String name = fileObject.getString("name");
                                String size = fileObject.getString("size");
                                String date = fileObject.getString("date");
                                boolean isDirectory = fileObject.getBoolean("isDirectory");
                                recyclerItems.add(new RecyclerItem(name, size, date, isDirectory));
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                            String errorMessage = jsonObject.optString("message", "Error fetching files");
                            Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("MainActivity", "JSON parsing error: " + e.getMessage());
                        Toast.makeText(getContext(), "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("MainActivity", "Volley error: " + error.getMessage());
                    Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("auth_token", "bf4edef043130d19e11048aab68d4c512b62d2de1d000514b65410876e9a96f2");
                params.put("path", "/public_html/myfolder");
                return params;
            }
        };

        queue.add(request);
    }
}