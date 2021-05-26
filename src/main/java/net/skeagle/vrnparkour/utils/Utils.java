package net.skeagle.vrnparkour.utils;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.skeagle.vrnlib.commandmanager.Messages;
import net.skeagle.vrnparkour.VRNparkour;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Utils {

    public static Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
    public static Type itemType = new TypeToken<Map<String, Object>>(){}.getType();

    public static void say(final CommandSender cs, final String... message) {
        if (cs == null) return;
        for (final String msg : message)
            cs.sendMessage(color(Messages.msg("prefix") + " " + msg));
    }

    public static void sayNoPrefix(final CommandSender cs, final String... message) {
        if (cs == null) return;
        for (final String msg : message) {
            cs.sendMessage(color(msg));
        }
    }

    public static String color(String s) {
        for (int i = 0; i < s.length(); ++i) {
            if (s.length() - i > 8) {
                final String temp = s.substring(i, i + 8);
                if (temp.startsWith("&#")) {
                    final char[] chars = temp.replaceFirst("&#", "").toCharArray();
                    final StringBuilder rgbColor = new StringBuilder();
                    rgbColor.append("&x");
                    for (final char c : chars) {
                        rgbColor.append("&").append(c);
                    }
                    s = s.replaceAll(temp, rgbColor.toString());
                }
            }
        }
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static String[] color(final String... i) {
        for (final String uncolored : i) {
            color(uncolored);
        }
        return i;
    }

    public static void sayActionBar(final Player p, final String msg) {
        p.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                TextComponent.fromLegacyText(color(msg)));
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

    public static ItemStack buildItemStack(final Material mat, final int amount, final String s, final String... array) {
        return ItemUtil.genItem(mat).amount(amount).name(s).lore(Arrays.asList(array)).build();
    }

    public static void setBlockType(final Block b, final ItemStack itemStack) {
        b.setType(itemStack.getType(), false);
    }

    public static ItemStack itemFromJson(final String s) {
        return ItemStack.deserialize(Utils.gson.fromJson(s, Utils.itemType));
    }

    public static ItemStack itemFromJson(final JsonObject json, final String s) {
        return ItemStack.deserialize(Utils.gson.fromJson(json.get(s), Utils.itemType));
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
        return buildItemStack(mat, int1, s3, list.toArray(new String[0]));
    }

    public static boolean compareLocation(final Location location, final Location location2) {
        return location.getWorld().equals(location2.getWorld()) && location.getBlockX() == location2.getBlockX() && location.getBlockY() == location2.getBlockY() && location.getBlockZ() == location2.getBlockZ();
    }
}
