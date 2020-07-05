package org.astropeci.omw.structures;

import lombok.Getter;

public class NoSuchStructureException extends Exception {

    @Getter
    public String name;

    public NoSuchStructureException(String name) {
        super();
        this.name = name;
    }

    public NoSuchStructureException(String name, String message) {
        super(message);
        this.name = name;
    }

    public NoSuchStructureException(String name, String message, Throwable cause) {
        super(message, cause);
        this.name = name;
    }

    public NoSuchStructureException(String name, Throwable cause) {
        super(cause);
        this.name = name;
    }
}
