package com.example.filemanager.Utils;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.filemanager.Tabs.InternalStorage;
import com.example.filemanager.Tabs.ServerStorage;

public class ViewPagerAdapter extends FragmentStateAdapter{


    public ViewPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 1){
            return new ServerStorage();
        }
        return new InternalStorage();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
