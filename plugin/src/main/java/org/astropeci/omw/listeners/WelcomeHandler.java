package org.astropeci.omw.listeners;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class WelcomeHandler implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        TextComponent welcomeToComponent = new TextComponent("Welcome to ");
        welcomeToComponent.setColor(ChatColor.AQUA);

        TextComponent openComponent = new TextComponent("Open");
        openComponent.setColor(ChatColor.GREEN);
        openComponent.setBold(true);

        TextComponent mwComponent = new TextComponent("MissileWars");
        mwComponent.setColor(ChatColor.RED);
        mwComponent.setBold(true);

        TextComponent welcomeToOpenMwMessage = new TextComponent(welcomeToComponent, openComponent, mwComponent);

        TextComponent githubMessage = new TextComponent("Click here to browse the code or file an issue");
        githubMessage.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/LlewVallis/OpenMissileWars"));
        githubMessage.setColor(ChatColor.BLUE);
        githubMessage.setItalic(true);

        e.getPlayer().spigot().sendMessage(welcomeToOpenMwMessage);
        e.getPlayer().spigot().sendMessage(githubMessage);

        e.getPlayer().setPlayerListHeaderFooter(
                new BaseComponent[] { new TextComponent(openComponent, mwComponent) },
                new BaseComponent[] { }
        );
    }
}
