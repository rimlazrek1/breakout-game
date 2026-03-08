package model;

import java.awt.Color;
import java.awt.Graphics2D;

// Represente une brique qui peut etre detruite par la balle
public class Brick {
    private int x, y;
    private int width, height;
    private Color color;
    private boolean destroyed;
    private int points;
    private int hits;
    private int maxHits;
    
    public Brick(int x, int y, int width, int height, Color color, int points) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.color = color;
        this.points = points;
        this.destroyed = false;
        this.hits = 0;
        this.maxHits = 1;
    }
    
    public Brick(int x, int y, int width, int height, Color color, int points, int maxHits) {
        this(x, y, width, height, color, points);
        this.maxHits = maxHits;
    }
    
    public void hit() {// Incremente le nombre de fois que la brique a ete touchee
        hits++;
        if (hits >= maxHits) {
            destroyed = true;
        }
    }
    
    public boolean contains(int px, int py) {// Verifie si la brique contient un point
        return !destroyed && px >= x && px <= x + width && py >= y && py <= y + height;
    }
    
    public void draw(Graphics2D g) {// Dessine la brique sur l'ecran
        if (!destroyed) {
            // Si la brique a ete touchee, on la dessine plus foncee pour montrer les degats
            Color drawColor = (hits > 0) ? color.darker() : color;
            g.setColor(drawColor);
            g.fillRect(x, y, width, height);
            
            // Bordure noire autour de la brique pour la rendre plus visible
            g.setColor(Color.BLACK);
            g.drawRect(x, y, width, height);
        }
    }
    
    // Getters
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public Color getColor() { return color; }
    public boolean isDestroyed() { return destroyed; }
    public int getPoints() { return points; }
    public int getHits() { return hits; }
    public int getMaxHits() { return maxHits; }
}

