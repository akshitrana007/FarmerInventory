package org.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MessageDAO {

    public boolean sendMessage(int senderId, String senderRole, int receiverId, String receiverRole, String content) {
        String sql = "INSERT INTO messages (sender_id, sender_role, receiver_id, receiver_role, content) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, senderId);
            stmt.setString(2, senderRole);
            stmt.setInt(3, receiverId);
            stmt.setString(4, receiverRole);
            stmt.setString(5, content);

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.out.println("Error sending message: " + e.getMessage());
            return false;
        }
    }

    // Fetches the full back-and-forth between two specific people, oldest first.
    public List<Message> getConversation(int userAId, String userARole, int userBId, String userBRole) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT * FROM messages WHERE " +
                "(sender_id = ? AND sender_role = ? AND receiver_id = ? AND receiver_role = ?) OR " +
                "(sender_id = ? AND sender_role = ? AND receiver_id = ? AND receiver_role = ?) " +
                "ORDER BY sent_at ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userAId);
            stmt.setString(2, userARole);
            stmt.setInt(3, userBId);
            stmt.setString(4, userBRole);

            stmt.setInt(5, userBId);
            stmt.setString(6, userBRole);
            stmt.setInt(7, userAId);
            stmt.setString(8, userARole);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                messages.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error fetching conversation: " + e.getMessage());
        }

        return messages;
    }

    // Total unread messages waiting for this person, across all conversations.
    public int getUnreadCount(int receiverId, String receiverRole) {
        String sql = "SELECT COUNT(*) AS unread_count FROM messages WHERE receiver_id = ? AND receiver_role = ? AND is_read = FALSE";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, receiverId);
            stmt.setString(2, receiverRole);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("unread_count");
            }

        } catch (SQLException e) {
            System.out.println("Error fetching unread count: " + e.getMessage());
        }

        return 0;
    }

    // Marks every message from one specific person as read, once their chat window is opened.
    public boolean markConversationRead(int receiverId, String receiverRole, int senderId, String senderRole) {
        String sql = "UPDATE messages SET is_read = TRUE WHERE receiver_id = ? AND receiver_role = ? AND sender_id = ? AND sender_role = ? AND is_read = FALSE";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, receiverId);
            stmt.setString(2, receiverRole);
            stmt.setInt(3, senderId);
            stmt.setString(4, senderRole);

            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println("Error marking conversation read: " + e.getMessage());
            return false;
        }
    }

    private Message mapRow(ResultSet rs) throws SQLException {
        return new Message(
                rs.getInt("message_id"),
                rs.getInt("sender_id"),
                rs.getString("sender_role"),
                rs.getInt("receiver_id"),
                rs.getString("receiver_role"),
                rs.getString("content"),
                rs.getTimestamp("sent_at")
        );
    }
}