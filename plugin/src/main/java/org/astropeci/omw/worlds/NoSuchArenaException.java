package org.astropeci.omw.worlds;

public class NoSuchArenaException extends Exception {

    private final String name;

    public NoSuchArenaException(String name) {
        super();
        this.name = name;
    }

    public NoSuchArenaException(String name, String message) {
        super(message);
        this.name = name;
    }

    public NoSuchArenaException(String name, String message, Throwable cause) {
        super(message, cause);
        this.name = name;
    }

    public NoSuchArenaException(String name, Throwable cause) {
        super(cause);
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
