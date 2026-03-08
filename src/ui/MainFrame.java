package ui;

import database.DatabaseManager;
import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

// Fenetre principale du jeu
public class MainFrame extends JFrame {
    private GamePanel gamePanel;
    
    public MainFrame() {
        setTitle("Breakout Game - Java Swing");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //Pour fermer proprement la connexion a la base de donnees
        setResizable(false); //ne permetre pas de redimensionner la fenetre
        
        gamePanel = new GamePanel();
        add(gamePanel);
        
        // Pour fermer proprement la connexion à la base de données
        addWindowListener(new WindowAdapter() { //ajoute un listener pour la fermeture de la fenetre
            @Override
            public void windowClosing(WindowEvent e) {
                DatabaseManager.getInstance().closeConnection(); //ferme la connexion a la base de donnees
                System.exit(0);
            }
        });
        
        pack(); //ajuste la taille de la fenetre au contenu
        setLocationRelativeTo(null); //centrer la fenetre sur l'ecran
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> { // lance le frame dans le thread EDT
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); 
            } catch (Exception e) {
                e.printStackTrace(); 
            }
            
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}

