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

public class DirectoryAdapter extends RecyclerView.Adapter<DirectoryAdapter.DirectoryViewHolder> {

    private List<File> directories;
    private File selectedDirectory;
    private OnDirectorySelectedListener listener;

    public interface OnDirectorySelectedListener {
        void onDirectorySelected(File directory);
    }

    public DirectoryAdapter(List<File> directories, OnDirectorySelectedListener listener) {
        this.directories = directories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DirectoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_folder, parent, false);
        return new DirectoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DirectoryViewHolder holder, int position) {
        File directory = directories.get(position);
        holder.directoryName.setText(directory.getName());

        // Highlight selected directory
        if (directory.equals(selectedDirectory)) {
            holder.itemView.setBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.light_lavander));
        } else {
            holder.itemView.setBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.light_lavander));
        }

        holder.itemView.setOnClickListener(v -> {
            selectedDirectory = directory; // Update selected directory
            notifyDataSetChanged(); // Refresh the view to show the selected directory
            listener.onDirectorySelected(directory); // Notify listener
        });
    }

    @Override
    public int getItemCount() {
        return directories.size();
    }

    public File getSelectedDirectory() {
        return selectedDirectory;
    }

    public static class DirectoryViewHolder extends RecyclerView.ViewHolder {
        TextView directoryName;

        public DirectoryViewHolder(@NonNull View itemView) {
            super(itemView);
            directoryName = itemView.findViewById(R.id.folderNameTextView);
        }
    }
}
