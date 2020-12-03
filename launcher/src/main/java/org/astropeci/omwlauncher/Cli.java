package org.astropeci.omwlauncher;

import lombok.experimental.UtilityClass;
import picocli.CommandLine;

import java.util.Scanner;

@UtilityClass
public class Cli {

    private static final CommandLine.Help.Ansi ANSI = CommandLine.Help.Ansi.AUTO;

    public void print(String value) {
        System.out.println(ANSI.string(value));
    }

    public void printWithoutNewline(String value) {
        System.out.print(ANSI.string(value));
    }

    public void printError(String value) {
        System.err.println(ANSI.string("@|red,bold " + value + "|@"));
    }

    public String prompt(String value) {
        System.out.print(ANSI.string(value));
        return new Scanner(System.in).nextLine();
    }
}
