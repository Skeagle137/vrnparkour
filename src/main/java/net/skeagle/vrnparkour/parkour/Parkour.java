package net.skeagle.vrnparkour.parkour;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.skeagle.vrnlib.commandmanager.Messages;
import net.skeagle.vrnlib.misc.Task;
import net.skeagle.vrnparkour.VRNparkour;
import net.skeagle.vrnparkour.parkour.faildetection.FailDetection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import net.skeagle.vrnlib.sql.SQLHelper;

import java.util.*;

import static net.skeagle.vrnparkour.utils.Utils.*;

public class Parkour implements Listener {
    private final String name;
    private boolean ready;
    private int failHeight;
    List<UUID> editMode;
    final SettingsGui settingsGui;
    private List<ItemStack> failBlocks;
    private FailDetection failDetection;
    private ParkourCheckpoint start;
    private ParkourCheckpoint end;
    Map<UUID, Integer> inParkour;
    final Map<UUID, Long> parkourTimer;
    private final ParkourLeaderboard leaderboard;
    private final List<ParkourCheckpoint> checkPoints;

    public Parkour(final String s) {
        this(s, VRNparkour.getInstance().getParkourManager().getFailDetection("height"), false, 0, null, null, new ArrayList<>(), new ArrayList<>());
    }

    public Parkour(final String name, final FailDetection failDetection, final boolean ready, final int failHeight, final ParkourCheckpoint start, final ParkourCheckpoint end, final List<ParkourCheckpoint> checkPoints, final List<ItemStack> failBlocks) {
        this.end = end;
        this.name = name;
        this.ready = ready;
        this.start = start;
        this.failHeight = failHeight;
        this.failBlocks = failBlocks;
        this.failDetection = failDetection;
        this.checkPoints = checkPoints;
        this.leaderboard = new ParkourLeaderboard(this);
        this.editMode = new ArrayList<>();
        this.inParkour = new HashMap<>();
        this.parkourTimer = new HashMap<>();
        (this.settingsGui = new SettingsGui(this)).loadGui();
    }

    @EventHandler
    public void blockPlace(final BlockPlaceEvent blockPlaceEvent) {
        if (!this.editMode.contains(blockPlaceEvent.getPlayer().getUniqueId())) {
            return;
        }
        if (blockPlaceEvent.getBlock().getType() != Material.LIGHT_WEIGHTED_PRESSURE_PLATE) {
            return;
        }
        final Location location = blockPlaceEvent.getBlock().getLocation();
        if (this.start == null) {
            this.start = new ParkourCheckpoint(location, VRNparkour.getInstance().getHologramManager().createHologram(location, Messages.msg("checkpointStart")));
        }
        else {
            if (this.end != null) {
                this.addCheckpoint(this.end);
                this.end.getHologram().setHologram(Messages.msg("checkpoint").replaceAll("%checkpoint%", this.getCheckPoints().size() + ""));
            }
            this.end = new ParkourCheckpoint(location, VRNparkour.getInstance().getHologramManager().createHologram(location, Messages.msg("checkpointEnd")));
        }
    }

    @EventHandler
    public void pistonExtendEvent(final BlockPistonExtendEvent blockPistonExtendEvent) {
        for (final Block block : blockPistonExtendEvent.getBlocks()) {
            if (this.getStart() != null && (this.getStart().getLocation().equals(block.getLocation()) || this.getStart().getLocation().clone().subtract(0.0, 1.0, 0.0).equals(block.getLocation())))
                blockPistonExtendEvent.setCancelled(true);
            if (this.getEnd() != null && (this.getEnd().getLocation().equals(block.getLocation()) || this.getEnd().getLocation().clone().subtract(0.0, 1.0, 0.0).equals(block.getLocation())))
                blockPistonExtendEvent.setCancelled(true);
            this.getCheckPoints().forEach(parkourCheckpoint -> {
                if (parkourCheckpoint.getLocation().equals(block.getLocation()) || parkourCheckpoint.getLocation().clone().subtract(0.0, 1.0, 0.0).equals(block.getLocation()))
                    blockPistonExtendEvent.setCancelled(true);
            });
        }
    }

