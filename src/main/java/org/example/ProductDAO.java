package org.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    // Add a new product
    public boolean addProduct(Product product) {
        String sql = "INSERT INTO products (farmer_id, product_name, category, stock_quantity, unit, price_per_unit, low_stock_threshold) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, product.getFarmerId());
            stmt.setString(2, product.getProductName());
            stmt.setString(3, product.getCategory());
            stmt.setInt(4, product.getStockQuantity());
            stmt.setString(5, product.getUnit());
            stmt.setDouble(6, product.getPricePerUnit());
            stmt.setInt(7, product.getLowStockThreshold());

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.out.println("Error adding product: " + e.getMessage());
            return false;
        }
    }

    // Get all products
    public List<Product> getProductsByFarmer(int farmerId) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE farmer_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, farmerId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Product p = new Product(
                        rs.getInt("product_id"),
                        rs.getInt("farmer_id"),
                        rs.getString("product_name"),
                        rs.getString("category"),
                        rs.getInt("stock_quantity"),
                        rs.getString("unit"),
                        rs.getDouble("price_per_unit"),
                        rs.getInt("low_stock_threshold")
                );
                products.add(p);
            }

        } catch (SQLException e) {
            System.out.println("Error fetching products: " + e.getMessage());
        }

        return products;
    }

    // Update stock quantity only (kept for backward compatibility)
    public boolean updateStock(int productId, int newQuantity) {
        String sql = "UPDATE products SET stock_quantity = ? WHERE product_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, newQuantity);
            stmt.setInt(2, productId);

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.out.println("Error updating stock: " + e.getMessage());
            return false;
        }
    }

    // ---- NEW: Update every editable field of a product at once ----
    public boolean updateProduct(int productId, String name, String category, int stock,
                                 String unit, double price, int threshold) {
        String sql = "UPDATE products SET product_name = ?, category = ?, stock_quantity = ?, " +
                "unit = ?, price_per_unit = ?, low_stock_threshold = ? WHERE product_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setString(2, category);
            stmt.setInt(3, stock);
            stmt.setString(4, unit);
            stmt.setDouble(5, price);
            stmt.setInt(6, threshold);
            stmt.setInt(7, productId);

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.out.println("Error updating product: " + e.getMessage());
            return false;
        }
    }

    // Delete a product
    public boolean deleteProduct(int productId) {
        String sql = "DELETE FROM products WHERE product_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, productId);

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.out.println("Error deleting product: " + e.getMessage());
            return false;
        }
    }

    // Search products by name
    public List<Product> searchProduct(String keyword, int farmerId) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE product_name LIKE ? AND farmer_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + keyword + "%");
            stmt.setInt(2, farmerId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Product p = new Product(
                        rs.getInt("product_id"),
                        rs.getInt("farmer_id"),
                        rs.getString("product_name"),
                        rs.getString("category"),
                        rs.getInt("stock_quantity"),
                        rs.getString("unit"),
                        rs.getDouble("price_per_unit"),
                        rs.getInt("low_stock_threshold")
                );
                products.add(p);
            }

        } catch (SQLException e) {
            System.out.println("Error searching products: " + e.getMessage());
        }

        return products;
    }

    // Search products by name across ALL farmers (for customer browsing)
    public List<Product> searchProductsAllFarmers(String keyword) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE product_name LIKE ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Product p = new Product(
                        rs.getInt("product_id"),
                        rs.getInt("farmer_id"),
                        rs.getString("product_name"),
                        rs.getString("category"),
                        rs.getInt("stock_quantity"),
                        rs.getString("unit"),
                        rs.getDouble("price_per_unit"),
                        rs.getInt("low_stock_threshold")
                );
                products.add(p);
            }

        } catch (SQLException e) {
            System.out.println("Error searching products across farmers: " + e.getMessage());
        }

        return products;
    }

    // Get total number of distinct products
    public int getTotalProducts(int farmerId) {
        String sql = "SELECT COUNT(*) AS total FROM products WHERE farmer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, farmerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("total");
        } catch (SQLException e) {
            System.out.println("Error getting total products: " + e.getMessage());
        }
        return 0;
    }

    // Get total stock across all products
    public int getTotalStock(int farmerId) {
        String sql = "SELECT SUM(stock_quantity) AS total FROM products WHERE farmer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, farmerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("total");
        } catch (SQLException e) {
            System.out.println("Error getting total stock: " + e.getMessage());
        }
        return 0;
    }

    // Get count of low-stock products
    public int getLowStockCount(int farmerId) {
        String sql = "SELECT COUNT(*) AS total FROM products WHERE stock_quantity <= low_stock_threshold AND farmer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, farmerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("total");
        } catch (SQLException e) {
            System.out.println("Error getting low stock count: " + e.getMessage());
        }
        return 0;
    }

    // Get list of low-stock products
    public List<Product> getLowStockProducts(int farmerId) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE stock_quantity <= low_stock_threshold AND farmer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, farmerId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Product p = new Product(
                        rs.getInt("product_id"),
                        rs.getInt("farmer_id"),
                        rs.getString("product_name"),
                        rs.getString("category"),
                        rs.getInt("stock_quantity"),
                        rs.getString("unit"),
                        rs.getDouble("price_per_unit"),
                        rs.getInt("low_stock_threshold")
                );
                products.add(p);
            }
        } catch (SQLException e) {
            System.out.println("Error getting low stock products: " + e.getMessage());
        }
        return products;
    }
}