package com.example.filemanager.Utils;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.filemanager.Todo_Tabs.MyTask;
import com.example.filemanager.Todo_Tabs.SharedTask;

public class TodoViewPagerAdapter extends FragmentStateAdapter{

    public TodoViewPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 1){
            return new SharedTask();
        }
        return new MyTask();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
