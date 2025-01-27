package com.java;

import java.util.Scanner;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;

public class MainConsole {
    // Le menu principal (dans main) sépare le poids, calories et TDEE en
    // différentes méthodes/sous-menu
    // C'est plus clair et facile à lire comme ça
    public static void main(String[] args) {
        // Message de bienvenue
        displayWelcomeMessage();
        int mainChoice;

        do {
            DatabaseManager dbManager = new DatabaseManager();
            Scanner scanner = new Scanner(System.in);

            System.out.println("\nMenu Principal :");
            System.out.println("1. Gérer le poids");
            System.out.println("2. Gérer les calories");
            System.out.println("3. Gérer les dépenses énergétiques quotidiennes (TDEE)");
            System.out.println("0. Quitter");
            System.out.print("Entrez votre choix : ");

            if (scanner.hasNextInt()) {
                mainChoice = scanner.nextInt();
                scanner.nextLine(); // Consomme la nouvelle ligne
                switch (mainChoice) {
                    // Gérer le poids
                    case 1:
                        manageWeight(scanner, dbManager);
                        break;

                    // Gérer les calories
                    case 2:
                        manageCalories(scanner, dbManager);
                        break;

                    // Gérer le TDEE
                    case 3:
                        manageTDEE(scanner, dbManager);
                        break;

                    // Quitter
                    case 0:
                        System.out.println("Au revoir !");
                        break;

                    // Choix invalide
                    default:
                        System.out.println("Choix invalide. Veuillez réessayer.");
                }
            } else {
                System.out.println("Choix invalide. Veuillez entrer un nombre.");
                scanner.nextLine(); // Consomme l'entrée invalide
                mainChoice = -1; // Réinitialise le choix pour continuer la boucle
            }
        } while (mainChoice != 0);
    }

