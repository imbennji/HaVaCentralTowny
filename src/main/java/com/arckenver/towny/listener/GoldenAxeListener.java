package com.arckenver.towny.listener;

import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.object.Point;
import com.arckenver.towny.object.Rect;
import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;


import java.util.Optional;

public class GoldenAxeListener {

	// This method can be called to automatically set the points based on the player's current chunk and claimed chunk
	public static void setAutomaticPoints(Player player) {
		Location<World> playerLocation = player.getLocation();
		int chunkX = playerLocation.getChunkPosition().getX();
		int chunkZ = playerLocation.getChunkPosition().getZ();

		// Retrieve the chunkX and chunkZ from the column value
		String[] chunkCoordinates = (chunkX + "," + chunkZ).split(",");
		int retrievedChunkX = Integer.parseInt(chunkCoordinates[0]);
		int retrievedChunkZ = Integer.parseInt(chunkCoordinates[1]);

		// Get the claimed chunk (handling the Optional)
		Optional<Chunk> optionalClaimedChunk = playerLocation.getExtent().getChunk(retrievedChunkX, 0, retrievedChunkZ);
		if (optionalClaimedChunk.isPresent()) {
			Chunk claimedChunk = optionalClaimedChunk.get();

			// Calculate the corners of the chunk
			Vector3i chunkMin = claimedChunk.getBlockMin();
			Vector3i chunkMax = claimedChunk.getBlockMax();

			// Convert chunkMin and chunkMax to Vector3d
			Vector3d chunkMinDouble = chunkMin.toDouble();
			Vector3d chunkMaxDouble = chunkMax.toDouble();

			// Set the corners as the first and second points
			Point firstPoint = new Point(playerLocation.getExtent(), chunkMin.getX(), chunkMin.getZ());
			Point secondPoint = new Point(playerLocation.getExtent(), chunkMax.getX(), chunkMax.getZ());

			DataHandler.setFirstPoint(player.getUniqueId(), firstPoint);
			DataHandler.setSecondPoint(player.getUniqueId(), secondPoint);

			// Count the number of blocks in the chunk
			int blockCount = 0;

			// Check all blocks within the claimed chunk
			World world = player.getWorld();
			for (int x = chunkMin.getX(); x <= chunkMax.getX(); x++) {
				for (int y = chunkMin.getY(); y <= chunkMax.getY(); y++) {
					for (int z = chunkMin.getZ(); z <= chunkMax.getZ(); z++) {
						Location<World> blockLocation = new Location<>(world, x, y, z);
						BlockState blockState = blockLocation.getBlock();

						// Perform checks or actions on each block here
						Vector3d blockPosition = blockLocation.getPosition();

						// Compare blockPosition with chunkMinDouble and chunkMaxDouble
						if (blockPosition.compareTo(chunkMinDouble) >= 0 && blockPosition.compareTo(chunkMaxDouble) <= 0) {
							if (blockState.getType() != BlockTypes.AIR) {
								blockCount++;
							}
						}
					}
				}
			}

			String coord = firstPoint.getX() + " " + firstPoint.getY() + ")" +
					" (" + new Rect(firstPoint, secondPoint).size() + ")";
		}
	}
}
