package net.skeagle.vrnparkour.config;

import net.skeagle.vrnlib.configmanager.ConfigManager;
import net.skeagle.vrnlib.configmanager.annotations.ConfigValue;
import net.skeagle.vrnparkour.VRNparkour;

public class Settings {

    @ConfigValue("max-path-size")
    public static int maxPathSize = 200;
    @ConfigValue("allow-checkpoint-skipping")
    public static boolean allowCheckpointSkipping = false;
    @ConfigValue("auto-save-interval")
    public static int autoSaveInterval = 10;

    private ConfigManager config;

    public Settings(final VRNparkour plugin) {
        config = new ConfigManager(plugin).register(this).saveDefaults().load();
    }

    private Settings() {
    }

    public ConfigManager get() {
        return config;
    }
}
