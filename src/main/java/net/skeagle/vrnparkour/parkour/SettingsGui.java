package net.skeagle.vrnparkour.parkour;

import net.skeagle.vrnlib.commandmanager.Messages;
import net.skeagle.vrnparkour.VRNparkour;
import net.skeagle.vrnparkour.config.ConfigurableItem;
import net.skeagle.vrnparkour.parkour.faildetection.FailDetection;
import net.skeagle.vrnparkour.utils.ItemUtil;
import net.skeagle.vrnparkour.utils.Utils;
import net.skeagle.vrnparkour.utils.guiholders.FailblockGuiHolder;
import net.skeagle.vrnparkour.utils.guiholders.SettingsGuiHolder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static net.skeagle.vrnparkour.utils.Utils.color;
import static net.skeagle.vrnparkour.utils.Utils.say;

public class SettingsGui implements Listener {

    private final Parkour parkour;
    private final SettingsGuiHolder holder;
    private final Inventory gui;
    private final Inventory failBlocksGui;
    private ItemStack failHeight;
    private ItemStack failDetect;
    private ItemStack failBlocks;
    private ItemStack delete;
    private ItemStack ready;

    public SettingsGui(final Parkour parkour) {
        this.failBlocksGui = Bukkit.createInventory(new FailblockGuiHolder(parkour), 54, Messages.msg("failBlockGuiTitle").replaceAll("%name%", parkour.getName()));
        this.gui = Bukkit.createInventory(new SettingsGuiHolder(), 27, Messages.msg("parkourGuiTitle").replaceAll("%name%", parkour.getName()));
        this.failBlocksGui.setContents(parkour.getFailBlocks().toArray(new ItemStack[0]));
        Bukkit.getPluginManager().registerEvents(this, VRNparkour.getInstance());
        this.holder = (SettingsGuiHolder) this.gui.getHolder();
        this.parkour = parkour;
    }

    public void loadGui() {
        this.gui.clear();
        this.failBlocks = ConfigurableItem.FLOOR_BLOCKS.get();
        this.ready = (this.parkour.isReady() ? ConfigurableItem.PARKOUR_UNREADY.get() : ConfigurableItem.PARKOUR_READY.get());
        this.delete = ConfigurableItem.DELETE_PARKOUR.get();
        final int height = this.parkour.getFailHeight();
        final int n = Math.min(height, 64);
        failHeight = ItemUtil.genItem(ConfigurableItem.FAIL_HEIGHT.get("%height%", this.parkour.getFailHeight() + "")).amount((n > 0) ? n : 1).build();
        final List<String> lore = new ArrayList<>();
        lore.add("");
        for (final FailDetection failDetection : VRNparkour.getInstance().getParkourManager().getFailDetectionTypes()) {
            if (this.parkour.getFailDetection().equals(failDetection))
                lore.add(color("&a&l" + failDetection.key()));
            else
                lore.add(color("&a" + failDetection.key()));
        }
        failDetect = ItemUtil.genItem(ConfigurableItem.FAIL_DETECT.get()).lore(lore).build();
        this.gui.setItem(10, this.failHeight);
        this.gui.setItem(12, this.failDetect);
        this.gui.setItem(14, this.failBlocks);
        this.gui.setItem(16, this.ready);
        this.gui.setItem(22, this.delete);
    }

    @EventHandler
    public void inventoryClickEvent(final InventoryClickEvent inventoryClickEvent) {
        if (inventoryClickEvent.getClickedInventory() == null || inventoryClickEvent.getInventory().getHolder() != this.holder)
            return;
        final ItemStack currentItem = inventoryClickEvent.getCurrentItem();
        if (currentItem == null)
            return;
        if (currentItem.equals(this.failHeight)) {
            if (inventoryClickEvent.getClick() == ClickType.LEFT)
                this.parkour.setFailHeight(this.parkour.getFailHeight() + 1);
            else if (inventoryClickEvent.getClick() == ClickType.RIGHT)
                this.parkour.setFailHeight(this.parkour.getFailHeight() - 1);
            else if (inventoryClickEvent.getClick() == ClickType.SHIFT_LEFT)
                this.parkour.setFailHeight(this.parkour.getFailHeight() + 10);
            else if (inventoryClickEvent.getClick() == ClickType.SHIFT_RIGHT)
                this.parkour.setFailHeight(this.parkour.getFailHeight() - 10);
            inventoryClickEvent.setCancelled(true);
        }
        if (currentItem.equals(this.failDetect)) {
            int index = VRNparkour.getInstance().getParkourManager().getFailDetectionTypes().indexOf(this.parkour.getFailDetection());
            if (++index >= VRNparkour.getInstance().getParkourManager().getFailDetectionTypes().size())
                index = 0;
            this.parkour.setFailDetection(VRNparkour.getInstance().getParkourManager().getFailDetectionTypes().get(index));
            inventoryClickEvent.setCancelled(true);
        }
        if (currentItem.equals(this.failBlocks)) {
            inventoryClickEvent.getWhoClicked().openInventory(this.failBlocksGui);
            inventoryClickEvent.setCancelled(true);
        }
        if (currentItem.equals(this.ready)) {
            if (!this.parkour.setReady(!this.parkour.isReady()))
                say(inventoryClickEvent.getWhoClicked(), Messages.msg("parkourReadyFail"));
            inventoryClickEvent.setCancelled(true);
        }
        if (currentItem.equals(this.delete))
            VRNparkour.getInstance().getParkourManager().deleteParkour(this.parkour);
        this.loadGui();
    }

    @EventHandler
    public void inventoryClose(final InventoryCloseEvent inventoryCloseEvent) {
        if (!(inventoryCloseEvent.getInventory().getHolder() instanceof FailblockGuiHolder)) {
            parkour.save();
            return;
        }
        final Parkour parkour = ((FailblockGuiHolder)inventoryCloseEvent.getInventory().getHolder()).getParkour();
        final List<ItemStack> failBlocks = new ArrayList<>();
        for (final ItemStack itemStack : inventoryCloseEvent.getInventory().getContents()) {
                try {
                    Utils.gson.toJson(itemStack.serialize());
                }
                catch (Exception ex) {
                    break;
                }
                if (itemStack.getType() != Material.AIR)
                    failBlocks.add(itemStack);
        }
        parkour.setFailBlocks(failBlocks);
        parkour.save();
    }

    public void open(final Player player) {
        player.openInventory(gui);
    }
}
