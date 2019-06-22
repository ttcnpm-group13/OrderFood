package com.example.eat.Model;

public class Notification {
    public String body;
    public String tile;

    public Notification(String body, String tile) {
        this.body = body;
        this.tile = tile;
    }

    public Notification() {
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTile() {
        return tile;
    }

    public void setTile(String tile) {
        this.tile = tile;
    }
}
