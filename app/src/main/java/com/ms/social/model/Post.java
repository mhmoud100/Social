package com.ms.social.model;

public class Post {
    String UserId, UserName, Text, Date;

    public Post() {}

    public Post(String UserId,String UserName, String Text, String Date) {
        this.UserId = UserId;
        this.UserName = UserName;
        this.Text = Text;
        this.Date = Date;
    }


    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
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
