package exceptions;

// Exception lancee si on essaie d'utiliser un etat de jeu qui n'existe pas
public class InvalidGameStateException extends Exception {
    public InvalidGameStateException(String message) {
        super(message); //prend le message de l'exception
    }
}











