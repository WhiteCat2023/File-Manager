package com.example.filemanager.Todo_Tabs;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.filemanager.R;
import com.example.filemanager.Utils.ToDoListAdapter;
import com.example.filemanager.Utils.TodoListItemPersonal;
import com.example.filemanager.Utils.TodoListPersonalAdapter;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MyTask extends Fragment implements TodoListPersonalAdapter.OnDeleteClickListener{

//    private ExtendedFloatingActionButton fab;
//    private RecyclerView myTaskRecyclerView;
//    private SwipeRefreshLayout myTaskRefreshLayout;
//    private ImageView emptyStateImageView;
//    private TextView emptyStateTextView;
//    private List<ToDoListItem> toDoListItem;
//    private ToDoListAdapter adapter;
//    private TodoDatabaseHelper dbHelper;
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//
//        View view = inflater.inflate(R.layout.fragment_my_task, container, false);

//        dbHelper = new TodoDatabaseHelper(getContext());
//        fab = view.findViewById(R.id.fab);
//        myTaskRecyclerView = view.findViewById(R.id.myTaskRecyclerView);
//        myTaskRefreshLayout = view.findViewById(R.id.myTaskRefreshLayout);
//        emptyStateImageView = view.findViewById(R.id.myTaskEmptyStateView);
//        emptyStateTextView = view.findViewById(R.id.myTaskEmptyStateTextView);
//        myTaskRefreshLayout = view.findViewById(R.id.myTaskRefreshLayout);
//        toDoListItem = new ArrayList<>();
//        adapter = new ToDoListAdapter(toDoListItem, this);
//        myTaskRecyclerView.setAdapter(adapter);
//        myTaskRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
//
//
//        fab.setOnClickListener(v -> {
//
//            Intent intent = new Intent(this.requireContext(), NewTask2.class);
//            startActivity(intent);
//
//        });
//
//        // Pull-to-refresh functionality
//        myTaskRefreshLayout.setOnRefreshListener(this::fetchToDoItems);
//
//        // Fetch tasks from the local database
//        fetchToDoItems();
//
//        // Auto-update functionality
//        requireActivity().getSupportFragmentManager().setFragmentResultListener("requestKey", this, (requestKey, result) -> {
//            if (result.getBoolean("taskCreated")) {
//                fetchToDoItems();
//            }
//        });
//
//        return view;
//    }
//
//    private void fetchToDoItems() {
//        loadTasksFromLocalDatabase();
//        if (myTaskRefreshLayout != null) {
//            myTaskRefreshLayout.setRefreshing(false);
//        }
//    }
//
//
//    private void updateEmptyStateVisibility() {
//        if (toDoListItem.isEmpty()) {
//            emptyStateImageView.setVisibility(View.VISIBLE);
//            emptyStateTextView.setVisibility(View.VISIBLE);
//            myTaskRecyclerView.setVisibility(View.GONE);
//        } else {
//            emptyStateImageView.setVisibility(View.GONE);
//            emptyStateTextView.setVisibility(View.GONE);
//            myTaskRecyclerView.setVisibility(View.VISIBLE);
//        }
//    }
//
//    private void loadTasksFromLocalDatabase() {
//        // Clear the existing list of tasks
//        toDoListItem.clear();
//
//        // Get tasks from the local SQLite database
//        List<ToDoListItem> tasks = dbHelper.getAllTasks();
//
//        // Check if the tasks list is empty
//        if (tasks.isEmpty()) {
//            // Update the empty state view
//            updateEmptyStateVisibility();
//        } else {
//            // Add tasks to the toDoListItem list
//            toDoListItem.addAll(tasks);
//
//            // Notify the adapter about data changes
//            adapter.notifyDataSetChanged();
//
//            // Update the empty state view
//            updateEmptyStateVisibility();
//        }
//    }
//
//
//    @Override
//    public void onDeleteClick(int position) {
//        ToDoListItem task = toDoListItem.get(position);
//        int taskId = task.getTaskId();
//
//        // Remove from the local database
//        dbHelper.deleteTask(taskId);
//
//        // Remove the task from the list and notify the adapter
//        toDoListItem.remove(position);
//        adapter.notifyItemRemoved(position);
//
//        // Check if the list is empty after deletion
//        if (toDoListItem.isEmpty()) {
//            Toast.makeText(getContext(), "All tasks completed", Toast.LENGTH_SHORT).show();
//        } else {
//            adapter.notifyItemRangeChanged(position, toDoListItem.size());
//            Toast.makeText(getContext(), "Task Completed and Removed", Toast.LENGTH_SHORT).show();
//        }
//
//        // Update the empty state view
//        updateEmptyStateVisibility();
//    }
    private RecyclerView recyclerView;
    private List<TodoListItemPersonal> todoListItemPersonal;
    private TodoListPersonalAdapter adapter;
    private RequestQueue requestQueue;
    private SwipeRefreshLayout refreshLayout;
    private ImageView emptyStateImageView;
    private TextView emptyStateTextView;
    private ExtendedFloatingActionButton newFabTask;

    private static final String todoUrl = "https://skcalamba.scarlet2.io/android_api/todo/get_task_personal.php";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_task, container, false);

        // Initialize views
        emptyStateImageView = view.findViewById(R.id.myTaskEmptyStateView);
        emptyStateTextView = view.findViewById(R.id.myTaskEmptyStateTextView);
        recyclerView = view.findViewById(R.id.myTaskRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        newFabTask = view.findViewById(R.id.fab);

        newFabTask.setOnClickListener(v -> {
            Intent intent = new Intent(this.getContext(), NewTask2.class);
            startActivity(intent);
        });

        // Initialize the list and adapter
        todoListItemPersonal = new ArrayList<>();
        adapter = new TodoListPersonalAdapter(todoListItemPersonal, this);
        recyclerView.setAdapter(adapter);

        // Initialize the request queue
        requestQueue = Volley.newRequestQueue(requireContext());

        // Initialize refresh layout and fetch items
        refreshLayout = view.findViewById(R.id.myTaskRefreshLayout);
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
                        todoListItemPersonal.clear();
                        // Parse the JSON response
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject taskObject = response.getJSONObject(i);
                            // Extract task details
                            TodoListItemPersonal item = new TodoListItemPersonal(
                                    taskObject.getInt("taskId"),
                                    taskObject.getString("taskName"),
                                    taskObject.getString("status"),
                                    taskObject.getBoolean("isComplete"),
                                    taskObject.getString("startDate"),
                                    taskObject.getString("endDate"),
                                    taskObject.getString("type"),
                                    taskObject.getString("created_by")
                            );
                            todoListItemPersonal.add(item);
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
        if (todoListItemPersonal.isEmpty()) {
            emptyStateImageView.setVisibility(View.VISIBLE);
            emptyStateTextView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyStateImageView.setVisibility(View.GONE);
            emptyStateTextView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
        Log.d("Todo", "Item Count: " + todoListItemPersonal.size());
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
        TodoListItemPersonal task = todoListItemPersonal.get(position);
        int taskId = task.getTaskId();

        markTaskAsComplete(taskId);

        todoListItemPersonal.remove(position);
        adapter.notifyItemRemoved(position);

        // Check if the list is empty after the removal
        if (todoListItemPersonal.isEmpty()) {
            Toast.makeText(getContext(), "All tasks completed", Toast.LENGTH_SHORT).show();
        } else {
            adapter.notifyItemRangeChanged(position, todoListItemPersonal.size());
            Toast.makeText(getContext(), "Task Completed and Removed", Toast.LENGTH_SHORT).show();
        }

        Log.d("Todo", "After Removal, Item Count: " + todoListItemPersonal.size());
    }
}