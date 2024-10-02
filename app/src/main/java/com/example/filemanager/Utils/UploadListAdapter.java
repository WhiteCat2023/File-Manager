package com.example.filemanager.Utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filemanager.R;

import java.util.List;

public class UploadListAdapter extends RecyclerView.Adapter<UploadListAdapter.UploadViewHolder> {

    private final List<UploadItem> uploadItems;

    public UploadListAdapter(List<UploadItem> uploadItems) {
        this.uploadItems = uploadItems;
    }

    @NonNull
    @Override
    public UploadListAdapter.UploadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.upload_list_item, parent, false);
        return new UploadViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UploadListAdapter.UploadViewHolder holder, int position) {
        UploadItem uploadItem = uploadItems.get(position);
        holder.fileName.setText(uploadItem.getFileName());
        holder.fileSize.setText(uploadItem.getFileSize());

        // Show or hide the progress bar based on the progress value
        holder.progressBar.setVisibility(uploadItem.getProgress() > 0 ? View.VISIBLE : View.GONE);
        holder.progressBar.setProgress(uploadItem.getProgress());
    }

    @Override
    public int getItemCount() {
        return uploadItems.size();
    }

    // ViewHolder class
    static class UploadViewHolder extends RecyclerView.ViewHolder {
        TextView fileName, fileSize;
        ProgressBar progressBar;

        public UploadViewHolder(@NonNull View itemView) {
            super(itemView);
            fileName = itemView.findViewById(R.id.uploadFileName);
            fileSize = itemView.findViewById(R.id.uploadFileSize);
            progressBar = itemView.findViewById(R.id.progress_bar);
        }
    }

    // Method to update progress
    public void updateProgress(int position, int progress) {
        if (position >= 0 && position < uploadItems.size()) {
            uploadItems.get(position).setProgress(progress);
            notifyItemChanged(position);
        }
    }
}
