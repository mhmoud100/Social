package com.ms.social.model;

import java.util.List;

public class Post {
    String UserId, Text, Date;
    List<String> Saved_by, Liked_by;
    List<Comment> Comments;

    public Post() {}

    public Post(String userId, String text, String date, List<String> saved_by, List<String> liked_by, List<Comment> comments) {
        UserId = userId;
        Text = text;
        Date = date;
        Saved_by = saved_by;
        Liked_by = liked_by;
        Comments = comments;
    }

    public List<String> getSaved_by() {
        return Saved_by;
    }

    public void setSaved_by(List<String> saved_by) {
        Saved_by = saved_by;
    }

    public List<String> getLiked_by() {
        return Liked_by;
    }

    public void setLiked_by(List<String> liked_by) {
        Liked_by = liked_by;
    }

    public List<Comment> getComments() {
        return Comments;
    }

    public void setComments(List<Comment> comments) {
        Comments = comments;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public String getText() {
        return Text;
    }

    public void setText(String text) {
        Text = text;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }


}
