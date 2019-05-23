package com.example.eat.Model;

import com.facebook.share.widget.ShareDialog;

import java.util.List;

public class Request {
    private String phone;
    private  String name;
    private  String address;
    private String total;
    private String status; // tình trạng của đơn hàng.
    private List<Order> foods;// Danh sách các món được đặt
    private String comment;


    public Request() {
    }

    public Request(String phone, String name, String address, String total, String status, List<Order> foods, String comment) {
        this.phone = phone;
        this.name = name;
        this.address = address;
        this.total = total;
        this.status = status;
        this.foods = foods;
        this.comment = comment;

    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Order> getFoods() {
        return foods;
    }

    public void setFoods(List<Order> foods) {
        this.foods = foods;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

}

