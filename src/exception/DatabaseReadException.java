package exception;

public class DatabaseReadException extends Exception {
    public DatabaseReadException() {}
    public DatabaseReadException(String message) { super(message); }
}
