package com.example.kimlongflower;

import java.io.Serializable;

public class ItemsModel implements Serializable {
    private String name;
    private String quantity;
    private String unit;
    private int image;

    public ItemsModel(String name,String quantity,String unit,int image){
        this.name = name;
        this.quantity = quantity;
        this.image = image;
        this.unit = unit;
    }

    public String getUnit(){return unit;}

    public void setUnit(String unit){
        this.unit = unit;
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
