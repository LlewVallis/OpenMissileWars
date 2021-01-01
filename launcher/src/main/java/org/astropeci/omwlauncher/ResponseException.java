package org.astropeci.omwlauncher;

/**
 * And exception which will have it's message (but not stacktrace) printed to the console if unhandled.
 *
 * If this is uncaught, the exit code of the launcher will additionally be non-zero.
 */
public class ResponseException extends RuntimeException {

    public ResponseException(String message) {
        super(message);
    }
}
