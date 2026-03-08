package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

// Gere la connexion a la base de donnees SQLite pour sauvegarder les scores
public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:breakout_scores.db"; 
    private static DatabaseManager instance;
    private Connection connection;
    
    private DatabaseManager() {
        initializeDatabase();
    }
    
    // on veut une seule connexion a la base de donnees
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) { // Si l'instance n'existe pas, on la cree
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    // Initialise la connexion et cree les tables si elles n'existent pas
    private void initializeDatabase() {
        try {
            // Charger le driver JDBC pour SQLite (Java Database Connectivity)
            Class.forName("org.sqlite.JDBC");// On charge le driver JDBC pour SQLite
            connection = DriverManager.getConnection(DB_URL); // On se connecte a la base de donnees
            connection.setAutoCommit(true); // Les changements sont sauvegardés automatiquement
            createTables();
            System.out.println("Base de données initialisée avec succès: " + DB_URL);
        } catch (ClassNotFoundException e) {
            System.out.println("Driver SQLite non trouvé. Le jeu fonctionnera sans sauvegarde des scores.");
            System.out.println("Pour activer la sauvegarde, téléchargez sqlite-jdbc.jar et placez-le dans lib/");
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'initialisation de la base de données: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Cree la table des scores si elle n'existe pas
    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) { // On cree une instruction SQL
            try {
                stmt.executeQuery("SELECT id FROM scores LIMIT 1"); // On verifie si la table existe
                System.out.println("Ancienne structure détectée, migration en cours...");
                stmt.execute("DROP TABLE IF EXISTS scores");// On supprime la table si elle existe
            } catch (SQLException e) {
                // L'ancienne table n'existe pas, c'est bon
            }
            
            // Creer la table avec le nom du joueur comme primary key
            // Cela permet de cumuler les scores pour le meme joueur
            String createScoresTable = "CREATE TABLE IF NOT EXISTS scores (" +
                "player_name TEXT PRIMARY KEY, " +
                "total_score INTEGER NOT NULL DEFAULT 0, " +
                "best_level INTEGER NOT NULL DEFAULT 0, " +
                "games_played INTEGER NOT NULL DEFAULT 0, " +
                "last_played TEXT NOT NULL" +
                ")";
            
            stmt.execute(createScoresTable);
            System.out.println("Table scores créée/mise à jour avec succès");
        }
    }
    
    // Retourne la connexion a la base de donnees
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection(DB_URL);
            } catch (ClassNotFoundException e) {
                throw new SQLException("Driver SQLite non disponible", e);
            }
        }
        return connection;
    }
    
    // Verifie si la base de donnees est disponible (driver charge)
    public boolean isAvailable() {
        return connection != null;
    }
    
    // Ferme proprement la connexion quand on quitte le jeu
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la fermeture de la connexion: " + e.getMessage());
        }
    }
}

