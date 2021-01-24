package com.imperfection.kimlongflower;

public class Company {
    private String companyName;
    private String address;
    private String phone1;
    private String phone2;
    private String typeOfCompany;
    private String province;
    private String city;

    public Company(){

    }

    public Company(String typeOfCompany, String companyName, String address, String province, String city, String phone1, String phone2) {
        this.typeOfCompany = typeOfCompany;
        this.companyName = companyName;
        this.address = address;
        this.province = province;
        this.city = city;
        this.phone1 = phone1;
        this.phone2 = phone2;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone1() {
        return phone1;
    }

    public String getPhone2() {
        return phone2;
    }

    public String getTypeOfCompany() {
        return typeOfCompany;
    }

    public String getProvince() {
        return province;
    }

    public String getCity() {
        return city;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPhone1(String phone1) {
        this.phone1 = phone1;
    }

    public void setPhone2(String phone2) {
        this.phone2 = phone2;
    }

    public void setTypeOfCompany(String typeOfCompany) {
        this.typeOfCompany = typeOfCompany;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
