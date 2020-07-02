package org.astropeci.omw.worlds;

public class NoSuchArenaException extends Exception {

    public NoSuchArenaException() {
        super();
    }

    public NoSuchArenaException(String message) {
        super(message);
    }

    public NoSuchArenaException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoSuchArenaException(Throwable cause) {
        super(cause);
    }
}
