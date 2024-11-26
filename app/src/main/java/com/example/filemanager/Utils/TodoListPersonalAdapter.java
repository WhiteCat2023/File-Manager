package com.example.filemanager.Utils;

import android.content.Context;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.filemanager.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class TodoListPersonalAdapter extends RecyclerView.Adapter<TodoListPersonalAdapter.ViewHolder>  {
    private List<TodoListItemPersonal> tasks;
    private OnDeleteClickListener onDeleteClickListener;

    public TodoListPersonalAdapter(List<TodoListItemPersonal> tasks, OnDeleteClickListener onDeleteClickListener) {
        this.tasks = tasks;
        this.onDeleteClickListener = onDeleteClickListener;
    }
    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }

    @NonNull
    @Override
    public TodoListPersonalAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.todo_list_item, parent, false);
        return new TodoListPersonalAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TodoListPersonalAdapter.ViewHolder holder, int position) {
        TodoListItemPersonal item = tasks.get(position);
        holder.taskName.setText(item.getTaskName());

        // Apply strikethrough if the task is completed
        if (item.isComplete()) {
            holder.taskName.setPaintFlags(holder.taskName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.taskName.setPaintFlags(holder.taskName.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        }

        holder.status.setText(item.getStatus());
        holder.startDate.setText(item.getStartDate());
        holder.endDate.setText(item.getEndDate());
        holder.isComplete.setChecked(item.isComplete());

        // Update the strikethrough based on checkbox state
        holder.isComplete.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                holder.taskName.setPaintFlags(holder.taskName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                item.setComplete(true);  // Update the task completion status
            } else {
                holder.taskName.setPaintFlags(holder.taskName.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                item.setComplete(false);  // Update the task completion status
            }

            // Optionally, update the task's completion status in the database here
            // updateTaskCompletion(item.getTaskId(), isChecked, buttonView.getContext());
        });

        holder.optionMenu.setOnClickListener(v -> showPopupMenu(v, position));
    }


    private void showPopupMenu(View view, int position) {
        // Creating a popup menu
        PopupMenu popup = new PopupMenu(view.getContext(), view);
        popup.inflate(R.menu.todo_item_utils); // Inflate the menu

        // Handle menu item clicks
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.todoDelete) {

                TodoListItemPersonal itemsToDelete = tasks.get(position);
                deleteFromDatabase(itemsToDelete.getTaskId(), view.getContext(), ()->{
                    tasks.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(view.getContext(), "Task Deleted", Toast.LENGTH_SHORT).show();
                });
                return true;
            }
            return false;
        });

        // Show the popup menu
        popup.show();
    }

    private void deleteFromDatabase(int taskId, Context context, Runnable onSuccess) {
        String deleteUrl ="https://skcalamba.scarlet2.io/android_api/todo/delete_todo_item.php";

        JSONObject itemsToDelete = new JSONObject();
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        try{
            itemsToDelete.put("taskId", taskId);
        }catch (JSONException e){
            e.printStackTrace();
            Toast.makeText(context, "Error parsing JSON", Toast.LENGTH_SHORT).show();
            Log.e("SharedTask", "JSON parsing error: " + e.getMessage());
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                deleteUrl,
                itemsToDelete,
                response -> {
                    Toast.makeText(context, "Task Deleted", Toast.LENGTH_SHORT).show();
                    onSuccess.run();
                },error -> {
            error.printStackTrace();
            Toast.makeText(context, "Error deleting task", Toast.LENGTH_SHORT).show();
        });
        requestQueue.add(request);
    }



    @Override
    public int getItemCount() {
        return tasks != null ? tasks.size(): 0;
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView taskName, status, startDate, endDate;
        CheckBox isComplete;
        ImageView optionMenu;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            isComplete = itemView.findViewById(R.id.todoCheckBox);
            taskName = itemView.findViewById(R.id.todoTaskName);
            status = itemView.findViewById(R.id.todoTaskStatus);
            startDate = itemView.findViewById(R.id.startDate);
            endDate = itemView.findViewById(R.id.endDate);
            optionMenu = itemView.findViewById(R.id.optionMenu);
        }
    }

}
