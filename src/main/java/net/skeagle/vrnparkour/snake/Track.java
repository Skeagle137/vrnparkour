package net.skeagle.vrnparkour.snake;

import com.google.gson.*;
import net.skeagle.vrnlib.commandmanager.Messages;
import net.skeagle.vrnlib.misc.Task;
import net.skeagle.vrnparkour.VRNparkour;
import net.skeagle.vrnparkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import net.skeagle.vrnlib.sql.SQLHelper;

import java.util.ArrayList;
import java.util.List;

import static net.skeagle.vrnparkour.utils.Utils.sayNoPrefix;

public class Track implements Runnable, Listener {
    private Task task;
    private final Task editTask;
    public transient Gson gson;
    private boolean running;
    private int speed;
    private final String name;
    private boolean direction;
    private ItemStack airItem;
    private List<Location> path;
    SettingsGui settings;
    private List<SnakeHead> heads;

    public Track(final String s, final List<Location> list) {
        this(s, list, new ItemStack(Material.RED_MUSHROOM), 10, true);
    }

    public Track(final String name, final List<Location> path, final ItemStack airItem, final int speed, final boolean direction) {
        this.gson = Utils.gson;
        this.running = false;
        Bukkit.getPluginManager().registerEvents(this, VRNparkour.getInstance());
        this.path = path;
        this.name = name;
        this.heads = new ArrayList<>();
        this.airItem = airItem;
        this.settings = new SettingsGui(this);
        task = null;
        this.speed = speed;
        this.direction = direction;
        this.reset();
        this.settings.loadGui();
        editTask = Task.syncRepeating(() -> {
            if (!this.running) {
                this.heads.forEach(snakeHead -> Bukkit.getOnlinePlayers().forEach(player -> {
                    Location loc = path.get(snakeHead.getIndex());
                    player.spawnParticle(Particle.FLAME, loc.clone().add(0.5, 1.0, 0.5), 10, 0.0, 0.0, 0.0, 0.001);
                }));
            }
        }, 0L, 10L);
    }

    @Override
    public void run() {
        this.heads.forEach(SnakeHead::move);
    }

    public int getSpeed() {
        return this.speed;
    }

    public String getName() {
        return this.name;
    }

    public int getSize() {
        return this.path.size();
    }

    public boolean isRunning() {
        return this.running;
    }

    public List<Location> getPath() {
        return this.path;
    }

    public ItemStack getAirItem() {
        return this.airItem;
    }

    public boolean getDirection() {
        return this.direction;
    }

    public SettingsGui getSettings() {
        return this.settings;
    }

    public void addHead(final SnakeHead snakeHead) {
        this.heads.add(snakeHead);
    }

    public void setAirItem(final ItemStack airItem) {
        this.airItem = airItem;
    }

    public void setDirection(final boolean direction) {
        this.direction = direction;
        int i = 0;
        for (SnakeHead head : heads) {
            while (i < head.getLength()) {
                head.move();
                ++i;
            }
        }
        this.reset();
    }

    public void setSpeed(int speed) {
        speed = (Math.min(speed, 60));
        speed = (Math.max(speed, 1));
        this.speed = speed;
        if (!this.running)
            return;
        task.cancel();
        task = Task.syncRepeating(this, 0L, this.getSpeed());
    }

    public void start() {
        task = Task.syncRepeating(this, 0L, this.getSpeed());
        this.running = true;

    }

    public void stop() {
        task.cancel();
        this.running = false;
        task = null;
        this.reset();
    }

    public void reset() {
        this.path.forEach(location -> Utils.setBlockType(location.getBlock(), this.getAirItem()));
        int i = 0;
        int n;
        int n2;
        int n3;
        for (SnakeHead head : heads) {
            Utils.setBlockType(this.path.get(head.getIndex()).getBlock(), head.getItem());
            while (i < head.getLength()) {
                if (this.getDirection()) {
                    n = head.getIndex() - i;
                    n2 = ((n < 0) ? (this.path.size() - n * -1) : n);
                } else {
                    n3 = head.getIndex() + i;
                    n2 = ((n3 >= this.path.size()) ? (-(this.path.size() + -n3)) : n3);
                }
                Utils.setBlockType(this.path.get(n2).getBlock(), head.getItem());
                ++i;
            }
        }
    }

    public void delete(final boolean b) {
        if (b)
            for (Location loc : getPath())
                loc.getBlock().setType(Material.AIR);
        BlockPhysicsEvent.getHandlerList().unregister(this);
        BlockBreakEvent.getHandlerList().unregister(this);
        PlayerInteractEvent.getHandlerList().unregister(this);
        editTask.cancel();
        this.heads = null;
        this.path = null;
        task.cancel();
        this.running = false;
        task = null;
    }

    @EventHandler
    public void pistonExtendEvent(final BlockPistonExtendEvent e) {
        for (Block b : e.getBlocks())
            if (this.getPath().contains(b.getLocation()))
                e.setCancelled(true);
    }

    @EventHandler
    public void pistonRetractEvent(final BlockPistonRetractEvent e) {
        for (Block b : e.getBlocks())
            if (getPath().contains(b.getLocation())) {
                e.setCancelled(true);
                e.getBlock().getState().update(false, false);
            }
    }

