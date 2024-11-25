package com.example.filemanager.Fragments;

import android.os.Bundle;
import android.os.Handler;
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
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.filemanager.R;
import com.example.filemanager.Utils.AnnouncementsAdapter;
import com.example.filemanager.Utils.AnnouncementsItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Announcements extends Fragment {

    private RequestQueue queue;
    private RecyclerView recyclerView;
    private AnnouncementsAdapter adapter;
    private List<AnnouncementsItem> announcementsList;
    private SwipeRefreshLayout refreshLayout;

    private Handler handler;
    private final int REFRESH_INTERVAL = 5000; // 10 seconds



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_announcements, container, false);

        refreshLayout = view.findViewById(R.id.announcementsSwipeRefreshLayout);
        recyclerView = view.findViewById(R.id.announcementsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext())); // Setting layout manager

        queue = Volley.newRequestQueue(requireContext());
        announcementsList = new ArrayList<>();

        adapter = new AnnouncementsAdapter(announcementsList);
        recyclerView.setAdapter(adapter);

        // Set up swipe to refresh functionality
        refreshLayout.setOnRefreshListener(() -> {
            fetchAnnouncements();
            refreshLayout.setRefreshing(false);
        });

        // Fetch initial data
        fetchAnnouncements();

        // Auto-refresh handler
        handler = new Handler();
        startAutoRefresh();

        return view;
    }

    private void startAutoRefresh() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshLayout.setRefreshing(false);
                fetchAnnouncements();
                refreshLayout.setRefreshing(false);
                handler.postDelayed(this, REFRESH_INTERVAL); // Repeat every 10 seconds
            }
        }, REFRESH_INTERVAL);
    }

    private void fetchAnnouncements() {
        String url = "https://skcalamba.scarlet2.io/ann_retrieve.php?endpoint=announcements";



        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        List<AnnouncementsItem> newAnnouncementsList = new ArrayList<>();

                        // Parse the JSON response
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject announcementObject = response.getJSONObject(i);
                            AnnouncementsItem item = new AnnouncementsItem(
                                    announcementObject.getInt("ann_id"),
                                    announcementObject.getString("ann_title"),
                                    announcementObject.getString("ann_content"),
                                    announcementObject.getString("created_by"),
                                    announcementObject.getString("ann_created_at"),
                                    announcementObject.getString("profile_picture")
                            );
                            newAnnouncementsList.add(item);
                        }

                        // Check if new items exist
                        if (!announcementsList.equals(newAnnouncementsList)) {
                            announcementsList.clear();
                            announcementsList.addAll(newAnnouncementsList);
                            Collections.reverse(announcementsList);
                            adapter.notifyDataSetChanged();
                        }
                    } catch (JSONException e) {
                        refreshLayout.setRefreshing(true);
                        e.printStackTrace();
                        Toast.makeText(requireContext(), "Failed to parse data", Toast.LENGTH_SHORT).show();
                        refreshLayout.setRefreshing(false);
                    }
                }, error -> {
                    error.printStackTrace();
                    refreshLayout.setRefreshing(true);
                    Log.e("Volley", "Error: " + error.getMessage());
                    Toast.makeText(requireContext(), "Failed to fetch data", Toast.LENGTH_SHORT).show();
                    refreshLayout.setRefreshing(false);
                });

        queue.add(jsonArrayRequest);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Stop the auto-refresh when fragment is destroyed
        handler.removeCallbacksAndMessages(null);
    }
}
