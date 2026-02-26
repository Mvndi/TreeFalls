package net.mvndicraft.treefalls.listener;

import net.mvndicraft.treefalls.TreeFallsPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.inventory.ItemStack;

public class FallingLogListener implements Listener {

    // Towny & most other plugins compatibility works by ignoring cancelled events
    @EventHandler(ignoreCancelled = true)
    public void onLogHitGround(EntityChangeBlockEvent event) {
        if (event.getEntity().getPersistentDataContainer().has(TreeFallsPlugin.getInstance().getFallingLogKey())) {

            TreeFallsPlugin.debug(() -> "Dropping drops: " + ItemStack.of(event.getTo()));
            event.setCancelled(true);
            Location location = event.getBlock().getLocation();

            // Ensure the entity is actually a FallingBlock
            if (!(event.getEntity() instanceof org.bukkit.entity.FallingBlock falling)) {
                return;
            }

            // Get the original block data from the falling entity
            BlockData blockData = falling.getBlockData();

            World world = location.getWorld();
            if (world == null) {
                return;
            }

            // Create a fake block state for loot simulation
            Block tempBlock = world.getBlockAt(location);
            Material originalType = tempBlock.getType();

            tempBlock.setBlockData(blockData, false);

            try {
                for (ItemStack drop : tempBlock.getDrops(null, null)) {
                    world.dropItemNaturally(location, drop);
                }
            } finally {
                tempBlock.setType(originalType, false);
            }
        }
    }
}
