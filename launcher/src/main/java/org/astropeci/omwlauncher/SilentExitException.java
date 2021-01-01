package org.astropeci.omwlauncher;

/**
 * An exception which is not treated as an unexpected error if unhandled.
 *
 * If this is uncaught, no error message is printed and the exit code will be 0.
 */
public class SilentExitException extends RuntimeException { }
