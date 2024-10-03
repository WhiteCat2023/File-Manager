package com.example.filemanager.Utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filemanager.R;

import java.util.List;

public class DeletedItemAdapter extends RecyclerView.Adapter<DeletedItemAdapter.ViewHolder> {
    private final List<RecyclerItem> deletedItems; // List of deleted files// Listener for actions
    private final OnItemActionListener actionListener;

    public interface OnItemActionListener {
        void onRestoreClick(RecyclerItem item);
        void onDeleteClick(RecyclerItem item);
    }

    public DeletedItemAdapter(List<RecyclerItem> deletedItems, OnItemActionListener actionListener) {
        this.deletedItems = deletedItems;
        this.actionListener = actionListener;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each deleted item
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.file_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecyclerItem item = deletedItems.get(position);
        holder.fileName.setText(item.getFileName());
        holder.fileSize.setText(item.getFileSize());
        holder.date.setVisibility(View.GONE);
        if (item.isDirectory()){
            holder.icon.setImageResource(R.drawable.ic_folder);
            holder.fileToolbar.setOnClickListener(view -> {
                PopupMenu popupMenu = new PopupMenu(view.getContext(), holder.fileToolbar);
                popupMenu.inflate(R.menu.server_item_directory);

                // Handle menu item clicks
                popupMenu.setOnMenuItemClickListener(menuItem -> {
                    if (menuItem.getItemId() == R.id.delete) {
                        actionListener.onDeleteClick(item);  // Handle delete
                        return true;
                    }
                    return false;
                });
                popupMenu.show();
            });
        }else{
            holder.icon.setImageResource(R.drawable.ic_file);
            // Set up the toolbar click to show the popup menu
            holder.fileToolbar.setOnClickListener(view -> {
                PopupMenu popupMenu = new PopupMenu(view.getContext(), holder.fileToolbar);
                popupMenu.inflate(R.menu.restore_delete_options); // Inflate your menu

                // Handle menu item clicks
                popupMenu.setOnMenuItemClickListener(menuItem -> {
                    if (menuItem.getItemId() == R.id.restore) {
                        actionListener.onRestoreClick(item);  // Handle download
                        return true;
                    }
                    if (menuItem.getItemId() == R.id.trashDelete) {
                        actionListener.onDeleteClick(item);  // Handle delete
                        return true;
                    }
                    return false;
                });

                popupMenu.show(); // File icon for files
            });
        }
    }

    @Override
    public int getItemCount() {
        return deletedItems.size(); // Return the size of the deleted items list
    }

    // ViewHolder class to hold references to the views
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView fileName, fileSize, date;
        public ImageView fileToolbar, icon;
        public ViewHolder(View itemView) {
            super(itemView);
            fileName = itemView.findViewById(R.id.fileName);
            fileSize = itemView.findViewById(R.id.fileSize);
            fileToolbar = itemView.findViewById(R.id.fileToolbar);
            date = itemView.findViewById(R.id.Date);
            icon = itemView.findViewById(R.id.fileIcon);
        }
    }
}