    // Gère tout ce que l'utilisateur peut faire concernant son poids
    private static void manageWeight(Scanner scanner, DatabaseManager dbManager) {
        int choix;
        String datePattern = "\\d{4}-\\d{2}-\\d{2}"; // Utilisé afin de standardisé le format des dates

        try {
            if (dbManager.connect()) {
                WeightManager weightManager = new WeightManager(dbManager);

                dbManager.initializeDatabase();

                do {
                    System.out.println("\nMenu Poids :");
                    System.out.println("1. Ajouter une entrée de poids pour aujourd'hui");
                    System.out.println("2. Supprimer une entrée de poids");
                    System.out.println("3. Afficher l'historique des poids");
                    System.out.println("4. Calculer la moyenne des poids");
                    System.out.println("5. Mettre à jour une entrée de poids");
                    System.out.println("6. Obtenir le changement de poids entre deux dates");
                    System.out.println("7. Ajouter une entrée de poids pour une date donnée");
                    System.out.println("8. Supprimer toutes les entrées de poids");
                    System.out.println("0. Retour au menu principal");
                    System.out.print("Entrez votre choix : ");

                    if (scanner.hasNextInt()) {
                        choix = scanner.nextInt();
                        scanner.nextLine(); // Consomme la nouvelle ligne
                        try {
                            switch (choix) {
                                // Ajouter une entrée de poids pour aujourd'hui
                                case 1:
                                    System.out.print("Entrez votre poids : ");
                                    String weightInput = scanner.nextLine();
                                    try {
                                        double weight = parseInput(weightInput);
                                        if (weightManager.addWeightEntry(weight,
                                                java.time.LocalDate.now().toString())) {
                                            System.out.println("Entrée de poids ajoutée avec succès !");
                                        }
                                    } catch (ParseException e) {
                                        System.out.println("Entrée invalide. Veuillez entrer un nombre valide.");
                                    }
                                    break;

                                // Supprimer une entrée de poids
                                case 2:
                                    if (weightManager.displayWeightTable()) { // Si c'est faux ça affiche que la table
                                                                              // est vide
                                        String dateToDelete;
                                        do {
                                            System.out.print("Entrez la date de l'entrée à supprimer (AAAA-MM-JJ) : ");
                                            dateToDelete = scanner.nextLine();
                                            if (!dateToDelete.matches(datePattern)) {
                                                System.out.println(
                                                        "Format de date invalide. Veuillez entrer une date au format AAAA-MM-JJ.");
                                            }
                                        } while (!dateToDelete.matches(datePattern));
                                        if (weightManager.deleteWeightEntry(dateToDelete)) {
                                            System.out.println("Entrée de poids retirée avec succès !");
                                        }
                                    }
                                    break;

                                // Afficher l'historique des poids
                                case 3:
                                    weightManager.displayWeightTable();
                                    break;

                                // Calculer la moyenne des poids
                                case 4:
                                    Double averageWeight = weightManager.calculateAverageWeight();
                                    if (averageWeight != null) {
                                        System.out.println("Votre poid moyen est : " + averageWeight);
                                    } else {
                                        System.out.println(
                                                "La moyenne ne peut pas être calculée car aucune donnée a été entrée.");
                                    }
                                    break;

                                // Mettre à jour une entrée de poids
                                case 5:
                                    String dateToUpdate;
                                    if (weightManager.displayWeightTable()) { // Si c'est faux ça affiche que la table
                                                                              // est vide

                                        // On modifie selon la date (valide le format)
                                        do {
                                            System.out.print(
                                                    "Entrez la date de l'entrée à mettre à jour (AAAA-MM-JJ) : ");
                                            dateToUpdate = scanner.nextLine();
                                            if (!dateToUpdate.matches(datePattern)) {
                                                System.out.println(
                                                        "Format de date invalide. Veuillez entrer une date avec le bon format (AAAA-MM-JJ).");
                                            }
                                        } while (!dateToUpdate.matches(datePattern));

                                        // Valide si c'est une date avec une entrée de poids
                                        if (weightManager.weightExistAtDate(dateToUpdate)) {
                                            System.out.print("Entrez le nouveau poids : ");
                                            String newWeightInput = scanner.nextLine();
                                            double newWeight = parseInput(newWeightInput);

                                            // On supprime d'abord l'ancienne entrée
                                            weightManager.deleteWeightEntry(dateToUpdate);

                                            // Puis on ajoute la nouvelle entrée avec le nouveau poids
                                            weightManager.addWeightEntry(newWeight, dateToUpdate);
                                        } else {
                                            System.out.println("Aucune entrée de poids à la date spécifiée.");
                                        }
                                    }
                                    break;

                                // Obtenir le changement de poids entre deux dates
                                case 6:
                                    String startDate, endDate;
                                    if (weightManager.displayWeightTable()) { // Si c'est faux ça affiche que la table
                                                                              // est vide
                                        // Valide la date de début
                                        do {
                                            System.out.print("Entrez la date de début (AAAA-MM-JJ) : ");
                                            startDate = scanner.nextLine();
                                            if (!startDate.matches(datePattern)) {
                                                System.out.println(
                                                        "Format de date invalide. Veuillez entrer une date au format AAAA-MM-JJ.");
                                            }
                                        } while (!startDate.matches(datePattern));

                                        // Valide la date de fin
                                        do {
                                            System.out.print("Entrez la date de fin (AAAA-MM-JJ) : ");
                                            endDate = scanner.nextLine();
                                            if (!endDate.matches(datePattern)) {
                                                System.out.println(
                                                        "Format de date invalide. Veuillez entrer une date au format AAAA-MM-JJ.");
                                            }
                                        } while (!endDate.matches(datePattern));

                                        // Compare les deux dates
                                        double weightChange = weightManager.getWeightChange(startDate, endDate);

                                        // Le cas 'else' est déjà géré par la fonction
                                        if (weightChange != 0) {
                                            System.out.println("Changement de poids entre " + startDate + " et "
                                                    + endDate + " : " + weightChange);
                                        }
                                    }
                                    break;

                                // Ajouter une entrée de poids pour une date donnée
                                case 7:
                                    String date;

                                    // Valide date
                                    do {
                                        System.out.print("Entrez la date de l'entrée (AAAA-MM-JJ) : ");
                                        date = scanner.nextLine();
                                        if (!date.matches(datePattern)) {
                                            System.out.println(
                                                    "Format de date invalide. Veuillez entrer une date au format AAAA-MM-JJ.");
                                        }
                                    } while (!date.matches(datePattern));

                                    // valide entrée de poids
                                    System.out.print("Entrez votre poids : ");
                                    weightInput = scanner.nextLine();
                                    try {
                                        double weight = parseInput(weightInput);
                                        if (weightManager.addWeightEntry(weight, date)) {
                                            System.out.println("Entrée de poids ajoutée avec succès !");
                                        }
                                    } catch (ParseException e) {
                                        System.out.println("Entrée invalide. Veuillez entrer un nombre valide.");
                                    }
                                    break;
                                
                                // Supprimer toutes les entrées de poids
                                case 8:
                                    weightManager.removeAllWeightEntries();
                                    break;

                                // Retour au menu principal
                                case 0:
                                    break;

                                // Pour un autre chiffre que dans la liste...
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
                } while (choix != 0);
            } else {
                System.out.println("Échec de la connexion à la base de données.");
            }
        } catch (Exception e) {
            System.err.println("Erreur : " + e.getMessage());
        }
    }

    // Gère tout ce que l'utilisateur peut faire concernant ses calories
    private static void manageCalories(Scanner scanner, DatabaseManager dbManager) {
        int choix;
        String datePattern = "\\d{4}-\\d{2}-\\d{2}"; // Utilisé afin de standardisé le format des dates

        if (dbManager.connect()) {
            CaloriesManager caloriesManager = new CaloriesManager(dbManager);
            dbManager.initializeDatabase();

            do {
                System.out.println("\nMenu Calories :");
                System.out.println("1. Ajouter des calories consommées pour aujourd'hui");
                System.out.println("2. Ajouter une entrée de calories pour une date spécifiée");
                System.out.println("3. Supprimer une entrée de calories");
                System.out.println("4. Afficher l'historique des calories consommées");
                System.out.println("5. Mettre à jour une entrée de calories consommées");
                System.out.println("6. Supprimer toutes les entrées de calories");
                System.out.println("0. Retour au menu principal");
                System.out.print("Entrez votre choix : ");

                if (scanner.hasNextInt()) {
                    choix = scanner.nextInt();
                    scanner.nextLine();

                    try {
                        switch (choix) {
                            // Ajouter une entrée de calories pour aujourd'hui
                            case 1:
                                System.out.print("Entrez le nombre de calories consommées aujourd'hui : ");
                                String caloriesInput = scanner.nextLine();
                                try {
                                    int calories = (int) parseInput(caloriesInput);
                                    if (caloriesManager.addCaloriesEntry(calories,
                                            java.time.LocalDate.now().toString())) {
                                        System.out.println("Entrée de calories ajoutée avec succès !");
                                    }
                                } catch (ParseException e) {
                                    System.out.println("Entrée invalide. Veuillez entrer un nombre valide.");
                                }
                                break;

                            // Ajouter une entrée de calories pour une date spécifiée
                            case 2:
                                String date;

                                // Valide date
                                do {
                                    System.out.print("Entrez la date de l'entrée (AAAA-MM-JJ) : ");
                                    date = scanner.nextLine();
                                    if (!date.matches(datePattern)) {
                                        System.out.println(
                                                "Format de date invalide. Veuillez entrer une date au format AAAA-MM-JJ.");
                                    }
                                } while (!date.matches(datePattern));

                                // Valide entrée de calories
                                System.out.print("Entrez les calories consommées : ");
                                caloriesInput = scanner.nextLine();
                                try {
                                    int calories = (int) parseInput(caloriesInput);
                                    if (caloriesManager.addCaloriesEntry(calories, date)) {
                                        System.out.println("Entrée de calories ajoutée avec succès !");
                                    }
                                } catch (ParseException e) {
                                    System.out.println("Entrée invalide. Veuillez entrer un nombre valide.");
                                }
                                break;

                            // Supprimer une entrée de calories
                            case 3:
                                if (caloriesManager.displayCaloriesTable()) { // Si c'est faux ça affiche que la table
                                                                              // est vide
                                    String dateToDelete;
                                    do {
                                        System.out.print("Entrez la date de l'entrée à supprimer (AAAA-MM-JJ) : ");
                                        dateToDelete = scanner.nextLine();
                                        if (!dateToDelete.matches(datePattern)) {
                                            System.out.println(
                                                    "Format de date invalide. Veuillez entrer une date au format AAAA-MM-JJ.");
                                        }
                                    } while (!dateToDelete.matches(datePattern));
                                    if (caloriesManager.removeCaloriesEntry(dateToDelete)) {
                                        System.out.println("Entrée de calories supprimée avec succès !");
                                    }
                                }
                                break;

                            // Afficher l'historique des calories consommées
                            case 4:
                                caloriesManager.displayCaloriesTable();
                                break;

                            // Mettre à jour une entrée de calories consommées
                            case 5:
                                String dateToUpdate;
                                if (caloriesManager.displayCaloriesTable()) { // Si c'est faux ça affiche que la table
                                                                              // est vide

                                    // On modifie selon la date (valide le format)
                                    do {
                                        System.out.print("Entrez la date de l'entrée à mettre à jour (AAAA-MM-JJ) : ");
                                        dateToUpdate = scanner.nextLine();
                                        if (!dateToUpdate.matches(datePattern)) {
                                            System.out.println(
                                                    "Format de date invalide. Veuillez entrer une date avec le bon format (AAAA-MM-JJ).");
                                        }
                                    } while (!dateToUpdate.matches(datePattern));

                                    // Valide si c'est une date avec une entrée de calories
                                    if (caloriesManager.caloriesExistAtDate(dateToUpdate)) {
                                        System.out.print("Entrez le vrai nombre de calories consommée le "
                                                + dateToUpdate + " : ");
                                        String newCaloriesInput = scanner.nextLine();
                                        int newCalories = (int) parseInput(newCaloriesInput);

                                        // On supprime d'abord l'ancienne entrée
                                        caloriesManager.removeCaloriesEntry(dateToUpdate);

                                        // Puis on ajoute la nouvelle entrée avec le vrai nombre de calories consommée
                                        caloriesManager.addCaloriesEntry(newCalories, dateToUpdate);
                                    } else {
                                        System.out.println("Aucune entrée de calories à la date spécifiée.");
                                    }
                                }
                                break;

                            // Supprimer toutes les entrées de calories
                            case 6:
                                caloriesManager.removeAllCaloriesEntries();
                                break;

                            // Retour au menu principal
                            case 0:
                                break;

                            // Pour un autre chiffre que dans la liste...
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
            } while (choix != 0);
        }
    }

    /*
     * Gère tout ce que l'utilisateur peut faire concernant ses calories
     * On ne crée pas de table pour le TDEE
     * Ce serait incohérent de garder en note le TDEE dans une table si
     * l'utilisateur peut entrer
     * des nouvelles entrée de poids/calories à des dates antérieures
     */
    private static void manageTDEE(Scanner scanner, DatabaseManager dbManager) {
        int choix;

        if (dbManager.connect()) {
            TDEEManager tdeeManager = new TDEEManager(dbManager);
            dbManager.initializeDatabase();
            
            do {
                System.out.println("\nMenu TDEE :");
                System.out.println("1. Consulter le TDEE à ce jour");
                System.out.println("0. Retour au menu principal");
                System.out.print("Entrez votre choix : ");

                if (scanner.hasNextInt()) {
                    choix = scanner.nextInt();
                    scanner.nextLine();

                    try {
                        switch (choix) {
                            // Consulter le TDEE à ce jour
                            case 1:
                                int currentTDEE = tdeeManager.calculateTDEE(java.time.LocalDate.now().toString());

                                // Le cas else est géré par la fonction
                                if (currentTDEE != 0) {
                                    System.out.println("Votre TDEE à ce jour est : " + currentTDEE + " calories");
                                }
                                break;

                            // Retour au menu principal
                            case 0:
                                break;

                            // Pour un autre chiffre que dans la liste...
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
            } while (choix != 0);
        }
    }

    // Méthode aux pour parse un input de format String en double
    // Remplace également les ',' par '.' pour le formatage correct des nombres
    // (utile surtout pour mon numpad, mais pour les chiffres anglo VS franco)
    private static double parseInput(String weightInput) throws ParseException {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        symbols.setGroupingSeparator(',');

        DecimalFormat format = new DecimalFormat();
        format.setDecimalFormatSymbols(symbols);
        format.setParseBigDecimal(true);

        return format.parse(weightInput.replace(',', '.')).doubleValue();
    }

    // Méthode pour afficher un message de bienvenue
    private static void displayWelcomeMessage() {
        System.out.println("**********************************************************");
        System.out.println("*                                                        *");
        System.out.println("*                Bienvenue dans TDEE-App                 *");
        System.out.println("*                                                        *");
        System.out.println("*      Suivez facilement votre poids et vos calories     *");
        System.out.println("*   Découvrez votre point de maintenance personnalisé !  *");
        System.out.println("*                                                        *");
        System.out.println("*         Prenez le contrôle de votre bien-être          *");
        System.out.println("*             et atteignez vos objectifs !               *");
        System.out.println("*                                                        *");
        System.out.println("**********************************************************");
    }
}
