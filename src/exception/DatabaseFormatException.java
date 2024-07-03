package exception;

public class DatabaseFormatException extends Exception {
    public DatabaseFormatException() {
    }

    public DatabaseFormatException(String message) {
        super(message);
    }
}
