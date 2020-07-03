package org.astropeci.omw.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.astropeci.omw.commandbuilder.*;
import org.astropeci.omw.listeners.NightVisionHandler;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class NightVisionCommand {

    private final TabExecutor executor;
    private final NightVisionHandler handler;

    public NightVisionCommand(NightVisionHandler handler) {
        executor = new CommandBuilder().build(new ReflectionCommandCallback(this));

        this.handler = handler;
    }

    public void register(Plugin plugin) {
        CommandBuilder.registerCommand(
                plugin,
                "nightvis",
                "Toggle the night vision effect",
                "nightvis",
                "omw.nightvis",
                executor
        );
    }

    @PlayerOnlyCommand
    @ExecuteCommand
    public boolean execute(CommandContext ctx) {
        boolean nightVision = handler.toggleNightVision((Player) ctx.sender);

        TextComponent statusComponent;
        if (nightVision) {
            statusComponent = new TextComponent("on");
            statusComponent.setColor(ChatColor.AQUA);
        } else {
            statusComponent = new TextComponent("off");
            statusComponent.setColor(ChatColor.RED);
        }

        TextComponent leadupComponent = new TextComponent("Night vision toggled ");
        leadupComponent.setColor(ChatColor.GREEN);

        TextComponent message = new TextComponent(leadupComponent, statusComponent);
        ctx.sender.sendMessage(message);

        return true;
    }
}
