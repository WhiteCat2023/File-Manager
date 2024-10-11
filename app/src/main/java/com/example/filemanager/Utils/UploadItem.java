package com.example.filemanager.Utils;

public class UploadItem {
    private final String fileName;
    private final String fileSize;

    public UploadItem(String fileName, String fileSize) {
        this.fileName = fileName;
        this.fileSize = fileSize;

    }

    public String getFileName() {return fileName;}

    public String getFileSize() {return fileSize;}

}
