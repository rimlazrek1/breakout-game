package ui;

import database.ScoreDAO;
import exceptions.GameOverException;
import game.GameEngine;
import model.GameState;
import model.Paddle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

// Panel principal qui affiche le jeu et gere les entrees utilisateur
public class GamePanel extends JPanel {
    private GameEngine gameEngine;
    private Timer gameTimer;
    private boolean[] keys;
    private long speedBoostMessageTime = 0; // Quand le message "VITESSE BOOSTER" a ete affiche
    private int lastBoostLevel = 0; // Pour detecter quand on passe a un nouveau niveau de boost
    private boolean victoryHandled = false; // Pour eviter de sauvegarder le score plusieurs fois
    private List<database.ScoreDAO.ScoreRecord> cachedScores = null; // Cache pour eviter de charger les scores a chaque frame
    private long scoresCacheTime = 0; // Quand on a charge les scores la dernière fois
    private GameState previousState = GameState.MENU; // Pour revenir au bon acran apres avoir vu les scores
    private int menuSelection = 0; // Option selectionnee dans le menu (0=Commencer, 1=Scores, 2=Quitter)
    private int gameOverSelection = 0; // Option selectionnee dans game over (0=Scores, 1=Recommencer, 2=Quitter)
    private boolean gameOverSelectionInitialized = false; // Pour reinitialiser la sélection une seule fois
    private static final long SPEED_BOOST_MESSAGE_DURATION = 2000; // Le message s'affiche pendant 2 secondes
    private static final long SCORES_CACHE_DURATION = 1000; // Le cache des scores dure 1 seconde
    
