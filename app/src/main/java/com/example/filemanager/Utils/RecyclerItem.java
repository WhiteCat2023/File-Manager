package com.example.filemanager.Utils;

import com.example.filemanager.Fragments.Files;

public class RecyclerItem {
    private final String FileName;
    private final String FileSize;
    private final String FileDate;
    private final String OriginalFilePath;


    private final boolean isDirectory;
    public RecyclerItem(
            String FileName,
            String FileSize,
            String FileDate,
            boolean isDirectory,
            String OriginalFilePath){

        this.FileName = FileName;
        this.FileSize = FileSize;
        this.FileDate = FileDate;
        this.isDirectory = isDirectory;
        this.OriginalFilePath = OriginalFilePath;
    }

    //Getters

    public String getFileDate() {
        return "Date: " + FileDate;
    }

    public String getFileName() {
        return FileName;
    }

    public String getFileSize() {
        return "Size: " + FileSize;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public String getOriginalFilePath() {
        return OriginalFilePath;
    }

}
