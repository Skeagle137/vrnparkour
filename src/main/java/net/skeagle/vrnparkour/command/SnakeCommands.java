package net.skeagle.vrnparkour.command;

import net.skeagle.vrnlib.commandmanager.Messages;
import net.skeagle.vrnparkour.VRNparkour;
import net.skeagle.vrnparkour.snake.BlockCrawler;
import net.skeagle.vrnparkour.snake.Track;
import net.skeagle.vrnparkour.utils.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.skeagle.vrnlib.commandmanager.CommandHook;

import java.util.List;

import static net.skeagle.vrnparkour.utils.Utils.say;
import static net.skeagle.vrnparkour.utils.Utils.sayNoPrefix;

public class SnakeCommands {

    private final BlockCrawler crawler;

    public SnakeCommands() {
        this.crawler = new BlockCrawler();
    }

    @CommandHook("listsnakes")
    public void onList(final CommandSender sender) {
        if (VRNparkour.getInstance().getSnakeManager().getTracks().size() == 0)
            say(sender, Messages.msg("noSnakes"));
        else {
            sayNoPrefix(sender, Messages.msg("snakeListHeader"));
            VRNparkour.getInstance().getSnakeManager().getTracks().forEach(track -> sayNoPrefix(sender, "- " + track.getName()));
        }
    }

    @CommandHook("snakesettings")
    public void onSettings(final Player player, final Track track) {
        track.getSettings().open(player);
    }

    @CommandHook("createsnake")
    public void onCreate(final Player player, final String name) {
        if (VRNparkour.getInstance().getSnakeManager().getTrack(name) != null) {
            say(player, Messages.msg("snakeNameInUse").replaceAll("%name%", name));
            return;
        }
        final Location add = player.getLocation().clone().add(0.0, -1.0, 0.0);
        if (add.getBlock().getType() == Material.AIR) {
            say(player, Messages.msg("standOnBlock"));
            return;
        }
        List<Location> locations;
        try {
            locations = this.crawler.getLocations(player.getUniqueId(), add);
        }
        catch (BlockCrawler.CrawlerOverflowException ex2) {
            say(player, Messages.msg("calculationError"));
            return;
        }
        catch (BlockCrawler.InvalidPathException ex) {
            say(player, Messages.msg("invalidPath").replaceAll("%location%", Utils.serializeLocation(ex.getBlock().getLocation())));
            return;
        }
        if (locations.size() <= 0) {
            say(player, Messages.msg("error"));
            return;
        }
        VRNparkour.getInstance().getSnakeManager().createTrack(name, locations);
        say(player, Messages.msg("newSnake").replaceAll("%name%", name));
    }
}

