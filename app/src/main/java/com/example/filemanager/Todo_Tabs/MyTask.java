package com.example.filemanager.Todo_Tabs;

import android.content.Intent;
import android.os.Bundle;
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

import com.example.filemanager.R;
import com.example.filemanager.Utils.ToDoListAdapter;
import com.example.filemanager.Utils.ToDoListItem;
import com.example.filemanager.Utils.TodoDatabaseHelper;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MyTask extends Fragment implements ToDoListAdapter.OnDeleteClickListener{

    private ExtendedFloatingActionButton fab;
    private RecyclerView myTaskRecyclerView;
    private SwipeRefreshLayout myTaskRefreshLayout;
    private ImageView emptyStateImageView;
    private TextView emptyStateTextView;
    private List<ToDoListItem> toDoListItem;
    private ToDoListAdapter adapter;
    private TodoDatabaseHelper dbHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_my_task, container, false);

        dbHelper = new TodoDatabaseHelper(getContext());
        fab = view.findViewById(R.id.fab);
        myTaskRecyclerView = view.findViewById(R.id.myTaskRecyclerView);
        myTaskRefreshLayout = view.findViewById(R.id.myTaskRefreshLayout);
        emptyStateImageView = view.findViewById(R.id.myTaskEmptyStateView);
        emptyStateTextView = view.findViewById(R.id.myTaskEmptyStateTextView);
        myTaskRefreshLayout = view.findViewById(R.id.myTaskRefreshLayout);
        toDoListItem = new ArrayList<>();
        adapter = new ToDoListAdapter(toDoListItem, this);
        myTaskRecyclerView.setAdapter(adapter);
        myTaskRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));


        fab.setOnClickListener(v -> {

            Intent intent = new Intent(this.requireContext(), NewTask2.class);
            startActivity(intent);

        });

        // Pull-to-refresh functionality
        myTaskRefreshLayout.setOnRefreshListener(this::fetchToDoItems);

        // Fetch tasks from the local database
        fetchToDoItems();

        // Auto-update functionality
        requireActivity().getSupportFragmentManager().setFragmentResultListener("requestKey", this, (requestKey, result) -> {
            if (result.getBoolean("taskCreated")) {
                fetchToDoItems();
            }
        });

        return view;
    }

    private void fetchToDoItems() {
        loadTasksFromLocalDatabase();
        if (myTaskRefreshLayout != null) {
            myTaskRefreshLayout.setRefreshing(false);
        }
    }


    private void updateEmptyStateVisibility() {
        if (toDoListItem.isEmpty()) {
            emptyStateImageView.setVisibility(View.VISIBLE);
            emptyStateTextView.setVisibility(View.VISIBLE);
            myTaskRecyclerView.setVisibility(View.GONE);
        } else {
            emptyStateImageView.setVisibility(View.GONE);
            emptyStateTextView.setVisibility(View.GONE);
            myTaskRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void loadTasksFromLocalDatabase() {
        // Clear the existing list of tasks
        toDoListItem.clear();

        // Get tasks from the local SQLite database
        List<ToDoListItem> tasks = dbHelper.getAllTasks();

        // Check if the tasks list is empty
        if (tasks.isEmpty()) {
            // Update the empty state view
            updateEmptyStateVisibility();
        } else {
            // Add tasks to the toDoListItem list
            toDoListItem.addAll(tasks);

            // Notify the adapter about data changes
            adapter.notifyDataSetChanged();

            // Update the empty state view
            updateEmptyStateVisibility();
        }
    }


    @Override
    public void onDeleteClick(int position) {
        ToDoListItem task = toDoListItem.get(position);
        int taskId = task.getTaskId();

        // Remove from the local database
        dbHelper.deleteTask(taskId);

        // Remove the task from the list and notify the adapter
        toDoListItem.remove(position);
        adapter.notifyItemRemoved(position);

        // Check if the list is empty after deletion
        if (toDoListItem.isEmpty()) {
            Toast.makeText(getContext(), "All tasks completed", Toast.LENGTH_SHORT).show();
        } else {
            adapter.notifyItemRangeChanged(position, toDoListItem.size());
            Toast.makeText(getContext(), "Task Completed and Removed", Toast.LENGTH_SHORT).show();
        }

        // Update the empty state view
        updateEmptyStateVisibility();
    }

}