package com.example.filemanager.Utils;

public class UploadItem {
    private final String fileName;
    private final String fileSize;
    private int progress;

    public UploadItem(String fileName, String fileSize, int progress) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.progress = progress;
    }

    public String getFileName() {return fileName;}

    public String getFileSize() {return fileSize;}
    public int getProgress() {return progress;}

    public void setProgress(int progress) {this.progress = progress;}
}
