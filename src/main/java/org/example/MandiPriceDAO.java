package org.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MandiPriceDAO {

    /**
     * Looks up the cached mandi price for a given crop + area.
     * Returns null if no cached price exists yet for that combination.
     */
    public Double getCachedMandiPrice(String cropName, String area) {
        String sql = "SELECT price FROM mandi_prices WHERE crop_name = ? AND area = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cropName);
            stmt.setString(2, area);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("price");
            }

        } catch (SQLException e) {
            System.out.println("Error fetching cached mandi price: " + e.getMessage());
        }

        return null; // no cached price found
    }
}