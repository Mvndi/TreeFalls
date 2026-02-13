package net.mvndicraft.treefalls.listener;

import net.mvndicraft.treefalls.TreeFallsPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.persistence.PersistentDataType;

public class BrokenLogListener implements Listener {

    // Towny & most other plugins compatibility works by ignoring cancelled events
    @EventHandler(ignoreCancelled = true)
    public void onLogBroken(BlockBreakEvent event) {
        if (TreeFallsPlugin.getInstance().isWood(event.getBlock().getType())
                && TreeFallsPlugin.getInstance().isAxe(event.getPlayer().getInventory().getItemInMainHand().getType())
                && TreeFallsPlugin.getInstance().isGameModeOK(event.getPlayer())) {
            event.setCancelled(true);
            Location location = event.getBlock().getLocation();
            BlockData blockData = event.getBlock().getBlockData();
            event.getBlock().setType(Material.AIR);
            event.getBlock().getWorld().spawn(location.add(0.5, 0.2, 0.5), FallingBlock.class, entity -> {
                entity.setBlockData(blockData);
                entity.getPersistentDataContainer().set(TreeFallsPlugin.getInstance().getFallingLogKey(), PersistentDataType.BOOLEAN, true);
                entity.setDropItem(true);
            });
        }
    }
}
