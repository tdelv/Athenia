package edu.brown.cs.athenia.databaseparser;

/**
 * Exceptions for use with DatabaseParser.
 * @author Thomas Del Vecchio
 */
public class DatabaseParserException extends Exception {
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new DatabaseParserException.
     */
    public DatabaseParserException() {
        super();
    }

    /**
     * Creates a new DatabaseParserException.
     * @param message
     *          The message to be passed with the exception.
     */
    public DatabaseParserException(String message) {
        super(message);
    }

    /**
     * Creates a new DatabaseParserException.
     * @param message
     *          The message to be passed with the exception.
     * @param cause
     *          The cause of the exception.
     */
    public DatabaseParserException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new DatabaseParserException.
     * @param cause
     *          The cause of the exception.
     */
    public DatabaseParserException(Throwable cause) {
        super(cause);
    }
}