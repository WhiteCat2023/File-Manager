package com.example.filemanager.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.filemanager.R;
import com.example.filemanager.Utils.ViewPagerAdapter;
import com.google.android.material.tabs.TabLayout;

import java.util.Objects;

public class Files extends Fragment {

    TabLayout tabLayout;
    ViewPager2 viewPager2;
    ViewPagerAdapter viewPagerAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_files, container, false);


        tabLayout = view.findViewById(R.id.innerNav);
        viewPager2 = view.findViewById(R.id.viewPager);

        //Sets the adapter
        viewPagerAdapter = new ViewPagerAdapter(this);

        //Sets the adapter
        viewPager2.setAdapter(viewPagerAdapter);

        //Sets the adapter
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager2.setCurrentItem(tab.getPosition());

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
}
