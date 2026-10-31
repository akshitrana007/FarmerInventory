package org.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FeedbackDAO {

    public boolean addFeedback(Feedback feedback) {
        String sql = "INSERT INTO feedback (order_id, customer_id, farmer_id, product_id, rating, comment) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, feedback.getOrderId());
            stmt.setInt(2, feedback.getCustomerId());
            stmt.setInt(3, feedback.getFarmerId());
            stmt.setInt(4, feedback.getProductId());
            stmt.setInt(5, feedback.getRating());
            stmt.setString(6, feedback.getComment());

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean hasFeedback(int orderId) {
        String sql = "SELECT feedback_id FROM feedback WHERE order_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Average rating for a specific product (used on CustomerDashboardFrame)
    public Double getAverageRating(int productId) {
        String sql = "SELECT AVG(rating) AS avg_rating FROM feedback WHERE product_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next() && rs.getObject("avg_rating") != null) {
                return rs.getDouble("avg_rating");
            }
            return null;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public int getRatingCount(int productId) {
        String sql = "SELECT COUNT(*) AS total FROM feedback WHERE product_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("total");
            }
            return 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    // ---- NEW: Farmer-facing methods ----

    // Overall average rating across ALL of this farmer's products
    public Double getAverageRatingForFarmer(int farmerId) {
        String sql = "SELECT AVG(rating) AS avg_rating FROM feedback WHERE farmer_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, farmerId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next() && rs.getObject("avg_rating") != null) {
                return rs.getDouble("avg_rating");
            }
            return null;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public int getRatingCountForFarmer(int farmerId) {
        String sql = "SELECT COUNT(*) AS total FROM feedback WHERE farmer_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, farmerId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("total");
            }
            return 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    // Full list of individual feedback entries for a farmer, with customer name + product name joined in
    public List<FeedbackDetail> getFeedbackForFarmer(int farmerId) {
        List<FeedbackDetail> list = new ArrayList<>();
        String sql = "SELECT c.customer_name, p.product_name, f.rating, f.comment, f.created_at " +
                "FROM feedback f " +
                "JOIN customers c ON f.customer_id = c.customer_id " +
                "JOIN products p ON f.product_id = p.product_id " +
                "WHERE f.farmer_id = ? " +
                "ORDER BY f.created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, farmerId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                FeedbackDetail detail = new FeedbackDetail();
                detail.setCustomerName(rs.getString("customer_name"));
                detail.setProductName(rs.getString("product_name"));
                detail.setRating(rs.getInt("rating"));
                detail.setComment(rs.getString("comment"));
                detail.setCreatedAt(rs.getTimestamp("created_at"));
                list.add(detail);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }
}