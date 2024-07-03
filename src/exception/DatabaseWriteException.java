package exception;

public class DatabaseWriteException extends Exception {
    public DatabaseWriteException() {
    }

    public DatabaseWriteException(String message) {
        super(message);
    }
}
