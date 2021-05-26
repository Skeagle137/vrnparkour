package net.skeagle.vrnparkour.utils.guiholders;

import net.skeagle.vrnparkour.parkour.Parkour;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class FailblockGuiHolder implements InventoryHolder {
    private Parkour parkour;

    public FailblockGuiHolder(final Parkour parkour) {
        this.parkour = parkour;
    }

    public Parkour getParkour() {
        return this.parkour;
    }

    public Inventory getInventory() {
        return null;
    }
}