    @EventHandler
    public void entityExplode(final EntityExplodeEvent e) {
        final ArrayList<Block> list = new ArrayList<>();
        for (final Block block2 : e.blockList())
            if (this.getPath().contains(block2.getLocation()))
                list.add(block2);
        list.forEach(block -> e.blockList().remove(block));
    }

    @EventHandler
    public void physicsUpdate(final BlockPhysicsEvent e) {
        if (this.path.contains(e.getBlock().getLocation())) {
            e.setCancelled(true);
            e.getBlock().getState().update(false, false);
        }
    }

    @EventHandler
    public void blockBreak(final BlockBreakEvent e) {
        if (this.path.contains(e.getBlock().getLocation())) {
            e.setCancelled(true);
            e.getBlock().getState().update(false, false);
        }
    }

    @EventHandler
    public void onSandFall(final EntityChangeBlockEvent e) {
        if (e.getEntity().getType() == EntityType.FALLING_BLOCK && e.getTo() == Material.AIR && e.getBlock().getType().name().contains("SAND") && this.getPath().contains(e.getBlock().getLocation())) {
            e.setCancelled(true);
            e.getBlock().getState().update(false, false);
        }
    }

    @EventHandler
    public void rightClick(final PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK && e.getAction() != Action.LEFT_CLICK_BLOCK)
            return;
        if (!this.path.contains(e.getClickedBlock().getLocation()))
            return;
        e.setCancelled(true);
        if (e.getHand() != EquipmentSlot.HAND || this.running)
            return;
        final Block clickedBlock = e.getClickedBlock();
        final Player player = e.getPlayer();
        if (!player.hasPermission("vrnparkour.snake.edit"))
            return;
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (clickedBlock.getType() == this.getAirItem().getType()) {
                final ItemStack clone = e.getPlayer().getEquipment().getItemInMainHand().clone();
                final Material type = clone.getType();
                final String lowerCase = type.name().toLowerCase();
                try {
                    Utils.gson.toJson(clone.serialize());
                } catch (Exception ex) {
                    return;
                }
                if (!type.isSolid() || !type.isBlock() || lowerCase.contains("door") || lowerCase.contains("plate") || lowerCase.contains("leaves") || lowerCase.contains("sign") || lowerCase.contains("banner") || lowerCase.contains("bed") || lowerCase.contains("coral") || lowerCase.contains("egg"))
                    return;
                final int index = this.path.indexOf(clickedBlock.getLocation());
                for (int i = 1; i <= 1; ++i) {
                    final int n = index - i;
                    if (this.path.get((n < 0) ? (this.getPath().size() - n * -1) : n).getBlock().getType() != this.getAirItem().getType())
                        return;
                }
                for (int j = 1; j <= 1; ++j) {
                    final int n2 = index + j;
                    if (this.path.get((n2 >= this.getPath().size()) ? (-(this.getPath().size() + -n2)) : n2).getBlock().getType() != this.getAirItem().getType())
                        return;
                }
                this.heads.add(new SnakeHead(index, clone, 1, this));
                this.reset();
                sayNoPrefix(player, Messages.msg("snakeHeadAdded"));
            } else {
                final SnakeHead snakeHead3 = this.heads.stream().filter(snakeHead -> this.path.get(snakeHead.getIndex()).equals(clickedBlock.getLocation())).findFirst().orElse(null);
                if (snakeHead3 == null) {
                    sayNoPrefix(player, Messages.msg("clickSnakeHead"));
                    return;
                }
                if (player.isSneaking()) {
                    snakeHead3.setLength(snakeHead3.getLength() - 1);
                    sayNoPrefix(player, Messages.msg("snakeHeadDecrease"));
                } else {
                    if (this.getPath().get(snakeHead3.getTailEnd(2)).getBlock().getType() != this.getAirItem().getType())
                        return;
                    if (this.getPath().get(snakeHead3.getTailEnd(1)).getBlock().getType() != this.getAirItem().getType())
                        return;
                    snakeHead3.setLength(snakeHead3.getLength() + 1);
                    sayNoPrefix(player, Messages.msg("snakeHeadIncrease"));
                }
                this.reset();
            }
        } else if (e.getAction() == Action.LEFT_CLICK_BLOCK && clickedBlock.getType() != this.getAirItem().getType()) {
            final SnakeHead snakeHead4 = this.heads.stream().filter(snakeHead2 -> this.path.get(snakeHead2.getIndex()).equals(clickedBlock.getLocation())).findFirst().orElse(null);
            if (snakeHead4 == null) {
                sayNoPrefix(player, Messages.msg("clickSnakeHead"));
                return;
            }
            this.heads.remove(snakeHead4);
            this.reset();
            sayNoPrefix(player, Messages.msg("snakeHeadRemoved"));
        }
    }

    public void save() {
        SQLHelper db = VRNparkour.getInstance().getDB();
        Task.asyncDelayed(() -> {
            db.execute("DELETE FROM snake WHERE name = (?)", name);
            List<String> path = new ArrayList<>();
            getPath().forEach(loc -> path.add(Utils.serializeLocation(loc)));
            List<JsonObject> headlist = new ArrayList<>();
            heads.forEach(h -> headlist.add(h.serialize()));
            db.execute("INSERT INTO snake " +
                            "(name, running, airItem, direction, speed, heads, path) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?)", getName(), isRunning(), gson.toJson(getAirItem().serialize()), getDirection(),
                    getSpeed(), gson.toJson(headlist), gson.toJson(path));
        });
    }
}

