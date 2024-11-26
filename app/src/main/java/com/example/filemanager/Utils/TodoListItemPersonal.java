package com.example.filemanager.Utils;

public class TodoListItemPersonal {
    private String taskName;
    private String status;
    private boolean isComplete;
    private String startDate;
    private String endDate;
    private int taskId;
    private String type;
    private String created_by;

    public TodoListItemPersonal(int taskId, String taskName, String status, boolean isComplete, String startDate, String endDate, String type, String created_by) {
        this.taskName = taskName;
        this.status = status;
        this.isComplete = isComplete;
        this.startDate = startDate;
        this.endDate = endDate;
        this.taskId = taskId;
        this.type = type;
        this.created_by = created_by;
    }

    public String getCreated_by() {
        return created_by;
    }

    public String getEndDate() {
        return endDate;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public String getStartDate() {
        return startDate;
    }

    public int getTaskId() {
        return taskId;
    }

    public String getStatus() {
        return status;
    }

    public String getTaskName() {
        return taskName;
    }

    public String getType() {
        return type;
    }
    public void setComplete(boolean b) {
        this.isComplete = b;
    }
}
