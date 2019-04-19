package edu.brown.cs.athenia.driveapi;

/**
 * Exceptions for use with DriveApi when user does not have loaded credentials.
 * @author Thomas Del Vecchio
 */
public class UnauthenticatedUserException extends DriveApiException {
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new UnauthenticatedUserException.
     */
    public UnauthenticatedUserException() {
        super();
    }

    /**
     * Creates a new UnauthenticatedUserException.
     * @param message
     *          The message to be passed with the exception.
     */
    public UnauthenticatedUserException(String message) {
        super(message);
    }

    /**
     * Creates a new UnauthenticatedUserException.
     * @param message
     *          The message to be passed with the exception.
     * @param cause
     *          The cause of the exception.
     */
    public UnauthenticatedUserException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new UnauthenticatedUserException.
     * @param cause
     *          The cause of the exception.
     */
    public UnauthenticatedUserException(Throwable cause) {
        super(cause);
    }
}