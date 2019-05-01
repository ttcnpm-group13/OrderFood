package com.example.eat.Model;

public class Category {
    private String name;
    private  String Image;

    public Category() {
    }

    public Category(String name, String image) {
        this.name = name;
        this.Image = image;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return this.Image;
    }

    public void setImage(String image) {
        this.Image = image;
    }
}
