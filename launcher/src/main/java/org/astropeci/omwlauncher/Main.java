package org.astropeci.omwlauncher;

import picocli.CommandLine;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "omw-launcher",
        description = "The OpenMissileWars launcher"
)
public class Main implements Callable<Integer> {

    @CommandLine.Option(names = {"--help"}, usageHelp = true, description = "Show the help menu for this command")
    private boolean help;

    @CommandLine.Option(names = {"--nogui"}, description = "Use the headless CLI mode - does not open the graphical launcher")
    private boolean headless;

    @CommandLine.Option(names = {"--version"}, description = "Use a specific version - if the version is already installed this allows the launcher to run offline")
    private String versionInput = "";

    @CommandLine.Option(names = {"--accept-eula"}, description = "Accept the Minecraft EULA for all versions run through the launcher")
    private boolean acceptEula;

    @CommandLine.Option(names = {"--memory"}, description = "Set the mebibytes of RAM allocated to the server when it is launched", defaultValue = "2048")
    private int memory;

    private void run() {
        versionInput = versionInput.trim();

        if (memory < 256) {
            throw new ResponseException("Cannot allocate less than 256M of RAM");
        } else if (memory > 1024 * 1024) {
            throw new ResponseException("Cannot allocate more than 1T of RAM");
        }

        if (GraphicsEnvironment.isHeadless() && !headless) {
            Cli.print("Running in a headless environment but --nogui was not passed, forcing headless mode");
            headless = true;
        }

        if (!headless) {
            Cli.print("Running in GUI mode, pass --nogui for the CLI version");

            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                Cli.printError("Could not set look and feel");
            }

            new GuiFlow(versionInput, acceptEula, memory).run();
        } else {
            new CliFlow(versionInput, acceptEula, memory).run();
        }
    }

    public static void main(String[] args) {
        int code = new CommandLine(new Main()).execute(args);
        System.exit(code);
    }

    @Override
    public Integer call() {
        try {
            run();
        } catch (ResponseException e) {
            Cli.printError(e.getMessage());
            return -1;
        } catch (SilentExitException e) {
            return 0;
        }

        return 0;
    }
}