    @EventHandler
    public void pistonRetractEvent(final BlockPistonRetractEvent blockPistonRetractEvent) {
        for (final Block block : blockPistonRetractEvent.getBlocks()) {
            if (this.getStart() != null && (this.getStart().getLocation().equals(block.getLocation()) || this.getStart().getLocation().clone().subtract(0.0, 1.0, 0.0).equals(block.getLocation())))
                blockPistonRetractEvent.setCancelled(true);
            if (this.getEnd() != null && (this.getEnd().getLocation().equals(block.getLocation()) || this.getEnd().getLocation().clone().subtract(0.0, 1.0, 0.0).equals(block.getLocation())))
                blockPistonRetractEvent.setCancelled(true);
            this.getCheckPoints().forEach(parkourCheckpoint -> {
                if (parkourCheckpoint.getLocation().equals(block.getLocation()) || parkourCheckpoint.getLocation().clone().subtract(0.0, 1.0, 0.0).equals(block.getLocation()))
                    blockPistonRetractEvent.setCancelled(true);
            });
        }
    }

    @EventHandler
    public void blockBreak(final BlockBreakEvent blockBreakEvent) {
        final Block block = blockBreakEvent.getBlock();
        if (!this.editMode.contains(blockBreakEvent.getPlayer().getUniqueId())) {
            if (this.getStart() != null && (this.getStart().getLocation().equals(block.getLocation()) || this.getStart().getLocation().clone().subtract(0.0, 1.0, 0.0).equals(block.getLocation())))
                blockBreakEvent.setCancelled(true);
            if (this.getEnd() != null && (this.getEnd().getLocation().equals(block.getLocation()) || this.getEnd().getLocation().clone().subtract(0.0, 1.0, 0.0).equals(block.getLocation())))
                blockBreakEvent.setCancelled(true);
            this.getCheckPoints().forEach(parkourCheckpoint3 -> {
                if (parkourCheckpoint3.getLocation().equals(block.getLocation()) || parkourCheckpoint3.getLocation().clone().subtract(0.0, 1.0, 0.0).equals(block.getLocation()))
                    blockBreakEvent.setCancelled(true);
            });
            return;
        }
        if (blockBreakEvent.getBlock().getType() != Material.LIGHT_WEIGHTED_PRESSURE_PLATE)
            return;
        if (this.getStart() != null && block.getLocation().equals(this.getStart().getLocation())) {
            if (this.getCheckPoints().size() == 0) {
                VRNparkour.getInstance().getHologramManager().removeHologram(this.getStart().getHologram().getId());
                this.start = null;
            }
            else {
                VRNparkour.getInstance().getHologramManager().removeHologram(this.getStart().getHologram().getId());
                this.setStart(this.getCheckPoints().get(0));
                this.getCheckPoints().remove(0);
                this.getStart().getHologram().setHologram(Messages.msg("checkpointStart"));
            }
        }
        else if (this.getEnd() != null && block.getLocation().equals(this.getEnd().getLocation())) {
            if (this.getCheckPoints().size() == 0) {
                VRNparkour.getInstance().getHologramManager().removeHologram(this.getEnd().getHologram().getId());
                this.end = null;
            }
            else {
                VRNparkour.getInstance().getHologramManager().removeHologram(this.getEnd().getHologram().getId());
                final ParkourCheckpoint end = this.getCheckPoints().get(this.getCheckPoints().size() - 1);
                this.getCheckPoints().remove(end);
                this.setEnd(end);
                this.getEnd().getHologram().setHologram(Messages.msg("checkpointEnd"));
            }
        }
        else {
            final ParkourCheckpoint parkourCheckpoint4 = this.getCheckPoints().stream().filter(parkourCheckpoint -> parkourCheckpoint.getLocation().equals(block.getLocation())).findFirst().orElse(null);
            if (parkourCheckpoint4 == null)
                return;
            VRNparkour.getInstance().getHologramManager().removeHologram(parkourCheckpoint4.getHologram().getId());
            this.getCheckPoints().remove(parkourCheckpoint4);
            int n;
            for (ParkourCheckpoint pc : getCheckPoints()) {
                n = this.getCheckPoints().indexOf(pc) + 1;
                pc.getHologram().setHologram(Messages.msg("checkpoint").replaceAll("%checkpoint%", n + ""));
            }
        }
    }

