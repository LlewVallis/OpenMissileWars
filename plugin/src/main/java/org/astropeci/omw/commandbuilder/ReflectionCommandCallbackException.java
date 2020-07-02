package org.astropeci.omw.commandbuilder;

public class ReflectionCommandCallbackException extends RuntimeException {

    public ReflectionCommandCallbackException(String message) {
        super(message);
    }

    public ReflectionCommandCallbackException(String message, Throwable cause) {
        super(message, cause);
    }
}
