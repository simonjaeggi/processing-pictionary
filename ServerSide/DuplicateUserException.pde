/**
 * Custom exception for handling scenarios where an attempt is made to create or register a user that already exists.
 * This class extends {@link Exception} and provides a constructor that allows for specifying an error message.
 */
public class DuplicateUserException extends Exception {

    /**
     * Constructs a new {@code DuplicateUserException} with the specified detail message.
     *
     * @param message the detail message.
     */
    public DuplicateUserException(String message) {
        super(message);
    }
}
