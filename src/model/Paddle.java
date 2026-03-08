package model;

import java.awt.Color;
import java.awt.Graphics2D;

// Represente la raquette que le joueur controle pour renvoyer la balle
public class Paddle {
    private int x, y;
    private int width, height;
    private int velocity;
    private Color color;
    private int minX, maxX;
    
    public Paddle(int x, int y, int width, int height, int minX, int maxX) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.minX = minX;
        this.maxX = maxX;
        this.color = Color.CYAN;
        this.velocity = 0;
    }
    
    public void update() {// Met a jour la position de la raquette en fonction de sa vitesse
        x += velocity;
        // Empecher la raquette de sortir de l'ecran
        if (x < minX) {
            x = minX;
        } else if (x + width > maxX) {
            x = maxX - width;
        }
    }
    
    public void moveLeft() {
        velocity = -5;
    }
    
    public void moveRight() {
        velocity = 5;
    }
    
    public void stop() {
        velocity = 0;
    }
    
    public boolean contains(int px, int py) {// Verifie si la raquette contient un point
        return px >= x && px <= x + width && py >= y && py <= y + height;
    }
    
    public void draw(Graphics2D g) {
        g.setColor(color);
        g.fillRoundRect(x, y, width, height, 10, 10);
        g.setColor(Color.BLACK);
        g.drawRoundRect(x, y, width, height, 10, 10);
    }
    
    // Getters
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getCenterX() { return x + width / 2; }
    
    // Setters
    public void setX(int x) {
        this.x = x;
        if (this.x < minX) this.x = minX;
        if (this.x + width > maxX) this.x = maxX - width;
    }
}











