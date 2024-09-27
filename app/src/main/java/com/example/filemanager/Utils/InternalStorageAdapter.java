//package com.example.filemanager.Utils;
//
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.PopupMenu;
//import android.widget.TextView;
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//import com.example.filemanager.R;
//import java.util.List;
//
//public class ServerStorageAdapter extends RecyclerView.Adapter<ServerStorageAdapter.ViewHolder> {
//    private final List<RecyclerItem> recyclerItems;
//    private final OnItemClickListener onItemClickListener;
//    private final OnItemActionListener actionListener;
//
//    // Functional interface for handling click events
//    @FunctionalInterface
//    public interface OnItemClickListener {
//        void onItemClick(RecyclerItem item);
//    }
//
//    // Interface for additional actions
//    public interface OnItemActionListener {
//        void onDownloadClick(RecyclerItem item);
//        void onDeleteClick(RecyclerItem item);
//    }
//
//    // Constructor for the adapter
//    public ServerStorageAdapter(List<RecyclerItem> recyclerItems, OnItemClickListener onItemClickListener, OnItemActionListener actionListener) {
//        this.recyclerItems = recyclerItems;
//        this.onItemClickListener = onItemClickListener;
//        this.actionListener = actionListener;
//    }
//
//    // Create new views (invoked by the layout manager)
//    @NonNull
//    @Override
//    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_list_item, parent, false);
//        return new ViewHolder(view);
//    }
//
//    // Replace the contents of a view (invoked by the layout manager)
//    @Override
//    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        RecyclerItem item = recyclerItems.get(position);
//
//        // Set text fields with file/folder data
//        holder.fileName.setText(item.getFileName());
//        holder.fileSize.setText(item.getFileSize());
//        holder.fileDate.setText(item.getFileDate());
//
//        // Change icon based on whether the item is a directory or a file
//        if (item.isDirectory()) {
//            holder.icon.setImageResource(R.drawable.ic_folder);
//            holder.fileToolbar.setOnClickListener(view -> {
//                PopupMenu popupMenu = new PopupMenu(view.getContext(), holder.fileToolbar);
//                popupMenu.inflate(R.menu.server_item_directory);
//
//                // Handle menu item clicks
//                popupMenu.setOnMenuItemClickListener(menuItem -> {
//                    if (menuItem.getItemId() == R.id.delete) {
//                        actionListener.onDeleteClick(item);  // Handle delete
//                        return true;
//                    }
//                    return false;
//                });
//                popupMenu.show();
//            });
//        } else {
//            holder.icon.setImageResource(R.drawable.ic_file);
//            // Set up the toolbar click to show the popup menu
//            holder.fileToolbar.setOnClickListener(view -> {
//                PopupMenu popupMenu = new PopupMenu(view.getContext(), holder.fileToolbar);
//                popupMenu.inflate(R.menu.item_list_utils); // Inflate your menu
//
//                // Handle menu item clicks
//                popupMenu.setOnMenuItemClickListener(menuItem -> {
//                    if (menuItem.getItemId() == R.id.download) {
//                        actionListener.onDownloadClick(item);  // Handle download
//                        return true;
//                    }
//                    if (menuItem.getItemId() == R.id.delete) {
//                        actionListener.onDeleteClick(item);  // Handle delete
//                        return true;
//                    }
//                    return false;
//                });
//
//                popupMenu.show(); // File icon for files
//            });
//        }
//
//        // Show the popup menu
//
//
//        // Handle the click event for the entire item view
//        holder.itemView.setOnClickListener(v -> {
//            if (onItemClickListener != null) {
//                onItemClickListener.onItemClick(item);  // Trigger click event for the item
//            }
//        });
//    }
//
//    // Return the size of the dataset (invoked by the layout manager)
//    @Override
//    public int getItemCount() {
//        return recyclerItems.size();
//    }
//
//    // Provide a reference to the type of views being used (custom ViewHolder)
//    public static class ViewHolder extends RecyclerView.ViewHolder {
//        TextView fileName, fileSize, fileDate;
//        ImageView icon;
//        ImageView fileToolbar;
//
//        public ViewHolder(@NonNull View itemView) {
//            super(itemView);
//            // Initialize views
//            fileName = itemView.findViewById(R.id.fileName);
//            fileSize = itemView.findViewById(R.id.fileSize);
//            fileDate = itemView.findViewById(R.id.Date);
//            icon = itemView.findViewById(R.id.fileIcon);
//            fileToolbar = itemView.findViewById(R.id.fileToolbar);
//        }
//    }
//}
