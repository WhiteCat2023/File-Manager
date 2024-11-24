package com.example.filemanager.Utils;

import static java.lang.Math.round;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.filemanager.R;
import com.google.android.material.imageview.ShapeableImageView;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import com.squareup.picasso.Picasso;

public class AnnouncementsAdapter extends RecyclerView.Adapter<AnnouncementsAdapter.AnnouncementViewHolder> {
    private List<AnnouncementsItem> announcements;
    private String profile_url = "https://skcalamba.scarlet2.io/profile/";
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
        holder.announcements_author.setText(item.getCreatedBy());
        holder.announcements_time.setText(time_ago(item.getDate()));
        if(item.getProfile() != null){
            Picasso.get().load(profile_url + item.getProfile()).into(holder.cornerImage);
        }

    }

    @Override
    public int getItemCount() {
        return announcements.size();
    }

    public class AnnouncementViewHolder extends RecyclerView.ViewHolder {
        public TextView announcements_title;
        public TextView announcements_description;
        public TextView announcements_author;
        public TextView announcements_time;
        public ShapeableImageView cornerImage;

        public AnnouncementViewHolder(View itemView) {
            super(itemView);
            announcements_title = itemView.findViewById(R.id.announcements_title);
            announcements_description = itemView.findViewById(R.id.announcements_description);
            announcements_author = itemView.findViewById(R.id.announcements_author);
            announcements_time = itemView.findViewById(R.id.announcements_time);
            cornerImage = itemView.findViewById(R.id.cornerImage);
        }
    }
    private String time_ago(String date){
        LocalDateTime timeAgo = LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        LocalDateTime now = LocalDateTime.now();
        int time_difference = (int) (now.toEpochSecond(ZoneOffset.UTC) - timeAgo.toEpochSecond(ZoneOffset.UTC));
        int seconds = time_difference;
        int minutes = round(seconds/60);
        int hours = round(seconds/3600);
        int days = round(seconds/86400);
        int weeks = round(seconds/604800);
        int months = round(seconds/2629440);
        int years = round(seconds/31553280);

        if(seconds <= 60){
            return "Just now";
        } else if(minutes <= 60){
            return minutes == 1 ? "a minute ago" : minutes + " minutes ago";
        } else if(hours <= 24){
            return hours == 1 ? "an hour ago" : hours + " hours ago";
        } else if(days <= 30){
            return days == 1 ? "a day ago" : days + " days ago";
        } else if(months <= 12){
            return months == 1 ? "a month ago" : months + " months ago";
        } else{
            return years == 1 ? "a year ago" : years + " years ago";
        }
    }
}