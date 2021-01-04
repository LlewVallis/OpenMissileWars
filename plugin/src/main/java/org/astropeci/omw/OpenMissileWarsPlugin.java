package org.astropeci.omw;

import lombok.SneakyThrows;
import org.astropeci.omw.commands.*;
import org.astropeci.omw.item.EquipmentProvider;
import org.astropeci.omw.listeners.*;
import org.astropeci.omw.settings.GlobalSettings;
import org.astropeci.omw.structures.StructureManager;
import org.astropeci.omw.teams.GameTeam;
import org.astropeci.omw.teams.GlobalTeamManager;
import org.astropeci.omw.worlds.*;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

public class OpenMissileWarsPlugin extends JavaPlugin {

    private ArenaPool arenaPool;
    private StructureManager structureManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        Bukkit.getPluginManager().registerEvents(new Listener() {

            @EventHandler
            private void onServerLoad(ServerLoadEvent e) {
                OpenMissileWarsPlugin.this.onServerLoad();
            }
        }, this);
    }

    @SneakyThrows({ ArenaAlreadyExistsException.class })
    private void onServerLoad() {
        GlobalSettings settings = new GlobalSettings(getConfig());

        GlobalTeamManager globalTeamManager = new GlobalTeamManager();
        globalTeamManager.configScoreboard();

        WorldManager worldManager = new WorldManager(globalTeamManager);
        worldManager.configureWorld(worldManager.getDefaultWorld());
        worldManager.cleanArenas();

        Hub hub = new Hub(worldManager);

        Template template = new Template(globalTeamManager, worldManager, hub, this);
        template.createWorldIfMissing();

        arenaPool = new ArenaPool(settings, template);

        Bukkit.getLogger().info("Creating starting arenas specified in the config: " + String.join(", ", settings.getStartingArenas()));
        for (String startingArenaName : settings.getStartingArenas()) {
            arenaPool.create(startingArenaName);
        }

        structureManager = new StructureManager(this, worldManager);

        EquipmentProvider equipmentProvider = new EquipmentProvider();

        NightVisionHandler nightVisionHandler = new NightVisionHandler();
        PistonBreakHandler pistonBreakHandler = new PistonBreakHandler(this);

        new HubCommand(hub).register(getCommand("hub"));
        new TemplateCommand(template).register(getCommand("template"));
        new ArenaCommand(arenaPool).register(getCommand("arena"));
        new ListArenasCommand(arenaPool).register(getCommand("arenas"));
        new CreateArenaCommand(arenaPool).register(getCommand("arena-create"));
        new DeleteArenaCommand(arenaPool).register(getCommand("arena-delete"));
        new LoadStructureCommand(structureManager).register(getCommand("structure-load"));
        new NightVisionCommand(nightVisionHandler).register(getCommand("nightvis"));
        new SpectateCommand(arenaPool).register(getCommand("sp"));
        new PingCommand().register(getCommand("ping"));
        new GithubCommand().register(getCommand("github"));
        new IssueCommand().register(getCommand("issue"));
        new ResetArenaCommand(this, arenaPool).register(getCommand("reset"));
        new DispenseItemCommand(arenaPool).register(getCommand("item-dispense"));

        new StopOverrideCommand().register(getCommand("stop-warning"));
        new RestartOverrideCommand().register(getCommand("restart-warning"));
        new ReloadOverrideCommand().register(getCommand("reload-warning"));

        new JoinTeamCommand(GameTeam.GREEN, globalTeamManager, arenaPool, worldManager, equipmentProvider)
                .register(getCommand("green"));
        new JoinTeamCommand(GameTeam.RED, globalTeamManager, arenaPool, worldManager, equipmentProvider)
                .register(getCommand("red"));

        registerEventHandler(new SpawnHandler(hub, arenaPool, globalTeamManager));
        registerEventHandler(new WelcomeHandler());
        registerEventHandler(new ChatTransformer(globalTeamManager, arenaPool));
        registerEventHandler(new HungerDisabler());
        registerEventHandler(new ItemDeployHandler(settings, arenaPool, structureManager, globalTeamManager, this));
        registerEventHandler(new ItemDropPreventer());
        registerEventHandler(new FireballHandler());
        registerEventHandler(new ShieldHandler(structureManager, globalTeamManager, this));
        registerEventHandler(new PortalBreakListener(arenaPool, this));
        registerEventHandler(new ExplosionModifier());
        registerEventHandler(nightVisionHandler);
        registerEventHandler(pistonBreakHandler);

        pistonBreakHandler.register();
        registerEventHandler(new ObsidianBreakPreventer());
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        if ("void".equals(id)) {
            return new VoidChunkGenerator();
        } else {
            return null;
        }
    }

    private void registerEventHandler(Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
    }

    @Override
    public void onDisable() {
        if (arenaPool != null) arenaPool.close();
        if (structureManager != null) structureManager.close();
    }
}
