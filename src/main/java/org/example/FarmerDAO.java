package org.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FarmerDAO {

    // Check login credentials, return the matching Farmer if valid, else null
    public Farmer login(String username, String password) {
        String sql = "SELECT * FROM farmers WHERE username = ? AND password = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Farmer(
                        rs.getInt("farmer_id"),
                        rs.getString("full_name"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("contact_number"),
                        rs.getString("farm_location")
                );
            }

        } catch (SQLException e) {
            System.out.println("Error during login: " + e.getMessage());
        }

        return null; // no match found
    }

    // Register a new farmer
    public boolean registerFarmer(Farmer farmer) {
        String sql = "INSERT INTO farmers (full_name, username, password, contact_number, farm_location) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, farmer.getFullName());
            stmt.setString(2, farmer.getUsername());
            stmt.setString(3, farmer.getPassword());
            stmt.setString(4, farmer.getContactNumber());
            stmt.setString(5, farmer.getFarmLocation());

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.out.println("Error registering farmer: " + e.getMessage());
            return false;
        }
    }
}