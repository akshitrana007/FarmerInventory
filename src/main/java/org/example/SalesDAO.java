package org.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SalesDAO {

    // Record a sale AND reduce product stock together (as one transaction)
    public boolean recordSale(Sale sale) {
        String insertSaleSql = "INSERT INTO sales (product_id, customer_id, farmer_id, quantity_sold, total_amount) VALUES (?, ?, ?, ?, ?)";
        String updateStockSql = "UPDATE products SET stock_quantity = stock_quantity - ? WHERE product_id = ? AND stock_quantity >= ?";

        Connection conn = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // start transaction

            // Step 1: reduce stock (only if enough stock exists)
            PreparedStatement stockStmt = conn.prepareStatement(updateStockSql);
            stockStmt.setInt(1, sale.getQuantitySold());
            stockStmt.setInt(2, sale.getProductId());
            stockStmt.setInt(3, sale.getQuantitySold());
            int stockRows = stockStmt.executeUpdate();

            if (stockRows == 0) {
                // Not enough stock, cancel everything
                conn.rollback();
                System.out.println("Sale failed: not enough stock available.");
                return false;
            }

            // Step 2: insert the sale record
            PreparedStatement saleStmt = conn.prepareStatement(insertSaleSql);
            saleStmt.setInt(1, sale.getProductId());
            saleStmt.setInt(2, sale.getCustomerId());
            saleStmt.setInt(3, sale.getFarmerId());
            saleStmt.setInt(4, sale.getQuantitySold());
            saleStmt.setDouble(5, sale.getTotalAmount());
            saleStmt.executeUpdate();

            conn.commit(); // both steps succeeded, save permanently
            return true;

        } catch (SQLException e) {
            System.out.println("Error recording sale: " + e.getMessage());
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                System.out.println("Rollback failed: " + ex.getMessage());
            }
            return false;

        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.out.println("Error resetting connection: " + e.getMessage());
            }
        }
    }

    // Get all sales history
    public List<Sale> getAllSales(int farmerId) {
        List<Sale> sales = new ArrayList<>();
        String sql = "SELECT * FROM sales WHERE farmer_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, farmerId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Sale s = new Sale(
                        rs.getInt("sale_id"),
                        rs.getInt("product_id"),
                        rs.getInt("customer_id"),
                        rs.getInt("farmer_id"),
                        rs.getInt("quantity_sold"),
                        rs.getDouble("total_amount"),
                        rs.getTimestamp("sale_date")
                );
                sales.add(s);
            }

        } catch (SQLException e) {
            System.out.println("Error fetching sales: " + e.getMessage());
        }

        return sales;
    }
    // Get total number of sales transactions
    public int getTotalSalesCount(int farmerId) {
        String sql = "SELECT COUNT(*) AS total FROM sales WHERE farmer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, farmerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("total");
        } catch (SQLException e) {
            System.out.println("Error getting total sales: " + e.getMessage());
        }
        return 0;
    }

    // Get total revenue from all sales
    public double getTotalRevenue(int farmerId) {
        String sql = "SELECT SUM(total_amount) AS total FROM sales WHERE farmer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, farmerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getDouble("total");
        } catch (SQLException e) {
            System.out.println("Error getting total revenue: " + e.getMessage());
        }
        return 0.0;
    }
}