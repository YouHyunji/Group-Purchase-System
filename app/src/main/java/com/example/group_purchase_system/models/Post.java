package com.example.group_purchase_system.models;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Post {
    public Post(String title, String contents, String name) {
    }

    private  String documentId;
    private  String name;
    private  String title;
    private  String contents;

    @ServerTimestamp
    private Date date;

    public Post(String documentId, String title, String contents, String name) {
        this.documentId = documentId;
        this.name = name;
        this.title = title;
        this.contents = contents;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) { this.title = title; }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "Post{" +
                "documentId='" + documentId + '\'' +
                ", title='" + title + '\'' +
                ", contents='" + contents + '\'' +
                ", date=" + date +
                '}';
    }
}