    // Dimensions
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    
    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true); // permetre de recevoir les touches du clavier
        
        keys = new boolean[256]; // tableau de 256 booleens pour les touches du clavier
        gameEngine = new GameEngine(WIDTH, HEIGHT);
        
        setupKeyListeners();
        setupMouseListeners();
        setupGameTimer();
        
        addAncestorListener(new javax.swing.event.AncestorListener() { //ajoute un listener pour le focus clavier
            public void ancestorAdded(javax.swing.event.AncestorEvent event) { 
                requestFocusInWindow(); //demander le focus clavier pour que les touches fonctionnent
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent event) {} 
            public void ancestorMoved(javax.swing.event.AncestorEvent event) {}
        });
    }
    
    private void setupKeyListeners() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) { //quand une touche est pressee
                keys[e.getKeyCode()] = true; //met la touche dans le tableau des touches pressees
                handleKeyPress(e.getKeyCode()); //appelle la methode pour gerer la touche
            }
            
            @Override
            public void keyReleased(KeyEvent e) { //quand une touche est relachee
                keys[e.getKeyCode()] = false; //met la touche dans le tableau des touches relachees
                handleKeyRelease(e.getKeyCode()); //appelle la methode pour gerer la touche relachee
            }
        });
    }
    
    // Configure les ecouteurs de souris pour deplacer la raquette et naviguer dans les menus
    private void setupMouseListeners() {
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) { 
                if (gameEngine.getState() == GameState.PLAYING) { //si le jeu est en cours
                    Paddle paddle = gameEngine.getPaddle(); //recupere la raquette
                    paddle.setX(e.getX() - paddle.getWidth() / 2);   //deplace la raquette selon la position de la souris
                } else if (gameEngine.getState() == GameState.MENU) { //si le jeu est dans le menu
                    // Navigation dans le menu avec la souris
                    int startY = 220;
                    int lineHeight = 50;
                    int mouseY = e.getY();
                    
                    for (int i = 0; i < 3; i++) { //parcours les 3 options du menu
                        int optionY = startY + i * lineHeight; //calcul la position de l'option selon la ligne
                        if (mouseY >= optionY - 20 && mouseY <= optionY + 20) { //si la souris est sur l'option
                            if (menuSelection != i) { //si la selection n'est pas sur l'option
                                menuSelection = i; //met la selection a l'option
                                repaint(); //rafraichit l'ecran
                            }
                            break;
                        }
                    }
                } else if (gameEngine.getState() == GameState.GAME_OVER || 
                          gameEngine.getState() == GameState.VICTORY) {
                    // Gérer la sélection du menu game over avec la souris
                    int startY = 320; 
                    int lineHeight = 50; 
                    int mouseY = e.getY();
                    
                    for (int i = 0; i < 3; i++) { //parcours les 3 options du menu game over
                        int optionY = startY + i * lineHeight; //calcul la position de l'option selon la ligne
                        if (mouseY >= optionY - 20 && mouseY <= optionY + 20) {
                            if (gameOverSelection != i) { //si la selection n'est pas sur l'option
                                gameOverSelection = i; //met la selection a l'option
                                repaint(); 
                            }
                            break;
                        }
                    }
                }
            }
        });
        
        addMouseListener(new MouseAdapter() { //ajoute un listener pour la souris
            @Override
            public void mouseClicked(MouseEvent e) { //quand la souris est cliquee
                if (gameEngine.getState() == GameState.MENU) { //si le jeu est dans le menu
                    handleMenuSelection(); //appelle la methode pour gerer la selection du menu
                } else if (gameEngine.getState() == GameState.GAME_OVER ||
                          gameEngine.getState() == GameState.VICTORY) {
                    handleGameOverSelection(); //appelle la methode pour gerer la selection du menu game over
                }
            }
        });
    }
    
    // Configure le timer qui met a jour le jeu 60 fois par seconde
    private void setupGameTimer() {
        gameTimer = new Timer(16, e -> { // 16ms = environ 60 FPS (frames par seconde)
            try {
                updateGame();
            } catch (GameOverException ex) {    
                handleGameOver(ex);
            }
            repaint();
        });
        gameTimer.start();
    }
    
    // Appele a chaque frame pour mettre a jour le jeu
    private void updateGame() throws GameOverException {
        // Deplacer la raquette selon les touches pressees
        if (keys[KeyEvent.VK_LEFT] || keys[KeyEvent.VK_A]) {
            gameEngine.getPaddle().moveLeft();
        } else if (keys[KeyEvent.VK_RIGHT] || keys[KeyEvent.VK_D]) {
            gameEngine.getPaddle().moveRight();
        } else {
            gameEngine.getPaddle().stop();
        }
        
        // Mettre a jour la logique du jeu (mouvement, collisions, etc.)
        gameEngine.update();
        
        // Detecter quand la vitesse augmente pour afficher le message
        int currentScore = gameEngine.getScore();
        int currentBoostLevel = currentScore / 50;
        if (currentBoostLevel > lastBoostLevel) { 
            speedBoostMessageTime = System.currentTimeMillis();
            lastBoostLevel = currentBoostLevel;
        }
        
        // Gerer la sauvegarde du score en cas de victoire
        GameState currentState = gameEngine.getState();
        if (currentState == GameState.VICTORY) {
            // Sauvegarder une seule fois
            if (!victoryHandled) { //si la victoire n'a pas ete geree
                victoryHandled = true; //met le flag a true
                handleVictory(); //appelle la methode pour gerer la victoire
                gameOverSelection = 0; //met la selection a 0
                gameOverSelectionInitialized = true; //met le flag a true
            }
        } else if (currentState == GameState.GAME_OVER) {
            // Reinitialiser la selection du menu une seule fois
            if (!gameOverSelectionInitialized) {
                gameOverSelection = 0;
                gameOverSelectionInitialized = true;
            }
        } else {
            // Reinitialiser les flags quand on quitte ces etats
            victoryHandled = false;
            gameOverSelectionInitialized = false;
        }
    }
    
    // Gere toutes les touches pressees
    private void handleKeyPress(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_SPACE:
                if (gameEngine.getState() == GameState.MENU) {
                    // Entree dans le menu principal
                    handleMenuSelection();
                } else if (gameEngine.getState() == GameState.PLAYING || 
                          gameEngine.getState() == GameState.PAUSED) {
                    gameEngine.togglePause();
                } else if (gameEngine.getState() == GameState.GAME_OVER ||
                          gameEngine.getState() == GameState.VICTORY) {
                    // Entree dans le menu game over
                    handleGameOverSelection();
                }
                break;
            case KeyEvent.VK_ENTER:
                if (gameEngine.getState() == GameState.MENU) {
                    handleMenuSelection();
                } else if (gameEngine.getState() == GameState.GAME_OVER ||
                          gameEngine.getState() == GameState.VICTORY) {
                    handleGameOverSelection();
                }
                break;
            case KeyEvent.VK_UP:
                if (gameEngine.getState() == GameState.MENU) {
                    menuSelection = (menuSelection - 1 + 3) % 3; // 3 options dans le menu
                    repaint();
                } else if (gameEngine.getState() == GameState.GAME_OVER ||
                          gameEngine.getState() == GameState.VICTORY) {
                    gameOverSelection = (gameOverSelection - 1 + 3) % 3; // 3 options dans game over
                    repaint();
                }
                break;
            case KeyEvent.VK_DOWN:
                if (gameEngine.getState() == GameState.MENU) {
                    menuSelection = (menuSelection + 1) % 3; // 3 options dans le menu
                    repaint();
                } else if (gameEngine.getState() == GameState.GAME_OVER ||
                          gameEngine.getState() == GameState.VICTORY) {
                    gameOverSelection = (gameOverSelection + 1) % 3; // 3 options dans game over
                    repaint();
                }
                break;
            case KeyEvent.VK_S:
                if (gameEngine.getState() == GameState.MENU || 
                    gameEngine.getState() == GameState.GAME_OVER ||
                    gameEngine.getState() == GameState.VICTORY) {
                    // Aller voir les scores
                    previousState = gameEngine.getState();
                    gameEngine.setState(GameState.SCORES);
                    cachedScores = null; // Recharger les scores
                    repaint();
                }
                break;
            case KeyEvent.VK_B:
            case KeyEvent.VK_M:
                if (gameEngine.getState() == GameState.SCORES) {
                    // Revenir a l'ecran précédent
                    gameEngine.setState(previousState);
                    if (previousState == GameState.MENU) {
                        menuSelection = 0;
                    } else if (previousState == GameState.GAME_OVER || previousState == GameState.VICTORY) {
                        gameOverSelection = 0;
                    }
                    repaint();
                }
                break;
            case KeyEvent.VK_R:
                if (gameEngine.getState() == GameState.GAME_OVER ||
                    gameEngine.getState() == GameState.VICTORY) {
                    gameEngine.reset();
                    gameEngine.startGame(); // Redemarrer directement
                    lastBoostLevel = 0; // Reinitialiser le niveau de boost
                    speedBoostMessageTime = 0; // Reinitialiser le message
                    victoryHandled = false; // Reinitialiser le flag de victoire
                }
                break;
            case KeyEvent.VK_Q:
            case KeyEvent.VK_ESCAPE:
                if (gameEngine.getState() == GameState.PAUSED ||
                    gameEngine.getState() == GameState.GAME_OVER ||
                    gameEngine.getState() == GameState.VICTORY) {
                    System.exit(0); // Quitter le jeu
                }
                break;
        }
    }
    
    // Execute l'action selectionnee dans le menu principal
    private void handleMenuSelection() {
        switch (menuSelection) {
            case 0: // Commencer le jeu
                gameEngine.startGame();
                lastBoostLevel = 0;
                victoryHandled = false;
                break;
            case 1: // Voir les scores
                previousState = GameState.MENU;
                gameEngine.setState(GameState.SCORES);
                cachedScores = null;
                repaint();
                break;
            case 2: // Quitter
                System.exit(0);
                break;
        }
    }
    
    // Execute l'action selectionnee dans le menu game over/victoire
    private void handleGameOverSelection() {
        switch (gameOverSelection) {
            case 0: // Voir les scores
                previousState = gameEngine.getState();
                gameEngine.setState(GameState.SCORES);
                cachedScores = null;
                repaint();
                break;
            case 1: // Recommencer
                gameEngine.reset();
                gameEngine.startGame();
                lastBoostLevel = 0;
                speedBoostMessageTime = 0;
                victoryHandled = false;
                gameOverSelection = 0; // Reinitialiser la selection
                gameOverSelectionInitialized = false; // Reinitialiser le flag
                menuSelection = 0; // Reinitialiser aussi le menu principal
                break;
            case 2: // Quitter
                System.exit(0);
                break;
        }
    }
    
    // Gere les touches relachees (pas vraiment utilise ici)
    private void handleKeyRelease(int keyCode) {
    }
    
    // Appele quand le joueur perd - demande le nom et sauvegarde le score
    private void handleGameOver(GameOverException e) {
        gameEngine.setState(GameState.GAME_OVER);
        gameOverSelection = 0; // Reinitialiser la sélection du menu
        
        // Sauvegarder le score dans la base de donnees
        try {
            ScoreDAO scoreDAO = new ScoreDAO();
            String playerName = JOptionPane.showInputDialog(
                this,
                "Entrez votre nom pour sauvegarder votre score:",
                "Sauvegarde du score",
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (playerName != null && !playerName.trim().isEmpty()) {
                scoreDAO.saveScore(playerName.trim(), e.getFinalScore(), gameEngine.getLevel());
            }
        } catch (Exception ex) {
            System.err.println("Erreur lors de la sauvegarde du score: " + ex.getMessage());
        }
    }
    
    // Appele quand le joueur gagne - demande le nom et sauvegarde le score
    private void handleVictory() {
        try {
            ScoreDAO scoreDAO = new ScoreDAO();
            String playerName = JOptionPane.showInputDialog(
                this,
                "Entrez votre nom pour sauvegarder votre score:",
                "Sauvegarde du score",
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (playerName != null && !playerName.trim().isEmpty()) {
                scoreDAO.saveScore(playerName.trim(), gameEngine.getScore(), gameEngine.getLevel());
            }
        } catch (Exception ex) {
            System.err.println("Erreur lors de la sauvegarde du score: " + ex.getMessage());
        }
    }
    
    // Methode appelee par Swing pour dessiner le jeu
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        GameState state = gameEngine.getState();
        
        switch (state) {
            case MENU:
                drawMenu(g2d);
                break;
            case SCORES:
                drawScoresScreen(g2d);
                break;
            case PLAYING:
            case PAUSED:
                drawGame(g2d);
                if (state == GameState.PAUSED) {
                    drawPauseScreen(g2d);
                }
                break;
            case GAME_OVER:
                drawGame(g2d);
                drawGameOverScreen(g2d);
                break;
            case VICTORY:
                drawGame(g2d);
                drawVictoryScreen(g2d);
                break;
        }
    }
    
    // Dessine l'ecran du menu principal
    private void drawMenu(Graphics2D g) {
        // Titre "Casse-briques" en vert
        g.setColor(Color.GREEN);
        g.setFont(new Font("Arial", Font.BOLD, 56));
        FontMetrics fm = g.getFontMetrics();
        String title = "Casse-briques";
        int x = (getWidth() - fm.stringWidth(title)) / 2;
        g.drawString(title, x, 100);
        
        // Options du menu
        String[] menuOptions = {
            "Commencer le jeu",
            "Voir les scores",
            "Quitter"
        };
        
        int startY = 220;
        int lineHeight = 50;
        
        g.setFont(new Font("Arial", Font.PLAIN, 28));
        for (int i = 0; i < menuOptions.length; i++) {
            if (i == menuSelection) {
                // Option selectionnee en jaune avec indicateur
                g.setColor(Color.YELLOW);
                String selected = "> " + menuOptions[i] + " <";
                fm = g.getFontMetrics();
                x = (getWidth() - fm.stringWidth(selected)) / 2;
                g.drawString(selected, x, startY + i * lineHeight);
            } else {
                // Option non selectionnee en blanc
                g.setColor(Color.WHITE);
                fm = g.getFontMetrics();
                x = (getWidth() - fm.stringWidth(menuOptions[i])) / 2;
                g.drawString(menuOptions[i], x, startY + i * lineHeight);
            }
        }
        
        // Instructions de controle
        g.setColor(Color.CYAN);
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        String[] instructions = {
            "Appuyez sur Espace pour mettre en pause",
            "Utilisez la souris ou les flèches ou A/D pour bouger la plateforme"
        };
        int infoY = startY + menuOptions.length * lineHeight + 30;
        for (int i = 0; i < instructions.length; i++) {
            fm = g.getFontMetrics();
            x = (getWidth() - fm.stringWidth(instructions[i])) / 2;
            g.drawString(instructions[i], x, infoY + i * 25);
        }
    }
    
    // Dessine l'ecran qui affiche les meilleurs scores
    private void drawScoresScreen(Graphics2D g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        FontMetrics fm = g.getFontMetrics();
        String title = "MEILLEURS SCORES";
        int x = (getWidth() - fm.stringWidth(title)) / 2;
        g.drawString(title, x, 50);
        
        // Verifier si le driver SQLite est disponible
        database.DatabaseManager dbManager = database.DatabaseManager.getInstance();
        if (!dbManager.isAvailable()) {
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.PLAIN, 18));
            String noDb = "Base de données non disponible";
            fm = g.getFontMetrics();
            x = (getWidth() - fm.stringWidth(noDb)) / 2;
            g.drawString(noDb, x, getHeight() / 2 - 20);
            
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 16));
            String info = "Vérifiez que sqlite-jdbc.jar est dans lib/";
            fm = g.getFontMetrics();
            x = (getWidth() - fm.stringWidth(info)) / 2;
            g.drawString(info, x, getHeight() / 2 + 10);
        } else {
            // Charger les scores avec un cache pour eviter de les recharger à chaque frame
            long currentTime = System.currentTimeMillis();
            List<database.ScoreDAO.ScoreRecord> scores = null;
            
            if (cachedScores == null || (currentTime - scoresCacheTime) > SCORES_CACHE_DURATION) {
                try {
                    database.ScoreDAO scoreDAO = new database.ScoreDAO();
                    scores = scoreDAO.getTopScores(10);
                    cachedScores = scores;
                    scoresCacheTime = currentTime;
                } catch (Exception e) {
                    scores = cachedScores; // Utiliser le cache en cas d'erreur
                }
            } else {
                scores = cachedScores; // Utiliser le cache
            }
            
            try {
                
                if (scores == null || scores.isEmpty()) {
                    g.setColor(Color.WHITE);
                    g.setFont(new Font("Arial", Font.PLAIN, 20));
                    String noScores = "Aucun score enregistré";
                    fm = g.getFontMetrics();
                    x = (getWidth() - fm.stringWidth(noScores)) / 2;
                    g.drawString(noScores, x, getHeight() / 2);
                } else {
                    g.setColor(Color.WHITE);
                    g.setFont(new Font("Arial", Font.BOLD, 16));
                    int y = 100;
                    int rank = 1;
                    for (database.ScoreDAO.ScoreRecord record : scores) {
                        if (record != null) {
                            String scoreText = String.format("%d. %s - %d points (Niveau: %d, Parties: %d)",
                                rank, record.getPlayerName(), record.getScore(), record.getLevel(), record.getGamesPlayed());
                            g.drawString(scoreText, 50, y);
                            y += 35;
                            rank++;
                            
                            // Limiter l'affichage pour eviter de dépasser l'écran
                            if (y > getHeight() - 100) break;
                        }
                    }
                }
            } catch (Exception e) {
                g.setColor(Color.RED);
                g.setFont(new Font("Arial", Font.PLAIN, 18));
                String error = "Erreur lors du chargement des scores: " + e.getMessage();
                fm = g.getFontMetrics();
                x = (getWidth() - fm.stringWidth(error)) / 2;
                if (x < 0) x = 10; // Si le message est trop long
                g.drawString(error, x, getHeight() / 2);
                e.printStackTrace(); // Pour le débogage
            }
        }
        
        g.setColor(Color.CYAN);
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        String back = "Appuyez sur M pour retourner au menu";
        fm = g.getFontMetrics();
        x = (getWidth() - fm.stringWidth(back)) / 2;
        g.drawString(back, x, getHeight() - 50);
    }
    
    // Dessine tous les elements du jeu (balle, raquette, briques, UI)
    private void drawGame(Graphics2D g) {
        gameEngine.getBall().draw(g);
        gameEngine.getPaddle().draw(g);
        gameEngine.getBricks().forEach(brick -> brick.draw(g));
        drawUI(g);
    }
    
    // Dessine l'interface utilisateur (score, niveau, message de boost)
    private void drawUI(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        
        // Score
        g.drawString("Score: " + gameEngine.getScore(), 10, 30);
        
        g.drawString("Niveau: " + gameEngine.getLevel(), 10, 60);
        
        // Afficher le message "VITESSE BOOSTER" pendant 2 secondes
        long currentTime = System.currentTimeMillis();
        if (speedBoostMessageTime > 0 && 
            (currentTime - speedBoostMessageTime) < SPEED_BOOST_MESSAGE_DURATION) {
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            int boostLevel = gameEngine.getScore() / 50;
            String text = "VITESSE BOOSTER x" + boostLevel + "!";
            FontMetrics fm = g.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(text)) / 2;
            int y = getHeight() / 2;
            g.drawString(text, x, y);
        }
    }
    
    // Dessine l'ecran de pause par-dessus le jeu
    private void drawPauseScreen(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, getWidth(), getHeight());
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 48));
        FontMetrics fm = g.getFontMetrics();
        String text = "PAUSE";
        int x = (getWidth() - fm.stringWidth(text)) / 2;
        g.drawString(text, x, getHeight() / 2);
        
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        text = "Appuyez sur ESPACE pour reprendre";
        fm = g.getFontMetrics();
        x = (getWidth() - fm.stringWidth(text)) / 2;
        g.drawString(text, x, getHeight() / 2 + 50);
        
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        text = "Appuyez sur Q pour quitter";
        fm = g.getFontMetrics();
        x = (getWidth() - fm.stringWidth(text)) / 2;
        g.drawString(text, x, getHeight() / 2 + 80);
    }
    
    // Dessine l'ecran de game over avec le score final et les options
    private void drawGameOverScreen(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(0, 0, getWidth(), getHeight());
        
        g.setColor(Color.RED);
        g.setFont(new Font("Arial", Font.BOLD, 48));
        FontMetrics fm = g.getFontMetrics();
        String text = "GAME OVER";
        int x = (getWidth() - fm.stringWidth(text)) / 2;
        g.drawString(text, x, 150);
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 24));
        text = "Score final: " + gameEngine.getScore();
        fm = g.getFontMetrics();
        x = (getWidth() - fm.stringWidth(text)) / 2;
        g.drawString(text, x, 220);
        
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        text = "Niveau atteint: " + gameEngine.getLevel();
        fm = g.getFontMetrics();
        x = (getWidth() - fm.stringWidth(text)) / 2;
        g.drawString(text, x, 260);
        
        // Menu avec les options disponibles
        String[] gameOverOptions = {
            "Voir les scores",
            "Recommencer",
            "Quitter"
        };
        
        int startY = 320;
        int lineHeight = 50;
        
        g.setFont(new Font("Arial", Font.PLAIN, 28));
        for (int i = 0; i < gameOverOptions.length; i++) {
            if (i == gameOverSelection) {
                // Option selectionnée en jaune avec indicateur
                g.setColor(Color.YELLOW);
                String selected = "> " + gameOverOptions[i] + " <";
                fm = g.getFontMetrics();
                x = (getWidth() - fm.stringWidth(selected)) / 2;
                g.drawString(selected, x, startY + i * lineHeight);
            } else {
                // Option non selectionnee en blanc
                g.setColor(Color.WHITE);
                fm = g.getFontMetrics();
                x = (getWidth() - fm.stringWidth(gameOverOptions[i])) / 2;
                g.drawString(gameOverOptions[i], x, startY + i * lineHeight);
            }
        }
    }
    
    // Dessine l'ecran de victoire avec le score final et les options
    private void drawVictoryScreen(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(0, 0, getWidth(), getHeight());
        
        g.setColor(Color.GREEN);
        g.setFont(new Font("Arial", Font.BOLD, 48));
        FontMetrics fm = g.getFontMetrics();
        String text = "BIEN JOUE!";
        int x = (getWidth() - fm.stringWidth(text)) / 2;
        g.drawString(text, x, 120);
        
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        text = "VICTOIRE!";
        fm = g.getFontMetrics();
        x = (getWidth() - fm.stringWidth(text)) / 2;
        g.drawString(text, x, 180);
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 24));
        text = "Score final: " + gameEngine.getScore();
        fm = g.getFontMetrics();
        x = (getWidth() - fm.stringWidth(text)) / 2;
        g.drawString(text, x, 240);
        
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        text = "Niveau atteint: " + gameEngine.getLevel();
        fm = g.getFontMetrics();
        x = (getWidth() - fm.stringWidth(text)) / 2;
        g.drawString(text, x, 280);
        
        // Menu avec les options disponibles
        String[] gameOverOptions = {
            "Voir les scores",
            "Recommencer",
            "Quitter"
        };
        
        int startY = 340;
        int lineHeight = 50;
        
        g.setFont(new Font("Arial", Font.PLAIN, 28));
        for (int i = 0; i < gameOverOptions.length; i++) {
            if (i == gameOverSelection) {
                // Option selectionnee en jaune avec indicateur
                g.setColor(Color.YELLOW);
                String selected = "> " + gameOverOptions[i] + " <";
                fm = g.getFontMetrics();
                x = (getWidth() - fm.stringWidth(selected)) / 2;
                g.drawString(selected, x, startY + i * lineHeight);
            } else {
                // Option non selectionnee en blanc
                g.setColor(Color.WHITE);
                fm = g.getFontMetrics();
                x = (getWidth() - fm.stringWidth(gameOverOptions[i])) / 2;
                g.drawString(gameOverOptions[i], x, startY + i * lineHeight);
            }
        }
        
    }
    
    public GameEngine getGameEngine() {
        return gameEngine;
    }
}

