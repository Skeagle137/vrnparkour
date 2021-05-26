package net.skeagle.vrnparkour.config;

import net.skeagle.vrnparkour.VRNparkour;
import net.skeagle.vrnparkour.utils.Utils;

public class ConfigManager {

    public ConfigManager() {
        VRNparkour.getInstance().getConfig().options().copyDefaults(true);
        VRNparkour.getInstance().saveConfig();
        for (final ConfigurableItem configurableItem : ConfigurableItem.values())
            if (!VRNparkour.getInstance().getConfig().contains("item." + configurableItem.name().toLowerCase())) {
                final String string = "item." + configurableItem.name().toLowerCase();
                VRNparkour.getInstance().getConfig().set(string, Utils.itemToConfig(string, configurableItem.get()));
            }
        VRNparkour.getInstance().saveConfig();
    }
}

