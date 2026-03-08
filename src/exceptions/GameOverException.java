package exceptions;

// GameOverException: exception lancee quand le joueur perd 
public class GameOverException extends Exception {
    private int finalScore;
    
    public GameOverException(String message, int finalScore) {
        super(message); //prend le message de l'exception
        this.finalScore = finalScore; //enregistre le score final
    }
    
    public GameOverException(String message) {
        super(message);
        this.finalScore = 0;
    }
    
    public int getFinalScore() {
        return finalScore;
    } //recupere le score final
}











