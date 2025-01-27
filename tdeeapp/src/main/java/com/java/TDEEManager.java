package com.java;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.time.LocalDate;

public class TDEEManager {
    private DatabaseManager dbManager;

    public class TDEEData {
        private double weight;
        private double calories;
        private double weightFactor;
    
        public TDEEData(double weight, double calories, double weightFactor) {
            this.weight = weight;
            this.calories = calories;
            this.weightFactor = weightFactor;
        }
    
        public double getWeight() {
            return weight;
        }
    
        public double getCalories() {
            return calories;
        }
    
        public double getWeightFactor() {
            return weightFactor;
        }

        @Override
        public String toString() {
            return String.format("Poids: %.2f, Calories: %.2f, Pondération: %.2f", weight, calories, weightFactor);
        }
    }

    public static class TDEEEntry {
        private double value;
        private String date;

        // Classe interne pour représenter une entrée de TDEE
        public TDEEEntry(double value, String date) {
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

    public TDEEManager(DatabaseManager databaseManager) {
        this.dbManager = databaseManager;
    }

    /*
        Méthode qui calcule le TDEE de la journée 
        Également utilisé pour aller chercher le TDEE de n'importe quelle journée. Ce calcul se fera alors de jour 0 à jour n

        Logique du calcul...
         - S'il n'y aucune entrée de poids, on ne peut pas calculer le TDEE
         - Dans les autres cas, on veut faire en sorte que les entrées les plus récentes aient une pondération plus importante.
           Ne pas en tenir compte fairait en sorte que les entrées de poids/calories d'il y a, par exemple, 2 mois seraient autant
           importantes que celle d'hier, ce qui ne fait aucun sens.
         - Le TDEE s'adapte à la situation courante de la personne, plus il y a de données, plus c'est précis.
         - Une valeur extrême ne doit trop impacter le calcul du TDEE. Par exemple, si la personne ne gagne jamais de poids en mangeant
           2500 calories (donc son poids les derniers jours était 180, 180.4, 180.2, 179.6, ...) mais qu'aujourd'hui il pèse 183 lbs, 
           c'est probablement que de la rétention d'eau. On le calcul, mais ça devrait pas 'flag' comme 
           "T'as gagné beaucoup trop de poids, on va réduire ton TDEE de 350 calories". 20 ou 30 calories de moins serait plus logique.
         - On va prendre les entrées de 30 jours (valerus inclus) avant la date de la dernière entrée pour le calcul. Par exemple,
           si la date de la dernière entrée est le 31 mars, on va aller chercher les valeurs jusqu'à le 31 mars - 30 jours = 1er mars
    */
    public int calculateTDEE(String date) {
        int tdee = 0;
        try {
            dbManager.connect();       
            Connection conn = dbManager.getConnection();  

            // Récupérer les entrées de poids et de calories des 30 derniers jours
            List<WeightManager.WeightEntry> weightEntries = getWeightEntries(date);
            List<CaloriesManager.CaloriesEntry> calorieEntries = getCalorieEntries(date);

            // Vérifier s'il y a des entrées de poids ou de calories
            if (weightEntries.isEmpty() && calorieEntries.isEmpty()) {
                System.out.println("Aucune entrée de poids ou de calories disponible pour calculer le TDEE.");
                return 0;
            }

            // Trouver la date avec l'entrée de poids + calories la plus récente
            String latestDate = null;

            // Créer un ensemble de toutes les dates présentes dans les entrées de poids
            Set<String> weightDates = new HashSet<>();
            for (WeightManager.WeightEntry weightEntry : weightEntries) {
                weightDates.add(weightEntry.getDate());
            }

            // Vérifier les dates des entrées de calories
            for (CaloriesManager.CaloriesEntry calorieEntry : calorieEntries) {
                String calorieDate = calorieEntry.getDate();
                // Si la date de calories existe aussi dans les dates de poids, on la considère
                if (weightDates.contains(calorieDate)) {
                    if (latestDate == null || calorieDate.compareTo(latestDate) > 0) {
                        latestDate = calorieDate;
                    }
                }
            }

            // Vérifier les dates des entrées de poids pour voir si une date plus récente existe
            for (WeightManager.WeightEntry weightEntry : weightEntries) {
                String weightDate = weightEntry.getDate();
                if (latestDate == null || weightDate.compareTo(latestDate) > 0) {
                    latestDate = weightDate;
                }
            }

            // Évidemment, plus la valeur de la demi-vie est élevée, plus l'impact d'un poids sera prononcé.
            // Afin de réduire l'impact d'une valeur hors-tendance le plus possible, j'ai attribué la valeur de 50.
            // Donc l'impact est amoindrit, mais le reflet d'un changement de poids sera moins rapidement reflété.
            // Cette valeur pourrait être modifiée automatiquement (éventuellement) selon la fréquence à laquelle 
            // l'utilisateur entre ses données (++fréquent = halflife++ et vice-versa)
            double halfLife = 50.0;

            List<TDEEData> tdeeDataList = new ArrayList<>(); // Liste pour stocker les données (poids, calories et pondération)

            // Commencer à partir de latestDate et reculer dans le temps
            for (int i = 0; i < 30; i++) {
                // Calculer la date à vérifier
                LocalDate currentDate = LocalDate.parse(latestDate).minusDays(i);
                String dateToCheck = currentDate.toString();

                // Vérifier si des entrées existent à cette date
                if (entriesExistAtDate(dateToCheck)) {
                    // Récupérer le poids et les calories pour cette date
                    double weight = getWeightAtDate(dateToCheck);
                    double calories = getCaloriesAtDate(dateToCheck);

                    // Plus l'entrée est récente, plus la pondération est élevée
                    double weightFactor = Math.exp(-i / halfLife);

                    // Ajouter les données à la liste
                    tdeeDataList.add(new TDEEData(weight, calories, weightFactor));
                }
            }

            // Calcul des moyennes pondérées
            double sumWeightedCalories = 0.0;
            double sumWeights = 0.0;
            double sumWeightChanges = 0.0;

            for (int i = 0; i < tdeeDataList.size(); i++) {
                TDEEData data = tdeeDataList.get(i);
                sumWeightedCalories += data.getCalories() * data.getWeightFactor();
                sumWeights += data.getWeightFactor();

                if (i > 0) {
                    double weightDiff = tdeeDataList.get(i - 1).getWeight() - data.getWeight();
                    sumWeightChanges += weightDiff * data.getWeightFactor();
                }
            }

            // Moyenne pondérée des calories
            double averageCalories = sumWeightedCalories / sumWeights;

            // Impact des variations de poids
            double averageWeightChange = sumWeightChanges / sumWeights;
            double adjustment = averageWeightChange * 3500; // 3500 kcal par livre

            // TDEE final
            tdee = (int) (averageCalories - adjustment);

            conn.close();

        } catch (Exception e) {
            System.err.println("Erreur de connection à la base de donnée");
            e.printStackTrace();
            return 0;
        }
        return tdee;
    }

    // Méthode pour obtenir le poids à une date spécifique
    private double getWeightAtDate(String date) {
        String query = "SELECT value FROM Weight WHERE date = ?";
        double weight = 0; 
        try {
            dbManager.connect();
            Connection conn = dbManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, date);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                weight = rs.getDouble("value");
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération du poids à la date spécifiée : " + e.getMessage());
        } finally {
            dbManager.close();
        }
        return weight; 
    }

