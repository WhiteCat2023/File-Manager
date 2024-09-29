package com.example.filemanager.Utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        holder.fileName.setText(uploadItems.get(position).getFileName());
        holder.fileSize.setText(uploadItems.get(position).getFileSize());

    }

    @Override
    public int getItemCount() {
        return uploadItems.size();
    }

    static class UploadViewHolder extends RecyclerView.ViewHolder {
        TextView fileName, fileSize;

        public UploadViewHolder(@NonNull View itemView) {
            super(itemView);
            fileName = itemView.findViewById(R.id.uploadFileName);
            fileSize = itemView.findViewById(R.id.uploadFileSize);
        }
    }
}
