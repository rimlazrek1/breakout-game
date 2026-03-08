package game;

import exceptions.GameOverException;
import model.Ball;
import model.Brick;
import model.GameState;
import model.Paddle;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GameEngine {
    private Ball ball;
    private Paddle paddle;
    private List<Brick> bricks;
    private GameState state;
    private int score;
    private int lives;
    private int level;
    private int screenWidth;
    private int screenHeight;
    
    
    private static final int INITIAL_LIVES = 1; // Le joueur n'a qu'une seule vie
    private static final int BRICK_ROWS = 5; // 5 rangees de briques
    private static final int BRICK_COLS = 10; // 10 briques par rangee
    private static final int BRICK_WIDTH = 60;
    private static final int BRICK_HEIGHT = 25;
    private static final int BRICK_SPACING = 5; // Espace entre les briques
    private static final int SPEED_BOOST_INTERVAL = 50; // La vitesse augmente tous les 50 points
    private int lastSpeedBoostScore = 0; // Pour eviter d'augmenter la vitesse plusieurs fois au meme palier
    
    public GameEngine(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.state = GameState.MENU;
        this.score = 0;
        this.lives = INITIAL_LIVES;
        this.level = 1;
        this.bricks = new ArrayList<>();
        
        initializeGame();
    }
    
    public void initializeGame() { 
        // Creer la balle au centre de l'ecran, un peu au-dessus de la raquette
        ball = new Ball(screenWidth / 2, screenHeight - 100, 10);
        ball.setColor(new java.awt.Color(255, 255, 255));
        
        // Creer la raquette en bas de l'ecran, centree
        int paddleWidth = 100;
        int paddleHeight = 15;
        paddle = new Paddle( // Creer la raquette
            screenWidth / 2 - paddleWidth / 2,
            screenHeight - 50,
            paddleWidth,
            paddleHeight,
            0,
            screenWidth
        );
        
        createBricks(); // Creer toutes les briques du niveau
    }
    
    // Cree toutes les briques du niveau en les placant en grille
    private void createBricks() {
        bricks.clear();
        
        int startX = (screenWidth - (BRICK_COLS * (BRICK_WIDTH + BRICK_SPACING) - BRICK_SPACING)) / 2;
        int startY = 50;
        
        Color[] colors = { 
            Color.RED,
            Color.ORANGE,
            Color.YELLOW,
            Color.GREEN,
            Color.CYAN
        };
        
        int[] points = {50, 40, 30, 20, 10}; // Points pour chaque brique
        
        for (int row = 0; row < BRICK_ROWS; row++) { // Pour chaque rangee de briques
            for (int col = 0; col < BRICK_COLS; col++) { // Pour chaque colonne de briques
                int x = startX + col * (BRICK_WIDTH + BRICK_SPACING);
                int y = startY + row * (BRICK_HEIGHT + BRICK_SPACING);
                
                Color color = colors[row % colors.length];
                int pointValue = points[row % points.length];
                
                // Les briques du bas necessitent moins de frappes que celles du haut
                // Ligne du bas (row 4) = 1 frappe, ligne du haut (row 0) = 5 frappes
                int maxHits = BRICK_ROWS - row;
                
                bricks.add(new Brick(x, y, BRICK_WIDTH, BRICK_HEIGHT, color, pointValue, maxHits));
            }
        }
    }
    
    // Appele a chaque frame pour mettre a jour le jeu
    public void update() throws GameOverException { 

        //verifier si le jeu est en cours de jeu
        if (state != GameState.PLAYING) {
            return; 
        }
        
        // Mettre a jour des positions de la balle et la raquette
        ball.update();
        paddle.update();
        
        // Verifier les collisions avec les murs
        CollisionDetector.checkWallCollision(ball, screenWidth, screenHeight);
        
        // Verifier la collision avec la raquette
        CollisionDetector.checkPaddleCollision(ball, paddle);
        
        // Detection des collisions avec les briques
        List<Brick> hitBricks = bricks.stream()
            .filter(brick -> CollisionDetector.checkBrickCollision(ball, brick))
            .collect(Collectors.toList());
        
        // Traitement des briques touchees
        hitBricks.forEach(brick -> {
            brick.hit();
            if (brick.isDestroyed()) {
                score += brick.getPoints();
            }
        });
        
        // Augmenter la vitesse de la balle tous les 50 points
        int currentBoostLevel = score / SPEED_BOOST_INTERVAL;
        int lastBoostLevel = lastSpeedBoostScore / SPEED_BOOST_INTERVAL;
        
        if (currentBoostLevel > lastBoostLevel && score > 0) { // Si le score est superieur a 0 et que le niveau de boost est superieur au dernier niveau de boost
            lastSpeedBoostScore = currentBoostLevel * SPEED_BOOST_INTERVAL;
            increaseBallSpeed(score);
            level++;
        }
        
        // Verifier la victoire
        boolean allDestroyed = bricks.stream()
            .allMatch(Brick::isDestroyed);
        
        if (allDestroyed) {
            levelComplete();
        }
        
        // Verifier si la balle a tombe
        if (ball.getY() > screenHeight) {
            loseLife();
        }
    }
    
    // Appele quand la balle tombe - avec 1 vie, c'est game over direct
    private void loseLife() throws GameOverException {
        lives--;
        throw new GameOverException("Game Over! Score final: " + score, score);
    }
    
    // Augmente la vitesse de la balle
    // A partir de 450 points, l'augmentation est plus faible pour que le jeu reste jouable
    private void increaseBallSpeed(int currentScore) {
        double currentVx = ball.getVelocityX();
        double currentVy = ball.getVelocityY();
        // Augmentation de 15% avant 450 points, 8% après
        double speedMultiplier = (currentScore >= 450) ? 1.08 : 1.15;
        ball.setVelocity(currentVx * speedMultiplier, currentVy * speedMultiplier);
        // La balle devient jaune pour indiquer le boost de vitesse
        ball.setColor(new java.awt.Color(255, 255, 0));
    }
    
    // Appele quand toutes les briques sont detruites
    private void levelComplete() {
        state = GameState.VICTORY;
        score += 1000; // Bonus de 1000 points pour la victoire
    }
    
    // Demarre une nouvelle partie
    public void startGame() {
        state = GameState.PLAYING;
        score = 0;
        lives = INITIAL_LIVES;
        level = 1;
        lastSpeedBoostScore = 0;
        initializeGame();
    }
    
    // Alterne entre pause et jeu
    public void togglePause() {
        if (state == GameState.PLAYING) {
            state = GameState.PAUSED;
        } else if (state == GameState.PAUSED) {
            state = GameState.PLAYING;
        }
    }
    
    // Remet le jeu a zero et retourne au menu
    public void reset() {
        state = GameState.MENU;
        score = 0;
        lives = INITIAL_LIVES;
        level = 1;
        lastSpeedBoostScore = 0; // Reinitialiser le boost de vitesse
        initializeGame();
    }
    
    // Getters
    public Ball getBall() { return ball; }
    public Paddle getPaddle() { return paddle; }
    public List<Brick> getBricks() { return bricks; }
    public GameState getState() { return state; }
    public int getScore() { return score; }
    public int getLives() { return lives; }
    public int getLevel() { return level; }
    
    // Setters
    public void setState(GameState state) { this.state = state; }
}

