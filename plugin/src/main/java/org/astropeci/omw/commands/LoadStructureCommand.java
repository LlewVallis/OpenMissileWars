package org.astropeci.omw.commands;

import io.github.llewvallis.commandbuilder.CommandBuilder;
import io.github.llewvallis.commandbuilder.CommandContext;
import io.github.llewvallis.commandbuilder.ExecuteCommand;
import io.github.llewvallis.commandbuilder.ReflectionCommandCallback;
import io.github.llewvallis.commandbuilder.arguments.BlockCoordArgument;
import io.github.llewvallis.commandbuilder.arguments.StringSetArgument;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.astropeci.omw.structures.Structure;
import org.astropeci.omw.structures.StructureManager;
import org.astropeci.omw.teams.GameTeam;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.ProxiedCommandSender;
import org.bukkit.entity.Entity;

import java.util.Optional;

@RequiredArgsConstructor
public class LoadStructureCommand {

    private final StructureManager structureManager;

    public void register(PluginCommand command) {
        new CommandBuilder()
                .argument(new StructureArgument(structureManager))
                .argument(new BlockCoordArgument(BlockCoordArgument.Axis.X))
                .argument(new BlockCoordArgument(BlockCoordArgument.Axis.Y))
                .argument(new BlockCoordArgument(BlockCoordArgument.Axis.Z))
                .argument(new StringSetArgument("south", "west", "north", "east").optional())
                .argument(new TeamArgument().optional())
                .build(new ReflectionCommandCallback(this), command);
    }

    @ExecuteCommand
    public void execute(CommandContext ctx, Structure structure, int x, int y, int z, String direction, GameTeam team) {
        Optional<World> worldOptional = getSenderWorld(ctx.getSender());

        if (worldOptional.isEmpty()) {
            TextComponent message = new TextComponent("Command sender must be in a world");
            message.setColor(ChatColor.RED);
            ctx.getSender().spigot().sendMessage(message);
            return;
        }

        if (direction == null) {
            direction = inferDirection(ctx.getSender());
        }

        if (team == null) {
            team = GameTeam.GREEN;
        }

        Structure.Rotation rotation = null;
        switch (direction) {
            case "south":
                rotation = Structure.Rotation.ROTATE_0;
                break;
            case "west":
                rotation = Structure.Rotation.ROTATE_90;
                break;
            case "north":
                rotation = Structure.Rotation.ROTATE_180;
                break;
            case "east":
                rotation = Structure.Rotation.ROTATE_270;
                break;
        }

        Location location = new Location(worldOptional.get(), x, y, z);
        boolean success = structure.load(location, team, rotation);

        TextComponent message;
        if (success) {
            message = new TextComponent("Loaded " + structure.getName());
            message.setColor(ChatColor.GREEN);
        } else {
            message = new TextComponent("Could not load " + structure.getName());
            message.setColor(ChatColor.RED);
        }

        ctx.getSender().spigot().sendMessage(message);
    }

    private Optional<World> getSenderWorld(CommandSender sender) {
        if (sender instanceof Entity) {
            return Optional.of(((Entity) sender).getWorld());
        }

        if (sender instanceof BlockCommandSender) {
            return Optional.of(((BlockCommandSender) sender).getBlock().getWorld());
        }

        if (sender instanceof ProxiedCommandSender) {
            return getSenderWorld(((ProxiedCommandSender) sender).getCallee());
        }

        return Optional.empty();
    }

    private String inferDirection(CommandSender sender) {
        if (sender instanceof Entity) {
            float yaw = ((Entity) sender).getLocation().getYaw();

            while (yaw < 0) {
                yaw += 360;
            }

            if (yaw < 45) {
                return "south";
            } else if (yaw < 135) {
                return "west";
            } else if (yaw < 225) {
                return "north";
            } else if (yaw < 315) {
                return "east";
            } else {
                return "south";
            }
        }

        return "south";
    }
}
