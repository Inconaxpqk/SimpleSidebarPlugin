package com.inconaxpqk.simpleSidebarPlugin;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import me.clip.placeholderapi.PlaceholderAPI;
import java.util.List;

public final class SimpleSidebarPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        startSidebarUpdater();
    }

    private boolean isPlaceholderAPIEnabled() {
        return Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
    }

    private String replacePlaceholders(Player player, String text) {

        // 1. свои плейсхолдеры
        text = text
                .replace("%player%", player.getName())
                .replace("%online%", String.valueOf(Bukkit.getOnlinePlayers().size()))
                .replace("%world%", player.getWorld().getName());

        // 2. PlaceholderAPI (Towny, Vault, LuckPerms и т.д.)
        if (isPlaceholderAPIEnabled()) {
            text = PlaceholderAPI.setPlaceholders(player, text);
        }

        return text;
    }


    private void startSidebarUpdater() {
        int interval = getConfig().getInt("sidebar.update-interval");
        if (interval == 0) {
            getLogger().warning("КОНФИГ СЛОМАН ЧИСЛО НЕ МОЖЕТ БЫТЬ 0!!!");
            return;
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getOnlinePlayers().forEach(player -> {
                    updateSidebar(player);
                    updateTab(player);
                });
            }
        }.runTaskTimer(this, 0L, interval);
    }
    private void updateTab(Player player) {
        boolean enabled = getConfig().getBoolean("tab.enabled");
        if (enabled == true){
            List<String> headerLines = getConfig().getStringList("tab.header");
            List<String> footerLines = getConfig().getStringList("tab.footer");

            StringBuilder headerBuilder = new StringBuilder();
            for (int i = 0; i < headerLines.size(); i++) {
                String text = replacePlaceholders(player, headerLines.get(i));
                text = ChatColor.translateAlternateColorCodes('&', text);
                headerBuilder.append(text);
                if (i < headerLines.size() - 1) {
                    headerBuilder.append("\n");
                }
            }


            StringBuilder footerBuilder = new StringBuilder();
            for (int i = 0; i < footerLines.size(); i++) {
                String text = replacePlaceholders(player, footerLines.get(i));
                text = ChatColor.translateAlternateColorCodes('&', text);
                footerBuilder.append(text);
                if (i < footerLines.size() - 1) {
                    footerBuilder.append("\n");
                }
            }

            player.setPlayerListHeaderFooter(
                    headerBuilder.toString(),
                    footerBuilder.toString()
            );
        }
    }
    private void updateSidebar(Player player) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) return;

        Scoreboard board = manager.getNewScoreboard();

        String title = ChatColor.translateAlternateColorCodes(
                '&',
                getConfig().getString("sidebar.title", "Sidebar")
        );

        Objective objective = board.registerNewObjective(
                "sidebar",
                Criteria.DUMMY,
                title
        );

        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        List<String> lines = getConfig().getStringList("sidebar.lines");
        int score = lines.size();

        for (String line : lines) {
            String text = replacePlaceholders(player, line);
            text = ChatColor.translateAlternateColorCodes('&', text);

            objective.getScore(text).setScore(score--);
        }

        player.setScoreboard(board);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}