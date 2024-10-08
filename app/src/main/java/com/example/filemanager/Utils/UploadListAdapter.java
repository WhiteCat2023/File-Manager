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

public class UploadListAdapter extends RecyclerView.Adapter<UploadListAdapter.ViewHolder> {
    private List<UploadItem> uploadItems;

    public UploadListAdapter(List<UploadItem> uploadItems) {
        this.uploadItems = uploadItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.upload_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UploadItem uploadItem = uploadItems.get(position);
        holder.fileNameTextView.setText(uploadItem.getFileName());
        holder.fileSizeTextView.setText(uploadItem.getFileSize());
        holder.progressBar.setProgress(uploadItem.getProgress());
    }

    @Override
    public int getItemCount() {
        return uploadItems.size();
    }

    public void updateProgress(int position, int progress) {
        uploadItems.get(position).setProgress(progress);
        notifyItemChanged(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView fileNameTextView;
        public TextView fileSizeTextView;
        public ProgressBar progressBar;

        public ViewHolder(View itemView) {
            super(itemView);
            fileNameTextView = itemView.findViewById(R.id.uploadFileName);
            fileSizeTextView = itemView.findViewById(R.id.uploadFileSize);
            progressBar = itemView.findViewById(R.id.progress_bar);
        }
    }
}
