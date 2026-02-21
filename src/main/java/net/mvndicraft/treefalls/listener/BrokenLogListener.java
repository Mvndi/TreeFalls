package net.mvndicraft.treefalls.listener;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import net.mvndicraft.treefalls.TreeFallsPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class BrokenLogListener implements Listener {

    // Towny & most other plugins compatibility works by ignoring cancelled events
    // HIGHEST priority to allow Towny or other plugin to cancel the event.
    // Towny get an extra test to avoid breaking trees that are half in a claim.
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onLogBroken(BlockBreakEvent event) {
        if (TreeFallsPlugin.getInstance().isWood(event.getBlock().getType())
                && TreeFallsPlugin.getInstance().isAxe(event.getPlayer().getInventory().getItemInMainHand().getType())
                && TreeFallsPlugin.getInstance().isGameModeOK(event.getPlayer())
                && TreeFallsPlugin.getInstance().isSneakingOK(event.getPlayer())) {
            event.setCancelled(true);
            cutTree(event.getBlock(), event.getPlayer());
        }
    }

    private void cutTree(Block block, Player player) {
        TreeFallsPlugin.debug("Cutting tree");
        int maxTreeSize = TreeFallsPlugin.getInstance().getConfig().getInt("max_tree_size", 256);
        Queue<Block> woodQueue = new LinkedList<>();
        woodQueue.add(block);
        int cuttedBlocks = 0;
        while (!woodQueue.isEmpty() && cuttedBlocks < maxTreeSize && reduceDurability(player)) {
            block = woodQueue.poll();
            if (TreeFallsPlugin.getInstance().hasTownyPerms(player, block.getLocation(), block.getType())) {
                fallBlock(block);
                TreeFallsPlugin.debug("Falling block: " + block);
                for (Block nextBlock : getNextBlocks(block)) {
                    if (TreeFallsPlugin.getInstance().isWood(nextBlock.getType()) && !woodQueue.contains(nextBlock)) {
                        woodQueue.add(nextBlock);
                        TreeFallsPlugin.debug("Added block to queue: " + nextBlock);
                    }
                }
                cuttedBlocks++;
            }
        }
        TreeFallsPlugin.debug("Cutted " + cuttedBlocks + " blocks");
        TreeFallsPlugin.debug("Can more blocks be cut? " + (cuttedBlocks < maxTreeSize));
        TreeFallsPlugin.debug("Is queue empty? " + woodQueue.isEmpty());
    }

    private List<Block> getNextBlocks(Block block) {
        List<Block> nextBlocks = new LinkedList<>();
        // 1 block up
        nextBlocks.add(block.getRelative(BlockFace.UP));
        nextBlocks.addAll(getSideBlocks(block.getRelative(BlockFace.UP)));
        // same level
        nextBlocks.addAll(getSideBlocks(block));
        // 1 block down
        nextBlocks.add(block.getRelative(BlockFace.DOWN));
        nextBlocks.addAll(getSideBlocks(block.getRelative(BlockFace.DOWN)));
        return nextBlocks;
    }
    private List<Block> getSideBlocks(Block block) {
        return List.of(block.getRelative(BlockFace.NORTH), block.getRelative(BlockFace.SOUTH), block.getRelative(BlockFace.WEST),
                block.getRelative(BlockFace.EAST), block.getRelative(BlockFace.NORTH_WEST), block.getRelative(BlockFace.NORTH_EAST),
                block.getRelative(BlockFace.SOUTH_WEST), block.getRelative(BlockFace.SOUTH_EAST));
    }

    private boolean reduceDurability(Player player) {
        if (getDurability(player.getInventory().getItemInMainHand()) == 0) {
            return false;
        }
        player.getInventory().getItemInMainHand().damage(1, player);
        return true;
    }

    private int getDurability(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return 0;
        }

        int max = item.getType().getMaxDurability();
        if (max <= 0) {
            return 0; // Not a damageable item
        }

        ItemMeta meta = item.getItemMeta();
        if (!(meta instanceof Damageable damageable)) {
            return max; // Undamaged
        }

        int damage = damageable.hasDamage() ? damageable.getDamage() : 0;

        return max - damage;
    }


    private void fallBlock(Block block) {
        Location location = block.getLocation();
        BlockData blockData = block.getBlockData();
        block.setType(Material.AIR);
        block.getWorld().spawn(location.add(0.5, 0.2, 0.5), FallingBlock.class, entity -> {
            entity.setBlockData(blockData);
            entity.getPersistentDataContainer().set(TreeFallsPlugin.getInstance().getFallingLogKey(), PersistentDataType.BOOLEAN, true);
            entity.setDropItem(true);
        });
    }
}
