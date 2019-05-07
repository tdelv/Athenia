package edu.brown.cs.athenia.driveapi;

/**
 * Exceptions for use with DriveApi.
 * @author Thomas Del Vecchio
 */
public class GoogleDriveApiException extends Exception {
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new GoogleDriveApiException.
     */
    public GoogleDriveApiException() {
        super();
    }

    /**
     * Creates a new GoogleDriveApiException.
     * @param message
     *          The message to be passed with the exception.
     */
    public GoogleDriveApiException(String message) {
        super(message);
    }

    /**
     * Creates a new GoogleDriveApiException.
     * @param message
     *          The message to be passed with the exception.
     * @param cause
     *          The cause of the exception.
     */
    public GoogleDriveApiException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new GoogleDriveApiException.
     * @param cause
     *          The cause of the exception.
     */
    public GoogleDriveApiException(Throwable cause) {
        super(cause);
    }
}