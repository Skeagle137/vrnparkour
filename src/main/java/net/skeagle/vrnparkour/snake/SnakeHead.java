package net.skeagle.vrnparkour.snake;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.skeagle.vrnlib.itemutils.ItemUtils;
import net.skeagle.vrnparkour.utils.Utils;
import org.bukkit.inventory.ItemStack;

public class SnakeHead {

    private final Track track;
    private ItemStack item;
    private int index;
    private int length;

    public SnakeHead(final int index, final ItemStack item, final int length, final Track track) {
        this.item = item;
        this.track = track;
        this.index = index;
        this.length = length;
    }

    public void move() {
        if (track.getPath().stream().allMatch(t -> t.getChunk().isLoaded())) {
            Utils.setBlockType(this.track.getPath().get(this.getTailEnd(0)).getBlock(), this.track.getAirItem());
            this.setIndex(this.track.getDirection() ? (this.getIndex() + 1) : (this.getIndex() - 1));
            this.setIndex((this.getIndex() >= this.track.getPath().size()) ? 0 : this.getIndex());
            this.setIndex((this.getIndex() < 0) ? (this.track.getPath().size() - 1) : this.getIndex());
            Utils.setBlockType(this.track.getPath().get(this.getIndex()).getBlock(), this.getItem());
        }
    }

    public int getTailEnd(final int n) {
        int n3;
        if (this.track.getDirection()) {
            final int n2 = this.getIndex() - this.getLength() + 1 - n;
            n3 = ((n2 < 0) ? (this.track.getPath().size() - n2 * -1) : n2);
        }
        else {
            final int n4 = this.getIndex() + this.getLength() - 1 + n;
            n3 = ((n4 >= this.track.getPath().size()) ? (-(this.track.getPath().size() + -n4)) : n4);
        }
        return n3;
    }

    public int getIndex() {
        return this.index;
    }

    public int getLength() {
        return this.length;
    }

    public ItemStack getItem() {
        return this.item;
    }

    public void setIndex(final int index) {
        this.index = index;
    }

    public void setItem(final ItemStack item) {
        this.item = item;
    }

    public void setLength(final int n) {
        this.length = ((n > 0) ? n : 1);
    }

    public JsonObject serialize() {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("index", getIndex());
        jsonObject.addProperty("length", getLength());
        jsonObject.addProperty("item", ItemUtils.toString(getItem()));
        return jsonObject;
    }
}

