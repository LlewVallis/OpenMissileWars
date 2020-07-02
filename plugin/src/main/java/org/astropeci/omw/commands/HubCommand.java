package org.astropeci.omw.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.astropeci.omw.commandbuilder.*;
import org.astropeci.omw.worlds.Hub;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class HubCommand {

    private final TabExecutor executor;

    public HubCommand() {
        executor = new CommandBuilder().build(new ReflectionCommandCallback(this));
    }

    public void register(Plugin plugin) {
        CommandBuilder.registerCommand(
                plugin,
                "hub",
                "Leave any active game and teleport to the spawn of the hub",
                "hub",
                "omw.hub.join",
                executor
        );
    }

    @PlayerOnlyCommand
    @ExecuteCommand
    public boolean execute(CommandContext ctx) {
        Hub.sendPlayer((Player) ctx.sender);

        TextComponent message = new TextComponent("Sending you to the hub");
        message.setColor(ChatColor.GREEN);
        ctx.sender.spigot().sendMessage(message);

        return true;
    }
}
