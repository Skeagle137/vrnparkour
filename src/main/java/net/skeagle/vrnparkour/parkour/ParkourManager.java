package net.skeagle.vrnparkour.parkour;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import net.skeagle.vrnlib.itemutils.ItemUtils;
import net.skeagle.vrnlib.misc.Task;
import net.skeagle.vrnparkour.VRNparkour;
import net.skeagle.vrnparkour.parkour.faildetection.FailDetection;
import net.skeagle.vrnparkour.parkour.faildetection.FloorFailDetection;
import net.skeagle.vrnparkour.parkour.faildetection.HeightFailDetection;
import net.skeagle.vrnparkour.parkour.faildetection.ParkourFailDetection;
import net.skeagle.vrnparkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import net.skeagle.vrnlib.sql.SQLHelper;

import java.util.*;

public class ParkourManager {
    private final List<Parkour> parkourList;
    private final List<FailDetection> failDetectionTypes;

    public ParkourManager() {
        this.parkourList = new ArrayList<>();
        this.failDetectionTypes = new ArrayList<>();
        this.registerDetectionType(new FloorFailDetection());
        this.registerDetectionType(new ParkourFailDetection());
        this.registerDetectionType(new HeightFailDetection());
    }

    public void load() {
        SQLHelper db = VRNparkour.getInstance().getDB();
        SQLHelper.Results res = db.queryResults("SELECT * FROM parkour");
        res.forEach(pk -> {
            final String replace = res.getString(2);
            final boolean ready = res.getBoolean(3);
            final FailDetection failDetection = getFailDetection(res.getString(4));
            final int failHeight = res.get(5);
            final ParkourCheckpoint start = (res.getString(6) == null) ? null : ParkourCheckpoint.Deserialize(res.getString(6));
            final ParkourCheckpoint end = (res.getString(7) == null) ? null : ParkourCheckpoint.Deserialize(res.getString(7));
            final JsonArray json = Utils.gson.fromJson(res.getString(8), new TypeToken<JsonArray>() {
            }.getType());
            List<ItemStack> failBlocks = new ArrayList<>();
            json.forEach(s -> failBlocks.add(ItemUtils.fromString(s.getAsJsonObject().getAsString())));
            final List<String> list = Utils.gson.fromJson(res.getString(9), new TypeToken<List<String>>() {
            }.getType());
            List<ParkourCheckpoint> checkpoints = new ArrayList<>();
            list.forEach(s -> checkpoints.add(ParkourCheckpoint.Deserialize(s)));
            final Parkour parkour = new Parkour(replace, failDetection, ready, failHeight, start, end, checkpoints, failBlocks);
            this.parkourList.add(parkour);
            Bukkit.getPluginManager().registerEvents(parkour, VRNparkour.getInstance());
            final JsonObject times = Utils.gson.fromJson(res.getString(10), new TypeToken<JsonObject>() {
            }.getType());
            parkour.getLeaderboard().loadTimes(times);
        });
    }

    public void save() {
        this.parkourList.forEach(Parkour::save);
    }

    public Parkour createParkour(final String s) {
        if (this.getParkour(s) != null)
            throw new RuntimeException("Duplicate parkour name!");
        final Parkour parkour = new Parkour(s);
        Bukkit.getPluginManager().registerEvents(parkour, VRNparkour.getInstance());
        this.parkourList.add(parkour);
        Task.asyncDelayed(parkour::save);
        return parkour;
    }

    public List<Parkour> getParkourList() {
        return this.parkourList;
    }

    public Parkour getParkour(final String s) {
        return this.parkourList.stream().filter(parkour -> parkour.getName().equalsIgnoreCase(s)).findFirst().orElse(null);
    }

    public void registerDetectionType(final FailDetection failDetection) {
        this.failDetectionTypes.add(failDetection);
    }

    public List<FailDetection> getFailDetectionTypes() {
        return this.failDetectionTypes;
    }

    public FailDetection getFailDetection(final String s) {
        for (final FailDetection failDetection : this.getFailDetectionTypes())
            if (failDetection.key().equalsIgnoreCase(s))
                return failDetection;
        return null;
    }

    public void deleteParkour(final Parkour parkour) {
        this.parkourList.remove(parkour);
        SQLHelper db = VRNparkour.getInstance().getDB();
        db.execute("DELETE FROM parkour WHERE name = (?)", parkour.getName());
        parkour.delete();
    }
}
