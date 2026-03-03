package net.mvndicraft.treefalls.listener;

import net.coreprotect.CoreProtect;
import net.mvndicraft.treefalls.TreeFallsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.world.StructureGrowEvent;

// Add an extra log to CoreProtect so that the latest is not a regular player placing blocks.
public class CoreProtectListener implements Listener {
    // Saplings will propagate their player placer if they grow naturaly
    @EventHandler(ignoreCancelled = true)
    public void onSaplingPlaced(BlockPlaceEvent event) {
        if (TreeFallsPlugin.getInstance().isSapling(event.getBlockPlaced().getType())
                && TreeFallsPlugin.getInstance().getConfig().getBoolean("player_grown_trees_are_natural_trees", true)) {
            TreeFallsPlugin.debug("A sapling was placed");
            Bukkit.getRegionScheduler().runDelayed(TreeFallsPlugin.getInstance(), event.getBlockPlaced().getLocation(), t -> {
                logTreePlacement(event.getPlayer(), event.getBlockPlaced().getLocation(), event.getBlockPlaced().getType(),
                        event.getBlockPlaced().getBlockData());
                TreeFallsPlugin.debug(() -> "Logged sapling placement at " + event.getBlockPlaced().getLocation());
            }, TreeFallsPlugin.getInstance().getCoreProtectTickDelay());
        }
    }

    // Saplings won't propagate their player placer if they grow with bonemeal we need to log for each tree block
    @EventHandler(ignoreCancelled = true)
    public void onTreeGrow(StructureGrowEvent event) {
        if (event.getPlayer() != null && TreeFallsPlugin.getInstance().getConfig().getBoolean("player_grown_trees_are_natural_trees", true)
                && event.isFromBonemeal()) {
            Bukkit.getRegionScheduler().runDelayed(TreeFallsPlugin.getInstance(), event.getLocation(), t -> {
                TreeFallsPlugin.debug("Logging all tree blocks as natural");
                for (BlockState blockState : event.getBlocks()) {
                    logTreePlacement(event.getPlayer(), blockState.getLocation(), blockState.getType(), blockState.getBlockData());
                }
            }, TreeFallsPlugin.getInstance().getCoreProtectTickDelay());
        }
    }

    private void logTreePlacement(Player player, Location location, Material material, BlockData blocData) {
        CoreProtect.getInstance().getAPI().logPlacement("#tree_" + player.getName(), location, material, blocData);
    }
}
