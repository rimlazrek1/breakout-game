package model;

import java.awt.Color;
import java.awt.Graphics2D;

// Represente la balle qui rebondit dans le jeu
public class Ball {
    private double x, y;
    private double velocityX, velocityY;
    private int radius;
    private Color color;
    private boolean active;
    
    public Ball(double x, double y, int radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.color = Color.WHITE;
        this.active = true;
        this.velocityX = 3;// La balle commence avec une vitesse de 3 pixels par frame
        this.velocityY = -3;// velocityY negatif = vers le haut
    }
    
    public void update() { // Met a jour la position de la balle en fonction de sa vitesse
        if (active) {
            x += velocityX;
            y += velocityY;
        }
    }
    
    public void reverseX() {// Inverse la vitesse de la balle sur l'axe X
        velocityX = -velocityX;
    }
    
    public void reverseY() {// Inverse la vitesse de la balle sur l'axe Y
        velocityY = -velocityY;
    }
    
    public void setVelocity(double vx, double vy) {
        this.velocityX = vx;
        this.velocityY = vy;
    }
    
    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    public void draw(Graphics2D g) {// Dessine la balle sur l'ecran
        if (active) {
            g.setColor(color);
            g.fillOval((int)(x - radius), (int)(y - radius), radius * 2, radius * 2);
            g.setColor(Color.GRAY);
            g.drawOval((int)(x - radius), (int)(y - radius), radius * 2, radius * 2);
        }
    }
    
    // Getters
    public double getX() { return x; }
    public double getY() { return y; }
    public double getVelocityX() { return velocityX; }
    public double getVelocityY() { return velocityY; }
    public int getRadius() { return radius; }
    public boolean isActive() { return active; }
    
    // Setters
    public void setActive(boolean active) { this.active = active; }
    public void setColor(Color color) { this.color = color; }
}











