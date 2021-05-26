package net.skeagle.vrnparkour.parkour.faildetection;

import net.skeagle.vrnparkour.parkour.Parkour;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ParkourFailDetection implements FailDetection {
    @Override
    public boolean check(final Parkour parkour, final Player player) {
        for (final ItemStack i : parkour.getFailBlocks()) {
            final Block block = player.getLocation().clone().subtract(0.0, 1.0, 0.0).getBlock();
            if (block.getType() == i.getType() || block.getType() == Material.AIR || block.getType() == Material.LIGHT_WEIGHTED_PRESSURE_PLATE)
                return true;
        }
        return false;
    }

    @Override
    public String key() {
        return "parkour";
    }
}
