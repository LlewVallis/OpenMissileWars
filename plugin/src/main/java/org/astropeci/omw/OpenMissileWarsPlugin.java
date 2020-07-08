package org.astropeci.omw;

import lombok.SneakyThrows;
import org.astropeci.omw.commands.*;
import org.astropeci.omw.item.EquipmentProvider;
import org.astropeci.omw.listeners.*;
import org.astropeci.omw.structures.StructureManager;
import org.astropeci.omw.teams.GameTeam;
import org.astropeci.omw.teams.GlobalTeamManager;
import org.astropeci.omw.worlds.*;
import org.bukkit.event.Listener;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.permission.Permission;
import org.bukkit.plugin.java.annotation.permission.Permissions;
import org.bukkit.plugin.java.annotation.plugin.ApiVersion;
import org.bukkit.plugin.java.annotation.plugin.Description;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.plugin.java.annotation.plugin.Website;
import org.bukkit.plugin.java.annotation.plugin.author.Author;

@Plugin(name = "OpenMissileWars", version = "1.0.0")
@Description("An open source recreation of the Missile Wars minigame")
@ApiVersion(ApiVersion.Target.v1_15)
@Author("Llew Vallis <llewvallis@gmail.com>")
@Website("https://github.com/LlewVallis/OpenMissileWars")
@Permissions({
        @Permission(name = "omw.arena.join", defaultValue = PermissionDefault.TRUE),
        @Permission(name = "omw.github", defaultValue = PermissionDefault.TRUE),
        @Permission(name = "omw.hub.join", defaultValue = PermissionDefault.TRUE),
        @Permission(name = "omw.issue", defaultValue = PermissionDefault.TRUE),
        @Permission(name = "omw.team.join", defaultValue = PermissionDefault.TRUE),
        @Permission(name = "omw.arena.list", defaultValue = PermissionDefault.TRUE),
        @Permission(name = "omw.nightvis", defaultValue = PermissionDefault.TRUE),
        @Permission(name = "omw.ping", defaultValue = PermissionDefault.TRUE),
        @Permission(name = "omw.spectate", defaultValue = PermissionDefault.TRUE),
})
public class OpenMissileWarsPlugin extends JavaPlugin {

    private ArenaPool arenaPool;
    private StructureManager structureManager;

    @Override
    @SneakyThrows({ ArenaAlreadyExistsException.class })
    public void onEnable() {
        GlobalTeamManager globalTeamManager = new GlobalTeamManager();
        globalTeamManager.configScoreboard();

        WorldManager worldManager = new WorldManager(globalTeamManager);
        worldManager.configureWorld(worldManager.getDefaultWorld());
        worldManager.cleanArenas();

        Hub hub = new Hub(worldManager);

        Template template = new Template(globalTeamManager, worldManager, hub, this);
        template.createWorldIfMissing();

        arenaPool = new ArenaPool(template);
        arenaPool.create("mw1");

        structureManager = new StructureManager(worldManager);

        EquipmentProvider equipmentProvider = new EquipmentProvider();

        NightVisionHandler nightVisionHandler = new NightVisionHandler();

        new HubCommand(hub).register(this);
        new TemplateCommand(template).register(this);
        new ArenaCommand(arenaPool).register(this);
        new ListArenasCommand(arenaPool).register(this);
        new CreateArenaCommand(arenaPool).register(this);
        new DeleteArenaCommand(arenaPool).register(this);
        new LoadStructureCommand(structureManager).register(this);
        new NightVisionCommand(nightVisionHandler).register(this);
        new SpectateCommand(arenaPool).register(this);
        new PingCommand().register(this);
        new GithubCommand().register(this);
        new IssueCommand().register(this);

        new JoinTeamCommand(GameTeam.GREEN, globalTeamManager, arenaPool, worldManager, equipmentProvider).register(this);
        new JoinTeamCommand(GameTeam.RED, globalTeamManager, arenaPool, worldManager, equipmentProvider).register(this);

        registerEventHandler(new SpawnHandler(hub, arenaPool, globalTeamManager));
        registerEventHandler(new WelcomeHandler());
        registerEventHandler(new ChatTransformer(globalTeamManager, arenaPool));
        registerEventHandler(new HungerDisabler());
        registerEventHandler(new ItemDeployHandler(arenaPool, structureManager, globalTeamManager, this));
        registerEventHandler(new ItemDropPreventer());
        registerEventHandler(new FireballHandler());
        registerEventHandler(new ShieldHandler(structureManager, globalTeamManager, this));
        registerEventHandler(new PortalBreakListener(arenaPool, this));
        registerEventHandler(new ExplosionModifier());
        registerEventHandler(nightVisionHandler);
    }

    private void registerEventHandler(Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
    }

    @Override
    public void onDisable() {
        arenaPool.close();
        structureManager.close();
    }
}
