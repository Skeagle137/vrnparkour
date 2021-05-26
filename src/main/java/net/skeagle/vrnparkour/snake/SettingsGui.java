package net.skeagle.vrnparkour.snake;

import net.skeagle.vrnlib.commandmanager.Messages;
import net.skeagle.vrnparkour.VRNparkour;
import net.skeagle.vrnparkour.config.ConfigurableItem;
import net.skeagle.vrnparkour.utils.Utils;
import net.skeagle.vrnparkour.utils.guiholders.SettingsGuiHolder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import net.skeagle.vrnlib.misc.Task;

public class SettingsGui implements Listener {
    private final transient Track track;
    private final SettingsGuiHolder holder;
    private final Inventory gui;
    private ItemStack speed;
    private ItemStack airBlock;
    private ItemStack direction;
    private ItemStack delete;
    private ItemStack start;
    private ItemStack stop;

    public SettingsGui(final Track track) {
        this.gui = Bukkit.createInventory(new SettingsGuiHolder(), 27, Messages.msg("snakeGuiTitle").replaceAll("%name%", track.getName()));
        Bukkit.getPluginManager().registerEvents(this, VRNparkour.getInstance());
        this.holder = (SettingsGuiHolder) this.gui.getHolder();
        this.track = track;
    }

    public void loadGui() {
        this.gui.clear();
        this.stop = ConfigurableItem.STOP.get();
        this.start = ConfigurableItem.START.get();
        this.speed = ConfigurableItem.SPEED.get("%speed%", this.track.getSpeed() + "");
        this.delete = ConfigurableItem.DELETE_SNAKE.get();
        this.airBlock = ConfigurableItem.AIRBLOCK.get();
        this.direction = ConfigurableItem.DIRECTION.get();
        this.airBlock.setType((this.track.getAirItem().getType() != Material.AIR) ? this.track.getAirItem().getType() : Material.BARRIER);
        this.speed.setAmount(this.track.getSpeed());
        this.gui.setItem(10, this.speed);
        this.gui.setItem(16, this.delete);
        this.gui.setItem(12, this.airBlock);
        this.gui.setItem(14, this.direction);
        if (this.track.isRunning())
            this.gui.setItem(22, this.stop);
        else
            this.gui.setItem(22, this.start);
    }

    @EventHandler
    public void inventoryClickEvent(final InventoryClickEvent inventoryClickEvent) {
        if (inventoryClickEvent.getClickedInventory() == null || inventoryClickEvent.getClickedInventory().getHolder() != this.holder)
            return;
        final ItemStack currentItem = inventoryClickEvent.getCurrentItem();
        if (currentItem == null)
            return;
        if (currentItem.equals(this.airBlock)) {
            if (inventoryClickEvent.getAction() == InventoryAction.DROP_ONE_SLOT) {
                this.track.setAirItem(new ItemStack(Material.AIR));
                this.track.reset();
            }
            else if (inventoryClickEvent.getCursor() != null && inventoryClickEvent.getCursor().getType() != Material.AIR) {
                final Material type = inventoryClickEvent.getCursor().getType();
                final String lowerCase = type.name().toLowerCase();
                try {
                    Utils.gson.toJson(inventoryClickEvent.getCursor().serialize());
                }
                catch (Exception ex) {
                    return;
                }
                if (!type.isBlock() || type.hasGravity() || lowerCase.contains("door") || lowerCase.contains("plate") || lowerCase.contains("leaves") || lowerCase.contains("sign") || lowerCase.contains("banner") || lowerCase.contains("bed") || lowerCase.contains("coral") || lowerCase.contains("egg")) {
                    inventoryClickEvent.setCancelled(true);
                    return;
                }
                final ItemStack clone = inventoryClickEvent.getCursor().clone();
                this.track.setAirItem(clone);
                this.track.reset();
            }
        }
        if (currentItem.equals(this.direction) && inventoryClickEvent.getAction() == InventoryAction.PICKUP_ALL)
            this.track.setDirection(!this.track.getDirection());
        if (currentItem.equals(this.stop) && inventoryClickEvent.getAction() == InventoryAction.PICKUP_ALL)
            this.track.stop();
        if (currentItem.equals(this.delete)) {
            if (inventoryClickEvent.getAction() == InventoryAction.PICKUP_HALF) {
                VRNparkour.getInstance().getSnakeManager().deleteTrack(this.track, false);
                inventoryClickEvent.getWhoClicked().closeInventory();
                return;
            }
            if (inventoryClickEvent.getAction() == InventoryAction.PICKUP_ALL) {
                VRNparkour.getInstance().getSnakeManager().deleteTrack(this.track, true);
                inventoryClickEvent.getWhoClicked().closeInventory();
                return;
            }
        }
        if (currentItem.equals(this.start) && inventoryClickEvent.getAction() == InventoryAction.PICKUP_ALL)
            this.track.start();
        if (currentItem.equals(this.speed)) {
            if (inventoryClickEvent.getClick() == ClickType.LEFT)
                this.track.setSpeed(this.track.getSpeed() - 1);
            else if (inventoryClickEvent.getClick() == ClickType.RIGHT)
                this.track.setSpeed(this.track.getSpeed() + 1);
            else if (inventoryClickEvent.getClick() == ClickType.SHIFT_LEFT)
                this.track.setSpeed(this.track.getSpeed() - 10);
            else if (inventoryClickEvent.getClick() == ClickType.SHIFT_RIGHT)
                this.track.setSpeed(this.track.getSpeed() + 10);
        }
        inventoryClickEvent.setCancelled(true);
        Task.syncDelayed(SettingsGui.this::loadGui, 1L);
    }

    @EventHandler
    public void inventoryClose(final InventoryCloseEvent e) {
        track.save();
    }

    public void open(final Player p) {
        p.openInventory(gui);
    }
}

