package net.mvndicraft.treefalls.listener;

import net.mvndicraft.treefalls.TreeFallsPlugin;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.inventory.ItemStack;

public class FallingLogListener implements Listener {

    // Towny & most other plugins compatibility works by ignoring cancelled events
    @EventHandler(ignoreCancelled = true)
    public void onLogHitGround(EntityChangeBlockEvent event) {
        if (event.getEntity().getPersistentDataContainer().has(TreeFallsPlugin.getInstance().getFallingLogKey())) {

            TreeFallsPlugin.debug(() -> "Dropping log drops: " + ItemStack.of(event.getTo()));
            event.setCancelled(true);
            Location location = event.getBlock().getLocation();

            event.getBlock().getWorld().dropItemNaturally(location, ItemStack.of(event.getTo()));
        }
    }
}
