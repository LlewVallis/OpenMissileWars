package org.astropeci.omw;

import org.astropeci.omw.commands.*;
import org.astropeci.omw.listeners.NightVisionHandler;
import org.astropeci.omw.listeners.SpawnHandler;
import org.astropeci.omw.listeners.WelcomeHandler;
import org.astropeci.omw.worlds.ArenaPool;
import org.astropeci.omw.worlds.Worlds;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
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
public class OpenMissileWarsPlugin extends JavaPlugin {

    private ArenaPool arenaPool;

    @Override
    public void onEnable() {
        arenaPool = new ArenaPool();

        Worlds.configureWorld(Bukkit.getWorld("world"));
        Worlds.cleanArenas();

        NightVisionHandler nightVisionHandler = new NightVisionHandler();

        new HubCommand().register(this);
        new TemplateCommand().register(this);
        new ArenaCommand(arenaPool).register(this);
        new ListArenasCommand(arenaPool).register(this);
        new CreateArenaCommand(arenaPool).register(this);
        new DeleteArenaCommand(arenaPool).register(this);
        new LoadStructureCommand().register(this);
        new NightVisionCommand(nightVisionHandler).register(this);

        registerEventHandler(new SpawnHandler());
        registerEventHandler(new WelcomeHandler());
        registerEventHandler(nightVisionHandler);
    }

    @Override
    public void onDisable() {
        arenaPool.close();
    }

    private void registerEventHandler(Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
    }
}
