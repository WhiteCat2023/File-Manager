package com.example.filemanager.Utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filemanager.R;

import java.util.List;

public class ToDoListAdapter extends RecyclerView.Adapter<ToDoListAdapter.ViewHolder> {

    private List<ToDoListItem> tasks;
    private OnDeleteClickListener onDeleteClickListener;

    public ToDoListAdapter(List<ToDoListItem> tasks, OnDeleteClickListener onDeleteClickListener) {
        this.tasks = tasks;
        this.onDeleteClickListener = onDeleteClickListener;
    }
    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }

    @NonNull
    @Override
    public ToDoListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.todo_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ToDoListAdapter.ViewHolder holder, int position) {
        ToDoListItem item = tasks.get(position);
        holder.taskName.setText(item.getTaskName());
        holder.status.setText(item.getStatus());
        holder.startDate.setText(item.getStartDate());
        holder.endDate.setText(item.getEndDate());
        holder.isComplete.setChecked(item.isComplete());
        holder.isComplete.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                onDeleteClickListener.onDeleteClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tasks != null ? tasks.size(): 0;
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView taskName, status, startDate, endDate;
        CheckBox isComplete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            isComplete = itemView.findViewById(R.id.todoCheckBox);
            taskName = itemView.findViewById(R.id.todoTaskName);
            status = itemView.findViewById(R.id.todoTaskStatus);
            startDate = itemView.findViewById(R.id.startDate);
            endDate = itemView.findViewById(R.id.endDate);
        }
    }
}
