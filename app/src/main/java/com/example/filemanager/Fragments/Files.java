package com.example.filemanager.Fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.filemanager.R;
import com.example.filemanager.Utils.FileAdapter;
import com.example.filemanager.Utils.ViewPagerAdapter;
import com.google.android.material.tabs.TabLayout;

import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Files extends Fragment {

    //initializing tablayout and viewpager
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ViewPagerAdapter viewPagerAdapter;

//    private ListView listView;
//    private FileAdapter adapter;
//    private ArrayList<File> fileList = new ArrayList<>();
//
//    private AppCompatButton btnInternal, btnExternal;

//

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_files, container, false);

        tabLayout = view.findViewById(R.id.innerNav);
        viewPager = view.findViewById(R.id.viewPager);
        viewPagerAdapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(viewPagerAdapter);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                viewPager.setVisibility(View.GONE);

            }
        });
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                tabLayout.getTabAt(position).select();
            }
        });

//        btnInternal = view.findViewById(R.id.internalBtn);
//        btnExternal = view.findViewById(R.id.externalBtn);

        // Set click listeners for buttons
//        btnInternal.setOnClickListener(View -> {
//            listFilesFromInternalStorage();
//        });
//
//        btnExternal.setOnClickListener(View -> {
//            new FTPListFilesTask().execute();
//        });

        return view;
    }

//    //Internal storage
//    private void listFilesFromInternalStorage() {
//        File internalStorageDir = getContext().getFilesDir();  // Internal storage directory
//        File[] files = internalStorageDir.listFiles();  // List all files in the directory
//
//        if (files != null) {
//            fileList.clear();
//            for (File file : files) {
//                fileList.add(file);  // Add file to the list
//            }
//        } else {
//            Toast.makeText(getContext(), "No files found in internal storage", Toast.LENGTH_SHORT).show();
//        }
//
//        // Notify adapter of data changes
//        if (adapter != null) {
//            adapter.notifyDataSetChanged();
//        }
//    }
//
//
}
