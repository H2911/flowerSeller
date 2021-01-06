package com.example.kimlongflower;

public class Transaction {
    private String performer;
    private String action;
    private String changeValue;
    private String date;
    private String time;

    public Transaction(String date, String time, String performer,String action, String changeValue){
        this.date = date;
        this.time = time;
        this.performer = performer;
        this.action = action;
        this.changeValue = changeValue;
    }

    public String getPerformer() {
        return performer;
    }

    public String getAction() {
        return action;
    }

    public String getChangeValue() {
        return changeValue;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }
}
