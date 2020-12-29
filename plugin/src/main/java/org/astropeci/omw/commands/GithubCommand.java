package org.astropeci.omw.commands;

import io.github.llewvallis.commandbuilder.*;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.PluginCommand;

public class GithubCommand {

    public void register(PluginCommand command) {
        new CommandBuilder().build(new ReflectionCommandCallback(this), command);
    }

    @ExecuteCommand
    @PlayerOnlyCommand
    public void execute(CommandContext ctx) {
        TextComponent message = new TextComponent("github.com/LlewVallis/OpenMissileWars");
        message.setColor(ChatColor.AQUA);
        message.setUnderlined(true);
        message.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/LlewVallis/OpenMissileWars"));

        ctx.getSender().sendMessage(message);
    }
}
