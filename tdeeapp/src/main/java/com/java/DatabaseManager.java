package com.java;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private Connection connection;

    // Méthode pour se connecter à la base de données SQLite
    public boolean connect() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:src/test/resources/bddTdee.db");
            return true;
        } catch (SQLException e) {
            System.err.println("Erreur de connexion : " + e.getMessage());
            return false;
        }
    }

    // Méthode pour fermer la connexion
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la fermeture de la connexion : " + e.getMessage());
        }
    }

    public void initializeDatabase() {
        String createWeightHistoryTable = """
            CREATE TABLE IF NOT EXISTS WeightHistory (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                date DATE NOT NULL,
                weight FLOAT NOT NULL
            );
        """;

        String createCaloriesEatenTable = """
            CREATE TABLE IF NOT EXISTS CaloriesEaten (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                date DATE NOT NULL,
                calories INTEGER NOT NULL
            );
        """;

        String createWeightTable = """
                CREATE TABLE IF NOT EXISTS weight (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    date TEXT NOT NULL,
                    value REAL NOT NULL
                );
            """;


        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createWeightHistoryTable);
            stmt.execute(createCaloriesEatenTable);
            {{stmt.execute(createWeightTable);}} // Ajout de la création de la table 'weight'
        } catch (SQLException e) {
            System.out.println("Error initializing database: " + e.getMessage());
        }
    }

    // Méthode pour obtenir la connexion
    public Connection getConnection() {
        return connection;
    }

    // Méthode pour reset les tests
    public void executeUpdate(String sql) {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace(); 
        }
    }
}
