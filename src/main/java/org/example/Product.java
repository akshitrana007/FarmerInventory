package org.example;

public class Product {

    private int productId;
    private int farmerId;
    private String productName;
    private String category;
    private int stockQuantity;
    private String unit;
    private double pricePerUnit;
    private int lowStockThreshold;

    public Product() {
    }

    public Product(int productId, int farmerId, String productName, String category, int stockQuantity, String unit, double pricePerUnit, int lowStockThreshold) {
        this.productId = productId;
        this.farmerId = farmerId;
        this.productName = productName;
        this.category = category;
        this.stockQuantity = stockQuantity;
        this.unit = unit;
        this.pricePerUnit = pricePerUnit;
        this.lowStockThreshold = lowStockThreshold;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(int farmerId) {
        this.farmerId = farmerId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public double getPricePerUnit() {
        return pricePerUnit;
    }

    public void setPricePerUnit(double pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
    }

    public int getLowStockThreshold() {
        return lowStockThreshold;
    }

    public void setLowStockThreshold(int lowStockThreshold) {
        this.lowStockThreshold = lowStockThreshold;
    }
}