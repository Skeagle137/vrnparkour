package net.skeagle.vrnparkour.parkour;

import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ParkourLeaderboard {
    private final Map<UUID, Long> times;
    private final Parkour parkour;

    public ParkourLeaderboard(final Parkour parkour) {
        this.parkour = parkour;
        this.times = new HashMap<>();
    }

    public void saveTime(final UUID uuid, final long n) {
        if (this.times.containsKey(uuid))
            if (this.times.get(uuid) > n)
                this.times.put(uuid, n);
        else
            this.times.put(uuid, n);
    }

    public JsonObject serialize() {
        final JsonObject json = new JsonObject();
        this.times.forEach((uuid, n) -> json.addProperty(uuid.toString(), n));
        return json;
    }

    public void loadTimes(List<JsonObject> list) {
        list.forEach(json -> json.entrySet().forEach(entry -> this.saveTime(UUID.fromString(entry.getKey()), (entry.getValue()).getAsLong())));
    }

    public String formatTime(final long n) {
        return String.format("%02d:%02d.%d", n / 60000L % 60L, n / 1000L % 60L, n % 1000L);
    }

    public Parkour getParkour() {
        return this.parkour;
    }

    public Map<UUID, Long> getTimes() {
        return this.times;
    }

    public Long getBest(final UUID uuid) {
        return this.times.getOrDefault(uuid, -1L);
    }
}

