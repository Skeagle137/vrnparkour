package net.skeagle.vrnparkour.snake;

import net.skeagle.vrnparkour.VRNparkour;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.*;

public class BlockCrawler {
    private final Map<UUID, List<Location>> map;
    private static final int[][] ADJ_LOC = new int[][] { { -1, 0, 0 }, { 1, 0, 0 }, { 0, -1, 0 }, { 0, 1, 0 }, { 0, 0, -1 }, { 0, 0, 1 } };

    public BlockCrawler() {
        this.map = new HashMap<>();
    }

    public List<Location> getLocations(final UUID uuid, final Location location) throws InvalidPathException, CrawlerOverflowException {
        this.map.put(uuid, new ArrayList<>());
        this.registerLocations(uuid, location);
        final List<Location> list = this.map.get(uuid);
        this.map.remove(uuid);
        return list;
    }

    private void registerLocations(final UUID uuid, final Location location) throws CrawlerOverflowException, InvalidPathException {
        final Block block = location.getBlock();
        if (block.getType() != Material.AIR) {
            if (this.getSurroundingBlocks(block).size() != 2)
                throw new InvalidPathException(block);
            if (!this.map.get(uuid).contains(block.getLocation())) {
                this.map.get(uuid).add(block.getLocation());
                for (final int[] array : BlockCrawler.ADJ_LOC) {
                    final Location location2 = block.getLocation();
                    location2.add(array[0], array[1], array[2]);
                    if (this.map.get(uuid).size() >= VRNparkour.config.getInt("maxPathSize"))
                        throw new CrawlerOverflowException();
                    this.registerLocations(uuid, location2);
                }
            }
        }
    }

    private List<Block> getSurroundingBlocks(final Block b) {
        final ArrayList<Block> list = new ArrayList<>();
        for (int i = 0; i < BlockCrawler.ADJ_LOC.length; ++i) {
            final Block block2 = b.getLocation().clone().add(BlockCrawler.ADJ_LOC[i][0], BlockCrawler.ADJ_LOC[i][1], BlockCrawler.ADJ_LOC[i][2]).getBlock();
            if (block2.getType() != Material.AIR) {
                list.add(block2);
            }
        }
        return list;
    }

    public static class CrawlerOverflowException extends Exception {}

    public static class InvalidPathException extends Exception
    {
        private final Block block;

        public InvalidPathException(final Block block) {
            this.block = block;
        }

        public Block getBlock() {
            return this.block;
        }
    }
}

