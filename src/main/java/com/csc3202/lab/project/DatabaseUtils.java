package com.csc3202.lab.project;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseUtils {
    public static void addUser(Connection connection, String username) {
        String sql = "INSERT INTO USERS (USERNAME) VALUES (?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.executeUpdate();
            System.out.println("User added: " + username);
        } catch (SQLException e) {
            System.err.println("Failed to add user: " + username);
            e.printStackTrace();
        }
    }

    public static void addMessage(Connection connection, String sender, String content) {
        String sql = "INSERT INTO MESSAGES (SENDER, CONTENT) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, sender);
            stmt.setString(2, content);
            stmt.executeUpdate();
            System.out.println("Message added: " + content);
        } catch (SQLException e) {
            System.err.println("Failed to add message.");
            e.printStackTrace();
        }
    }

    public static void getMessages(Connection connection) {
        String sql = "SELECT * FROM MESSAGES ORDER BY TIMESTAMP";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                System.out.println("[" + rs.getTimestamp("TIMESTAMP") + "] " +
                                   rs.getString("SENDER") + ": " +
                                   rs.getString("CONTENT"));
            }
        } catch (SQLException e) {
            System.err.println("Failed to retrieve messages.");
            e.printStackTrace();
        }
    }
}
