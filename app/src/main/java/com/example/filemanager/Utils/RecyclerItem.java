package com.example.filemanager.Utils;

import com.example.filemanager.Fragments.Files;

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
        return "Size: " + FileSize;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

 //   convertSize(Long.parseLong(FileSize))
//    //Size Conversion
//    public String convertSize(long sizeInBytes) {
//
//        String[] units = {"B", "KB", "MB", "GB"};
//        int unitIndex = 0;
//        double size = sizeInBytes;
//
//        while(size >= 1024 && unitIndex < units.length - 1){
//            size /= 1024;
//            unitIndex++;
//        }
//        return String.format("%.2f %s", size, units[unitIndex]);
//    }
}
