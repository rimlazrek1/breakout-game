package database;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

// Classe pour gerer les scores dans la base de donnees
// Pattern DAO : separe la logique metier de l'acces aux donnees
public class ScoreDAO {
    private DatabaseManager dbManager;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public ScoreDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }
    
    // Sauvegarde un score - si le joueur existe deja, on cumule les scores
    public void saveScore(String playerName, int score, int level) {
        if (!dbManager.isAvailable()) {
            System.out.println("Base de données non disponible. Score non sauvegardé.");
            return;
        }
        
        // Requetes SQL preparees pour eviter les injections SQL
        String checkSql = "SELECT total_score, best_level, games_played FROM scores WHERE player_name = ?";
        String insertSql = "INSERT INTO scores (player_name, total_score, best_level, games_played, last_played) VALUES (?, ?, ?, 1, ?)";
        String updateSql = "UPDATE scores SET total_score = total_score + ?, best_level = MAX(best_level, ?), games_played = games_played + 1, last_played = ? WHERE player_name = ?";
        
        try (Connection conn = dbManager.getConnection()) { // On se connecte a la base de donnees
            conn.setAutoCommit(true); // Les changements sont sauvegardes automatiquement
            
            // Verifier si ce joueur existe deja dans la base
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, playerName);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        // Le joueur existe : on cumule son score
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                            updateStmt.setInt(1, score);
                            updateStmt.setInt(2, level);
                            updateStmt.setString(3, LocalDateTime.now().format(DATE_FORMATTER));
                            updateStmt.setString(4, playerName);
                            updateStmt.executeUpdate();
                            int newTotal = rs.getInt("total_score") + score;
                            System.out.println("Score cumulé pour " + playerName + ": " + newTotal + " points (ajout: " + score + ")");
                        }
                    } else {
                        // Nouveau joueur : on cree une nouvelle entree
                        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                            insertStmt.setString(1, playerName);
                            insertStmt.setInt(2, score);
                            insertStmt.setInt(3, level);
                            insertStmt.setString(4, LocalDateTime.now().format(DATE_FORMATTER));
                            insertStmt.executeUpdate();
                            System.out.println("Nouveau joueur: " + playerName + " - Score: " + score);
                        }
                    }
                }
            }
        } catch (SQLException e) { 
            System.err.println("Erreur lors de la sauvegarde du score: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Recupere les meilleurs scores (tries par score total decroissant)
    public List<ScoreRecord> getTopScores(int limit) {
        if (!dbManager.isAvailable()) { 
            System.out.println("Base de données non disponible pour récupérer les scores");
            return new ArrayList<>();
        }
        
        List<ScoreRecord> scores = new ArrayList<>(); 
        String sql = "SELECT player_name, total_score, best_level, games_played, last_played FROM scores ORDER BY total_score DESC LIMIT ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) { 
            
            pstmt.setInt(1, limit);// On limite le nombre de scores a recuperer
            ResultSet rs = pstmt.executeQuery();// On execute la requete
            
            while (rs.next()) {// On parcourt les resultats
                scores.add(new ScoreRecord(
                    rs.getString("player_name"),
                    rs.getInt("total_score"),
                    rs.getInt("best_level"),
                    rs.getInt("games_played"),
                    rs.getString("last_played")
                ));
            }
            
            System.out.println("Scores récupérés: " + scores.size());
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des scores: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
        
        // Trier par score decroissant et limiter au nombre demande
        return scores.stream()
            .sorted(Comparator.comparingInt(ScoreRecord::getScore).reversed())
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    // Recupere tous les scores de la base de donnees
    public List<ScoreRecord> getAllScores() {
        List<ScoreRecord> scores = new ArrayList<>();
        String sql = "SELECT player_name, total_score, best_level, games_played, last_played FROM scores";
        
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                scores.add(new ScoreRecord(
                    rs.getString("player_name"),
                    rs.getInt("total_score"),
                    rs.getInt("best_level"),
                    rs.getInt("games_played"),
                    rs.getString("last_played")
                ));
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des scores: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Trier par score decroissant
        return scores.stream()
            .sorted(Comparator.comparingInt(ScoreRecord::getScore).reversed())
            .collect(Collectors.toList());
    }
    
    // Classe pour representer un score dans la base de donnees
    public static class ScoreRecord {
        private String playerName;
        private int score;
        private int level;
        private int gamesPlayed;
        private String datePlayed;
        
        public ScoreRecord(String playerName, int score, int level, String datePlayed) {
            this.playerName = playerName;
            this.score = score;
            this.level = level;
            this.gamesPlayed = 0;
            this.datePlayed = datePlayed;
        }
        
        public ScoreRecord(String playerName, int score, int level, int gamesPlayed, String datePlayed) {
            this.playerName = playerName;
            this.score = score;
            this.level = level;
            this.gamesPlayed = gamesPlayed;
            this.datePlayed = datePlayed;
        }
        
        // Getters
        public String getPlayerName() { return playerName; }
        public int getScore() { return score; }
        public int getLevel() { return level; }
        public int getGamesPlayed() { return gamesPlayed; }
        public String getDatePlayed() { return datePlayed; }
        
        @Override
        public String toString() {
            if (gamesPlayed > 0) {
                return String.format("%s - %d points (Meilleur niveau: %d, Parties: %d) - %s", 
                    playerName, score, level, gamesPlayed, datePlayed);
            } else {
                return String.format("%s - %d points (Niveau %d) - %s", 
                    playerName, score, level, datePlayed);
            }
        }
    }
}

