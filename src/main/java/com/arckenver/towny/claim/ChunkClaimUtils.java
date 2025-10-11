package com.arckenver.towny.claim;

import com.arckenver.towny.DataHandler;
import com.arckenver.towny.object.Point;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

/**
 * Utility methods to work with chunk-sized claims.
 */
public final class ChunkClaimUtils {
        /**
         * Horizontal size of a chunk in blocks.
         */
        public static final int CHUNK_SIZE = 16;

        /**
         * Horizontal area of a chunk in blocks (X * Z).
         */
        public static final int CHUNK_AREA = CHUNK_SIZE * CHUNK_SIZE;

        private ChunkClaimUtils() {
                // Utility class
        }

        /**
         * Calculates the number of chunks required to cover the given horizontal block area.
         *
         * @param blockArea the horizontal area, in blocks
         * @return the number of whole chunks needed to cover the area
         */
        public static int toChunkCount(int blockArea) {
                return (int) Math.ceil(blockArea / (double) CHUNK_AREA);
        }

        /**
         * Updates the stored selection points so that they match the chunk the player currently occupies.
         *
         * @param player the player whose current chunk should be selected
         */
        public static void selectCurrentChunk(Player player) {
                Location<World> location = player.getLocation();

                Point firstCorner = createChunkMinPoint(location);
                Point secondCorner = createChunkMaxPoint(location);

                DataHandler.setFirstPoint(player.getUniqueId(), firstCorner);
                DataHandler.setSecondPoint(player.getUniqueId(), secondCorner);
        }

        private static Point createChunkMinPoint(Location<World> location) {
                int chunkX = location.getChunkPosition().getX();
                int chunkZ = location.getChunkPosition().getZ();
                int minX = chunkX * CHUNK_SIZE;
                int minZ = chunkZ * CHUNK_SIZE;
                return new Point(location.getExtent(), minX, minZ);
        }

        private static Point createChunkMaxPoint(Location<World> location) {
                int chunkX = location.getChunkPosition().getX();
                int chunkZ = location.getChunkPosition().getZ();
                int maxX = chunkX * CHUNK_SIZE + CHUNK_SIZE - 1;
                int maxZ = chunkZ * CHUNK_SIZE + CHUNK_SIZE - 1;
                return new Point(location.getExtent(), maxX, maxZ);
        }
}
