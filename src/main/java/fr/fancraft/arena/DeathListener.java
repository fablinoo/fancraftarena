package fr.fancraft.arena;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class DeathListener implements Listener {

    private final JavaPlugin plugin;
    private final DatabaseManager db;
    private final ScoreboardManager scoreboard;

    public DeathListener(JavaPlugin plugin, DatabaseManager db, ScoreboardManager scoreboard) {
        this.plugin = plugin;
        this.db = db;
        this.scoreboard = scoreboard;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        event.setJoinMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "+" + ChatColor.GRAY + "] "
                + ChatColor.GOLD + player.getName());

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;
            player.sendMessage("");
            player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + " Bienvenue sur Fancraft Arena !");
            player.sendMessage(ChatColor.GRAY + " Utilise " + ChatColor.YELLOW + "/kit"
                    + ChatColor.GRAY + " pour choisir ton equipement.");
            player.sendMessage(ChatColor.GRAY + " Utilise " + ChatColor.YELLOW + "/classement"
                    + ChatColor.GRAY + " pour voir le top 10.");
            player.sendMessage("");
            scoreboard.setScoreboard(player);
        }, 20L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.setQuitMessage(ChatColor.GRAY + "[" + ChatColor.RED + "-" + ChatColor.GRAY + "] "
                + ChatColor.GOLD + event.getPlayer().getName());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = getKiller(victim);

        event.setDeathMessage(null);
        event.getDrops().clear();
        event.setDroppedExp(0);

        db.addDeath(victim.getUniqueId(), victim.getName())
                .exceptionally(ex -> { plugin.getLogger().warning("Erreur addDeath: " + ex.getMessage()); return null; });

        String message;
        if (killer != null && !killer.equals(victim)) {
            db.addKill(killer.getUniqueId(), killer.getName())
                    .exceptionally(ex -> { plugin.getLogger().warning("Erreur addKill: " + ex.getMessage()); return null; });
            message = ChatColor.GOLD + killer.getName() + ChatColor.GRAY
                    + " a tue " + ChatColor.GOLD + victim.getName();
            refreshLater(killer);
        } else {
            message = ChatColor.GOLD + victim.getName() + ChatColor.GRAY
                    + " " + getDeathCause(victim);
        }

        for (Player online : Bukkit.getOnlinePlayers()) {
            online.sendMessage(message);
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        refreshLater(event.getPlayer());
    }

    private Player getKiller(Player victim) {
        Player killer = victim.getKiller();
        if (killer != null) return killer;

        EntityDamageEvent lastDamage = victim.getLastDamageCause();
        if (lastDamage instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) lastDamage;
            if (damageEvent.getDamager() instanceof Player) {
                return (Player) damageEvent.getDamager();
            }
            if (damageEvent.getDamager() instanceof Projectile) {
                Projectile proj = (Projectile) damageEvent.getDamager();
                if (proj.getShooter() instanceof Player) {
                    return (Player) proj.getShooter();
                }
            }
        }
        return null;
    }

    private String getDeathCause(Player victim) {
        EntityDamageEvent cause = victim.getLastDamageCause();
        if (cause == null) return "est mort";

        switch (cause.getCause()) {
            case VOID: return "est tombe dans le vide";
            case FALL: return "a fait une chute mortelle";
            case LAVA: return "a brule dans la lave";
            case FIRE:
            case FIRE_TICK: return "a brule vif";
            case DROWNING: return "s'est noye";
            case SUFFOCATION: return "a suffoque";
            case STARVATION: return "est mort de faim";
            case LIGHTNING: return "a ete foudroye";
            case POISON: return "a ete empoisonne";
            case ENTITY_EXPLOSION:
            case BLOCK_EXPLOSION: return "a explose";
            case CONTACT: return "s'est pique sur un cactus";
            case SUICIDE: return "s'est suicide";
            default: return "est mort";
        }
    }

    private void refreshLater(Player player) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) scoreboard.setScoreboard(player);
        }, 10L);
    }
}
