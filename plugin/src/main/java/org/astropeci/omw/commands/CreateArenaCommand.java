package org.astropeci.omw.commands;

import io.github.llewvallis.commandbuilder.CommandBuilder;
import io.github.llewvallis.commandbuilder.CommandContext;
import io.github.llewvallis.commandbuilder.ExecuteCommand;
import io.github.llewvallis.commandbuilder.ReflectionCommandCallback;
import io.github.llewvallis.commandbuilder.arguments.StringArgument;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.astropeci.omw.worlds.ArenaAlreadyExistsException;
import org.astropeci.omw.worlds.ArenaPool;
import org.bukkit.command.PluginCommand;

@RequiredArgsConstructor
public class CreateArenaCommand {

    private final ArenaPool arenaPool;

    public void register(PluginCommand command) {
        new CommandBuilder()
                .argument(new StringArgument())
                .build(new ReflectionCommandCallback(this), command);
    }

    @ExecuteCommand
    public void execute(CommandContext ctx, String name) {
        try {
            arenaPool.create(name);

            TextComponent clickToJoinComponent = new TextComponent("here");
            clickToJoinComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/arena " + name));
            clickToJoinComponent.setBold(true);

            TextComponent message = new TextComponent(
                    new TextComponent("Created " + name + ", click "),
                    clickToJoinComponent,
                    new TextComponent(" to join")
            );
            message.setColor(ChatColor.GREEN);

            ctx.getSender().spigot().sendMessage(message);
        } catch (ArenaAlreadyExistsException e) {
            TextComponent message = new TextComponent("An arena with that name already exists");
            message.setColor(ChatColor.RED);
            ctx.getSender().spigot().sendMessage(message);
        }
    }
}
