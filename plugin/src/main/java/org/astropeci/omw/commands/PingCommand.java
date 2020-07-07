package org.astropeci.omw.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.astropeci.omw.commandbuilder.*;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class PingCommand {

    private final TabExecutor executor;

    public PingCommand() {
        executor = new CommandBuilder().build(new ReflectionCommandCallback(this));
    }

    public void register(Plugin plugin) {
        CommandBuilder.registerCommand(
                plugin,
                "ping",
                "display your current ping",
                "ping",
                "omw.ping",
                executor
        );
    }

    @PlayerOnlyCommand
    @ExecuteCommand
    public boolean execute(CommandContext ctx) {
        int ping = ((Player) ctx.sender).spigot().getPing();

        TextComponent message = new TextComponent("Your ping is " + ping);
        message.setColor(ChatColor.GREEN);

        ctx.sender.sendMessage(message);

        return true;
    }
}
