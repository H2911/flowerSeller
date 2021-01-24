package com.imperfection.kimlongflower;

public class Item {
    private String name,quantity,price,unit;
    public Item(String name, String quantity, String price,String unit){
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.unit = unit;
    }

    public String getSumOfItem(){
        int sum = Integer.parseInt(price)*Integer.parseInt(quantity);
        return String.valueOf(sum);
    }

    public String getName() {
        return name;
    }

    public String getQuantity() {
        return quantity;
    }

    public String getPrice() {
        return price;
    }

    public String getUnit() {
        return unit;
    }
}