    // Méthode pour obtenir les calories à une date spécifique
    private double getCaloriesAtDate(String date) {
        String query = "SELECT calories FROM CaloriesEaten WHERE date = ?";
        double calories = 0;
        try {
            dbManager.connect();
            Connection conn = dbManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, date);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                calories = rs.getDouble("calories");
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des calories à la date spécifiée : " + e.getMessage());
        } finally {
            dbManager.close();
        }
        return calories;
    }

    private boolean entriesExistAtDate(String date) {
        return weightExistsAtDate(date) && caloriesExistAtDate(date);
    }
    
    private boolean weightExistsAtDate(String date) {
        String query = "SELECT COUNT(*) FROM Weight WHERE date = ?";
        try {
            dbManager.connect();
            Connection conn = dbManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, date);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification de l'existence d'une entrée de poids : " + e.getMessage());
        } finally {
            dbManager.close();
        }
        return false;
    }
    
    private boolean caloriesExistAtDate(String date) {
        String query = "SELECT COUNT(*) FROM CaloriesEaten WHERE date = ?";
        try {
            dbManager.connect();
            Connection conn = dbManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, date);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification de l'existence d'une entrée de calories : " + e.getMessage());
        } finally {
            dbManager.close();
        }
        return false;
    }

    // Méthode pour récupérer les entrées de poids des 30 derniers jours
    private List<WeightManager.WeightEntry> getWeightEntries(String date) {
        List<WeightManager.WeightEntry> weightEntries = new ArrayList<>();
        String query = "SELECT value, date FROM Weight WHERE date <= ? ORDER BY date DESC LIMIT 30";
        try {
            dbManager.connect();
            Connection conn = dbManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, date);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                double value = rs.getDouble("value");
                String entryDate = rs.getString("date");
                weightEntries.add(new WeightManager.WeightEntry(value, entryDate));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des entrées de poids : " + e.getMessage());
        } finally {
            dbManager.close();
        }
        return weightEntries;
    }

    // Méthode pour récupérer les entrées de calories des 30 derniers jours
    private List<CaloriesManager.CaloriesEntry> getCalorieEntries(String date) {
        List<CaloriesManager.CaloriesEntry> calorieEntries = new ArrayList<>();
        String query = "SELECT calories, date FROM CaloriesEaten WHERE date <= ? ORDER BY date DESC LIMIT 30";
        try {
            dbManager.connect();
            Connection conn = dbManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, date);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int calories = rs.getInt("calories");
                String entryDate = rs.getString("date");
                calorieEntries.add(new CaloriesManager.CaloriesEntry(calories, entryDate));
            }
            dbManager.close();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des entrées de calories : " + e.getMessage());
        }
        return calorieEntries;
    }
}
