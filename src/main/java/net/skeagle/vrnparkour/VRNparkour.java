package net.skeagle.vrnparkour;

import net.skeagle.vrnlib.commandmanager.CommandHook;
import net.skeagle.vrnlib.commandmanager.Messages;
import net.skeagle.vrnparkour.command.ParkourCommands;
import net.skeagle.vrnparkour.command.SnakeCommands;
import net.skeagle.vrnparkour.config.ConfigManager;
import net.skeagle.vrnparkour.hologram.HologramManager;
import net.skeagle.vrnparkour.parkour.Parkour;
import net.skeagle.vrnparkour.parkour.ParkourManager;
import net.skeagle.vrnparkour.snake.SnakeManager;
import net.skeagle.vrnparkour.snake.Track;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import net.skeagle.vrnlib.commandmanager.ArgType;
import net.skeagle.vrnlib.commandmanager.CommandParser;
import net.skeagle.vrnlib.misc.Task;
import net.skeagle.vrnlib.sql.SQLHelper;

import static net.skeagle.vrnparkour.utils.Utils.say;
import static net.skeagle.vrnparkour.utils.Utils.sayActionBar;

public final class VRNparkour extends JavaPlugin {

    private SnakeManager snakeManager;
    private ParkourManager parkourManager;
    public static FileConfiguration config;
    private HologramManager hologramManager;
    private SQLHelper db;

    @Override
    public void onEnable() {
        Messages.load(this);
        this.saveConfig();
        config = this.getConfig();
        db = new SQLHelper(SQLHelper.openSQLite(this.getDataFolder().toPath().resolve("vrn_data.db")));
        db.execute("CREATE TABLE IF NOT EXISTS hologram (id STRING PRIMARY KEY, hologram STRING, location STRING);");
        db.execute("CREATE TABLE IF NOT EXISTS parkour (id INTEGER PRIMARY KEY AUTOINCREMENT, name STRING, ready BOOLEAN, failDetection STRING, failHeight INT, " +
                "start STRING, end STRING, failBlocks STRING, checkpoints STRING, leaderboard STRING);");
        db.execute("CREATE TABLE IF NOT EXISTS snake (id INTEGER PRIMARY KEY AUTOINCREMENT, name STRING, running BOOLEAN, airItem STRING, direction BOOLEAN, speed INT, " +
                "heads STRING, path STRING);");
        new ConfigManager();
        snakeManager = new SnakeManager();
        parkourManager = new ParkourManager();
        hologramManager = new HologramManager();
        parkourManager.load();
        Task.syncRepeating(this, () ->
                parkourManager.getParkourList().forEach(pk -> pk.getEditMode().forEach(uuid -> {
                    Player p = Bukkit.getPlayer(uuid);
                    if (p != null && p.isOnline())
                        sayActionBar(p, Messages.msg("editActionBar").replaceAll("%name%", pk.getName()));
                })), 0L, 20L);
        ArgType<Parkour> parkourType = new ArgType<>("parkour", parkourManager::getParkour)
                .tabStream(t -> parkourManager.getParkourList().stream().map(Parkour::getName));
        ArgType<Track> snakeType = new ArgType<>("snake", snakeManager::getTrack)
                .tabStream(t -> snakeManager.getTracks().stream().map(Track::getName));
        new CommandParser(this.getResource("commands.txt"))
                .setArgTypes(parkourType, snakeType)
                .parse().register("vrnparkour", new ParkourCommands(), new SnakeCommands(), this);
    }

    public SnakeManager getSnakeManager() {
        return this.snakeManager;
    }

    public ParkourManager getParkourManager() {
        return this.parkourManager;
    }

    public HologramManager getHologramManager() {
        return this.hologramManager;
    }

    public static VRNparkour getInstance() {
        return VRNparkour.getPlugin(VRNparkour.class);
    }

    public SQLHelper getDB() {
        return db;
    }

    @CommandHook("reload")
    public void onReload(final CommandSender sender) {
        Messages.load(this);
        VRNparkour.getInstance().reloadConfig();
        say(sender, Messages.msg("reloaded"));
    }
}
