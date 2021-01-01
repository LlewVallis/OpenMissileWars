package org.astropeci.omwlauncher;

import lombok.experimental.UtilityClass;
import picocli.CommandLine;

import java.util.Scanner;

/**
 * Utility class for sending fancy command line messages.
 */
@UtilityClass
public class Cli {

    // Used for formatting, stolen from the Picocli library.
    private static final CommandLine.Help.Ansi ANSI = CommandLine.Help.Ansi.AUTO;

    /**
     * Prints a string with Picocli formatting and then terminates the line.
     */
    public void print(String value) {
        System.out.println(ANSI.string(value));
    }

    /**
     * Similar to {@link #print(String)}, except a newline is not printed.
     */
    public void printWithoutNewline(String value) {
        System.out.print(ANSI.string(value));
    }

    /**
     * Similar to {@link #print(String)}, except the message is wrapped in red, bold formatting.
     */
    public void printError(String value) {
        System.err.println(ANSI.string("@|red,bold " + value + "|@"));
    }

    /**
     * Prints a message as if by {@link #printWithoutNewline(String)} and then reads a line of user input.
     * @return The line of input the user typed, excluding the line separator.
     */
    public String prompt(String value) {
        System.out.print(ANSI.string(value));
        return new Scanner(System.in).nextLine();
    }
}
