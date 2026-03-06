package fr.fancraft.arena;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DatabaseManager {

    private final JavaPlugin plugin;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "FancraftArena-DB");
        t.setDaemon(true);
        return t;
    });
    private final Executor syncExecutor;
    private Connection connection;

    public DatabaseManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.syncExecutor = task -> Bukkit.getScheduler().runTask(plugin, task);
    }

    public boolean init() {
        try {
            Class.forName("org.sqlite.JDBC");
            File dbFile = new File(plugin.getDataFolder(), "arena.db");
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());

            try (Statement stmt = connection.createStatement()) {
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS player_stats ("
                        + "uuid TEXT PRIMARY KEY, "
                        + "name TEXT NOT NULL, "
                        + "kills INTEGER DEFAULT 0, "
                        + "deaths INTEGER DEFAULT 0)");
            }
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Impossible d'initialiser la base de donnees: " + e.getMessage());
            return false;
        }
    }

    public void close() {
        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException ignored) {}
    }

    public Executor syncExecutor() {
        return syncExecutor;
    }

    public CompletableFuture<Void> addKill(UUID uuid, String name) {
        return CompletableFuture.runAsync(() -> upsert(uuid, name, 1, 0), executor);
    }

    public CompletableFuture<Void> addDeath(UUID uuid, String name) {
        return CompletableFuture.runAsync(() -> upsert(uuid, name, 0, 1), executor);
    }

    private void upsert(UUID uuid, String name, int kills, int deaths) {
        try {
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT OR IGNORE INTO player_stats (uuid, name, kills, deaths) VALUES (?, ?, 0, 0)")) {
                ps.setString(1, uuid.toString());
                ps.setString(2, name);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = connection.prepareStatement(
                    "UPDATE player_stats SET name = ?, kills = kills + ?, deaths = deaths + ? WHERE uuid = ?")) {
                ps.setString(1, name);
                ps.setInt(2, kills);
                ps.setInt(3, deaths);
                ps.setString(4, uuid.toString());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Erreur SQL sur upsert: " + e.getMessage());
        }
    }

    public CompletableFuture<List<StatsEntry>> getTopKills(int limit) {
        return CompletableFuture.supplyAsync(() -> {
            List<StatsEntry> entries = new ArrayList<>();
            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT name, kills, deaths FROM player_stats ORDER BY kills DESC LIMIT ?")) {
                ps.setInt(1, limit);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        entries.add(new StatsEntry(rs.getString("name"), rs.getInt("kills"), rs.getInt("deaths")));
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().warning("Erreur SQL sur getTopKills: " + e.getMessage());
            }
            return entries;
        }, executor);
    }

    public CompletableFuture<StatsEntry> getPlayerStats(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT name, kills, deaths FROM player_stats WHERE uuid = ?")) {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new StatsEntry(rs.getString("name"), rs.getInt("kills"), rs.getInt("deaths"));
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().warning("Erreur SQL sur getPlayerStats: " + e.getMessage());
            }
            return null;
        }, executor);
    }

    public CompletableFuture<Integer> getPlayerRank(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT COUNT(*) + 1 AS rank FROM player_stats WHERE kills > "
                            + "(SELECT COALESCE((SELECT kills FROM player_stats WHERE uuid = ?), 0))")) {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("rank");
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().warning("Erreur SQL sur getPlayerRank: " + e.getMessage());
            }
            return -1;
        }, executor);
    }

    public static class StatsEntry {
        private final String name;
        private final int kills;
        private final int deaths;

        public StatsEntry(String name, int kills, int deaths) {
            this.name = name;
            this.kills = kills;
            this.deaths = deaths;
        }

        public String getName() { return name; }
        public int getKills() { return kills; }
        public int getDeaths() { return deaths; }
    }
}
