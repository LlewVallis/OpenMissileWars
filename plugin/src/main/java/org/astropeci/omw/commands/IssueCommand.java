package org.astropeci.omw.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.astropeci.omw.commandbuilder.*;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.Plugin;

public class IssueCommand {

    private final TabExecutor executor;

    public IssueCommand() {
        executor = new CommandBuilder().build(new ReflectionCommandCallback(this));
    }

    public void register(Plugin plugin) {
        CommandBuilder.registerCommand(
                plugin,
                "issue",
                "Display a link to the OpenMissileWars issue page",
                "issue",
                "omw.issue",
                executor
        );
    }

    @PlayerOnlyCommand
    @ExecuteCommand
    public boolean execute(CommandContext ctx) {
        TextComponent message = new TextComponent("github.com/LlewVallis/OpenMissileWars/issues/new");
        message.setColor(ChatColor.AQUA);
        message.setUnderlined(true);
        message.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/LlewVallis/OpenMissileWars/issues/new"));

        ctx.sender.sendMessage(message);

        return true;
    }
}
