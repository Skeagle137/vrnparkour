package net.skeagle.vrnparkour.config;

import net.skeagle.vrnlib.itemutils.ItemBuilder;
import net.skeagle.vrnparkour.VRNparkour;
import net.skeagle.vrnparkour.utils.Utils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum ConfigurableItem {
    STOP(Material.RED_WOOL, 1, "&4Stop", new String[] { "" }),
    SPEED(Material.SUGAR, 1, "&a&lDelay %speed%", new String[] { "", "&aLeft click to decrease by 1", "&aRight click to increase by 1", "&aShift-left click to decrease by 10", "&aShift-right click to increase by 10" }),
    START(Material.LIME_WOOL, 1, "&aStart", new String[] { "" }),
    AIRBLOCK(Material.BARRIER, 1, "&b&lAir block", new String[] { "", "&bPut a block here to use as air block", "&cPress Q to have no air block" }),
    DIRECTION(Material.COMPASS, 1, "&e&lDirection", new String[] { "", "&eClick to change direction" }),
    DELETE_SNAKE(Material.BARRIER, 1, "&4&lDelete", new String[] { "", "&cLeft click to delete this snake entirely", "&cRight click to remove this snake from config" }),
    FAIL_HEIGHT(Material.LADDER, 1, "&a&lFail height %height%", new String[] { "&aLeft click to increase by 1", "&aRight click to decrease by 1", "&aShift-left click to increase by 10", "&aShift-right click to decrease by 10" }),
    FAIL_DETECT(Material.PAPER, 1, "&a&lFail detect method", new String[] { "" }),
    FLOOR_BLOCKS(Material.GRASS_BLOCK, 1, "&a&lFloor blocks", new String[] { "", "&aEdit the list of floor blocks" }),
    PARKOUR_READY(Material.LIME_DYE, 1, "&a&lSet ready", new String[] { "&aMark this parkour as ready for usage" }),
    DELETE_PARKOUR(Material.BARRIER, 1, "&4&lDelete", new String[] { "", "&cLeft click to delete this parkour" }),
    PARKOUR_UNREADY(Material.GRAY_DYE, 1, "&c&lUnready", new String[] { "", "&cMark this parkour as not ready" });

    private final Material type;
    private final int amount;
    private final String name;
    private final String[] lore;

    ConfigurableItem(final Material type, final int amount, final String name, final String[] lore) {
        this.type = type;
        this.amount = amount;
        this.name = name;
        this.lore = lore;
    }

    public ItemStack get(final String... array) {
        if (array.length % 2 != 0)
            throw new RuntimeException("Uneven amount of arguments passed to message!");
        String s = this.name;
        for (int i = 0; i < array.length; i += 2)
            s = s.replaceAll(array[i], array[i + 1]);
        if (VRNparkour.config.getConfigurationSection("item." + this.name().toLowerCase()) == null)
            return new ItemBuilder(this.type).setCount(this.amount).setName(s).setLore(this.lore);
        return Utils.configToItem("item." + this.name().toLowerCase(), array);
    }
}
