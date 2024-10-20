package com.example.filemanager.Utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.filemanager.R;

import java.util.List;

public class AnnouncementsAdapter extends RecyclerView.Adapter<AnnouncementsAdapter.AnnouncementViewHolder> {
    private List<AnnouncementsItem> announcements;

    public AnnouncementsAdapter(List<AnnouncementsItem> announcements) {
        this.announcements = announcements;
    }

    @Override
    public AnnouncementViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.announcements_item, parent, false);
        return new AnnouncementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AnnouncementViewHolder holder, int position) {
        AnnouncementsItem item = announcements.get(position);
        holder.announcements_title.setText(item.getTitle());
        holder.announcements_description.setText(item.getDescription());
    }

    @Override
    public int getItemCount() {
        return announcements.size();
    }

    public class AnnouncementViewHolder extends RecyclerView.ViewHolder {
        public TextView announcements_title;
        public TextView announcements_description;

        public AnnouncementViewHolder(View itemView) {
            super(itemView);
            announcements_title = itemView.findViewById(R.id.announcements_title);
            announcements_description = itemView.findViewById(R.id.announcements_description);
        }
    }
}