package com.imperfection.kimlongflower;

public class User {
    String userName,userId, phoneNumber;

    public  User(){ }

    public User(String userName, String userId, String phoneNumber) {
        this.userName = userName;
        this.userId = userId;
        this.phoneNumber = phoneNumber;
    }

    public String getUserName (){
        return userName;
    }

    public String getPassword(){
        return userId;
    }

    public String getPhoneNumber(){
        return  phoneNumber;
    }

    public void setUserName(String userName){
        this.userName = userName;
    }

    public void setPassword(String password){
        this.userId = password;
    }

    public void setPhoneNumber(String phoneNumber){
        this.phoneNumber = phoneNumber;
    }
}
