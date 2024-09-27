package com.example.filemanager.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.filemanager.R;
import com.example.filemanager.Utils.ToDoListAdapter;
import com.example.filemanager.Utils.ToDoListItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class Todo extends Fragment implements ToDoListAdapter.OnDeleteClickListener{

    private RecyclerView recyclerView;
    private List<ToDoListItem> toDoListItem;
    private ToDoListAdapter adapter;
    private RequestQueue requestQueue;
    private SwipeRefreshLayout refreshLayout;
    private TextView noTask;

    private static final String todoUrl = "https://skcalamba.scarlet2.io/android_api/todo/get_task.php";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_todo, container, false);


        noTask = view.findViewById(R.id.noTask);
        recyclerView = view.findViewById(R.id.todoRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Initialize the list and adapter
        toDoListItem = new ArrayList<>();
        adapter = new ToDoListAdapter(toDoListItem, this);
        recyclerView.setAdapter(adapter);

        // Initialize the request queue
        requestQueue = Volley.newRequestQueue(requireContext());

        fetchToDoItems();
        refreshLayout = view.findViewById(R.id.todoRefreshLayout);
        refreshLayout.setOnRefreshListener(() -> {
            fetchToDoItems();
            refreshLayout.setRefreshing(false);
        });


        return view;
    }

    // Fetching the ToDo items from the server
    private void fetchToDoItems() {

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.POST,
                todoUrl,
                null,
                response -> {
                    try{
                        // Clear the existing list
                        toDoListItem.clear();


                        // Parse the JSON response
                        if (response.length() == 0) {
                            // If no tasks, show "No Task" and hide the RecyclerView
                            noTask.setVisibility(View.VISIBLE);
                            refreshLayout.setVisibility(View.GONE);
                        } else {
                            // If there are tasks, process them and show RecyclerView
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject taskObject = response.getJSONObject(i);
                                int taskId = taskObject.getInt("taskId");
                                String taskName = taskObject.getString("taskName");
                                String status = taskObject.getString("status");
                                String startDate = taskObject.getString("startDate");
                                String endDate = taskObject.getString("endDate");
                                boolean isComplete = taskObject.getBoolean("isComplete");

                                // Create a ToDoListItem object and add it to the list
                                toDoListItem.add(new ToDoListItem(taskId, taskName, status, isComplete, startDate, endDate));
                            }
                            adapter.notifyDataSetChanged();

                            // Make sure RecyclerView is visible and "No Task" is hidden
                            refreshLayout.setVisibility(View.VISIBLE);
                            noTask.setVisibility(View.GONE);
                        }

                        Log.d("Todo", "Item Count: " + toDoListItem.size());
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext() , "Error parsing JSON", Toast.LENGTH_SHORT).show();
                        Log.e("Todo", "JSON parsing error: " + e.getMessage());
                    }
                },error ->{
                    refreshLayout.setVisibility(View.GONE);
                    noTask.setVisibility(View.VISIBLE);
                    error.printStackTrace();
                    Toast.makeText(getContext(), "No Task Found", Toast.LENGTH_SHORT).show();
                    Log.e("Todo", "Volley error: " + (error.getMessage() != null ? error.getMessage() : "Unknown error"));
                });
        requestQueue.add(request);
    }

    private void markTaskAsComplete(int taskId) {

        String markCompleteUrl = "https://skcalamba.scarlet2.io/android_api/todo/mark_complete.php";

        JSONObject postData = new JSONObject();
        try {
            postData.put("taskId", taskId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, markCompleteUrl, postData,
                response -> {
                    // Successfully updated task on the server
                    Log.d("Todo", "Task marked as complete on server");
                },
                error -> {
                    // Error updating the task on the server
                    error.printStackTrace();
                    Toast.makeText(getContext(), "Error updating task", Toast.LENGTH_SHORT).show();
                    Log.e("Todo", "Volley error: " + (error.getMessage() != null ? error.getMessage() : "Unknown error"));
                });

        requestQueue.add(request);
    }



    @Override
    public void onDeleteClick(int position) {
        // Handle task deletion on checkbox click

        ToDoListItem task = toDoListItem.get(position);
        int task_id = task.getTaskId();

        markTaskAsComplete(task_id);

        toDoListItem.remove(position);
        adapter.notifyItemRemoved(position);


        // Check if the list is empty after the removal
        if (toDoListItem.isEmpty()) {
            // Reload the RecyclerView to handle an empty state
            adapter.notifyDataSetChanged();
            noTask.setVisibility(View.VISIBLE);
            refreshLayout.setVisibility(View.GONE);
            Toast.makeText(getContext(), "All tasks completed", Toast.LENGTH_SHORT).show();
            Log.d("Todo", "After Removal, Item Count: " + toDoListItem.size());
        } else {
            adapter.notifyItemRangeChanged(position, toDoListItem.size());
            Toast.makeText(getContext(), "Task Completed and Removed", Toast.LENGTH_SHORT).show();
        }
    }
}