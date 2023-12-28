package com.example.yolov8detect.entiy;

public class User {
    public int id;
    public String phone;
    public String pws;
    public String name;


    public User(){}

    public User(String phone, String pws, String name) {
        this.phone = phone;
        this.pws = pws;
        this.name = name;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ",phone='" + phone + '\'' +
                ", pws='" + pws + '\'' +
                ", sex='" + name + '}';
    }
}
