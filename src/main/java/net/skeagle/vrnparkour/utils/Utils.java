package net.skeagle.vrnparkour.utils;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.skeagle.vrnlib.commandmanager.Messages;
import net.skeagle.vrnlib.itemutils.ItemBuilder;
import net.skeagle.vrnlib.misc.FormatUtils;
import net.skeagle.vrnparkour.VRNparkour;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Utils {

    public static Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();

    public static void say(final CommandSender cs, final String... message) {
        if (cs == null) return;
        for (final String msg : message)
            cs.sendMessage(FormatUtils.color(Messages.msg("prefix") + " " + msg));
    }

    public static void sayNoPrefix(final CommandSender cs, final String... message) {
        if (cs == null) return;
        for (final String msg : message) {
            cs.sendMessage(FormatUtils.color(msg));
        }
    }

    public static void sayActionBar(final Player p, final String msg) {
        p.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                TextComponent.fromLegacyText(FormatUtils.color(msg)));
    }

    public static String serializeLocation(final Location location) {
        if (location == null) {
            return null;
        }
        return location.getWorld().getName() + ";" + location.getX() + ";" + location.getY() + ";" + location.getZ();
    }

    public static Location deSerializeLocation(String replaceAll) {
        if (replaceAll == null) {
            return null;
        }
        replaceAll = replaceAll.replaceAll("\"", "");
        final String[] split = replaceAll.split(";");
        if (split.length != 4) {
            return null;
        }
        return new Location(Bukkit.getWorld(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]), Double.parseDouble(split[3]));
    }

    public static void setBlockType(final Block b, final ItemStack itemStack) {
        b.setType(itemStack.getType(), false);
    }

    public static ConfigurationSection itemToConfig(final String s, final ItemStack itemStack) {
        final ConfigurationSection section = VRNparkour.config.createSection(s);
        section.set("type", itemStack.getType().toString());
        section.set("amount", itemStack.getAmount());
        section.set("name", itemStack.getItemMeta().getDisplayName().replaceAll("§", "&"));
        final List<String> list = new ArrayList<>();
        itemStack.getItemMeta().getLore().forEach(s2 -> list.add(s2.replace("§", "&")));
        section.set("lore", list);
        return section;
    }

    public static ItemStack configToItem(final String s, final String... array) {
        if (array.length % 2 != 0)
            throw new RuntimeException("Uneven amount of arguments passed to message!");
        final ConfigurationSection configurationSection = VRNparkour.config.getConfigurationSection(s);
        String s3 = ChatColor.translateAlternateColorCodes('&', configurationSection.getString("name"));
        for (int i = 0; i < array.length; i += 2)
            s3 = s3.replace(array[i], array[i + 1]);
        final Material mat = Material.valueOf(configurationSection.getString("type"));
        final List<String> stringList = configurationSection.getStringList("lore");
        final int int1 = configurationSection.getInt("amount");
        final List<String> list = new ArrayList<>();
        stringList.forEach(s2 -> list.add(ChatColor.translateAlternateColorCodes('&', s2)));
        return new ItemBuilder(mat).setCount(int1).setName(s3).setLore(list.toArray(new String[0]));
    }
}
