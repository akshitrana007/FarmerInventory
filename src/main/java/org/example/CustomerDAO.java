package org.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

    // Add a new customer (old method, kept as-is for anything still using it)
    public boolean addCustomer(Customer customer) {
        String sql = "INSERT INTO customers (customer_name, contact_number, address) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, customer.getCustomerName());
            stmt.setString(2, customer.getContactNumber());
            stmt.setString(3, customer.getAddress());

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.out.println("Error adding customer: " + e.getMessage());
            return false;
        }
    }

    // Register a new customer WITH login credentials
    public boolean registerCustomer(Customer customer) {
        String sql = "INSERT INTO customers (customer_name, contact_number, address, username, password) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, customer.getCustomerName());
            stmt.setString(2, customer.getContactNumber());
            stmt.setString(3, customer.getAddress());
            stmt.setString(4, customer.getUsername());
            stmt.setString(5, customer.getPassword());

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.out.println("Error registering customer: " + e.getMessage());
            return false;
        }
    }

    // Check login credentials, return the matching Customer if valid, else null
    public Customer login(String username, String password) {
        String sql = "SELECT * FROM customers WHERE username = ? AND password = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Customer(
                        rs.getInt("customer_id"),
                        rs.getString("customer_name"),
                        rs.getString("contact_number"),
                        rs.getString("address"),
                        rs.getString("username"),
                        rs.getString("password")
                );
            }

        } catch (SQLException e) {
            System.out.println("Error during customer login: " + e.getMessage());
        }

        return null;
    }

    // Get all customers
    public List<Customer> getAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM customers";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Customer c = new Customer(
                        rs.getInt("customer_id"),
                        rs.getString("customer_name"),
                        rs.getString("contact_number"),
                        rs.getString("address")
                );
                customers.add(c);
            }

        } catch (SQLException e) {
            System.out.println("Error fetching customers: " + e.getMessage());
        }

        return customers;
    }

    // Search customer by name
    public List<Customer> searchCustomer(String keyword) {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM customers WHERE customer_name LIKE ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Customer c = new Customer(
                        rs.getInt("customer_id"),
                        rs.getString("customer_name"),
                        rs.getString("contact_number"),
                        rs.getString("address")
                );
                customers.add(c);
            }

        } catch (SQLException e) {
            System.out.println("Error searching customers: " + e.getMessage());
        }

        return customers;
    }
}