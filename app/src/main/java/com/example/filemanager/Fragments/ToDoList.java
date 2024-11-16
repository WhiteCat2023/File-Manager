package com.example.filemanager.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.filemanager.R;
import com.example.filemanager.Todo_Tabs.MyTask;
import com.example.filemanager.Todo_Tabs.SharedTask;
import com.example.filemanager.Utils.TodoViewPagerAdapter;
import com.example.filemanager.Utils.ViewPagerAdapter;
import com.google.android.material.tabs.TabLayout;

import java.util.Objects;


public class ToDoList extends Fragment {

    TabLayout tabLayout;
    ViewPager2 viewPager2;
    TodoViewPagerAdapter viewPagerAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_to_do_list, container, false);

        tabLayout = view.findViewById(R.id.innerTodoNav);
        viewPager2 = view.findViewById(R.id.viewTodoPager);

        //Sets the adapter
        viewPagerAdapter = new TodoViewPagerAdapter(this);

        //Sets the adapter
        viewPager2.setAdapter(viewPagerAdapter);

        //Sets the adapter
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager2.setCurrentItem(tab.getPosition());
//                setupOnBackPressed();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                //Sets the tab to the current position
                Objects.requireNonNull(tabLayout.getTabAt(position)).select();
            }
        });

        return view;
    }
//    private void setupOnBackPressed(){
//        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
//            @Override
//            public void handleOnBackPressed() {
//                if (isEnabled()){
//                    setEnabled(false);
//                    requireActivity().onBackPressed();
//                }
//            }
//        });
//    }
}