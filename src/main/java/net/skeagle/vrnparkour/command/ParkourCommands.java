package net.skeagle.vrnparkour.command;

import net.skeagle.vrnlib.commandmanager.Messages;
import net.skeagle.vrnparkour.VRNparkour;
import net.skeagle.vrnparkour.parkour.Parkour;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.skeagle.vrnlib.commandmanager.CommandHook;

import java.util.*;

import static net.skeagle.vrnparkour.utils.Utils.*;

public class ParkourCommands {

    @CommandHook("exit")
    public void onExit(final Player player) {
        VRNparkour.getInstance().getParkourManager().getParkourList().forEach(parkour -> {
            if (parkour.inParkour().contains(player.getUniqueId())) {
                parkour.failPlayer(player, parkour.getStart().getLocation().clone().add(1.0, 0.0, 0.0));
                parkour.remove(player);
                say(player, Messages.msg("parkourExit"));
            }
        });
    }

    @CommandHook("record")
    public void onRecord(final Player player, final Parkour parkour) {
        final long longValue = parkour.getLeaderboard().getBest(player.getUniqueId());
        if (longValue <= -1L)
            say(player, Messages.msg("parkourTimeNotSet"));
        else
            say(player, Messages.msg("parkourBestTime").replaceAll("%time%", parkour.getLeaderboard().formatTime(longValue)));
    }

    @CommandHook("leaderboard")
    public void leaderboard(final CommandSender sender, final Parkour parkour) {
        int n = 0;
        sayNoPrefix(sender, Messages.msg("leaderboardHeader").replaceAll("%parkour%", parkour.getName()));
        for (Map.Entry<UUID, Long> time : parkour.getLeaderboard().getTimes().entrySet()) {
            if (n > 10)
                break;
            ++n;
            sayNoPrefix(sender, Messages.msg("leaderboardEntry")
                    .replaceAll("%entry%", String.valueOf(n))
                    .replaceAll("%name%", Objects.requireNonNull(Bukkit.getOfflinePlayer(time.getKey()).getName()))
                    .replaceAll("%time%", String.valueOf((long)time.getValue())));
        }
    }

    @CommandHook("listparkours")
    public void onList(final CommandSender sender) {
        sayNoPrefix(sender, Messages.msg("parkourListHeader"));
        VRNparkour.getInstance().getParkourManager().getParkourList().forEach(parkour -> sayNoPrefix(sender, parkour.getName()));
    }

    @CommandHook("stopedit")
    public void onStopEdit(final Player player) {
        VRNparkour.getInstance().getParkourManager().getParkourList().forEach(parkour -> parkour.stopEditMode(player.getUniqueId()));
        say(player, Messages.msg("stoppedEdit"));
    }

    @CommandHook("edit")
    public void onEdit(final Player player, final Parkour parkour) {
        for (final Parkour pk : VRNparkour.getInstance().getParkourManager().getParkourList()) {
            if (pk.getEditMode().contains(player.getUniqueId())) {
                if (pk.equals(parkour)) {
                    say(player, Messages.msg("stoppedEdit"));
                    pk.stopEditMode(player.getUniqueId());
                    return;
                }
                say(player, Messages.msg("alreadyEditing"));
                return;
            }
        }
        if (parkour.isReady()) {
            say(player, Messages.msg("alreadyActive"));
            return;
        }
        say(player, Messages.msg("startedEdit").replaceAll("%name%", parkour.getName()));
        parkour.startEditMode(player.getUniqueId());
    }

    @CommandHook("createparkour")
    public void onCreate(final Player player, final String s) {
        if (VRNparkour.getInstance().getParkourManager().getParkour(s) != null) {
            say(player, Messages.msg("parkourNameInUse").replaceAll("%name%", s));
            return;
        }
        say(player, Messages.msg("newParkour").replaceAll("%name%", VRNparkour.getInstance().getParkourManager().createParkour(s).getName()));
    }

    @CommandHook("parkoursettings")
    public void onSettings(final Player player, final Parkour parkour) {
        parkour.getSettingsGui().open(player);
    }
}
