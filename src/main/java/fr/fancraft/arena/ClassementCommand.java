package fr.fancraft.arena;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ClassementCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final DatabaseManager db;

    public ClassementCommand(JavaPlugin plugin, DatabaseManager db) {
        this.plugin = plugin;
        this.db = db;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Commande reservee aux joueurs.");
            return true;
        }

        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();

        CompletableFuture<List<DatabaseManager.StatsEntry>> topFuture = db.getTopKills(10);
        CompletableFuture<Integer> rankFuture = db.getPlayerRank(uuid);
        CompletableFuture<DatabaseManager.StatsEntry> statsFuture = db.getPlayerStats(uuid);

        CompletableFuture.allOf(topFuture, rankFuture, statsFuture)
                .thenAcceptAsync(v -> {
                    List<DatabaseManager.StatsEntry> top = topFuture.join();
                    int rank = rankFuture.join();
                    DatabaseManager.StatsEntry stats = statsFuture.join();

                    player.sendMessage("");
                    player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "      Classement PvP - Top 10");
                    player.sendMessage(ChatColor.GRAY + " ----------------------------");

                    for (int i = 0; i < top.size(); i++) {
                        DatabaseManager.StatsEntry entry = top.get(i);
                        ChatColor color = getPositionColor(i + 1);
                        player.sendMessage(color + " #" + (i + 1) + " " + ChatColor.WHITE + entry.getName()
                                + ChatColor.GRAY + " - " + ChatColor.GREEN + entry.getKills() + " kills"
                                + ChatColor.GRAY + " / " + ChatColor.RED + entry.getDeaths() + " morts");
                    }

                    if (top.isEmpty()) {
                        player.sendMessage(ChatColor.GRAY + " Aucune donnee pour le moment.");
                    }

                    player.sendMessage(ChatColor.GRAY + " ----------------------------");
                    if (stats != null) {
                        player.sendMessage(ChatColor.YELLOW + " Ta position: " + ChatColor.WHITE + "#" + rank
                                + ChatColor.GRAY + " - " + ChatColor.GREEN + stats.getKills() + " kills"
                                + ChatColor.GRAY + " / " + ChatColor.RED + stats.getDeaths() + " morts");
                    } else {
                        player.sendMessage(ChatColor.YELLOW + " Tu n'as pas encore de stats.");
                    }
                    player.sendMessage("");
                }, db.syncExecutor())
                .exceptionally(ex -> {
                    plugin.getLogger().warning("Erreur lors du chargement du classement: " + ex.getMessage());
                    Bukkit.getScheduler().runTask(plugin, () ->
                            player.sendMessage(ChatColor.RED + "Erreur lors du chargement du classement."));
                    return null;
                });

        return true;
    }

    private ChatColor getPositionColor(int pos) {
        switch (pos) {
            case 1: return ChatColor.GOLD;
            case 2: return ChatColor.YELLOW;
            case 3: return ChatColor.AQUA;
            default: return ChatColor.GRAY;
        }
    }
}
