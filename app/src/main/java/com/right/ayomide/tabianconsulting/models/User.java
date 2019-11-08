package com.right.ayomide.tabianconsulting.models;

public class User {
    private String name, phone, profile_image, user_id, messaging_token, department;

    public User() {
    }

    public User(String name, String phone, String profile_image, String user_id, String messaging_token, String department) {
        this.name = name;
        this.phone = phone;
        this.profile_image = profile_image;
        this.user_id = user_id;
        this.messaging_token = messaging_token;
        this.department = department;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getProfile_image() {
        return profile_image;
    }

    public void setProfile_image(String profile_image) {
        this.profile_image = profile_image;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getMessaging_token() {
        return messaging_token;
    }

    public void setMessaging_token(String messaging_token) {
        this.messaging_token = messaging_token;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    @Override
    public String toString(){
        return "User{" +
                "name ='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", profile_image='" + profile_image +'\'' +
                '}';
    }

}
