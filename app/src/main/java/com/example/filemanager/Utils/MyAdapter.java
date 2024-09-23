package com.example.filemanager.Utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filemanager.R;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    private List<RecyclerItem> fileItemList;

   public MyAdapter(List<RecyclerItem> fileItemList){
       this.fileItemList = fileItemList;
   }

    @NonNull
    @Override
    public MyAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_list_item, parent, false);
       return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyAdapter.MyViewHolder holder, int position) {
        RecyclerItem recyclerItem = fileItemList.get(position);
        holder.fileName.setText(recyclerItem.getFileName());
        holder.fileSize.setText(recyclerItem.getFileSize());
        holder.fileDate.setText(recyclerItem.getFileDate());
        holder.icon.setImageResource(recyclerItem.isDirectory() ? R.drawable.ic_folder : R.drawable.ic_file);
    }

    @Override
    public int getItemCount() {
        return fileItemList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

       TextView fileName, fileSize, fileDate;
       ImageView icon;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            fileName = itemView.findViewById(R.id.fileName);
            fileSize = itemView.findViewById(R.id.fileSize);
            fileDate = itemView.findViewById(R.id.Date);
            icon = itemView.findViewById(R.id.fileIcon);
        }
    }
}

