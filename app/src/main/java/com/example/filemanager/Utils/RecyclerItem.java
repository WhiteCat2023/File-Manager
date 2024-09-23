package com.example.filemanager.Utils;

public class RecyclerItem {
    private String FileName;
    private String FileSize;
    private String FileDate;

    private boolean isDirectory;
    public RecyclerItem(String FileName, String FileSize, String FileDate, boolean isDirectory){
        this.FileName = FileName;
        this.FileSize = FileSize;
        this.FileDate = FileDate;
        this.isDirectory = isDirectory;
    }

    //Getters

    public String getFileDate() {
        return FileDate;
    }

    public void setFileDate(String fileDate) {
        FileDate = fileDate;
    }

    public String getFileName() {
        return FileName;
    }

    public void setFileName(String fileName) {
        FileName = fileName;
    }

    public String getFileSize() {
        return FileSize;
    }

    public void setFileSize(String fileSize) {
        FileSize = fileSize;
    }

    public boolean isDirectory() {
        return isDirectory;
    }
}
