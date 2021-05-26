package net.skeagle.vrnparkour.snake;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import net.skeagle.vrnparkour.VRNparkour;
import net.skeagle.vrnparkour.utils.Utils;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import net.skeagle.vrnlib.sql.SQLHelper;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SnakeManager {
    private final List<Track> tracks;
    private final Gson gson;

    public SnakeManager() {
        this.tracks = new ArrayList<>();
        this.gson = Utils.gson;
        this.load();
    }

    public List<Track> getTracks() {
        return this.tracks;
    }

    public void save() {
        this.tracks.forEach(Track::save);
    }

    public void load() {
        SQLHelper db = VRNparkour.getInstance().getDB();
        SQLHelper.Results res = db.queryResults("SELECT * FROM snake");
        res.forEach(pk -> {
            final String name = res.getString(2);
            final boolean running = res.getBoolean(3);
            final ItemStack item = Utils.itemFromJson(res.getString(4));
            final boolean direction = res.getBoolean(5);
            final int speed = res.get(6);
            Type type = new TypeToken<List<String>>() {
            }.getType();
            final List<String> list = gson.fromJson(res.getString(8), type);
            List<Location> path = new ArrayList<>();
            list.forEach(s -> path.add(Utils.deSerializeLocation(s)));
            final Track track = new Track(name, path, item, speed, direction);
            Type type2 = new TypeToken<List<JsonObject>>() {
            }.getType();
            final List<JsonObject> list2 = gson.fromJson(res.getString(7), type2);
            List<SnakeHead> heads = new ArrayList<>();
            list2.forEach(json -> heads.add(new SnakeHead(json.get("index").getAsInt(), Utils.itemFromJson(json, "item"), json.get("length").getAsInt(), track)));
            heads.forEach(track::addHead);
            track.reset();
            track.getSettings().loadGui();
            if (running) {
                track.start();
                track.getSettings().loadGui();
            }
            tracks.add(track);
        });
    }

    public void deleteTrack(final Track track, final boolean b) {
        this.tracks.remove(track);
        SQLHelper db = VRNparkour.getInstance().getDB();
        db.execute("DELETE FROM snake WHERE name = (?)", track.getName());
        track.delete(b);
    }

    public void createTrack(final String s, final List<Location> list) {
        Track track = new Track(s, list);
        this.tracks.add(track);
        track.save();
    }

    public Track getTrack(final String s) {
        return this.tracks.stream().filter(track -> track.getName().equalsIgnoreCase(s)).findFirst().orElse(null);
    }
}

