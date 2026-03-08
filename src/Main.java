import ui.MainFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> { //execute le code dans le thread EDT (Event Dispatch Thread)
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); //set le look and feel systeme
            } catch (Exception e) {
                e.printStackTrace(); 
            }
            
            MainFrame frame = new MainFrame(); //creer la fenetre principale
            frame.setVisible(true); //affiche la fenetre principale
        });
        
    }
}

