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
import org.bukkit.plugin.java.JavaPlugin;

public class OpenMissileWarsPlugin extends JavaPlugin {

    private ArenaPool arenaPool;
    private StructureManager structureManager;

    @Override
    @SneakyThrows({ ArenaAlreadyExistsException.class })
    public void onEnable() {
        saveDefaultConfig();

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

        structureManager = new StructureManager(this, worldManager);

        EquipmentProvider equipmentProvider = new EquipmentProvider();

        NightVisionHandler nightVisionHandler = new NightVisionHandler();
        PistonBreakHandler pistonBreakHandler = new PistonBreakHandler(this);

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
        new ResetArenaCommand(this, arenaPool).register(this);

        new StopOverrideCommand().register(this);
        new RestartOverrideCommand().register(this);
        new ReloadOverrideCommand().register(this);

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
        registerEventHandler(pistonBreakHandler);

        pistonBreakHandler.register();
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
