package net.skeagle.vrnparkour.parkour.faildetection;

import net.skeagle.vrnparkour.parkour.Parkour;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class FloorFailDetection implements FailDetection {
    @Override
    public boolean check(final Parkour parkour, final Player player) {
        final Material type = player.getLocation().clone().subtract(0.0, 1.0, 0.0).getBlock().getType();
        for (ItemStack i : parkour.getFailBlocks())
            if (type == i.getType())
                return false;
        return true;
    }

    @Override
    public String key() {
        return "floor";
    }
}
