# Tdee-App

Tdee-App est une application créée afin de répondre au besoin suivant : fournir un portrait réaliste du point de maintenance (TDEE) de l'utilisateur.
Pour ce faire, l'utilisateur peut entrer les calories consommées et son poids à n'importe quelle journée et à partir de ces entrées, l'application calcule
automatiquement le TDEE.

## Fonctionnalités

L'application permet de :
- Entrer des entrées de poids et calories
- Modifier et supprimer des entrées
- Calculer la moyenne
- Obtenir la différence de poids entre deux dates
- Voir toutes les entrées
- Supprimer toutes les entrées

Tdee-App est présentement seulement en ligne de commande et requiert seulement d'avoir créé une base de données dans le répertoire du projet (`bddTdee.db`). 
La création des tables se fera automatiquement. 

## Architecture

La classe principale (`MainConsole`) communique directement avec les classes de gestion de poids (`WeightManager`), gestion des calories (`CaloriesManager`) et le calcul du
TDEE (`TDEEManager`). 

Le code a été entièrement écrit en Java et la communication avec les bases de données se fait avec SQLite. Cette communication se fait par les plugins de Maven, qui
est utilisé pour les dépendances et la compilation.

## Auteur

Ce projet a été entièrement réalisé par moi (Jérémy Faubert) dans le cadre de créer une application que je peux utiliser pour suivre mon propre TDEE. Je n'étais pas satisfait des
options (gratuites) et du calcul fait par celles-ci. Également, je voulais apprendre à communiquer directement avec une base de données dans le code. 

## Améliorations futures

L'application fait présentement tout ce qu'elle est supposée : calculer le TDEE précisement, mais j'ai d'autres idées en tête pour l'améliorer :
    1. La création d'une interface visuelle (probablement JavaFX)
    2. La possibilité de définir combien de calories manger pour perdre/gagner du poids (selon la vitesse/nombre de calories par exemple)
    3. L'option de rajouter des recettes avec les ingrédients (quantités qui peuvent être changées), les instructions et les valeurs nutrionnelles (peut-être calculé       
       calculé automatiquement - Référence à 4.1)
    4. L'ajout du calcul des calories consommées aujourd'hui (sous forme de grille par exemple) qui va se reset à chaque jour 
        4.1 Lié ici est l'ajout d'une base de données avec les valeurs nutritionnelles de plusieurs aliments pour faciliter l'entrée
        4.2 Possiblité d'ajouter une recette et la portion consommées (1/3, 1/4, 1/5, ... et d'afficher les valeurs nutritionnelles pour chaque option)

Le code de cette application peut-être modifié à votre guise.