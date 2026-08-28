package org.example;

public class Farmer {

    private int farmerId;
    private String fullName;
    private String username;
    private String password;
    private String contactNumber;
    private String farmLocation;

    public Farmer() {
    }

    public Farmer(int farmerId, String fullName, String username, String password, String contactNumber, String farmLocation) {
        this.farmerId = farmerId;
        this.fullName = fullName;
        this.username = username;
        this.password = password;
        this.contactNumber = contactNumber;
        this.farmLocation = farmLocation;
    }

    public int getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(int farmerId) {
        this.farmerId = farmerId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getFarmLocation() {
        return farmLocation;
    }

    public void setFarmLocation(String farmLocation) {
        this.farmLocation = farmLocation;
    }
}