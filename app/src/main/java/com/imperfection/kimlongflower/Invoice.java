package com.imperfection.kimlongflower;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Invoice {
    private final String date;
    private final String time;
    private final String name;
    private List<Item> listItem;
    private final String action;
    private int sum = 0;

    //Invoice with default date time
    public Invoice(String name, String action, List<Item> listItem){
        Date date = new Date();
        SimpleDateFormat formatterDate = new SimpleDateFormat("dd_MM_yyyy");
        SimpleDateFormat formatterTime = new SimpleDateFormat("hh:mm:ss");
        this.date = formatterDate.format(date);
        this.time = formatterTime.format(date);
        this.name = name;
        this.listItem = listItem;
        this.action = action;
        for (Item item : listItem) {
            sum +=Integer.parseInt(item.getSumOfItem());
            System.out.println(sum);
        }
    }

    //Invoice with exactly date time
    public Invoice(String name, String action, List<Item> listItem, String date,String time){
        this.date = date;
        this.time = time;
        this.name = name;
        this.listItem = listItem;
        this.action = action;
        for (Item item : listItem) {
            sum +=Integer.parseInt(item.getSumOfItem());
            System.out.println(sum);
        }
    }

    public int getSum() {
        return sum;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public List<Item> getListItem() {
        return listItem;
    }

    public String getName() {
        return name;
    }

    public String getAction(){return action;}

}