    @EventHandler
    public void playerMoveEvent(final PlayerMoveEvent playerMoveEvent) {
        if (!this.isReady())
            return;
        final Player player = playerMoveEvent.getPlayer();
        final Block block = player.getLocation().getBlock();
        if (this.inParkour.containsKey(player.getUniqueId())) {
            if (!this.failDetection.check(this, player)) {
                this.failPlayer(player);
                return;
            }
            for (final ParkourCheckpoint parkourCheckpoint : this.getCheckPoints()) {
                if (parkourCheckpoint.getLocation().equals(block.getLocation())) {
                    final int index = this.getCheckPoints().indexOf(parkourCheckpoint);
                    if (index > this.inParkour.get(player.getUniqueId())) {
                        if (VRNparkour.getInstance().getConfig().getBoolean("allowCheckpointSkipping")) {
                            this.inParkour.put(player.getUniqueId(), index);
                            sayNoPrefix(player, Messages.msg("checkpointReached").replaceAll("%checkpoint%", index + 1 + ""));
                        }
                        else if (index == this.inParkour.get(player.getUniqueId()) + 1) {
                            this.inParkour.put(player.getUniqueId(), index);
                            sayNoPrefix(player, Messages.msg("checkpointReached").replaceAll("%checkpoint%", index + 1 + ""));
                        }
                        else {
                            this.failPlayer(player);
                            sayNoPrefix(player, Messages.msg("checkpointSkipped"));
                        }
                    }
                    return;
                }
                if (this.end.getLocation().equals(block.getLocation())) {
                    this.inParkour.remove(player.getUniqueId());
                    final long n = System.currentTimeMillis() - this.parkourTimer.get(player.getUniqueId());
                    long longValue = this.getLeaderboard().getBest(player.getUniqueId());
                    if (longValue <= -1L)
                        longValue = 99999999999999L;
                    this.getLeaderboard().saveTime(player.getUniqueId(), n);
                    final long min = Math.min(n, longValue);
                    System.out.println();
                    save();
                    if (min < longValue)
                        say(player, Messages.msg("parkourFinishNewRecord").replaceAll("%time%", this.getLeaderboard().formatTime(min)));
                    else
                        say(player, Messages.msg("parkourFinish").replaceAll("%time%", this.getLeaderboard().formatTime(n)));
                }
            }
        }
        else {
            for (Parkour p : VRNparkour.getInstance().getParkourManager().getParkourList())
                if (p.inParkour().contains(player.getUniqueId()))
                    return;
            if (this.start.getLocation().equals(block.getLocation())) {
                this.inParkour.put(player.getUniqueId(), -1);
                this.parkourTimer.put(player.getUniqueId(), System.currentTimeMillis());
                say(player, Messages.msg("parkourStart"));
            }
        }
    }

    @EventHandler
    public void playerDamageEvent(final EntityDamageEvent entityDamageEvent) {
        if (entityDamageEvent.getEntity() instanceof Player && this.inParkour().contains(entityDamageEvent.getEntity().getUniqueId()) && entityDamageEvent.getCause() == EntityDamageEvent.DamageCause.FALL)
            entityDamageEvent.setCancelled(true);
    }

    private void failPlayer(final Player player) {
        final int intValue = this.inParkour.get(player.getUniqueId());
        this.failPlayer(player, (intValue >= 0) ? this.checkPoints.get(intValue).getLocation() : this.start.getLocation());
    }

    public void failPlayer(final Player player, final Location location) {
        player.setFallDistance(0.0f);
        final Location location2 = player.getLocation();
        location2.setX(location.getX() + 0.5);
        location2.setY(location.getY());
        location2.setZ(location.getZ() + 0.5);
        player.teleport(location2);
    }

