package org.example;

import java.sql.Timestamp;

public class Order {

    private int orderId;
    private int customerId;
    private int farmerId;
    private int productId;
    private int quantity;
    private String status;
    private Timestamp requestedAt;
    private Timestamp respondedAt;

    // Extra display-only fields (not stored in the orders table itself,
    // but useful for showing details in the GUI without extra queries)
    private String customerName;
    private String customerContact;
    private String productName;
    private double pricePerUnit;

    public Order() {
    }

    public Order(int orderId, int customerId, int farmerId, int productId, int quantity,
                 String status, Timestamp requestedAt, Timestamp respondedAt) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.farmerId = farmerId;
        this.productId = productId;
        this.quantity = quantity;
        this.status = status;
        this.requestedAt = requestedAt;
        this.respondedAt = respondedAt;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
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

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(Timestamp requestedAt) {
        this.requestedAt = requestedAt;
    }

    public Timestamp getRespondedAt() {
        return respondedAt;
    }

    public void setRespondedAt(Timestamp respondedAt) {
        this.respondedAt = respondedAt;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerContact() {
        return customerContact;
    }

    public void setCustomerContact(String customerContact) {
        this.customerContact = customerContact;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public double getPricePerUnit() {
        return pricePerUnit;
    }

    public void setPricePerUnit(double pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
    }
}