package com.java;

import java.sql.*;

public class CaloriesManager {
    private DatabaseManager dbManager;

    public static class CaloriesEntry {
        private double value;
        private String date;

        // Classe interne pour représenter une entrée de calories
        public CaloriesEntry(double value, String date) {
            this.value = value;
            this.date = date;
        }

        public double getValue() {
            return value;
        }

        public String getDate() {
            return date;
        }
    }

    public CaloriesManager(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    // Méthode pour ajouter les calories consommées pour aujourd'hui
    public boolean addCaloriesEntry(int caloriesEntry, String date) {
        // On commence par vérifier s'il n'y aucune entrée de poids à cette date
        // Si c'est pour aujourd'hui...
        if (date.equals(java.time.LocalDate.now().toString()) && caloriesExistAtDate(date)) {
            System.out.println("Une entrée de calories à déjà été entrée aujourd'hui.");
            return false;
        }

        // Si c'est à une autre date...
        if (caloriesExistAtDate(date)) {
            System.out.println("Une entrée de calories existe déjà pour " + date);
            return false;
        }

        // Puis on ajoute le nouveau poids si aucun poids est associée à la date spécifiée
        String query = "INSERT INTO CaloriesEaten (calories, date) VALUES (?, ?)"; 
        try {
            dbManager.connect();       
            Connection conn = dbManager.getConnection();    
            PreparedStatement stmt = conn.prepareStatement(query); 
            stmt.setInt(1, caloriesEntry); 
            stmt.setString(2, date);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Échec de l'insertion de l'entrée de calories.");
            }
            conn.close(); // Ferme la connexion
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout d'une entrée de calories : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    // Méthode pour vérifier s'il y a déjà eu une entrée de calories à une date spécifiée
    public boolean caloriesExistAtDate(String date) {
        String query = "SELECT COUNT(*) FROM CaloriesEaten WHERE date = ?";
        try {
            dbManager.connect();
            Connection conn = dbManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, date);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                boolean success = rs.getInt(1) > 0;
                conn.close(); // Ferme la connexion
                return success; 
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification de l'existence d'une entrée de poids : " + e.getMessage());
            e.printStackTrace();
        }
        return false; 
    }

    // Méthode pour afficher une table des calories consommées + date correspondante
    public boolean displayCaloriesTable() {
        String query = "SELECT date, calories FROM CaloriesEaten ORDER BY date ASC"; // Requête pour récupérer les entrées de calories
        try {
            dbManager.connect();
            Connection conn = dbManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            if (!rs.isBeforeFirst()) { // Vérifie si la table est vide
                System.out.println("La table des calories est vide.");
                conn.close(); // Ferme la connexion
                return false;
            } else {
                System.out.println("+-----------------+------------+"); // Ligne de séparation en haut
                System.out.println("|    Calories     |    Date    |"); // En-tête du tableau
                System.out.println("+-----------------+------------+"); // Ligne de séparation
                while (rs.next()) {
                    String date = rs.getString("date");
                    int calories = rs.getInt("calories");
                    System.out.printf("| %-15d | %-10s |\n", calories, date); // Formatage pour alignement
                }
                System.out.println("+-----------------+------------+"); // Ligne de séparation en bas
                conn.close(); // Ferme la connexion
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'affichage de la table des calories : " + e.getMessage());
            return false;
        }
    }

    // Méthode pour supprimer les calories consommées à une date spécifiée
    public boolean removeCaloriesEntry(String date) {
        String query = "DELETE FROM CaloriesEaten WHERE date = ?"; 
        try {
            dbManager.connect();
            Connection conn = dbManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, date);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                System.out.println("Aucune entrée de calories trouvée pour la date : " + date);
                return false;
            } 
            conn.close(); // Ferme la connexion
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de l'entrée de calories : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    // Méthode pour supprimer toutes les entrées de calories
    public boolean removeAllCaloriesEntries() {
        String query = "DELETE FROM CaloriesEaten"; 
        try {
            dbManager.connect();
            Connection conn = dbManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.executeUpdate();
            conn.close(); // Ferme la connexion
            System.out.println("Toutes les entrées de calories ont été supprimées avec succès !");
            return true;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de toutes les entrées de calories : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}

