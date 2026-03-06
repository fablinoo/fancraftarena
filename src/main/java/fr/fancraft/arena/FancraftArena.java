package fr.fancraft.arena;

import org.bukkit.plugin.java.JavaPlugin;

public class FancraftArena extends JavaPlugin {

    private DatabaseManager db;

    @Override
    public void onEnable() {
        db = new DatabaseManager(this);
        if (!db.init()) {
            getLogger().severe("Base de donnees indisponible, desactivation du plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        ScoreboardManager scoreboard = new ScoreboardManager(this, db);
        KitManager kitManager = new KitManager();

        getServer().getPluginManager().registerEvents(new DeathListener(this, db, scoreboard), this);
        getServer().getPluginManager().registerEvents(kitManager, this);
        getCommand("classement").setExecutor(new ClassementCommand(this, db));
        getCommand("kit").setExecutor(new KitCommand(kitManager));

        getLogger().info("FancraftArena active.");
    }

    @Override
    public void onDisable() {
        if (db != null) {
            db.close();
        }
    }
}
