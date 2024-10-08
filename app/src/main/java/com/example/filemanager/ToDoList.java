package com.example.filemanager;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.filemanager.Fragments.SharedTask;


public class ToDoList extends Fragment {

    TextView sharedTask, personalTask;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_to_do_list, container, false);

        sharedTask = view.findViewById(R.id.sharedTask);
        personalTask = view.findViewById(R.id.personalTask);



        sharedTask.setOnClickListener(v -> {
            // Create new fragment and transaction
            Fragment newFragment = new SharedTask();
            // consider using Java coding conventions (upper first char class names!!!)
            assert getFragmentManager() != null;
            FragmentTransaction transaction = getFragmentManager().beginTransaction();

            // Replace whatever is in the fragment_container view with this fragment,
            // and add the transaction to the back stack
            transaction.replace(R.id.fragment_container, newFragment);
            transaction.addToBackStack(null);

            // Commit the transaction
            transaction.commit();
        });
        return view;
    }
}