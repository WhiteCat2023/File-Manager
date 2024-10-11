package com.example.filemanager.Utils;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.filemanager.R;

import java.util.List;

public class InternalStorageAdapter extends RecyclerView.Adapter<InternalStorageAdapter.ViewHolder> {
    private final List<RecyclerItem> recyclerItems;
    private final OnItemClickListener onItemClickListener;
    private final OnItemActionListener actionListener;

    // Functional interface for handling click events
    @FunctionalInterface
    public interface OnItemClickListener {
        void onItemClick(RecyclerItem item);
    }

    // Interface for additional actions
    public interface OnItemActionListener {
        void onRenameClick(RecyclerItem item);
        void onDeleteClick(RecyclerItem item);
//        void onMoveToClick(RecyclerItem item);
//        void onCopyToClick(RecyclerItem item);
    }

    // Constructor for the adapter
    public InternalStorageAdapter(List<RecyclerItem> recyclerItems, OnItemClickListener onItemClickListener, OnItemActionListener actionListener) {
        this.recyclerItems = recyclerItems;
        this.onItemClickListener = onItemClickListener;
        this.actionListener = actionListener;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_list_item, parent, false);
        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecyclerItem item = recyclerItems.get(position);

        // Set text fields with file/folder data
        holder.fileName.setText(item.getFileName());
        holder.fileSize.setText(item.getFileSize());
        holder.fileDate.setText(item.getFileDate());

        // Change icon based on whether the item is a directory or a file
        if (item.isDirectory()) {
            holder.icon.setImageResource(R.drawable.c_folder);
            holder.fileToolbar.setOnClickListener(view -> {
                PopupMenu popupMenu = new PopupMenu(view.getContext(), holder.fileToolbar);
                popupMenu.inflate(R.menu.server_item_directory);

                // Handle menu item clicks
                popupMenu.setOnMenuItemClickListener(menuItem -> {
                    if (menuItem.getItemId() == R.id.delete) {
                        actionListener.onDeleteClick(item);  // Handle delete
                        return true;
                    }
                    if (menuItem.getItemId() == R.id.rename) {
                        actionListener.onRenameClick(item);  // Handle Rename
                        return true;
                    }
//                    if (menuItem.getItemId() == R.id.moveTo) {
//                        actionListener.onMoveToClick(item);  // Handle move to
//                        return true;
//                    }
//                    if (menuItem.getItemId() == R.id.copyTo) {
//                        actionListener.onCopyToClick(item);  // Handle copy to
//                        return true;
//                    }
                    return false;
                });
                popupMenu.show();
            });
        } else {
           openFile(item, holder);
            // Set up the toolbar click to show the popup menu
            holder.fileToolbar.setOnClickListener(view -> {
                PopupMenu popupMenu = new PopupMenu(view.getContext(), holder.fileToolbar);
                popupMenu.inflate(R.menu.internal_item_directory); // Inflate your menu

                // Handle menu item clicks
                popupMenu.setOnMenuItemClickListener(menuItem -> {
                    if (menuItem.getItemId() == R.id.renameInternalItem) {
                        actionListener.onRenameClick(item);  // Handle download
                        return true;
                    }
                    if (menuItem.getItemId() == R.id.deleteInternalItem) {
                        actionListener.onDeleteClick(item);  // Handle delete
                        return true;
                    }
//                    if (menuItem.getItemId() == R.id.copyTo) {
//                        actionListener.onCopyToClick(item);
//                        return true;
//                    }
//                    if (menuItem.getItemId() == R.id.moveTo){
//                        actionListener.onMoveToClick(item);
//                        return true;
//                    }
                    return false;
                });

                popupMenu.show(); // File icon for files
            });
        }

        // Show the popup menu


        // Handle the click event for the entire item view
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(item);  // Trigger click event for the item
            }
        });
    }


    // Return the size of the dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return recyclerItems.size();
    }

    // Method to open the file with the appropriate viewer
    private void openFile(RecyclerItem item, @NonNull ViewHolder holder) {
        if (item == null || item.getFileName() == null) {
            Log.e("InternalStorage", "Invalid file.");
            return;
        }

        String fileName = item.getFileName();
        String fileExtension = getFileExtension(fileName);

        // Determine the appropriate file reader based on the file type
        switch (fileExtension) {
            case "pdf":
                holder.icon.setImageResource(R.drawable.c_pdf);
                break;
            case "txt":
                holder.icon.setImageResource(R.drawable.c_txt);
                break;
            case "doc":
            case "docx":
                holder.icon.setImageResource(R.drawable.c_doc);
                break;
            case "xls":
            case "xlsx":
                holder.icon.setImageResource(R.drawable.c_xls);
                break;
            case "ppt":
            case "pptx":
                holder.icon.setImageResource(R.drawable.c_ppt);
                break;
            case "jpg":
            case "jpeg":
                holder.icon.setImageResource(R.drawable.jpg);
                break;
            case "png":
                holder.icon.setImageResource(R.drawable.c_png);
                break;
            case "mp3":
                holder.icon.setImageResource(R.drawable.c_mp3);
                break;
            case "mp4":
                holder.icon.setImageResource(R.drawable.c_mp4);
                break;
            default:
                holder.icon.setImageResource(R.drawable.c_file);
                break;
        }
    }
    // Method to get the file extension
    private String getFileExtension(String fileName) {
        if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) {
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        }
        return "";
    }

    // Provide a reference to the type of views being used (custom ViewHolder)
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView fileName, fileSize, fileDate;
        ImageView icon;
        ImageView fileToolbar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize views
            fileName = itemView.findViewById(R.id.fileName);
            fileSize = itemView.findViewById(R.id.fileSize);
            fileDate = itemView.findViewById(R.id.Date);
            icon = itemView.findViewById(R.id.fileIcon);
            fileToolbar = itemView.findViewById(R.id.fileToolbar);
        }
    }
}
