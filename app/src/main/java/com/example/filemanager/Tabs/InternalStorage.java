package com.example.filemanager.Tabs;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.filemanager.R;
import com.example.filemanager.Utils.MyAdapter;
import com.example.filemanager.Utils.RecyclerItem;

import java.util.ArrayList;
import java.util.List;

public class InternalStorage extends Fragment {

    RecyclerView recyclerView;
    List<RecyclerItem> recyclerItems;
    RecyclerItem recyclerItem;
    MyAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {



        return inflater.inflate(R.layout.fragment_internal_storage, container, false);
    }
}