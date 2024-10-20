package com.example.filemanager.Utils;

public class AnnouncementsItem {
    private int annId;
    private String title;
    private String description;

    public AnnouncementsItem(int annId, String title, String description){
        this.title = title;
        this.annId = annId;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String getTitle() {
        return title;
    }
}
