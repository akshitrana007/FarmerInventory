package org.example;

import java.sql.Timestamp;

public class Sale {

    private int saleId;
    private int productId;
    private int customerId;
    private int farmerId;
    private int quantitySold;
    private double totalAmount;
    private Timestamp saleDate;

    public Sale() {
    }

    public Sale(int saleId, int productId, int customerId, int farmerId, int quantitySold, double totalAmount, Timestamp saleDate) {
        this.saleId = saleId;
        this.productId = productId;
        this.customerId = customerId;
        this.farmerId = farmerId;
        this.quantitySold = quantitySold;
        this.totalAmount = totalAmount;
        this.saleDate = saleDate;
    }

    public int getSaleId() {
        return saleId;
    }

    public void setSaleId(int saleId) {
        this.saleId = saleId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(int farmerId) {
        this.farmerId = farmerId;
    }

    public int getQuantitySold() {
        return quantitySold;
    }

    public void setQuantitySold(int quantitySold) {
        this.quantitySold = quantitySold;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Timestamp getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(Timestamp saleDate) {
        this.saleDate = saleDate;
    }
}