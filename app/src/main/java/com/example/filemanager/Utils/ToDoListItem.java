package com.example.filemanager.Utils;

public class ToDoListItem {
    private String taskName;
    private String status;
    private String date;
    private boolean checkbox;

    ToDoListItem(String taskName, String status, String date, boolean checkbox) {
        this.taskName = taskName;
        this.status = status;
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public String getStatus() {
        return status;
    }

    public String getTaskName() {
        return taskName;
    }
    public boolean isChecked() {
        return checkbox;
    }
    public void setCompleted(boolean completed) {
        checkbox = completed;
    }
}
