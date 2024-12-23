package com.java;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


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
    public void addWeightEntryToday(double weight, String date) {
        String query = "INSERT INTO weight (value, date) VALUES (?, ?)";
        try {
            dbManager.connect();       
            Connection conn = dbManager.getConnection();    
            PreparedStatement stmt = conn.prepareStatement(query); 
            {
                stmt.setDouble(1, weight);
                stmt.setString(2, date);
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("Échec de l'insertion de l'entrée de poids.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout d'une entrée de poids : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Méthode pour ajouter une entrée de poids à une date spécifique
    public void addWeightEntryChoosenDate(double weight, String date) {
        String query = "INSERT INTO weight (value, date) VALUES (?, ?)";
        try {
            dbManager.connect();
            Connection conn = dbManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
            {
                stmt.setDouble(1, weight);
                stmt.setString(2, date);
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("Échec de l'insertion de l'entrée de poids.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout d'une entrée de poids : " + e.getMessage());
            e.printStackTrace();
        }
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
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération du poids : " + e.getMessage());
        }
        return null;
    }

    // Méthode pour aller chercher dans la bdd tous les poids
    public List<WeightEntry> getWeightHistory() {
        List<WeightEntry> history = new ArrayList<>();
        String query = "SELECT value, date FROM weight ORDER BY date ASC";
        try {
            dbManager.connect();
            Connection conn = dbManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
            {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    double weight = rs.getDouble("value");
                    String date = rs.getString("date");
                    history.add(new WeightEntry(weight, date));
                }
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération de l'historique des poids : " + e.getMessage());
        }
        return history;
    }

    // Pour supprimer une entrée
    public void deleteWeightEntry(String date) {
        String query = "DELETE FROM weight WHERE date = ?";
        try {
            dbManager.connect();
            Connection conn = dbManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
            {
                stmt.setString(1, date);
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected == 0) {
                    System.out.println("Aucune entrée trouvée pour la date spécifiée.");
                } 
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de l'entrée de poids : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Méthode pour afficher la table des poids
    public boolean displayWeightTable() {
        String query = "SELECT date, value FROM weight ORDER BY date ASC";
        try {
            dbManager.connect();
            Connection conn = dbManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            if (!rs.isBeforeFirst()) { // Vérifie si le ResultSet est vide
                System.out.println("La table des poids est vide.");
                return false;
            } else {
                System.out.println("Table des poids :");
                while (rs.next()) {
                    String date = rs.getString("date");
                    double weight = rs.getDouble("value");
                    System.out.println("Date: " + date + ", Poids: " + weight);
                }
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
                    return rs.getDouble("average");
                }
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors du calcul de la moyenne de poids : " + e.getMessage());
        }
        return null;
    }
    
    // Méthode pour mettre à jour une entrée de poids existante
    // ------------------------------PROBLÉMATIQUE : LA BDD LOCK ??----------------------------------
    // Update "normalement" lock.
    // Delete, puis add avec le nouveau poid ne fonctionne pas apparement... 
    public boolean updateWeightEntry(double newWeight, String date) {
        // On supprime d'abord l'ancienne entrée
        deleteWeightEntry(date);

        // Puis on ajoute la nouvelle entrée avec le nouveau poids
        addWeightEntryChoosenDate(newWeight, date);

        // On vérifie si l'ajout a réussi (même si deleteWeightEntry affiche un message)
        return getWeightOnDate(date) == newWeight;
    }

    // Méthode pour obtenir l'historique des poids dans une plage de dates
    public List<WeightEntry> getWeightHistory(String startDate, String endDate) {
        List<WeightEntry> history = new ArrayList<>();
        String query = "SELECT value, date FROM weight WHERE date BETWEEN ? AND ? ORDER BY date ASC";
        try {
            dbManager.connect();
            Connection conn = dbManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
            {
                stmt.setString(1, startDate);
                stmt.setString(2, endDate);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    double weight = rs.getDouble("value");
                    String date = rs.getString("date");
                    history.add(new WeightEntry(weight, date));
                }
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération de l'historique des poids : " + e.getMessage());
        }
        return history;
    }

    // Méthode pour calculer la perte/gain de poids entre deux dates
    public double getWeightChange(String startDate, String endDate) {
        double startWeight = getWeightOnDate(startDate);
        double endWeight = getWeightOnDate(endDate);
        if (startWeight == 0 || endWeight == 0) return 0;
        return endWeight - startWeight;
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
}
