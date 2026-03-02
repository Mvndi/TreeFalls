package net.mvndicraft.treefalls;

import java.util.List;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import org.bukkit.block.Block;

public class CoreProtectPerms {
    private CoreProtectPerms() {}

    public static boolean isNaturalBlock(Block block) {
        CoreProtectAPI coreProtectAPI = CoreProtect.getInstance().getAPI();
        List<String[]> actionList = coreProtectAPI.blockLookup(block, 0);

        if (actionList.isEmpty()) {
            return true;
        }

        CoreProtectAPI.ParseResult latestActionResult = coreProtectAPI.parseResult(actionList.get(0));

        // not a player action -> natural
        // not a place block action -> natural
        // Does not match the block type -> natural
        return latestActionResult.getPlayer().isEmpty() || latestActionResult.getActionId() != 1
                || latestActionResult.getType() != block.getType();
    }
}
