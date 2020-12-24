package com.example.kimlongflower;

import java.io.Serializable;

public class ItemsModel implements Serializable {
    private String name;
    private String quantity;
    private int image;

    public ItemsModel(String name,String quantity,int image){
        this.name = name;
        this.quantity = quantity;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public String getQuantity() {
        return quantity;
    }

    public int getImage() {
        return image;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public void setImage(int image) {
        this.image = image;
    }
}
