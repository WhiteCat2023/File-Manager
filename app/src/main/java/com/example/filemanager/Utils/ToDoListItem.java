package com.example.filemanager.Utils;

public class ToDoListItem {
    private String taskName;
    private String status;
    private boolean isComplete;
    private String startDate;
    private String endDate;
    private int taskId;

    public ToDoListItem(int taskId, String taskName, String status, boolean isComplete, String startDate, String endDate) {
        this.taskName = taskName;
        this.status = status;
        this.isComplete = isComplete;
        this.startDate = startDate;
        this.endDate = endDate;
        this.taskId = taskId;
    }
    public int getTaskId() {
        return taskId;
    }
    public String getTaskName() {
        return taskName;
    }
    public String getStatus() {
        return status;
    }
    public boolean isComplete() {
        return isComplete;
    }
    public void setCompleted(boolean completed) {
        isComplete = completed;
    }
    public String getEndDate() {
        return endDate;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getDescription() {
        return status;
    }

    public void setComplete(boolean b) {
        this.isComplete = b;
    }
}
