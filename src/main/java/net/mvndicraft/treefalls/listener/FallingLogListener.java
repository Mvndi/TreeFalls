package net.mvndicraft.treefalls.listener;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.mvndicraft.treefalls.TreeFallsPlugin;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.inventory.ItemStack;

public class FallingLogListener implements Listener {

    // Towny & most other plugins compatibility works by ignoring cancelled events
    @EventHandler(ignoreCancelled = true)
    public void onLogHitGround(EntityChangeBlockEvent event) {
        if (event.getEntity().getPersistentDataContainer().has(TreeFallsPlugin.getInstance().getFallingLogKey())) {

            TreeFallsPlugin.debug(() -> "Dropping drops from EntityChangeBlockEvent: " + ItemStack.of(event.getTo()));
            event.setCancelled(true);

            // Ensure the entity is actually a FallingBlock
            if (!(event.getEntity() instanceof org.bukkit.entity.FallingBlock falling)) {
                return;
            }

            lootAsIfBroken(falling.getBlockData(), falling.getLocation());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDrop(EntityDropItemEvent event) {
        if (event.getEntity().getPersistentDataContainer().has(TreeFallsPlugin.getInstance().getFallingLogKey())) {

            TreeFallsPlugin.debug("Dropping drops from EntityDropItemEvent.");

            // Ensure the entity is actually a FallingBlock
            if (!(event.getEntity() instanceof org.bukkit.entity.FallingBlock falling)) {
                return;
            }

            event.setCancelled(true);

            falling.remove();

            lootAsIfBroken(falling.getBlockData(), falling.getLocation());
        }
    }

    // Paper API does not support block loottable.
    // There is a workaround for this using paper API. Create a fake block.
    // But fake blocks broke chests or other blocks they replace if we choose to place them at location.
    // Finding an air block to create a fake blocks might be long if there is a lot of blocks nearby.
    // So we use NMS here.
    // Which is probably better performance whise to than creating a fake block.
    private void lootAsIfBroken(BlockData blockData, Location location) {
        World world = location.getWorld();
        if (world == null) {
            return;
        }

        try {
            ServerLevel nmsWorld = ((CraftWorld) world).getHandle();

            BlockPos pos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());

            net.minecraft.world.level.block.state.BlockState nmsState = ((CraftBlockData) blockData).getState();

            LootParams.Builder builder = new LootParams.Builder(nmsWorld).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                    .withParameter(LootContextParams.BLOCK_STATE, nmsState)
                    .withParameter(LootContextParams.TOOL, net.minecraft.world.item.ItemStack.EMPTY);

            net.minecraft.world.level.storage.loot.LootTable table = nmsWorld.getServer().reloadableRegistries()
                    .getLootTable(nmsState.getBlock().getLootTable().orElseThrow());

            ObjectArrayList<net.minecraft.world.item.ItemStack> drops = table.getRandomItems(builder.create(LootContextParamSets.BLOCK));

            for (net.minecraft.world.item.ItemStack nmsStack : drops) {
                ItemStack bukkitStack = CraftItemStack.asBukkitCopy(nmsStack);
                world.dropItemNaturally(location, bukkitStack);
            }
        } catch (Exception e) {
            TreeFallsPlugin.getInstance().getLogger()
                    .warning("Failed to get loot table for block " + blockData.getMaterial().getKey().getKey());
        }
    }
}
