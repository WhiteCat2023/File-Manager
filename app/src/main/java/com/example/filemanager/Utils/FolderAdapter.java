package com.example.filemanager.Utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filemanager.R;

import java.io.File;
import java.util.List;

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.FolderViewHolder> {
    private List<File> folderList;
    private OnFolderSelectedListener onFolderSelectedListener;

    public interface OnFolderSelectedListener {
        void onFolderSelected(File selectedFolder);
    }

    public FolderAdapter(List<File> folderList, OnFolderSelectedListener listener) {
        this.folderList = folderList;
        this.onFolderSelectedListener = listener;
    }

    @NonNull
    @Override
    public FolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_folder, parent, false);
        return new FolderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FolderViewHolder holder, int position) {
        File folder = folderList.get(position);
        holder.folderName.setText(folder.getName());

        // Set click listener for folder selection
        holder.itemView.setOnClickListener(v -> {
            onFolderSelectedListener.onFolderSelected(folder);
        });
    }

    @Override
    public int getItemCount() {
        return folderList.size();
    }

    static class FolderViewHolder extends RecyclerView.ViewHolder {
        TextView folderName;

        public FolderViewHolder(@NonNull View itemView) {
            super(itemView);
            folderName = itemView.findViewById(R.id.folderNameTextView);
        }
    }
}
