package org.astropeci.omw.commands;

import io.github.llewvallis.commandbuilder.*;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.astropeci.omw.listeners.NightVisionHandler;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class NightVisionCommand {

    private final NightVisionHandler handler;

    public void register(PluginCommand command) {
        new CommandBuilder().build(new ReflectionCommandCallback(this), command);
    }

    @ExecuteCommand
    @PlayerOnlyCommand
    public void execute(CommandContext ctx) {
        boolean nightVision = handler.toggleNightVision((Player) ctx.getSender());

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
        ctx.getSender().sendMessage(message);
    }
}
