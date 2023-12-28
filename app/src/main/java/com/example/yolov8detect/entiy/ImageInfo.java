package com.example.yolov8detect.entiy;

public class ImageInfo {
    public long id;
    public String name;
    public long size;
    public String path;

    public String toString() {
        return "ImageInfo{" +
                "id=" + id +
                ",name=" + name +
                ",size=" + size +
                ",path=" + path +
                "}";
    }
}