    public String getName() {
        return this.name;
    }

    public boolean isReady() {
        return this.ready;
    }

    public ParkourCheckpoint getEnd() {
        return this.end;
    }

    public int getFailHeight() {
        return this.failHeight;
    }

    public List<UUID> getEditMode() {
        return this.editMode;
    }

    public ParkourCheckpoint getStart() {
        return this.start;
    }

    public void remove(final Player player) {
        this.inParkour.remove(player.getUniqueId());
    }

    public SettingsGui getSettingsGui() {
        return this.settingsGui;
    }

    public List<ItemStack> getFailBlocks() {
        return this.failBlocks;
    }

    public FailDetection getFailDetection() {
        return this.failDetection;
    }

    public List<ParkourCheckpoint> getCheckPoints() {
        return this.checkPoints;
    }

    public List<UUID> inParkour() {
        return new ArrayList<>(this.inParkour.keySet());
    }

    public boolean setReady(final boolean ready) {
        if (this.start != null && this.end != null && this.editMode.size() == 0) {
            this.ready = ready;
            return true;
        }
        return false;
    }

    public void startEditMode(final UUID uuid) {
        this.editMode.add(uuid);
    }

    public void setEnd(final ParkourCheckpoint end) {
        this.end = end;
    }

    public ParkourLeaderboard getLeaderboard() {
        return this.leaderboard;
    }

    public void setStart(final ParkourCheckpoint start) {
        this.start = start;
    }

    public void setFailBlocks(final List<ItemStack> failBlocks) {
        this.failBlocks = failBlocks;
    }

    public void addCheckpoint(final ParkourCheckpoint parkourCheckpoint) {
        this.checkPoints.add(parkourCheckpoint);
    }

    public void setFailDetection(final FailDetection failDetection) {
        this.failDetection = failDetection;
    }

    public void setFailHeight(int failHeight) {
        failHeight = Math.max(failHeight, 0);
        this.failHeight = failHeight;
    }

    public void stopEditMode(final UUID uuid) {
        this.editMode.remove(uuid);
        Player p = Bukkit.getPlayer(uuid);
        if (p != null)
            sayActionBar(p, "");
    }

    public void save() {
        SQLHelper db = VRNparkour.getInstance().getDB();
        Task.asyncDelayed(() -> {
            db.execute("DELETE FROM parkour WHERE name = (?)", getName());
            List<String> checkpointlist = new ArrayList<>();
            getCheckPoints().forEach(pk -> checkpointlist.add(pk.serialize()));
            List<JsonObject> failBlocks = new ArrayList<>();
            final JsonObject json = new JsonObject();
            getFailBlocks().forEach(block -> json.add("item", new JsonParser().parse(gson.toJson(block.serialize()))));
            db.execute("INSERT INTO parkour " +
                            "(name, failDetection, failHeight, ready, start, end, failBlocks, checkpoints, leaderboard) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)", getName(), getFailDetection().key(), getFailHeight(), isReady(),
                    ((this.start != null) ? this.start.serialize() : null), ((this.end != null) ? this.end.serialize() : null),
                    gson.toJson(failBlocks), gson.toJson(checkpointlist), gson.toJson(leaderboard.serialize()));
        });
    }

    public void delete() {
        this.inParkour = new HashMap<>();
        this.editMode = new ArrayList<>();
        if (this.start != null)
            VRNparkour.getInstance().getHologramManager().removeHologram(this.start.getHologram().getId());
        if (this.end != null)
            VRNparkour.getInstance().getHologramManager().removeHologram(this.end.getHologram().getId());
        this.getCheckPoints().forEach(parkourCheckpoint -> VRNparkour.getInstance().getHologramManager().removeHologram(parkourCheckpoint.getHologram().getId()));
        PlayerMoveEvent.getHandlerList().unregister(this);
        BlockBreakEvent.getHandlerList().unregister(this);
        BlockPlaceEvent.getHandlerList().unregister(this);
        PlayerInteractEvent.getHandlerList().unregister(this);
    }
}
