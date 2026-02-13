package net.mvndicraft.treefalls;

import co.aikar.commands.PaperCommandManager;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.mvndicraft.treefalls.listener.BrokenLogListener;
import net.mvndicraft.treefalls.listener.FallingLogListener;
import org.bstats.bukkit.Metrics;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class TreeFallsPlugin extends JavaPlugin {
    private Set<Material> woods;
    private Set<Material> axes;
    private Set<GameMode> gameModes;
    private NamespacedKey fallingLogKey = new NamespacedKey(this, "falling_log");
    @Override
    public void onEnable() {
        new Metrics(this, 29518);

        // Save config in our plugin data folder if it does not exist.
        saveDefaultConfig();

        woods = Stream.of(Material.values()).filter(material -> (material.name().endsWith("_LOG") || material.name().endsWith("_WOOD"))
                && !material.name().startsWith("LEGACY_")).collect(Collectors.toCollection(() -> EnumSet.noneOf(Material.class)));
        debug(() -> "woods set: " + woods.toString());

        axes = Stream.of(Material.values()).filter(material -> material.name().endsWith("_AXE") && !material.name().startsWith("LEGACY_"))
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(Material.class)));
        debug(() -> "axes set: " + axes.toString());

        PaperCommandManager manager = new PaperCommandManager(this);
        manager.registerCommand(new TreeFallsCommand());

        getServer().getPluginManager().registerEvents(new BrokenLogListener(), this);
        getServer().getPluginManager().registerEvents(new FallingLogListener(), this);
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        gameModes = getConfigGameMode("enabled_gamemode");
        debug("gameModes: " + gameModes);
    }

    public static TreeFallsPlugin getInstance() { return getPlugin(TreeFallsPlugin.class); }

    public boolean isWood(Material material) { return woods.contains(material); }
    public boolean isAxe(Material material) { return axes.contains(material); }
    public boolean isGameModeOK(Player player) { return gameModes.contains(player.getGameMode()); }
    public NamespacedKey getFallingLogKey() { return fallingLogKey; }

    private Set<GameMode> getConfigGameMode(String key) {
        return getConfig().getStringList(key).stream().map(gm -> safeMatchGameMode(gm, key)).filter(Objects::nonNull)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(GameMode.class)));
    }

    @Nullable
    private GameMode safeMatchGameMode(String name, String key) {
        try {
            return GameMode.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            getLogger().warning(() -> "Invalid GameMode in config at '" + key + "': " + name);
            return null;
        }
    }


    // Usual log with debug level
    public static void log(Level level, String message) { getInstance().getLogger().log(level, message); }
    public static void log(Level level, Supplier<String> messageProvider) { getInstance().getLogger().log(level, messageProvider); }
    public static void log(Level level, String message, Throwable e) { getInstance().getLogger().log(level, message, e); }
    public static void debug(String message) {
        if (getInstance().getConfig().getBoolean("debug", false)) {
            log(Level.INFO, message);
        }
    }
    public static void debug(Supplier<String> messageProvider) {
        if (getInstance().getConfig().getBoolean("debug", false)) {
            log(Level.INFO, messageProvider);
        }
    }
    public static void info(String message) { log(Level.INFO, message); }
    public static void info(String message, Throwable e) { log(Level.INFO, message, e); }
    public static void warning(String message) { log(Level.WARNING, message); }
    public static void warning(String message, Throwable e) { log(Level.WARNING, message, e); }
    public static void error(String message) { log(Level.SEVERE, message); }
    public static void error(String message, Throwable e) { log(Level.SEVERE, message, e); }
}
