# Breakout Game - Java Swing

## Description

Casse-Brique est un jeu vidéo classique développé en Java utilisant Swing pour l'interface graphique. Le joueur contrôle une raquette pour faire rebondir une balle et détruire des briques disposées en haut de l'écran. Le but est de casser toutes les briques sans laisser la balle tomber en bas de l'écran.

## Fonctionnalités

- **Contrôle de la raquette** : Déplacez la raquette avec la souris pendant le jeu.
- **Système de briques** : Différentes briques à détruire avec la balle.
- **Détection de collisions** : Gestion précise des collisions entre la balle, la raquette et les briques.
- **Système de score** : Points gagnés en détruisant des briques.
- **Sauvegarde des scores** : Les meilleurs scores sont enregistrés dans une base de données SQLite.
- **États du jeu** : Menu principal, jeu en cours, game over, victoire.
- **Boost de vitesse** : La balle accélère au fil du temps pour augmenter la difficulté.
- **Navigation au clavier** : Utilisez les touches fléchées et Entrée pour naviguer dans les menus.

## Comment jouer

1. Lancez le jeu.
2. Dans le menu principal, sélectionnez "Commencer" pour démarrer une partie.
3. Utilisez la souris pour déplacer la raquette de gauche à droite.
4. Faites rebondir la balle sur la raquette pour casser les briques.
5. Évitez que la balle ne tombe en bas de l'écran.
6. Une fois toutes les briques détruites, vous gagnez !
7. Consultez les scores dans le menu "Scores".

## Prérequis

- Java 11 (OpenJDK 11.0.29+7 ou supérieur)
- SQLite JDBC Driver 3.51.1.0 (inclus dans le projet)

## Installation et exécution

### Dans Eclipse IDE :
1. Ouvrez le projet dans Eclipse.
2. Cliquez droit sur `src/Main.java`.
3. Sélectionnez "Run as" > "Java Application".

### Depuis la ligne de commande :
1. Assurez-vous que Java 11 est installé.
2. Naviguez vers le répertoire du projet.
3. Compilez les sources :
   ```
   javac -cp "lib/*" -d bin src/**/*.java
   ```
4. Exécutez le jeu :
   ```
   java -cp "bin:lib/*" Main
   ```

## Structure du projet

- `src/` : Code source Java
  - `Main.java` : Point d'entrée du programme
  - `ui/` : Interface utilisateur (MainFrame, GamePanel)
  - `game/` : Logique du jeu (GameEngine, CollisionDetector)
  - `model/` : Modèles de données (Ball, Brick, Paddle, GameState)
  - `database/` : Gestion de la base de données (DatabaseManager, ScoreDAO)
  - `exceptions/` : Exceptions personnalisées (GameOverException, InvalidGameStateException)
- `lib/` : Bibliothèques externes (SQLite JDBC)
- `bin/` : Fichiers compilés

## Base de données

Le jeu utilise SQLite pour sauvegarder les scores. La base de données est créée automatiquement au premier lancement. Les scores sont stockés dans un fichier local.

## Contrôles

- **Souris** : Déplacer la raquette (pendant le jeu)
- **Flèches haut/bas** : Naviguer dans les menus
- **Entrée** : Sélectionner une option dans les menus

