package game;

import model.Ball;
import model.Brick;
import model.Paddle;

// Classe pour detecter toutes les collisions dans le jeu
public class CollisionDetector {
    
    // Verifie si la balle touche un mur et la fait rebondir
    public static boolean checkWallCollision(Ball ball, int screenWidth, int screenHeight) {
        boolean collision = false;
        
        // Si la balle touche un mur
        if (ball.getX() - ball.getRadius() <= 0 || ball.getX() + ball.getRadius() >= screenWidth) { 
            ball.reverseX(); // Inverse la vitesse de la balle sur l'axe X
            collision = true; // La collision est detectee
        }
        
        if (ball.getY() - ball.getRadius() <= 0) { // Si la balle touche le mur du haut
            ball.reverseY();// Inverse la vitesse de la balle sur l'axe Y
            collision = true; // La collision est detectee
        }
        
        return collision;
    }
    
    // Verifie si la balle touche la raquette et calcule l'angle de rebond
    public static boolean checkPaddleCollision(Ball ball, Paddle paddle) {
        double ballX = ball.getX(); // Position X de la balle
        double ballY = ball.getY(); // Position Y de la balle
        int radius = ball.getRadius();// Rayon de la balle
        
        // Verifier si la balle est dans la zone du paddle
        if (ballY + radius >= paddle.getY() && // Si la balle est au-dessus du paddle
            ballY - radius <= paddle.getY() + paddle.getHeight() && // Si la balle est en dessous du paddle
            ballX + radius >= paddle.getX() && // Si la balle est a droite du paddle
            ballX - radius <= paddle.getX() + paddle.getWidth()) { // Si la balle est a gauche du paddle
            
            // Calculer l'angle de rebond selon ou la balle touche le paddle
            // Si on touche le centre, la balle rebondit droit
            // Si on touche les bords, la balle rebondit en angle
            double paddleCenterX = paddle.getX() + paddle.getWidth() / 2.0;
            double hitPos = (ballX - paddleCenterX) / (paddle.getWidth() / 2.0);
            
            // Limiter entre -1 et 1 pour eviter les valeurs extremes
            hitPos = Math.max(-1, Math.min(1, hitPos));
            
            // Garder la meme vitesse mais changer la direction
            double speed = Math.sqrt(ball.getVelocityX() * ball.getVelocityX() + 
                                   ball.getVelocityY() * ball.getVelocityY());// Vitesse de la balle
            double angle = hitPos * Math.PI / 3; // Angle maximum de 60 degres
            
            double newVx = speed * Math.sin(angle);// Nouvelle vitesse sur l'axe X
            double newVy = -Math.abs(speed * Math.cos(angle)); // Toujours vers le haut
            
            ball.setVelocity(newVx, newVy);
            
            // Repositionner la balle juste au-dessus du paddle pour éviter qu'elle reste coincée
            ball.setPosition(ballX, paddle.getY() - radius - 1);
            
            return true;
        }
        
        return false;// Pas de collision
    }
    
    public static boolean checkBrickCollision(Ball ball, Brick brick) {// Verifie si la balle touche une brique et la fait rebondir
        if (brick.isDestroyed()) { // Si la brique est detruite
            return false;// Pas de collision
        }
        
        double ballX = ball.getX();
        double ballY = ball.getY();
        int radius = ball.getRadius();
        
        // Trouver le point le plus proche sur la brique (pour detecter la collision)
        double closestX = Math.max(brick.getX(), Math.min(ballX, brick.getX() + brick.getWidth()));// Point le plus proche sur l'axe X
        double closestY = Math.max(brick.getY(), Math.min(ballY, brick.getY() + brick.getHeight()));// Point le plus proche sur l'axe Y
        
        // Calculer la distance entre le centre de la balle et ce point
        double dx = ballX - closestX;// Distance sur l'axe X
        double dy = ballY - closestY;// Distance sur l'axe Y
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        // Si la distance est plus petite que le rayon, il y a collision
        if (distance < radius) {
            // Determiner si la collision est horizontale ou verticale
            // pour savoir dans quelle direction faire rebondir la balle pour eviter que la balle reste coincée
            if (Math.abs(dx) > Math.abs(dy)) {
                ball.reverseX(); // Rebond horizontal
            } else {
                ball.reverseY(); // Rebond vertical
            }
            
            return true;
        }
        
        return false;
    }
}











