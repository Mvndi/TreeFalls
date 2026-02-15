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
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class BrokenLogListener implements Listener {

    // Towny & most other plugins compatibility works by ignoring cancelled events
    @EventHandler(ignoreCancelled = true)
    public void onLogBroken(BlockBreakEvent event) {
        if (TreeFallsPlugin.getInstance().isWood(event.getBlock().getType())
                && TreeFallsPlugin.getInstance().isAxe(event.getPlayer().getInventory().getItemInMainHand().getType())
                && TreeFallsPlugin.getInstance().isGameModeOK(event.getPlayer())) {
            event.setCancelled(true);
            cutTree(event.getBlock(), event.getPlayer());
        }
    }

    private void cutTree(Block block, Player player) {
        TreeFallsPlugin.debug("Cutting tree");
        int maxTreeSize = TreeFallsPlugin.getInstance().getConfig().getInt("max_tree_size", 256);
        List<BlockFace> nextBlocks = List.of(BlockFace.UP, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST,
                BlockFace.DOWN);
        Queue<Block> woodQueue = new LinkedList<>();
        woodQueue.add(block);
        int cuttedBlocks = 0;
        while (!woodQueue.isEmpty() && cuttedBlocks < maxTreeSize && reduceDurability(player)) {
            block = woodQueue.poll();
            fallBlock(block);
            TreeFallsPlugin.debug("Falling block: " + block);
            for (BlockFace face : nextBlocks) {
                Block nextBlock = block.getRelative(face);
                if (TreeFallsPlugin.getInstance().isWood(nextBlock.getType())) {
                    woodQueue.add(nextBlock);
                    TreeFallsPlugin.debug("Added block to queue: " + nextBlock);
                }
            }
            cuttedBlocks++;
        }
        TreeFallsPlugin.debug("Cutted " + cuttedBlocks + " blocks");
        TreeFallsPlugin.debug("Can axes still be used? " + (getDurability(player.getInventory().getItemInMainHand()) > 0));
        TreeFallsPlugin.debug("Can more blocks be cut? " + (cuttedBlocks < maxTreeSize));
        TreeFallsPlugin.debug("Is queue empty? " + woodQueue.isEmpty());
    }

    private boolean reduceDurability(Player player) {
        if (getDurability(player.getInventory().getItemInMainHand()) > 0) {
            player.getInventory().getItemInMainHand().damage(1, player);
            return true;
        }
        return false;
    }

    private int getDurability(ItemStack item) {
        if (item == null || item.getType().isAir())
            return 0;

        ItemMeta meta = item.getItemMeta();
        if (!(meta instanceof Damageable damageable))
            return 0;

        return damageable.getDamage();
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
