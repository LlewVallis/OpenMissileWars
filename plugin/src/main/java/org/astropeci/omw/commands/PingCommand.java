package org.astropeci.omw.commands;

import io.github.llewvallis.commandbuilder.*;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

public class PingCommand {

    public void register(PluginCommand command) {
        new CommandBuilder().build(new ReflectionCommandCallback(this), command);
    }

    @ExecuteCommand
    @PlayerOnlyCommand
    public void execute(CommandContext ctx) {
        int ping = ((Player) ctx.getSender()).spigot().getPing();

        TextComponent message = new TextComponent("Your ping is " + ping);
        message.setColor(ChatColor.GREEN);

        ctx.getSender().sendMessage(message);
    }
}
