package com.example.filemanager.Utils;

public class RecyclerItem {
    private final String FileName;
    private final String FileSize;
    private final String FileDate;

    private final boolean isDirectory;
    public RecyclerItem(String FileName, String FileSize, String FileDate, boolean isDirectory){
        this.FileName = FileName;
        this.FileSize = FileSize;
        this.FileDate = FileDate;
        this.isDirectory = isDirectory;
    }

    //Getters

    public String getFileDate() {
        return "Date: " + FileDate;
    }

    public String getFileName() {
        return FileName;
    }

    public String getFileSize() {
        return "Size: " + FileSize + "MB";
    }

    public boolean isDirectory() {
        return isDirectory;
    }
}
