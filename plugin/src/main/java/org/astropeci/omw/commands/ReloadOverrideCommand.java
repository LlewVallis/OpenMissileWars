package org.astropeci.omw.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.astropeci.omw.commandbuilder.CommandBuilder;
import org.astropeci.omw.commandbuilder.CommandContext;
import org.astropeci.omw.commandbuilder.ExecuteCommand;
import org.astropeci.omw.commandbuilder.ReflectionCommandCallback;
import org.bukkit.Bukkit;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

public class ReloadOverrideCommand {

    private final TabExecutor executor;

    public ReloadOverrideCommand() {
        executor = new CommandBuilder().build(new ReflectionCommandCallback(this));
    }

    public void register(Plugin plugin) {
        CommandBuilder.registerCommand(
                plugin,
                "reload-warning",
                "Warning override for the /reload command",
                "reload-warning",
                "omw.dangerous-command-alias",
                executor
        );
    }

    @ExecuteCommand
    public boolean execute(CommandContext ctx) {
        if (ctx.sender instanceof Entity) {
            TextComponent message = new TextComponent("Use /bukkit:reload to reload the server\nDid you mean /reset?");
            message.setColor(ChatColor.RED);
            ctx.sender.sendMessage(message);
            return true;
        } else {
            return Bukkit.getServer().dispatchCommand(ctx.sender, "bukkit:reload");
        }
    }
}
