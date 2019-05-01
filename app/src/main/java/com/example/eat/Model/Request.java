package com.example.eat.Model;

import java.util.List;

public class Request {
    private String phone;
    private  String name;
    private  String address;
    private String total;
    private String status; // tình trạng của đơn hàng.
    private List<Order> foods;// Danh sách các món được đặt

    public Request() {
    }

    public Request(String phone, String name, String address, String total, List<Order> foods) {
        this.phone = phone;
        this.name = name;
        this.address = address;
        this.total = total;
        this.foods = foods;
        this.status = "0"; //Mặc định =0, 0 : đã đặt; 1 :Đang giao; 2 : Đã giao
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public List<Order> getFoods() {
        return foods;
    }

    public void setFoods(List<Order> foods) {
        this.foods = foods;
    }
}
