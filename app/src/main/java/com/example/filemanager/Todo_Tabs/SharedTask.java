package com.example.filemanager.Todo_Tabs;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class SharedTask extends Fragment implements ToDoListAdapter.OnDeleteClickListener {

    private RecyclerView recyclerView;
    private List<ToDoListItem> toDoListItem;
    private ToDoListAdapter adapter;
    private RequestQueue requestQueue;
    private SwipeRefreshLayout refreshLayout;
    private ImageView emptyStateImageView;
    private TextView emptyStateTextView;
    private ExtendedFloatingActionButton newFabTask;

    private static final String todoUrl = "https://skcalamba.scarlet2.io/android_api/todo/get_task.php";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sharedtask, container, false);

        // Initialize views
        emptyStateImageView = view.findViewById(R.id.todoEmptyStateImageView);
        emptyStateTextView = view.findViewById(R.id.todoEmptyStateTextView);
        recyclerView = view.findViewById(R.id.todoRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        newFabTask = view.findViewById(R.id.newFabsTasking);

        newFabTask.setOnClickListener(v -> {
            Intent intent = new Intent(this.getContext(), NewTask.class);
            startActivity(intent);
        });

        // Initialize the list and adapter
        toDoListItem = new ArrayList<>();
        adapter = new ToDoListAdapter(toDoListItem, this);
        recyclerView.setAdapter(adapter);

        // Initialize the request queue
        requestQueue = Volley.newRequestQueue(requireContext());

        // Initialize refresh layout and fetch items
        refreshLayout = view.findViewById(R.id.todoRefreshLayout);
        refreshLayout.setOnRefreshListener(this::fetchToDoItems);
        fetchToDoItems();

        return view;
    }

    // Fetching the ToDo items from the server
    private void fetchToDoItems() {
        if (refreshLayout != null) {
            refreshLayout.setRefreshing(true); // Start the refresh animation
        }

        // Create a JSON array request
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.POST,
                todoUrl,
                null,
                response -> {
                    try {
                        // Clear the existing list
                        toDoListItem.clear();
                        // Parse the JSON response
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject taskObject = response.getJSONObject(i);
                            // Extract task details
                            ToDoListItem item = new ToDoListItem(
                                    taskObject.getInt("taskId"),
                                    taskObject.getString("taskName"),
                                    taskObject.getString("status"),
                                    taskObject.getBoolean("isComplete"),
                                    taskObject.getString("startDate"),
                                    taskObject.getString("endDate")
                            );
                            toDoListItem.add(item);
                        }
                        adapter.notifyDataSetChanged();
                        updateEmptyStateVisibility();
                    } catch (JSONException e) {
                        handleError("Error parsing JSON: " + e.getMessage());
                    } finally {
                        if (refreshLayout != null) {
                            refreshLayout.setRefreshing(false);
                        }
                    }
                },
                error -> {
                    handleError("No Task Found");
                    if (refreshLayout != null) {
                        refreshLayout.setRefreshing(false);
                    }
                }
        );
        requestQueue.add(request);
    }

    private void updateEmptyStateVisibility() {
        if (toDoListItem.isEmpty()) {
            emptyStateImageView.setVisibility(View.VISIBLE);
            emptyStateTextView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyStateImageView.setVisibility(View.GONE);
            emptyStateTextView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
        Log.d("Todo", "Item Count: " + toDoListItem.size());
    }

    private void handleError(String message) {
        Log.e("Todo", message);
        emptyStateImageView.setVisibility(View.VISIBLE);
        emptyStateTextView.setVisibility(View.VISIBLE);
        emptyStateTextView.setText(message);
        recyclerView.setVisibility(View.GONE);
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
                response -> Log.d("Todo", "Task marked as complete on server"),
                error -> handleError("Error updating task")
        );

        requestQueue.add(request);
    }

    @Override
    public void onDeleteClick(int position) {
        ToDoListItem task = toDoListItem.get(position);
        int taskId = task.getTaskId();

        markTaskAsComplete(taskId);

        toDoListItem.remove(position);
        adapter.notifyItemRemoved(position);

        // Check if the list is empty after the removal
        if (toDoListItem.isEmpty()) {
            Toast.makeText(getContext(), "All tasks completed", Toast.LENGTH_SHORT).show();
        } else {
            adapter.notifyItemRangeChanged(position, toDoListItem.size());
            Toast.makeText(getContext(), "Task Completed and Removed", Toast.LENGTH_SHORT).show();
        }

        Log.d("Todo", "After Removal, Item Count: " + toDoListItem.size());
    }
}
