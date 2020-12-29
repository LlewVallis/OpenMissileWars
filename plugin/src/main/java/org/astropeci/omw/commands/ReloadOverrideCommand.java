package org.astropeci.omw.commands;

import io.github.llewvallis.commandbuilder.CommandBuilder;
import io.github.llewvallis.commandbuilder.CommandContext;
import io.github.llewvallis.commandbuilder.ExecuteCommand;
import io.github.llewvallis.commandbuilder.ReflectionCommandCallback;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Entity;

public class ReloadOverrideCommand {

    public void register(PluginCommand command) {
        new CommandBuilder().build(new ReflectionCommandCallback(this), command);
    }

    @ExecuteCommand
    public void execute(CommandContext ctx) {
        if (ctx.getSender() instanceof Entity) {
            TextComponent message = new TextComponent("Use /bukkit:reload to reload the server\nDid you mean /reset?");
            message.setColor(ChatColor.RED);
            ctx.getSender().sendMessage(message);
        } else {
            Bukkit.getServer().dispatchCommand(ctx.getSender(), "bukkit:reload");
        }
    }
}
