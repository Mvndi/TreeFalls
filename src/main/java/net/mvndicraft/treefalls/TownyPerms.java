package net.mvndicraft.treefalls;

import com.palmergames.bukkit.towny.object.TownyPermission.ActionType;
import com.palmergames.bukkit.towny.utils.PlayerCacheUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class TownyPerms {
    private TownyPerms() {}
    public static boolean canBreak(Player player, Location location, Material material) {
        return PlayerCacheUtil.getCachePermission(player, location, material, ActionType.DESTROY);
    }
}
