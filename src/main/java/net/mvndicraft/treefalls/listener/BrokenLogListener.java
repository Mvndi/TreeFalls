package net.mvndicraft.treefalls.listener;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import net.mvndicraft.treefalls.TreeFallsPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Leaves;
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
        long start = System.currentTimeMillis();
        TreeFallsPlugin.debug("Cutting tree");
        int maxWoodBlocksFalling = TreeFallsPlugin.getInstance().getConfig().getInt("max_wood_blocks_falling", 256);
        Queue<Block> woodQueue = new ArrayDeque<>();
        Set<Block> visitedWoodBlocks = new HashSet<>();
        Queue<Block> leavesQueue = new ArrayDeque<>();
        Set<Block> visitedLeavesBlocks = new HashSet<>();

        woodQueue.add(block);
        visitedWoodBlocks.add(block);

        int cuttedBlocks = 0;
        while (!woodQueue.isEmpty() && cuttedBlocks < maxWoodBlocksFalling && reduceDurability(player)) {
            block = woodQueue.poll();
            if (TreeFallsPlugin.getInstance().hasTownyPerms(player, block.getLocation(), block.getType())) {
                fallBlock(block);
                // TreeFallsPlugin.debug("Falling block: " + block);
                for (Block nextBlock : getNextBlocks(block)) {
                    if (TreeFallsPlugin.getInstance().isWood(nextBlock.getType()) && visitedWoodBlocks.add(nextBlock)) {
                        woodQueue.add(nextBlock);
                        TreeFallsPlugin.debug("Added wood block to queue: " + nextBlock);
                    } else if (TreeFallsPlugin.getInstance().isLeaves(nextBlock.getType()) && visitedLeavesBlocks.add(nextBlock)) {
                        leavesQueue.add(nextBlock);
                        TreeFallsPlugin.debug("Added leaves block to queue from wood proximity: " + nextBlock);
                    }
                }
                cuttedBlocks++;
            }
        }
        TreeFallsPlugin.debug("Cutted " + cuttedBlocks + " wood blocks");
        TreeFallsPlugin.debug("Can more blocks be cut? " + (cuttedBlocks < maxWoodBlocksFalling));
        TreeFallsPlugin.debug(() -> "Is queue empty? " + woodQueue.isEmpty() + " woodQueue size: " + woodQueue.size());

        // Bukkit.getRegionScheduler().runDelayed(TreeFallsPlugin.getInstance(), block.getLocation(), t -> fallLeaves(leavesQueue, player),
        // 2);
        fallLeaves(leavesQueue, visitedLeavesBlocks, player);

        long end = System.currentTimeMillis();
        TreeFallsPlugin.debug("Cut tree in " + (end - start) + "ms");
    }

    private void fallLeaves(Queue<Block> leavesQueue, Set<Block> visitedLeavesBlocks, Player player) {
        int cuttedBlocks = 0;
        int maxLeavesBlocksFalling = TreeFallsPlugin.getInstance().getConfig().getInt("max_leaves_blocks_falling", 256);
        while (!leavesQueue.isEmpty() && cuttedBlocks < maxLeavesBlocksFalling) {
            Block block = leavesQueue.poll();
            if (TreeFallsPlugin.getInstance().hasTownyPerms(player, block.getLocation(), block.getType()) && shouldLeaveFall(block)) {
                fallBlock(block);
                // TreeFallsPlugin.debug("Falling block: " + block);
                for (Block nextBlock : getConnectedBlocks(block)) {
                    if (TreeFallsPlugin.getInstance().isLeaves(nextBlock.getType()) && visitedLeavesBlocks.add(nextBlock)) {
                        leavesQueue.add(nextBlock);
                        TreeFallsPlugin.debug("Added leaves block to queue from leaves proximity: " + nextBlock);
                    }
                }
                cuttedBlocks++;
            }
        }

        TreeFallsPlugin.debug("Cutted " + cuttedBlocks + " leaves blocks");
        TreeFallsPlugin.debug("Can more blocks be cut? " + (cuttedBlocks < maxLeavesBlocksFalling));
        TreeFallsPlugin.debug(() -> "Is queue empty? " + leavesQueue.isEmpty() + " leavesQueue size: " + leavesQueue.size());
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

    private List<Block> getConnectedBlocks(Block block) {
        return List.of(block.getRelative(BlockFace.UP), block.getRelative(BlockFace.NORTH), block.getRelative(BlockFace.SOUTH),
                block.getRelative(BlockFace.WEST), block.getRelative(BlockFace.EAST), block.getRelative(BlockFace.DOWN));
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

    /**
     * True if is is not persistent & not near a wood block (distance < 7)
     * Or true if it's not a leave, then we ignore special leave rules.
     */
    private boolean shouldLeaveFall(Block block) {
        if (block.getBlockData() instanceof Leaves leaves) {
            boolean shouldLeaveFall = (!leaves.isPersistent() && !isNearAnyLog(block));
            TreeFallsPlugin.debug("Should leave fall? " + shouldLeaveFall);
            TreeFallsPlugin.debug("leaves.isPersistent()? " + leaves.isPersistent());

            return shouldLeaveFall;
        }
        return true;
    }

    private boolean isNearAnyLog(Block start) {
        int maxDistance = 6;

        Queue<Block> queue = new ArrayDeque<>();
        Set<Block> visited = new HashSet<>();

        queue.add(start);
        visited.add(start);

        int depth = 0;

        while (!queue.isEmpty() && depth <= maxDistance) {
            int size = queue.size();

            for (int i = 0; i < size; i++) {
                Block block = queue.poll();

                if (TreeFallsPlugin.getInstance().isWood(block.getType())) {
                    return true; // ANY log in world
                }

                for (Block next : getConnectedBlocks(block)) {
                    if (!visited.contains(next) && (TreeFallsPlugin.getInstance().isLeaves(next.getType())
                            || TreeFallsPlugin.getInstance().isWood(next.getType()))) {

                        visited.add(next);
                        queue.add(next);
                    }
                }
            }

            depth++;
        }

        return false;
    }
}
