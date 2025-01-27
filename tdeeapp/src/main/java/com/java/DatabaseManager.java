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
            connection = DriverManager.getConnection("jdbc:sqlite:tdeeapp\\bddTdee.db");

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

    // Méthode pour créer les tables initiales utilisées par l'application
    public void initializeDatabase() {

        // Pour la table du poids : ID, Date et Value
        String createWeightTable = """
                    CREATE TABLE IF NOT EXISTS Weight (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        date DATE NOT NULL,
                        value FLOAT NOT NULL
                    );
                """;

        // Pour la table des calories : ID, Date et Calories
        String createCaloriesEatenTable = """
                    CREATE TABLE IF NOT EXISTS CaloriesEaten (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        date DATE NOT NULL,
                        calories INTEGER NOT NULL
                    );
                """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createWeightTable);
            stmt.execute(createCaloriesEatenTable);
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
