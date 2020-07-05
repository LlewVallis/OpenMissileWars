package org.astropeci.omw.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.astropeci.omw.commandbuilder.*;
import org.astropeci.omw.worlds.Hub;
import org.astropeci.omw.worlds.Template;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class TemplateCommand {

    private final Template template;

    private final TabExecutor executor;

    public TemplateCommand(Template template) {
        executor = new CommandBuilder().build(new ReflectionCommandCallback(this));
        this.template = template;
    }

    public void register(Plugin plugin) {
        CommandBuilder.registerCommand(
                plugin,
                "template",
                "Leave any active game and teleport to the spawn of the template",
                "template",
                "omw.template.join",
                executor
        );
    }

    @PlayerOnlyCommand
    @ExecuteCommand
    public boolean execute(CommandContext ctx) {
        template.sendPlayer((Player) ctx.sender);

        TextComponent message = new TextComponent("Sending you to the template");
        message.setColor(ChatColor.GREEN);
        ctx.sender.spigot().sendMessage(message);

        return true;
    }
}
