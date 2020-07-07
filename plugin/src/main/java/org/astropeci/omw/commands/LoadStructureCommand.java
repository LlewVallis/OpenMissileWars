package org.astropeci.omw.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.astropeci.omw.commandbuilder.CommandBuilder;
import org.astropeci.omw.commandbuilder.CommandContext;
import org.astropeci.omw.commandbuilder.ExecuteCommand;
import org.astropeci.omw.commandbuilder.ReflectionCommandCallback;
import org.astropeci.omw.commandbuilder.arguments.CoordArgument;
import org.astropeci.omw.commandbuilder.arguments.StringSetArgument;
import org.astropeci.omw.structures.Structure;
import org.astropeci.omw.structures.StructureManager;
import org.astropeci.omw.teams.GameTeam;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ProxiedCommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.Optional;

public class LoadStructureCommand {

    private final TabExecutor executor;

    public LoadStructureCommand(StructureManager structureManager) {
        executor = new CommandBuilder()
                .addArgument(new StructureArgument(structureManager))
                .addArgument(new CoordArgument(CoordArgument.Axis.X))
                .addArgument(new CoordArgument(CoordArgument.Axis.Y))
                .addArgument(new CoordArgument(CoordArgument.Axis.Z))
                .addArgument(new StringSetArgument("south", "west", "north", "east").optional())
                .addArgument(new TeamArgument().optional())
                .build(new ReflectionCommandCallback(this));
    }

    public void register(Plugin plugin) {
        CommandBuilder.registerCommand(
                plugin,
                "structure-load",
                "Loads an OpenMissileWars structure",
                "structure-load <name> <x> <y> <z> [direction] [team]",
                "omw.structure.load",
                executor
        );
    }

    @ExecuteCommand
    public boolean execute(CommandContext ctx, Structure structure, int x, int y, int z, String direction, GameTeam team) {
        Optional<World> worldOptional = getSenderWorld(ctx.sender);

        if (worldOptional.isEmpty()) {
            TextComponent message = new TextComponent("Command sender must be in a world");
            message.setColor(ChatColor.RED);
            ctx.sender.spigot().sendMessage(message);

            return true;
        }

        if (direction == null) {
            direction = inferDirection(ctx.sender);
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

        ctx.sender.spigot().sendMessage(message);

        return true;
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
