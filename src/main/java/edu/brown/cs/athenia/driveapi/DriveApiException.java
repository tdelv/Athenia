package edu.brown.cs.athenia.driveapi;

/**
 * Exceptions for use with DriveApi.
 * @author Thomas Del Vecchio
 */
public class DriveApiException extends Exception {
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new DriveApiException.
     */
    public DriveApiException() {
        super();
    }

    /**
     * Creates a new DriveApiException.
     * @param message
     *          The message to be passed with the exception.
     */
    public DriveApiException(String message) {
        super(message);
    }

    /**
     * Creates a new DriveApiException.
     * @param message
     *          The message to be passed with the exception.
     * @param cause
     *          The cause of the exception.
     */
    public DriveApiException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new DriveApiException.
     * @param cause
     *          The cause of the exception.
     */
    public DriveApiException(Throwable cause) {
        super(cause);
    }
}