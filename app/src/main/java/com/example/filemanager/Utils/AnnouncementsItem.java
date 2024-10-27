package com.example.filemanager.Utils;

public class AnnouncementsItem {
    private int annId;
    private String title;
    private String description;
    private String createdBy;
    private String date;

    public AnnouncementsItem(int annId, String title, String description, String createdBy, String date){
        this.title = title;
        this.annId = annId;
        this.description = description;
        this.createdBy = createdBy;
        this.date = date;

    }

    public String getDescription() {
        return description;
    }

    public String getTitle() {
        return title;
    }

    public String getCreatedBy() {
        return createdBy;
    }
    public String getDate() {
        return date;
    }

    public int getAnnId(){
        return annId;
    }

}
