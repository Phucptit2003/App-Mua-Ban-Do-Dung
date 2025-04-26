package com.example.app.Model;

public class Accessory {
    private int imgSrc;
    private String name;
    private Double price;

    public Accessory() {
    }

    public Accessory(int imgSrc, String name, Double price) {
        this.imgSrc = imgSrc;
        this.name = name;
        price = price;
    }

    public int getImgSrc() {
        return imgSrc;
    }

    public void setImgSrc(int imgSrc) {
        this.imgSrc = imgSrc;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}
