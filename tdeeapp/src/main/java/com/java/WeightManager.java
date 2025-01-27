package com.java;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


// WeightManager dépend de DatabaseManager. On crée cette dépendance via un constructeur
// On crée une connection, puis on va la chercher pour effectuer le query. Dans un bloc try (...) {} ça ne
// fonctionne pas; la base de donnée n'est plus visible
public class WeightManager {
    private DatabaseManager dbManager;

    public WeightManager(DatabaseManager databaseManager) {
        this.dbManager = databaseManager;
    }

    // Classe interne pour représenter une entrée de poids
    public static class WeightEntry {
        private double value;
        private String date;

        public WeightEntry(double value, String date) {
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

    // Méthode pour ajouter un poids
    public boolean addWeightEntry(double weight, String date) {
        // On commence par vérifier s'il n'y aucune entrée de poids à cette date
        // Si c'est pour aujourd'hui...
        if (date.equals(java.time.LocalDate.now().toString()) && weightExistAtDate(date)) {
            System.out.println("Une entrée de poids à déjà été entrée aujourd'hui.");
            return false;
        }

        // Si c'est à une autre date...
        if (weightExistAtDate(date)) {
            System.out.println("Une entrée de poids existe déjà pour " + date);
            return false; 
        }

        // Puis on ajoute le nouveau poids si aucun poids est associée à la date spécifiée
        String query = "INSERT INTO weight (value, date) VALUES (?, ?)";
        try {
            dbManager.connect();       
            Connection conn = dbManager.getConnection();    
            PreparedStatement stmt = conn.prepareStatement(query); 
            stmt.setDouble(1, weight);
            stmt.setString(2, date);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Échec de l'insertion de l'entrée de poids.");
            }
            conn.close(); // Ferme la connexion
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout d'une entrée de poids : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    // Méthode pour récupérer le poids actuel
    public Double getLatestWeight() {
        String query = "SELECT value FROM weight ORDER BY date DESC LIMIT 1";
        try {
            dbManager.connect();
            Connection conn = dbManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
            {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getDouble("value");
                }
            }
            conn.close(); // Ferme la connexion
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération du poids : " + e.getMessage());
        }
        return null;
    }

    // Pour supprimer une entrée
    public boolean deleteWeightEntry(String date) {
        String query = "DELETE FROM weight WHERE date = ?";
        try {
            dbManager.connect();
            Connection conn = dbManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, date);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                System.out.println("Aucune entrée trouvée pour la date spécifiée.");
                return false;
            }          
            conn.close(); // Ferme la connexion
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de l'entrée de poids : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    // Méthode pour afficher la table des poids
    public boolean displayWeightTable() {
        String query = "SELECT date, value FROM weight ORDER BY date ASC";
        try {
            dbManager.connect();
            Connection conn = dbManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            if (!rs.isBeforeFirst()) { // Vérifie si la table est vide
                System.out.println("La table des poids est vide.");
                conn.close(); // Ferme la connexion
                return false;
            } else {
                System.out.println("+-----------------+------------+"); // Ligne de séparation en haut
                System.out.println("|     Poids       |    Date    |"); // En-tête du tableau
                System.out.println("+-----------------+------------+"); // Ligne de séparation
                while (rs.next()) {
                    String date = rs.getString("date");
                    double weight = rs.getDouble("value");
                    System.out.printf("| %-15.2f | %-10s |\n", weight, date); // Formatage pour alignement
                }
                System.out.println("+-----------------+------------+"); // Ligne de séparation en bas
                conn.close(); // Ferme la connexion
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'affichage de la table des poids : " + e.getMessage());
            return false;
        }
    }
    
    // Pour calculer la moyenne du poids de toute la table
    public Double calculateAverageWeight() {
        String query = "SELECT AVG(value) AS average FROM weight";
        try {
            dbManager.connect();
            Connection conn = dbManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
            {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    conn.close(); // Ferme la connexion
                    return rs.getDouble("average");
                }
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors du calcul de la moyenne de poids : " + e.getMessage());
        }
        return null;
    }

    // Méthode pour vérifier qu'une entrée de poid existe à une date donnée
    public boolean weightExistAtDate(String date) {
        String query = "SELECT COUNT(*) FROM weight WHERE date = ?";
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

    // Méthode auxiliaire pour obtenir le poids à une date spécifique
    public double getWeightOnDate(String date) {
        String query = "SELECT value FROM weight WHERE date = ?";
        try {
            dbManager.connect();
            Connection conn = dbManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
            {
                stmt.setString(1, date);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getDouble("value");
                }
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération du poids à une date spécifique : " + e.getMessage());
        }
        return 0;
    }

    // Méthode pour calculer la perte/gain de poids entre deux dates
    public double getWeightChange(String startDate, String endDate) {
        String query = "SELECT value FROM weight WHERE date = ?";
        double startWeight = 0;
        double endWeight = 0;

        try {
            dbManager.connect();
            Connection conn = dbManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);

            // Pour startDate
            stmt.setString(1, startDate);
            ResultSet rsStart = stmt.executeQuery(); 
            if (rsStart.next()) {
                startWeight = rsStart.getDouble("value");
            } else {
                System.out.println("Aucune entrée de poids trouvée pour la date de début spécifiée : " + startDate);
                return 0;
            }
            rsStart.close(); 

            // Pour endDate
            stmt.setString(1, endDate);
            ResultSet rsEnd = stmt.executeQuery(); 
            if (rsEnd.next()) {
                endWeight = rsEnd.getDouble("value");
            } else {
                System.out.println("Aucune entrée de poids trouvée pour la date de fin spécifiée : " + endDate);
                return 0;
            }
            rsEnd.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println("Erreur lors du calcul du changement de poids : " + e.getMessage());
            e.printStackTrace();
            return 0;
        }

        return endWeight - startWeight;
    }

    // Méthode pour obtenir les données de poids sous forme de List<Map<String, Object>>
    public List<Map<String, Object>> getWeightData() {
        String query = "SELECT date, weight FROM WeightHistory ORDER BY date ASC"; 
        List<Map<String, Object>> weightData = new ArrayList<>();
        try {
            dbManager.connect();
            Connection conn = dbManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> entry = new HashMap<>();
                entry.put("date", rs.getString("date"));
                entry.put("weight", rs.getDouble("value"));
                weightData.add(entry);
            }
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des données de poids : " + e.getMessage());
            e.printStackTrace();
        }
        return weightData;
    }

    // Méthode pour obtenir les données de calories sous forme de List<Map<String, Object>>
    public List<Map<String, Object>> getCalorieData() {
        String query = "SELECT date, calories FROM CaloriesEaten ORDER BY date ASC";
        List<Map<String, Object>> calorieData = new ArrayList<>();
        try {
            dbManager.connect();
            Connection conn = dbManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> entry = new HashMap<>();
                entry.put("date", rs.getString("date"));
                entry.put("calories", rs.getInt("amount")); // Assuming 'amount' column stores calories
                calorieData.add(entry);
            }
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des données de calories : " + e.getMessage());
            e.printStackTrace();
        }
        return calorieData;
    }

    // Méthode pour supprimer toutes les entrées de poids
    public boolean removeAllWeightEntries() {
        String query = "DELETE FROM weight"; 
        try {
            dbManager.connect();
            Connection conn = dbManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.executeUpdate();
            conn.close(); // Ferme la connexion
            System.out.println("Toutes les entrées de poids ont été supprimées avec succès !");
            return true;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de toutes les entrées de poids : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
