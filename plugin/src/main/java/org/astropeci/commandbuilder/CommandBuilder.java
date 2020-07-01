package org.astropeci.commandbuilder;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

public class CommandBuilder {

    private final List<ArgumentParser<?>> arguments = new ArrayList<>();
    private ArgumentParser<?> variadicArgument = null;

    private boolean constructed = false;

    public <T> CommandBuilder addArgument(ArgumentParser<T> argument) {
        assertNotConstructed();
        assertCanAddArgument();

        arguments.add(argument);

        return this;
    }

    public <T> CommandBuilder addVariadicArgument(ArgumentParser<T> argument) {
        assertNotConstructed();
        assertCanAddArgument();

        variadicArgument = argument;

        return this;
    }

    public TabExecutor build(CommandCallback callback) {
        constructed = true;
        return new BuiltExecutor(arguments, variadicArgument, callback);
    }

    private void assertNotConstructed() {
        if (constructed) {
            throw new IllegalArgumentException("a command builder cannot be used after it has built");
        }
    }

    private void assertCanAddArgument() {
        if (variadicArgument != null) {
            throw new IllegalArgumentException("no arguments can be added after a variadic argument");
        }
    }

    public static void registerCommand(
        Plugin plugin,
        String name,
        String description,
        String usage,
        String permission,
        TabExecutor executor
    ) {
        Server server = Bukkit.getServer();

        CommandMap commandMap;
        try {
            Field commandMapField = server.getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            commandMap = (CommandMap) commandMapField.get(server);
        } catch (NoSuchFieldException | ClassCastException | IllegalAccessException e) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to get command map for dynamic command registration", e);
            return;
        }

        PluginCommand command;
        try {
            Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            constructor.setAccessible(true);
            command = constructor.newInstance(name, plugin);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to instantiate command for dynamic command registration", e);
            return;
        }

        command.setDescription(description);
        command.setUsage(usage);
        command.setPermission(permission);

        command.setExecutor(executor);
        command.setTabCompleter(executor);

        String namePrefix = plugin.getDescription().getName().toLowerCase(Locale.ENGLISH);
        commandMap.register(name, namePrefix, command);
    }
}
