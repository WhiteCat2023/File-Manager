package com.example.filemanager.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.filemanager.R;
import com.example.filemanager.Utils.ToDoListAdapter;
import com.example.filemanager.Utils.ToDoListItem;

import java.util.List;


public class Todo extends Fragment {

    RecyclerView recyclerView;
    List<ToDoListItem> toDoListItem;
    ToDoListAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_todo, container, false);
        recyclerView = view.findViewById(R.id.todoRecyclerView);
        if (recyclerView == null){
            Log.e("TodoList", "Todo RecyclerView is null, check your layout ID");
            return view;
        }



        recyclerView = view.findViewById(R.id.todoRecyclerView);



        return view;
    }
}