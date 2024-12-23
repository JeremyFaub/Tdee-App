package com.java;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.List;
import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        DatabaseManager dbManager = new DatabaseManager();
        Scanner scanner = new Scanner(System.in);
        String datePattern = "\\d{4}-\\d{2}-\\d{2}"; 

        try {
            if (dbManager.connect()) {
                System.out.println("Connexion à la base de données réussie !");
                WeightManager weightManager = new WeightManager(dbManager);
                dbManager.initializeDatabase();
                int choix;
                do {
                    System.out.println("\nMenu :");
                    System.out.println("1. Ajouter une entrée de poids pour aujourd'hui");
                    System.out.println("2. Supprimer une entrée de poids");
                    System.out.println("3. Afficher l'historique des poids");
                    System.out.println("4. Calculer la moyenne des poids");
                    System.out.println("5. Mettre à jour une entrée de poids");
                    System.out.println("6. Obtenir le changement de poids entre deux dates");
                    System.out.println("7. Ajouter une entrée de poids pour une date donnée");
                    System.out.println("8. Quitter");
                    System.out.print("Entrez votre choix : ");

                    if (scanner.hasNextInt()) {
                        choix = scanner.nextInt();
                        scanner.nextLine(); // Consomme la nouvelle ligne
                        try {
                            switch (choix) {
                                case 1:
                                    System.out.print("Entrez votre poids : ");
                                    String weightInput = scanner.nextLine();
                                    try {
                                        double weight = parseWeight(weightInput);
                                        weightManager.addWeightEntryToday(weight, java.time.LocalDate.now().toString());
                                        System.out.println("Entrée de poids ajoutée avec succès !");
                                    } catch (ParseException e) {
                                        System.out.println("Entrée invalide. Veuillez entrer un nombre valide.");
                                    }
                                    break;
                                case 2:
                                    if(weightManager.displayWeightTable()) {
                                        String dateToDelete;
                                        do {
                                            System.out.print("Entrez la date de l'entrée à supprimer (AAAA-MM-JJ) : ");
                                            dateToDelete = scanner.nextLine();
                                            if (!dateToDelete.matches(datePattern)) {
                                                System.out.println("Format de date invalide. Veuillez entrer une date au format AAAA-MM-JJ.");
                                            }
                                        } while (!dateToDelete.matches(datePattern));
                                        weightManager.deleteWeightEntry(dateToDelete);   
                                        System.out.println("Entrée de poids supprimée avec succès !");                             
                                    }
                                    break;
                                case 3:
                                    List<WeightManager.WeightEntry> history = weightManager.getWeightHistory();
                                    if (history.isEmpty()) {
                                        System.out.println("Aucune entrée de poids trouvée.");
                                    } else {
                                        System.out.println("Historique des poids :");
                                        for (WeightManager.WeightEntry entry : history) {
                                            System.out.println("Date: " + entry.getDate() + ", Poids: " + entry.getValue());
                                        }
                                    }
                                    break;
                                case 4:
                                    Double averageWeight = weightManager.calculateAverageWeight();
                                    if (averageWeight != null) {
                                        System.out.println("Moyenne des poids : " + averageWeight);
                                    } else {
                                        System.out.println("Impossible de calculer la moyenne des poids.");
                                    }
                                    break;
                                case 5:
                                    String dateToUpdate;
                                    do {
                                        System.out.print("Entrez la date de l'entrée à mettre à jour (AAAA-MM-JJ) : ");
                                        dateToUpdate = scanner.nextLine();
                                        if (!dateToUpdate.matches(datePattern)) {
                                            System.out.println("Format de date invalide. Veuillez entrer une date au format AAAA-MM-JJ.");
                                        }
                                    } while (!dateToUpdate.matches(datePattern));
                                    System.out.print("Entrez le nouveau poids : ");
                                    String newWeightInput = scanner.nextLine();
                                    try {
                                        double newWeight = parseWeight(newWeightInput);
                                        if (weightManager.updateWeightEntry(newWeight, dateToUpdate)) {
                                            System.out.println("Entrée de poids mise à jour avec succès !");
                                        } else {
                                            System.out.println("Échec de la mise à jour de l'entrée de poids.");
                                        }
                                    } catch (ParseException e) {
                                        System.out.println("Entrée invalide. Veuillez entrer un nombre valide.");
                                    }
                                    break;
                                case 6:
                                    String startDate, endDate;
                                    do {
                                        System.out.print("Entrez la date de début (AAAA-MM-JJ) : ");
                                        startDate = scanner.nextLine();
                                        if (!startDate.matches(datePattern)) {
                                            System.out.println("Format de date invalide. Veuillez entrer une date au format AAAA-MM-JJ.");
                                        }
                                    } while (!startDate.matches(datePattern));
                                    do {
                                        System.out.print("Entrez la date de fin (AAAA-MM-JJ) : ");
                                        endDate = scanner.nextLine();
                                        if (!endDate.matches(datePattern)) {
                                            System.out.println("Format de date invalide. Veuillez entrer une date au format AAAA-MM-JJ.");
                                        }
                                    } while (!endDate.matches(datePattern));
                                    double weightChange = weightManager.getWeightChange(startDate, endDate);
                                    System.out.println("Changement de poids entre " + startDate + " et " + endDate + " : " + weightChange);
                                    break;
                                case 7: 
                                    String date;
                                    do {
                                        System.out.print("Entrez la date de l'entrée (AAAA-MM-JJ) : ");
                                        date = scanner.nextLine();
                                        if (!date.matches(datePattern)) {
                                            System.out.println("Format de date invalide. Veuillez entrer une date au format AAAA-MM-JJ.");
                                        }
                                    } while (!date.matches(datePattern));
                                    System.out.print("Entrez votre poids : ");
                                    weightInput = scanner.nextLine();
                                    try {
                                        double weight = parseWeight(weightInput);
                                        weightManager.addWeightEntryChoosenDate(weight, date);
                                        System.out.println("Entrée de poids ajoutée avec succès !");
                                    } catch (ParseException e) {
                                        System.out.println("Entrée invalide. Veuillez entrer un nombre valide.");
                                    }
                                    break;
                                case 8:
                                    System.out.println("Au revoir !");
                                    break;
                                default:
                                    System.out.println("Choix invalide. Veuillez réessayer.");
                            }
                        } catch (Exception e) {
                            System.err.println("Erreur : " + e.getMessage());
                        }
                    } else {
                        System.out.println("Choix invalide. Veuillez entrer un nombre.");
                        scanner.nextLine(); // Consomme l'entrée invalide
                        choix = -1; // Réinitialise le choix pour continuer la boucle
                    }
                } while (choix != 8);
            } else {
                System.out.println("Échec de la connexion à la base de données.");
            }
        } finally {
            scanner.close();
            dbManager.close();
        }
    }

    // Méthode pour permettre d'utiliser un point ou une virgule lorsqu'on entre un poid !
    private static double parseWeight(String weightInput) throws ParseException {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        symbols.setGroupingSeparator(',');

        DecimalFormat format = new DecimalFormat();
        format.setDecimalFormatSymbols(symbols);
        format.setParseBigDecimal(true);

        return format.parse(weightInput.replace(',', '.')).doubleValue();
    }
}