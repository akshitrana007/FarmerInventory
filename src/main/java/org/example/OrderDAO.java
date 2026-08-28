package org.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    // Customer creates a new order request
    public boolean createOrder(int customerId, int farmerId, int productId, int quantity) {
        String sql = "INSERT INTO orders (customer_id, farmer_id, product_id, quantity, status) " +
                "VALUES (?, ?, ?, ?, 'PENDING')";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, customerId);
            stmt.setInt(2, farmerId);
            stmt.setInt(3, productId);
            stmt.setInt(4, quantity);

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.out.println("Error creating order: " + e.getMessage());
            return false;
        }
    }

    // Farmer views all PENDING order requests sent to them
    public List<Order> getPendingOrdersForFarmer(int farmerId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, c.customer_name, c.contact_number, p.product_name, p.price_per_unit " +
                "FROM orders o " +
                "JOIN customers c ON o.customer_id = c.customer_id " +
                "JOIN products p ON o.product_id = p.product_id " +
                "WHERE o.farmer_id = ? AND o.status = 'PENDING' " +
                "ORDER BY o.requested_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, farmerId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Order o = mapRow(rs);
                orders.add(o);
            }

        } catch (SQLException e) {
            System.out.println("Error fetching pending orders: " + e.getMessage());
        }

        return orders;
    }

    // Customer views all their own orders (any status)
    public List<Order> getOrdersForCustomer(int customerId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, c.customer_name, c.contact_number, p.product_name, p.price_per_unit " +
                "FROM orders o " +
                "JOIN customers c ON o.customer_id = c.customer_id " +
                "JOIN products p ON o.product_id = p.product_id " +
                "WHERE o.customer_id = ? " +
                "ORDER BY o.requested_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Order o = mapRow(rs);
                orders.add(o);
            }

        } catch (SQLException e) {
            System.out.println("Error fetching customer orders: " + e.getMessage());
        }

        return orders;
    }

    // Farmer accepts an order: mark ACCEPTED, reduce stock, record a sale
    public boolean acceptOrder(int orderId) {
        String selectSql = "SELECT * FROM orders WHERE order_id = ?";
        String updateOrderSql = "UPDATE orders SET status = 'ACCEPTED', responded_at = NOW() WHERE order_id = ?";
        String updateStockSql = "UPDATE products SET stock_quantity = stock_quantity - ? WHERE product_id = ? AND stock_quantity >= ?";
        String insertSaleSql = "INSERT INTO sales (product_id, customer_id, farmer_id, quantity_sold, total_amount) " +
                "VALUES (?, ?, ?, ?, ?)";

        Connection conn = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            int customerId, farmerId, productId, quantity;

            try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                selectStmt.setInt(1, orderId);
                ResultSet rs = selectStmt.executeQuery();

                if (!rs.next()) {
                    conn.rollback();
                    return false;
                }

                customerId = rs.getInt("customer_id");
                farmerId = rs.getInt("farmer_id");
                productId = rs.getInt("product_id");
                quantity = rs.getInt("quantity");
            }

            double pricePerUnit;
            try (PreparedStatement priceStmt = conn.prepareStatement("SELECT price_per_unit FROM products WHERE product_id = ?")) {
                priceStmt.setInt(1, productId);
                ResultSet rs = priceStmt.executeQuery();
                if (!rs.next()) {
                    conn.rollback();
                    return false;
                }
                pricePerUnit = rs.getDouble("price_per_unit");
            }

            try (PreparedStatement stockStmt = conn.prepareStatement(updateStockSql)) {
                stockStmt.setInt(1, quantity);
                stockStmt.setInt(2, productId);
                stockStmt.setInt(3, quantity);
                int rowsUpdated = stockStmt.executeUpdate();

                if (rowsUpdated == 0) {
                    // Not enough stock
                    conn.rollback();
                    return false;
                }
            }

            double totalAmount = pricePerUnit * quantity;
            try (PreparedStatement saleStmt = conn.prepareStatement(insertSaleSql)) {
                saleStmt.setInt(1, productId);
                saleStmt.setInt(2, customerId);
                saleStmt.setInt(3, farmerId);
                saleStmt.setInt(4, quantity);
                saleStmt.setDouble(5, totalAmount);
                saleStmt.executeUpdate();
            }

            try (PreparedStatement updateOrderStmt = conn.prepareStatement(updateOrderSql)) {
                updateOrderStmt.setInt(1, orderId);
                updateOrderStmt.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            System.out.println("Error accepting order: " + e.getMessage());
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                System.out.println("Rollback failed: " + ex.getMessage());
            }
            return false;

        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException ex) {
                System.out.println("Error closing connection: " + ex.getMessage());
            }
        }
    }

    // Farmer rejects an order
    public boolean rejectOrder(int orderId) {
        String sql = "UPDATE orders SET status = 'REJECTED', responded_at = NOW() WHERE order_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, orderId);
            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.out.println("Error rejecting order: " + e.getMessage());
            return false;
        }
    }

    // Helper: turns one row of the ResultSet into an Order object
    private Order mapRow(ResultSet rs) throws SQLException {
        Order o = new Order(
                rs.getInt("order_id"),
                rs.getInt("customer_id"),
                rs.getInt("farmer_id"),
                rs.getInt("product_id"),
                rs.getInt("quantity"),
                rs.getString("status"),
                rs.getTimestamp("requested_at"),
                rs.getTimestamp("responded_at")
        );
        o.setCustomerName(rs.getString("customer_name"));
        o.setCustomerContact(rs.getString("contact_number"));
        o.setProductName(rs.getString("product_name"));
        o.setPricePerUnit(rs.getDouble("price_per_unit"));
        return o;
    }
}