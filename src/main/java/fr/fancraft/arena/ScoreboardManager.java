package fr.fancraft.arena;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ScoreboardManager {

    private final JavaPlugin plugin;
    private final DatabaseManager db;

    public ScoreboardManager(JavaPlugin plugin, DatabaseManager db) {
        this.plugin = plugin;
        this.db = db;
    }

    public void setScoreboard(Player player) {
        UUID uuid = player.getUniqueId();

        CompletableFuture<DatabaseManager.StatsEntry> statsFuture = db.getPlayerStats(uuid);
        CompletableFuture<Integer> rankFuture = db.getPlayerRank(uuid);

        statsFuture.thenCombineAsync(rankFuture, (stats, rank) -> {
            if (!player.isOnline()) return null;

            int kills = stats != null ? stats.getKills() : 0;
            int deaths = stats != null ? stats.getDeaths() : 0;
            String ratio = deaths == 0 ? (kills > 0 ? kills + ".00" : "0.00")
                    : String.format("%.2f", (double) kills / deaths);

            Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
            Objective obj = board.registerNewObjective("arena", "dummy");
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);
            obj.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Fancraft");

            int line = 11;
            setLine(obj, line--, " ");
            setLine(obj, line--, ChatColor.WHITE + " Pseudo: " + ChatColor.GREEN + player.getName());
            setLine(obj, line--, "  ");
            setLine(obj, line--, ChatColor.WHITE + " Kills: " + ChatColor.GREEN + kills);
            setLine(obj, line--, ChatColor.WHITE + " Morts: " + ChatColor.RED + deaths);
            setLine(obj, line--, ChatColor.WHITE + " Ratio: " + ChatColor.AQUA + ratio);
            setLine(obj, line--, "   ");
            setLine(obj, line--, ChatColor.WHITE + " Rang: " + ChatColor.GOLD + "#" + rank);
            setLine(obj, line--, "    ");
            setLine(obj, line, ChatColor.YELLOW + " fancraft.eu");

            player.setScoreboard(board);
            return null;
        }, db.syncExecutor()).exceptionally(ex -> {
            plugin.getLogger().warning("Erreur lors de la mise a jour du scoreboard: " + ex.getMessage());
            return null;
        });
    }

    private void setLine(Objective obj, int score, String text) {
        obj.getScore(text).setScore(score);
    }
}
