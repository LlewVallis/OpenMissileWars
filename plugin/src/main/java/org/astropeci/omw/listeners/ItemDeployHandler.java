package org.astropeci.omw.listeners;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.astropeci.omw.structures.NoSuchStructureException;
import org.astropeci.omw.structures.Structure;
import org.astropeci.omw.structures.StructureManager;
import org.astropeci.omw.teams.GameTeam;
import org.astropeci.omw.teams.GlobalTeamManager;
import org.astropeci.omw.worlds.ArenaPool;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Optional;

@RequiredArgsConstructor
public class ItemDeployHandler implements Listener {

    private final ArenaPool arenaPool;
    private final StructureManager structureManager;
    private final GlobalTeamManager globalTeamManager;

    @EventHandler
    @SneakyThrows({ NoSuchStructureException.class })
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();

        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (arenaPool.getPlayerArena(player).isEmpty()) {
            return;
        }

        Optional<GameTeam> teamOptional = globalTeamManager.getPlayerTeam(player);
        if (teamOptional.isEmpty()) {
            return;
        }

        GameTeam team = teamOptional.get();

        Structure.Rotation rotation = team == GameTeam.GREEN ?
                Structure.Rotation.ROTATE_180 :
                Structure.Rotation.ROTATE_0;

        String structureName;
        switch (e.getMaterial()) {
            case CREEPER_SPAWN_EGG:
                structureName = "tomahawk";
                break;
            case GUARDIAN_SPAWN_EGG:
                structureName = "guardian";
                break;
            case GHAST_SPAWN_EGG:
                structureName = "juggernaut";
                break;
            case WITCH_SPAWN_EGG:
                structureName = "shieldbuster";
                break;
            case OCELOT_SPAWN_EGG:
                structureName = "lightning";
                break;
            default:
                return;
        }

        Structure structure = new Structure(structureName, structureManager);

        Location target = e.getClickedBlock().getLocation();
        structure.load(target, team, rotation);
    }
}
