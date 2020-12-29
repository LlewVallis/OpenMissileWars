package org.astropeci.omw.commands;

import io.github.llewvallis.commandbuilder.*;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.astropeci.omw.worlds.Template;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class TemplateCommand {

    private final Template template;

    public void register(PluginCommand command) {
        new CommandBuilder().build(new ReflectionCommandCallback(this), command);
    }

    @ExecuteCommand
    @PlayerOnlyCommand
    public void execute(CommandContext ctx) {
        template.sendPlayer((Player) ctx.getSender());

        TextComponent message = new TextComponent("Sending you to the template");
        message.setColor(ChatColor.GREEN);
        ctx.getSender().spigot().sendMessage(message);
    }
}
