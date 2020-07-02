package org.astropeci.omw.worlds;

public class ArenaAlreadyExistsException extends Exception {

    public ArenaAlreadyExistsException() {
        super();
    }

    public ArenaAlreadyExistsException(String message) {
        super(message);
    }

    public ArenaAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    public ArenaAlreadyExistsException(Throwable cause) {
        super(cause);
    }
}
