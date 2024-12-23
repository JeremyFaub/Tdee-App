package com.java;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CaloriesManager {
    private DatabaseManager databaseManager;

    public CaloriesManager(DatabaseManager dbManager) {
        this.databaseManager = dbManager;
    }

    // Ajouter des calories pour un repas
    public void addCalories(int calories, String description) {
        String sql = "INSERT INTO calories (date, description, amount) VALUES (?, ?, ?)";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, java.time.LocalDate.now().toString());
            pstmt.setString(2, description);
            pstmt.setInt(3, calories);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error adding calories: " + e.getMessage());
        }
    }

    // Obtenir la somme des calories pour aujourd'hui
    public int getCaloriesToday() {
        String sql = "SELECT SUM(amount) FROM calories WHERE date = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, java.time.LocalDate.now().toString());
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;

        } catch (SQLException e) {
            System.out.println("Error retrieving calories: " + e.getMessage());
            return 0;
        }
    }

    // Lister les repas d'aujourd'hui
    public List<String> listMealsToday() {
        List<String> meals = new ArrayList<>();
        String sql = "SELECT description, amount FROM calories WHERE date = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, java.time.LocalDate.now().toString());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                meals.add(rs.getString("description") + ": " + rs.getInt("amount") + " calories");
            }

        } catch (SQLException e) {
            System.out.println("Error listing meals: " + e.getMessage());
        }
        return meals;
    }
}

