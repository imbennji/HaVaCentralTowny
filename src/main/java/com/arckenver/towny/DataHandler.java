package com.arckenver.towny;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.arckenver.towny.channel.AdminSpyMessageChannel;
import com.arckenver.towny.channel.TownyMessageChannel;
import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.claim.ChunkClaimUtils;
import com.arckenver.towny.object.*;
import com.arckenver.towny.serializer.NationDeserializer;
import com.arckenver.towny.serializer.NationSerializer;
import com.arckenver.towny.serializer.TownyDeserializer;
import com.arckenver.towny.serializer.TownySerializer;
import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.math.IntMath;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.util.blockray.BlockRay;
import org.spongepowered.api.util.blockray.BlockRayHit;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class DataHandler
{
	// --- Files / serialization ---
        private static File townyDir;
        private static File nationDir;
        private static Gson gson;

	// Residents file + storage
	private static final Map<UUID, Resident> RESIDENTS = new HashMap<>();
	private static File residentsFile;

	// --- Existing storage ---
        private static Hashtable<UUID, Towny> towny;
        private static Hashtable<UUID, Nation> nations;
	private static Hashtable<UUID, Hashtable<Vector2i, ArrayList<Towny>>> worldChunks;
	private static HashMap<UUID, Towny> lastTownyWalkedOn;
	private static HashMap<UUID, Plot> lastPlotWalkedOn;
	private static Hashtable<UUID, Point> firstPoints;
	private static Hashtable<UUID, Point> secondPoints;
	private static Hashtable<UUID, UUID> markJobs;
        private static ArrayList<Request> inviteRequests;
        private static ArrayList<Request> joinRequests;
        private static ArrayList<NationRequest> nationInviteRequests;
	private static AdminSpyMessageChannel spyChannel;

	// ------------------------------------------------------------
	// Lifecycle
	// ------------------------------------------------------------
        public static void init(File rootDir)
        {
                townyDir = new File(rootDir, "towns");
                nationDir = new File(rootDir, "nations");

                gson = (new GsonBuilder())
                                .registerTypeAdapter(Towny.class, new TownySerializer())
                                .registerTypeAdapter(Towny.class, new TownyDeserializer())
                                .registerTypeAdapter(Nation.class, new NationSerializer())
                                .registerTypeAdapter(Nation.class, new NationDeserializer())
                                .setPrettyPrinting()
                                .create();

		// residents.json (next to towns/)
		residentsFile = new File(rootDir, "residents.json");
		if (!residentsFile.exists()) {
			try {
				residentsFile.getParentFile().mkdirs();
				Files.write(residentsFile.toPath(), "{}".getBytes(StandardCharsets.UTF_8));
			} catch (IOException e) {
				TownyPlugin.getLogger().error("Failed to create residents.json", e);
			}
		}
	}

        public static void load()
        {
                townyDir.mkdirs();
                towny = new Hashtable<>();
                nationDir.mkdirs();
                nations = new Hashtable<>();

                File[] files = townyDir.listFiles();
                if (files != null) {
                        for (File f : files)
                        {
				if (f.isFile() && f.getName().matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.json"))
				{
					try {
						String json = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
						Towny townyObject = gson.fromJson(json, Towny.class);
						towny.put(townyObject.getUUID(), townyObject);
					} catch (IOException e) {
						TownyPlugin.getLogger().error("Error while loading file " + f.getName(), e);
					}
				}
                        }
                }

                File[] nationFiles = nationDir.listFiles();
                if (nationFiles != null) {
                        for (File f : nationFiles) {
                                if (f.isFile() && f.getName().matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.json")) {
                                        try {
                                                String json = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
                                                Nation nation = gson.fromJson(json, Nation.class);
                                                nations.put(nation.getUUID(), nation);
                                        } catch (IOException e) {
                                                TownyPlugin.getLogger().error("Error while loading nation file " + f.getName(), e);
                                        }
                                }
                        }
                }

                // Load residents
                loadResidents();

                calculateWorldChunks();
                reconcileNationMemberships();
                lastTownyWalkedOn = new HashMap<>();
                lastPlotWalkedOn = new HashMap<>();
                firstPoints = new Hashtable<>();
                secondPoints = new Hashtable<>();
                markJobs = new Hashtable<>();
                inviteRequests = new ArrayList<>();
                joinRequests = new ArrayList<>();
                nationInviteRequests = new ArrayList<>();
                spyChannel = new AdminSpyMessageChannel();
        }

        public static void save()
        {
                for (UUID uuid : towny.keySet()) {
                        saveTowny(uuid);
                }
                for (UUID uuid : nations.keySet()) {
                        saveNation(uuid);
                }
                saveResidents();
        }

	// ------------------------------------------------------------
	// Admin spy channel
	// ------------------------------------------------------------
	public static TownyMessageChannel getSpyChannel()
	{
		return spyChannel;
	}

	// ------------------------------------------------------------
	// Towny (town) API
	// ------------------------------------------------------------
        public static void addTowny(Towny townyInstance) {
                towny.put(townyInstance.getUUID(), townyInstance);
                saveTowny(townyInstance.getUUID());
        }

        public static Towny getTowny(UUID uuid)
        {
                return towny.get(uuid);
        }

	public static Towny getTowny(String name)
	{
		for (Towny t : towny.values())
		{
			if (t.getRealName().equalsIgnoreCase(name))
			{
				return t;
			}
		}
		return null;
	}

	public static Towny getTownyByTag(String tag)
	{
		for (Towny t : towny.values())
		{
			if (t.getTag().equalsIgnoreCase(tag))
			{
				return t;
			}
		}
		return null;
	}

	public static Towny getTowny(Location<World> loc)
	{
		if (!worldChunks.containsKey(loc.getExtent().getUniqueId()))
		{
			return null;
		}
		Vector2i area = new Vector2i(
				IntMath.divide(loc.getBlockX(), 16, RoundingMode.FLOOR),
				IntMath.divide(loc.getBlockZ(), 16, RoundingMode.FLOOR)
		);
		if (!worldChunks.get(loc.getExtent().getUniqueId()).containsKey(area))
		{
			return null;
		}
		for (Towny t : worldChunks.get(loc.getExtent().getUniqueId()).get(area))
		{
			if (t.getRegion().isInside(loc))
			{
				return t;
			}
		}
		return null;
	}

	public static Towny getTownyOfPlayer(UUID uuid)
	{
		for (Towny t : towny.values())
		{
			for (UUID citizen : t.getCitizens())
			{
				if (citizen.equals(uuid))
				{
					return t;
				}
			}
		}
		return null;
	}

	public static Set<UUID> getTownOutlaws(UUID townId) {
		Towny town = getTowny(townId);
		if (town == null) {
			return Collections.emptySet();
		}
		return new LinkedHashSet<>(town.getOutlaws());
	}

	public static boolean isTownOutlaw(UUID townId, UUID playerId) {
		Towny town = getTowny(townId);
		return town != null && town.isOutlaw(playerId);
	}

	public static boolean addTownOutlaw(UUID townId, UUID playerId) {
		Towny town = getTowny(townId);
		if (town == null || playerId == null) {
			return false;
		}
		if (!town.addOutlaw(playerId)) {
			return false;
		}
		saveTowny(townId);
		Sponge.getServer().getPlayer(playerId).ifPresent(player -> {
			String townName = town.getDisplayName();
			player.sendMessage(Text.of(LanguageHandler.INFO_OUTLAW_NOTIFY_ADD.replace("{TOWN}", townName)));
		});
		return true;
	}

	public static boolean removeTownOutlaw(UUID townId, UUID playerId) {
		Towny town = getTowny(townId);
		if (town == null || playerId == null) {
			return false;
		}
		if (!town.removeOutlaw(playerId)) {
			return false;
		}
		saveTowny(townId);
		Sponge.getServer().getPlayer(playerId).ifPresent(player -> {
			String townName = town.getDisplayName();
			player.sendMessage(Text.of(LanguageHandler.INFO_OUTLAW_NOTIFY_REMOVE.replace("{TOWN}", townName)));
		});
		return true;
	}

        public static void removeTowny(UUID uuid)
        {
                Towny oldTowny = getTowny(uuid);
                if (oldTowny != null) {
                        MessageChannel.TO_CONSOLE.send(Text.of("Removing Towny " + uuid + ": "));
                        MessageChannel.TO_CONSOLE.send(Utils.formatTownyDescription(oldTowny, Utils.CLICKER_ADMIN));
                        if (oldTowny.hasNation()) {
                                Nation nation = nations.get(oldTowny.getNationUUID());
                                if (nation != null) {
                                        nation.removeTown(uuid);
                                        if (nation.getCapital() == null || nation.getTowns().isEmpty()) {
                                                removeNation(nation.getUUID());
                                        } else {
                                                saveNation(nation.getUUID());
                                        }
                                }
                        }
                }
                towny.remove(uuid);

                ArrayList<UUID> toRemove = new ArrayList<>();
		for (Towny t : lastTownyWalkedOn.values())
		{
			if (t != null && t.getUUID().equals(uuid))
			{
				toRemove.add(t.getUUID());
			}
		}
		for (UUID uuidToRemove : toRemove)
		{
			lastTownyWalkedOn.remove(uuidToRemove);
		}

		calculateWorldChunks();

		inviteRequests.removeIf(req -> req.getTownyUUID().equals(uuid));
		joinRequests.removeIf(req -> req.getTownyUUID().equals(uuid));

                File file = new File(townyDir, uuid.toString() + ".json");
                file.delete();
        }

        public static void addNation(Nation nation)
        {
                nations.put(nation.getUUID(), nation);
                saveNation(nation.getUUID());
        }

        public static Nation getNation(UUID uuid)
        {
                return nations.get(uuid);
        }

        public static Nation getNation(String name)
        {
                for (Nation nation : nations.values())
                {
                        if (nation.getRealName().equalsIgnoreCase(name))
                        {
                                return nation;
                        }
                }
                return null;
        }

        public static Collection<Nation> getNations()
        {
                return nations.values();
        }

        public static Nation getNationByTag(String tag)
        {
                if (tag == null) {
                        return null;
                }
                for (Nation nation : nations.values())
                {
                        if (nation.hasTag() && nation.getTag().equalsIgnoreCase(tag))
                        {
                                return nation;
                        }
                }
                return null;
        }

        public static Nation getNationOfTown(UUID townUUID)
        {
                Towny t = towny.get(townUUID);
                if (t == null || !t.hasNation())
                {
                        return null;
                }
                return nations.get(t.getNationUUID());
        }

        public static Nation getNationOfPlayer(UUID playerUUID)
        {
                Towny town = getTownyOfPlayer(playerUUID);
                if (town == null)
                {
                        return null;
                }
                return town.hasNation() ? nations.get(town.getNationUUID()) : null;
        }

        public static void removeNation(UUID uuid)
        {
                Nation nation = nations.remove(uuid);
                if (nation == null)
                {
                        return;
                }

                for (UUID townUUID : new LinkedHashSet<>(nation.getTowns()))
                {
                        Towny t = towny.get(townUUID);
                        if (t != null)
                        {
                                t.clearNation();
                                saveTowny(townUUID);
                        }
                }

                nationInviteRequests.removeIf(req -> req.getNationUUID().equals(uuid));

                File file = new File(nationDir, uuid.toString() + ".json");
                file.delete();
        }

	public static Hashtable<UUID, Towny> getTowny()
	{
		return towny;
	}

	public static boolean getFlag(String flag, Location<World> loc)
	{
		Towny t = getTowny(loc);
		if (t == null)
		{
			return ConfigHandler.getNode("worlds")
					.getNode(loc.getExtent().getName())
					.getNode("flags").getNode(flag).getBoolean();
		}
		Plot plot = t.getPlot(loc);
		if (plot == null)
		{
			return t.getFlag(flag);
		}
		return plot.getFlag(flag);
	}

        public static boolean getPerm(String perm, UUID playerUUID, Location<World> loc)
        {
                String canonicalPerm = Towny.canonicalizePerm(perm);
                Towny town = getTowny(loc);
                if (town == null)
                {
                        CommentedConfigurationNode permsNode = ConfigHandler.getNode("worlds")
                                        .getNode(loc.getExtent().getName())
                                        .getNode("perms");
                        CommentedConfigurationNode valueNode = permsNode.getNode(canonicalPerm);
                        if (!valueNode.isVirtual()) {
                                return valueNode.getBoolean();
                        }
                        if (Towny.PERM_DESTROY.equals(canonicalPerm)) {
                                return permsNode.getNode(Towny.PERM_BUILD).getBoolean();
                        }
                        if (Towny.PERM_SWITCH.equals(canonicalPerm) || Towny.PERM_ITEM_USE.equals(canonicalPerm)) {
                                return permsNode.getNode(Towny.PERM_INTERACT).getBoolean();
                        }
                        return valueNode.getBoolean();
                }

                Plot plot = town.getPlot(loc);
                Towny playerTown = getTownyOfPlayer(playerUUID);

                if (plot == null)
                {
                        if (town.isStaff(playerUUID))
                        {
                                return true;
                        }
                        if (town.isCitizen(playerUUID))
                        {
                                return town.getPerm(Towny.TYPE_RESIDENT, canonicalPerm);
                        }
                        if (playerTown != null)
                        {
                                if (shareNation(town, playerTown))
                                {
                                        return town.getPerm(Towny.TYPE_NATION, canonicalPerm);
                                }
                                if (areNationsAllied(town, playerTown))
                                {
                                        return town.getPerm(Towny.TYPE_ALLY, canonicalPerm);
                                }
                        }
                        return town.getPerm(Towny.TYPE_OUTSIDER, canonicalPerm);
                }

                if (town.isStaff(playerUUID) || plot.isOwner(playerUUID))
                        return true;
                if (plot.isCoowner(playerUUID))
                        return plot.getPerm(Towny.TYPE_FRIEND, canonicalPerm);
                if (town.isCitizen(playerUUID))
                        return plot.getPerm(Towny.TYPE_RESIDENT, canonicalPerm);
                if (playerTown != null)
                {
                        if (shareNation(town, playerTown) || areNationsAllied(town, playerTown))
                        {
                                return plot.getPerm(Towny.TYPE_ALLY, canonicalPerm);
                        }
                }

                return plot.getPerm(Towny.TYPE_OUTSIDER, canonicalPerm);
        }

        private static boolean shareNation(Towny town, Towny other) {
                if (town == null || other == null) {
                        return false;
                }
                if (!town.hasNation() || !other.hasNation()) {
                        return false;
                }
                return town.getNationUUID().equals(other.getNationUUID());
        }

        private static boolean areNationsAllied(Towny town, Towny other) {
                if (town == null || other == null) {
                        return false;
                }
                if (!town.hasNation() || !other.hasNation()) {
                        return false;
                }
                Nation nation = getNation(town.getNationUUID());
                Nation target = getNation(other.getNationUUID());
                if (nation == null || target == null) {
                        return false;
                }
                return nation.getAllies().contains(target.getUUID()) || target.getAllies().contains(nation.getUUID());
        }

	// ------------------------------------------------------------
	// Players convenience
	// ------------------------------------------------------------
	public static String getPlayerName(UUID uuid)
	{
		Optional<Player> optPlayer = Sponge.getServer().getPlayer(uuid);
		if (optPlayer.isPresent())
		{
			return optPlayer.get().getName();
		}
		try
		{
			return Sponge.getServer().getGameProfileManager().get(uuid).get().getName().get();
		}
		catch (Exception e)
		{
			return null;
		}
	}

	public static Collection<String> getPlayerNames()
	{
		return Sponge.getServer().getGameProfileManager().getCache().getProfiles().stream()
				.filter(gp -> gp.getName().isPresent())
				.map(gp -> gp.getName().get())
				.collect(Collectors.toList());
	}

	public static UUID getPlayerUUID(String name)
	{
		Optional<Player> optPlayer = Sponge.getServer().getPlayer(name);
		if (optPlayer.isPresent())
		{
			return optPlayer.get().getUniqueId();
		}
		try
		{
			return Sponge.getServer().getGameProfileManager().get(name).get().getUniqueId();
		}
		catch (Exception e)
		{
			return null;
		}
	}

	public static String getCitizenTitle(UUID uuid)
	{
		if (!ConfigHandler.getNode("others", "enableTownyRanks").getBoolean())
		{
			return "";
		}
		Towny t = getTownyOfPlayer(uuid);
		if (t == null)
		{
			return LanguageHandler.FORMAT_HERMIT;
		}
		if (t.isPresident(uuid))
		{
			return ConfigHandler.getTownyRank(t.getNumCitizens()).getNode("mayorTitle").getString();
		}
		if (t.isMinister(uuid))
		{
			return LanguageHandler.FORMAT_COMAYOR;
		}
		return LanguageHandler.FORMAT_CITIZEN;
	}

	// ------------------------------------------------------------
	// Claim checks
	// ------------------------------------------------------------
	public static boolean canClaim(Location<World> loc, boolean ignoreMinDistance)
	{
		return canClaim(loc, ignoreMinDistance, null);
	}

	public static boolean canClaim(Location<World> loc, boolean ignoreMinDistance, UUID toExclude)
	{
		for (Towny t : towny.values()) {
			if (!t.getUUID().equals(toExclude) &&
					t.getRegion().distance(loc) < ConfigHandler.getNode("others", "minTownyDistance").getInt())
			{
				if (ignoreMinDistance)
				{
					if (t.getRegion().isInside(loc))
					{
						return false;
					}
				}
				else
				{
					MessageChannel.TO_CONSOLE.send(Text.of("too close: ", loc, " towny: ", t.getName()));
					return false;
				}
			}
		}
		return true;
	}

	public static boolean canClaim(Rect rect, boolean ignoreMinDistance, UUID toExclude)
	{
		Optional<World> optWorld = Sponge.getServer().getWorld(rect.getWorld());
		if (!optWorld.isPresent())
		{
			return false;
		}
		World world = optWorld.get();
		return canClaim(world.getLocation(rect.getMaxX(), 0, rect.getMaxY()), ignoreMinDistance, toExclude) &&
				canClaim(world.getLocation(rect.getMaxX(), 0, rect.getMinY()), ignoreMinDistance, toExclude) &&
				canClaim(world.getLocation(rect.getMinX(), 0, rect.getMaxY()), ignoreMinDistance, toExclude) &&
				canClaim(world.getLocation(rect.getMinX(), 0, rect.getMinY()), ignoreMinDistance, toExclude);
	}

        public static void calculateWorldChunks()
        {
                worldChunks = new Hashtable<>();
                for (Towny t : towny.values())
                {
                        addToWorldChunks(t);
                }
        }

        private static void reconcileNationMemberships()
        {
                // Ensure towns reference an existing nation
                for (Towny t : towny.values())
                {
                        if (!t.hasNation())
                        {
                                continue;
                        }
                        Nation nation = nations.get(t.getNationUUID());
                        if (nation == null)
                        {
                                t.clearNation();
                                continue;
                        }
                        if (!nation.hasTown(t.getUUID()))
                        {
                                nation.addTown(t.getUUID());
                        }
                }

                // Remove orphaned towns from nations and resynchronise membership
                for (Nation nation : nations.values())
                {
                        java.util.Set<UUID> entries = new LinkedHashSet<>(nation.getTowns());
                        for (UUID townId : entries)
                        {
                                Towny t = towny.get(townId);
                                if (t == null)
                                {
                                        nation.removeTown(townId);
                                        continue;
                                }
                                if (!nation.getUUID().equals(t.getNationUUID()))
                                {
                                        t.setNationUUID(nation.getUUID());
                                }
                        }

                        if (nation.getCapital() != null && !nation.hasTown(nation.getCapital()))
                        {
                                nation.setCapital(null);
                        }
                }
        }

	public static void addToWorldChunks(Towny t)
	{
		for (Rect r : t.getRegion().getRects())
		{
			if (!worldChunks.containsKey(r.getWorld()))
			{
				worldChunks.put(r.getWorld(), new Hashtable<>());
			}
			Hashtable<Vector2i, ArrayList<Towny>> chunks = worldChunks.get(r.getWorld());
			for (int i = IntMath.divide(r.getMinX(), 16, RoundingMode.FLOOR);
				 i < IntMath.divide(r.getMaxX(), 16, RoundingMode.FLOOR) + 1; i++)
			{
				for (int j = IntMath.divide(r.getMinY(), 16, RoundingMode.FLOOR);
					 j < IntMath.divide(r.getMaxY(), 16, RoundingMode.FLOOR) + 1; j++)
				{
					Vector2i vect = new Vector2i(i, j);
					if (!chunks.containsKey(vect))
					{
						chunks.put(vect, new ArrayList<>());
					}
					if (!chunks.get(vect).contains(t))
					{
						chunks.get(vect).add(t);
					}
				}
			}
		}
	}

	// ------------------------------------------------------------
	// Last walked-on caching
	// ------------------------------------------------------------
	public static Towny getLastTownyWalkedOn(UUID uuid)
	{
		return lastTownyWalkedOn.get(uuid);
	}

	public static void setLastTownyWalkedOn(UUID uuid, Towny t)
	{
		lastTownyWalkedOn.put(uuid, t);
	}

	public static Plot getLastPlotWalkedOn(UUID uuid)
	{
		return lastPlotWalkedOn.get(uuid);
	}

	public static void setLastPlotWalkedOn(UUID uuid, Plot plot)
	{
		lastPlotWalkedOn.put(uuid, plot);
	}

	// ------------------------------------------------------------
	// Mark jobs (particle outline)
	// ------------------------------------------------------------
	public static void toggleMarkJob(Player player)
	{
		if (markJobs.containsKey(player.getUniqueId()))
		{
			Sponge.getScheduler().getTaskById(markJobs.get(player.getUniqueId())).ifPresent(Task::cancel);
			markJobs.remove(player.getUniqueId());
			return;
		}
		ParticleEffect townyParticule = ParticleEffect.builder().type(ParticleTypes.DRAGON_BREATH).quantity(1).build();
		ParticleEffect plotParticule = ParticleEffect.builder().type(ParticleTypes.HAPPY_VILLAGER).quantity(1).build();
		Task t = Sponge.getScheduler()
				.createTaskBuilder()
				.execute(task -> {
					if (!player.isOnline())
					{
						task.cancel();
						markJobs.remove(player.getUniqueId());
						return;
					}
					Location<World> loc = player.getLocation().add(0, 2, 0);
					loc = loc.sub(8, 0, 8);
					for (int x = 0; x < 16; ++x)
					{
						for (int y = 0; y < 16; ++y)
						{
							Towny tt = DataHandler.getTowny(loc);
							if (tt != null)
							{
								BlockRay<World> blockRay = BlockRay.from(loc)
										.direction(new Vector3d(0, -1, 0))
										.distanceLimit(50)
										.stopFilter(BlockRay.blockTypeFilter(BlockTypes.AIR))
										.build();
								Optional<BlockRayHit<World>> block = blockRay.end();
								if (block.isPresent())
								{
									if (tt.getPlot(loc) != null)
									{
										player.spawnParticles(plotParticule, block.get().getPosition(), 60);
									}
									else
									{
										player.spawnParticles(townyParticule, block.get().getPosition(), 60);
									}
								}
							}
							loc = loc.add(0,0,1);
						}
						loc = loc.add(1,0,0);
						loc = loc.sub(0,0,16);
					}
				})
				.delay(1, TimeUnit.SECONDS)
				.interval(1, TimeUnit.SECONDS)
				.async()
				.submit(TownyPlugin.getInstance());
		markJobs.put(player.getUniqueId(), t.getUniqueId());
	}

	// ------------------------------------------------------------
	// Selection points
	// ------------------------------------------------------------
        public static Point getFirstPoint(UUID uuid)
        {
                Point stored = firstPoints.get(uuid);
                if (stored != null)
                {
                        return stored;
                }
                Optional<Player> player = Sponge.getServer().getPlayer(uuid);
                if (!player.isPresent())
                {
                        return null;
                }
                Vector3i chunk = player.get().getLocation().getChunkPosition();
                return new Point(
                                player.get().getWorld(),
                                chunk.getX() * ChunkClaimUtils.CHUNK_SIZE,
                                chunk.getZ() * ChunkClaimUtils.CHUNK_SIZE
                );
        }

	public static void setFirstPoint(UUID uuid, Point point)
	{
		firstPoints.put(uuid, point);
	}

	public static void removeFirstPoint(UUID uuid)
	{
		firstPoints.remove(uuid);
	}

        public static Point getSecondPoint(UUID uuid)
        {
                Point stored = secondPoints.get(uuid);
                if (stored != null)
                {
                        return stored;
                }
                Optional<Player> player = Sponge.getServer().getPlayer(uuid);
                if (!player.isPresent())
                {
                        return null;
                }
                Vector3i chunk = player.get().getLocation().getChunkPosition();
                int baseX = chunk.getX() * ChunkClaimUtils.CHUNK_SIZE;
                int baseZ = chunk.getZ() * ChunkClaimUtils.CHUNK_SIZE;
                return new Point(
                                player.get().getWorld(),
                                baseX + ChunkClaimUtils.CHUNK_SIZE - 1,
                                baseZ + ChunkClaimUtils.CHUNK_SIZE - 1
                );
        }

	public static void setSecondPoint(UUID uuid, Point point)
	{
		secondPoints.put(uuid, point);
	}

	public static void removeSecondPoint(UUID uuid)
	{
		secondPoints.remove(uuid);
	}

	// ------------------------------------------------------------
	// Join/Invite requests
	// ------------------------------------------------------------
	public static Request getJoinRequest(UUID townyUUID, UUID uuid)
	{
		for (Request req : joinRequests)
		{
			if (req.match(townyUUID, uuid))
			{
				return req;
			}
		}
		return null;
	}

	public static void addJoinRequest(Request req)
	{
		joinRequests.add(req);
	}

	public static void removeJoinRequest(Request req)
	{
		joinRequests.remove(req);
	}

	public static Request getInviteRequest(UUID townyUUID, UUID uuid)
	{
		for (Request req : inviteRequests)
		{
			if (req.match(townyUUID, uuid))
			{
				return req;
			}
		}
		return null;
	}

	public static void addInviteRequest(Request req)
	{
		inviteRequests.add(req);
	}

        public static void removeInviteRequest(Request req)
        {
                inviteRequests.remove(req);
        }

        public static NationRequest getNationInviteRequest(UUID nationUUID, UUID townUUID)
        {
                for (NationRequest req : nationInviteRequests)
                {
                        if (req.match(nationUUID, townUUID))
                        {
                                return req;
                        }
                }
                return null;
        }

        public static void addNationInviteRequest(NationRequest req)
        {
                nationInviteRequests.add(req);
        }

        public static void removeNationInviteRequest(NationRequest req)
        {
                nationInviteRequests.remove(req);
        }

	// ------------------------------------------------------------
	// Save towns
	// ------------------------------------------------------------
        public static void saveTowny(UUID uuid) {
                Towny t = towny.get(uuid);
                if (t == null) {
                        TownyPlugin.getLogger().warn("Trying to save null towny!");
                        return;
                }
                File file = new File(townyDir, uuid.toString() + ".json");
                try {
                        if (!file.exists()) {
                                file.createNewFile();
                        }
                        String json = gson.toJson(t, Towny.class);
                        Files.write(file.toPath(), json.getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                        TownyPlugin.getLogger().error("Error while saving file " + file.getName() + " for towny " + t.getName(), e);
                }
        }

        public static void saveNation(UUID uuid) {
                Nation nation = nations.get(uuid);
                if (nation == null) {
                        TownyPlugin.getLogger().warn("Trying to save null nation!");
                        return;
                }
                File file = new File(nationDir, uuid.toString() + ".json");
                try {
                        if (!file.exists()) {
                                file.createNewFile();
                        }
                        String json = gson.toJson(nation, Nation.class);
                        Files.write(file.toPath(), json.getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                        TownyPlugin.getLogger().error("Error while saving nation file " + file.getName() + " for nation " + nation.getName(), e);
                }
        }

	// ------------------------------------------------------------
	// Residents: load/save + helpers
	// ------------------------------------------------------------
	private static void loadResidents() {
		try (BufferedReader r = Files.newBufferedReader(residentsFile.toPath(), StandardCharsets.UTF_8)) {
			Map<String, Resident> tmp = gson.fromJson(
					r,
					new TypeToken<Map<String, Resident>>(){}.getType()
			);
			RESIDENTS.clear();
                        if (tmp != null) {
                                for (Map.Entry<String, Resident> e : tmp.entrySet()) {
                                        try {
                                                UUID id = UUID.fromString(e.getKey());
                                                Resident src = e.getValue();
                                                if (src == null) {
                                                        src = new Resident(id);
                                                } else {
                                                        src.normalizeAfterLoad(id);
                                                }
                                                RESIDENTS.put(id, src);
                                        } catch (IllegalArgumentException bad) {
                                                TownyPlugin.getLogger().warn("Skipping malformed resident key: " + e.getKey());
                                        }
                                }
			}
		} catch (IOException ex) {
			TownyPlugin.getLogger().error("Failed loading residents.json", ex);
		}
	}

	private static void saveResidents() {
		Map<String, Resident> out = new LinkedHashMap<>();
		for (Map.Entry<UUID, Resident> e : RESIDENTS.entrySet()) {
			out.put(e.getKey().toString(), e.getValue());
		}
		try (BufferedWriter w = Files.newBufferedWriter(residentsFile.toPath(), StandardCharsets.UTF_8)) {
			gson.toJson(out, w);
		} catch (IOException ex) {
			TownyPlugin.getLogger().error("Failed saving residents.json", ex);
		}
	}

        private static Resident ensureResident(UUID id) {
                Resident r = RESIDENTS.computeIfAbsent(id, Resident::new);
                r.normalizeAfterLoad(id);
                return r;
        }

        public static Optional<Resident> getResident(UUID id) {
                return Optional.ofNullable(RESIDENTS.get(id));
        }

        public static Collection<Resident> getResidents() {
                return Collections.unmodifiableCollection(RESIDENTS.values());
        }

        // ------------------------------------------------------------
        // Resident identity + lifecycle helpers
        // ------------------------------------------------------------
        public static void markResidentLogin(UUID id, String currentName) {
                Resident r = ensureResident(id);
                long now = System.currentTimeMillis();
                if (r.getRegisteredAt() <= 0L) {
                        r.setRegisteredAt(now);
                }
                r.setLastOnlineAt(now);
                r.setLastKnownName(currentName);
                saveResidents();
        }

        public static void markResidentLogout(UUID id) {
                Resident r = ensureResident(id);
                r.setLastLogoutAt(System.currentTimeMillis());
                saveResidents();
        }

        public static long getResidentRegisteredAt(UUID id) { return ensureResident(id).getRegisteredAt(); }
        public static long getResidentLastOnline(UUID id) { return ensureResident(id).getLastOnlineAt(); }
        public static long getResidentLastLogout(UUID id) { return ensureResident(id).getLastLogoutAt(); }

        public static String getResidentLastKnownName(UUID id) { return ensureResident(id).getLastKnownName(); }

        public static String getResidentSurname(UUID id) { return ensureResident(id).getSurname(); }
        public static void setResidentSurname(UUID id, String surname) {
                Resident r = ensureResident(id);
                r.setSurname(surname);
                saveResidents();
        }

        public static String getResidentChatPrefix(UUID id) { return ensureResident(id).getChatPrefix(); }
        public static void setResidentChatPrefix(UUID id, String prefix) {
                Resident r = ensureResident(id);
                r.setChatPrefix(prefix);
                saveResidents();
        }

        public static String getResidentChatSuffix(UUID id) { return ensureResident(id).getChatSuffix(); }
        public static void setResidentChatSuffix(UUID id, String suffix) {
                Resident r = ensureResident(id);
                r.setChatSuffix(suffix);
                saveResidents();
        }

        public static String getResidentLocale(UUID id) { return ensureResident(id).getLocale(); }
        public static void setResidentLocale(UUID id, String locale) {
                Resident r = ensureResident(id);
                r.setLocale(locale);
                saveResidents();
        }

        public static void setResidentTaxExemptUntil(UUID id, long epochMs) {
                Resident r = ensureResident(id);
                r.setTaxExemptUntil(epochMs);
                saveResidents();
        }

        public static long getResidentTaxExemptUntil(UUID id) { return ensureResident(id).getTaxExemptUntil(); }

        public static Queue<String> getResidentNameHistory(UUID id) {
                return new ArrayDeque<>(ensureResident(id).getNameHistory());
        }

        // ------------------------------------------------------------
        // Town membership metadata (no nations)
        // ------------------------------------------------------------
        public static Optional<UUID> getResidentTownId(UUID id) {
                return Optional.ofNullable(ensureResident(id).getTownId());
        }

        public static void setResidentTownId(UUID id, UUID townId) {
                Resident r = ensureResident(id);
                r.setTownId(townId);
                if (townId == null) {
                        r.clearTownRanks(null);
                }
                saveResidents();
        }

        public static Set<String> getResidentTownRanks(UUID id) {
                return new LinkedHashSet<>(ensureResident(id).getTownRanks());
        }

        public static boolean addResidentTownRank(UUID id, String rank, UUID actor) {
                Resident r = ensureResident(id);
                boolean added = r.addTownRank(rank, actor);
                if (added) saveResidents();
                return added;
        }

        public static boolean removeResidentTownRank(UUID id, String rank, UUID actor) {
                Resident r = ensureResident(id);
                boolean removed = r.removeTownRank(rank, actor);
                if (removed) saveResidents();
                return removed;
        }

        public static List<Resident.TownRankHistoryEntry> getResidentTownRankHistory(UUID id) {
                return new ArrayList<>(ensureResident(id).getTownRankHistory());
        }

        public static boolean isResidentMayor(UUID id) {
                return ensureResident(id).isMayor();
        }

        public static boolean isResidentAssistant(UUID id) {
                return ensureResident(id).isAssistant();
        }

        // ------------------------------------------------------------
        // Economy helpers
        // ------------------------------------------------------------
        public static BigDecimal getResidentBalance(UUID id) {
                return ensureResident(id).getBalance();
        }

        public static void setResidentBalance(UUID id, BigDecimal balance) {
                Resident r = ensureResident(id);
                r.setBalance(balance);
                saveResidents();
        }

        public static void recordResidentDeposit(UUID id, BigDecimal amount, String type, String cause) {
                if (amount == null) return;
                Resident r = ensureResident(id);
                r.recordTransaction(amount.abs(), type, cause);
                r.setBankrupt(false);
                saveResidents();
        }

        public static boolean recordResidentWithdrawal(UUID id, BigDecimal amount, String type, String cause) {
                if (amount == null) return false;
                Resident r = ensureResident(id);
                BigDecimal neg = amount.abs().negate();
                if (r.getBalance().add(neg).compareTo(BigDecimal.ZERO) < 0) {
                        return false;
                }
                r.recordTransaction(neg, type, cause);
                saveResidents();
                return true;
        }

        public static List<Resident.EconomyLedgerEntry> getResidentLedger(UUID id) {
                return new ArrayList<>(ensureResident(id).getLedger());
        }

        public static void setResidentBankrupt(UUID id, boolean bankrupt) {
                Resident r = ensureResident(id);
                r.setBankrupt(bankrupt);
                if (bankrupt) {
                        r.setBankruptcyDeclaredAt(System.currentTimeMillis());
                }
                saveResidents();
        }

        public static boolean isResidentBankrupt(UUID id) { return ensureResident(id).isBankrupt(); }

        public static long getResidentBankruptcyDeclaredAt(UUID id) {
                return ensureResident(id).getBankruptcyDeclaredAt();
        }

        public static void markResidentTaxPaid(UUID id) {
                Resident r = ensureResident(id);
                r.setLastTaxPaidAt(System.currentTimeMillis());
                saveResidents();
        }

        public static long getResidentLastTaxPaidAt(UUID id) { return ensureResident(id).getLastTaxPaidAt(); }

        // ------------------------------------------------------------
        // Jail mechanics
        // ------------------------------------------------------------
        public static boolean isResidentJailed(UUID id) { return ensureResident(id).isJailed(); }

        public static void setResidentJailed(UUID id, boolean jailed, UUID jailTown, UUID jailPlot, long releaseAt) {
                Resident r = ensureResident(id);
                r.setJailed(jailed);
                r.setJailTownId(jailTown);
                r.setJailPlotId(jailPlot);
                r.setJailReleaseAt(releaseAt);
                if (!jailed) {
                        r.setJailEscapes(0);
                }
                saveResidents();
        }

        public static void incrementResidentJailEscapes(UUID id) {
                Resident r = ensureResident(id);
                r.setJailEscapes(r.getJailEscapes() + 1);
                saveResidents();
        }

        public static int getResidentJailEscapes(UUID id) { return ensureResident(id).getJailEscapes(); }

        public static long getResidentJailRelease(UUID id) { return ensureResident(id).getJailReleaseAt(); }

        public static Optional<UUID> getResidentJailTown(UUID id) {
                return Optional.ofNullable(ensureResident(id).getJailTownId());
        }

        public static List<Resident.JailRequest> getResidentJailRequests(UUID id) {
                Resident r = ensureResident(id);
                r.pruneExpiredJailRequests(System.currentTimeMillis());
                return new ArrayList<>(r.getJailRequests());
        }

        public static void addResidentJailRequest(UUID id, Resident.JailRequest request) {
                Resident r = ensureResident(id);
                r.addJailRequest(request);
                saveResidents();
        }

        public static boolean tryReleaseResidentFromJail(UUID id) {
                Resident resident = ensureResident(id);
                if (!resident.isJailed()) {
                        return false;
                }
                boolean shouldRelease = false;
                long releaseAt = resident.getJailReleaseAt();
                if (releaseAt > 0 && releaseAt <= System.currentTimeMillis()) {
                        shouldRelease = true;
                } else {
                        UUID townId = resident.getJailTownId();
                        UUID plotId = resident.getJailPlotId();
                        if (townId == null || plotId == null) {
                                shouldRelease = true;
                        } else {
                                Towny town = getTowny(townId);
                                if (town == null) {
                                        shouldRelease = true;
                                } else {
                                        Plot plot = town.getPlots().get(plotId);
                                        if (plot == null || plot.getType() != PlotType.JAIL) {
                                                shouldRelease = true;
                                        }
                                }
                        }
                }
                if (shouldRelease) {
                        setResidentJailed(id, false, null, null, 0L);
                        return true;
                }
                return false;
        }

        public static Optional<Location<World>> getResidentJailLocation(UUID id) {
                if (tryReleaseResidentFromJail(id)) {
                        return Optional.empty();
                }
                Resident resident = ensureResident(id);
                if (!resident.isJailed()) {
                        return Optional.empty();
                }
                UUID townId = resident.getJailTownId();
                UUID plotId = resident.getJailPlotId();
                if (townId == null || plotId == null) {
                        return Optional.empty();
                }
                Towny town = getTowny(townId);
                if (town == null) {
                        return Optional.empty();
                }
                Plot plot = town.getPlots().get(plotId);
                if (plot == null || plot.getType() != PlotType.JAIL) {
                        return Optional.empty();
                }
                return getJailSpawn(town, plot);
        }

        public static Optional<Plot> findPrimaryJailPlot(Towny town) {
                if (town == null) {
                        return Optional.empty();
                }
                return town.getPlotsOfType(PlotType.JAIL).stream().findFirst();
        }

        public static Optional<Location<World>> getJailSpawn(Towny town, Plot plot) {
                if (town == null || plot == null || plot.getType() != PlotType.JAIL) {
                        return Optional.empty();
                }
                Rect rect = plot.getRect();
                Optional<World> worldOpt = Sponge.getServer().getWorld(rect.getWorld());
                if (!worldOpt.isPresent()) {
                        return Optional.empty();
                }
                World world = worldOpt.get();
                int centerX = (rect.getMinX() + rect.getMaxX()) / 2;
                int centerZ = (rect.getMinY() + rect.getMaxY()) / 2;
                Vector3i highest = world.getHighestPositionAt(centerX, centerZ);
                double y = highest.getY() + 1;
                Location<World> location = world.getLocation(centerX + 0.5, y, centerZ + 0.5);
                return Optional.of(location);
        }

        public static List<UUID> getTownJailedResidents(UUID townId) {
                List<UUID> jailed = new ArrayList<>();
                if (townId == null) {
                        return jailed;
                }
                for (Resident resident : RESIDENTS.values()) {
                        if (!resident.isJailed()) {
                                continue;
                        }
                        if (!townId.equals(resident.getJailTownId())) {
                                continue;
                        }
                        if (tryReleaseResidentFromJail(resident.getId())) {
                                continue;
                        }
                        jailed.add(resident.getId());
                }
                return jailed;
        }

        public static void releaseResidentsInJailPlot(UUID townId, UUID plotId) {
                if (townId == null || plotId == null) {
                        return;
                }
                for (Resident resident : RESIDENTS.values()) {
                        if (!resident.isJailed()) {
                                continue;
                        }
                        if (!townId.equals(resident.getJailTownId())) {
                                continue;
                        }
                        if (!plotId.equals(resident.getJailPlotId())) {
                                continue;
                        }
                        setResidentJailed(resident.getId(), false, null, null, 0L);
                        Sponge.getServer().getPlayer(resident.getId()).ifPresent(player ->
                                player.sendMessage(Text.of(LanguageHandler.INFO_JAIL_RELEASE)));
                }
        }

        public static Optional<Location<World>> getResidentJailLocation(UUID id) {
                if (tryReleaseResidentFromJail(id)) {
                        return Optional.empty();
                }
                Resident resident = ensureResident(id);
                if (!resident.isJailed()) {
                        return Optional.empty();
                }
                UUID townId = resident.getJailTownId();
                UUID plotId = resident.getJailPlotId();
                if (townId == null || plotId == null) {
                        return Optional.empty();
                }
                Towny town = getTowny(townId);
                if (town == null) {
                        return Optional.empty();
                }
                Plot plot = town.getPlots().get(plotId);
                if (plot == null || plot.getType() != PlotType.JAIL) {
                        return Optional.empty();
                }
                return getJailSpawn(town, plot);
        }

        public static Optional<Plot> findPrimaryJailPlot(Towny town) {
                if (town == null) {
                        return Optional.empty();
                }
                return town.getPlotsOfType(PlotType.JAIL).stream().findFirst();
        }

        public static Optional<Location<World>> getJailSpawn(Towny town, Plot plot) {
                if (town == null || plot == null || plot.getType() != PlotType.JAIL) {
                        return Optional.empty();
                }
                Rect rect = plot.getRect();
                Optional<World> worldOpt = Sponge.getServer().getWorld(rect.getWorld());
                if (!worldOpt.isPresent()) {
                        return Optional.empty();
                }
                World world = worldOpt.get();
                int centerX = (rect.getMinX() + rect.getMaxX()) / 2;
                int centerZ = (rect.getMinY() + rect.getMaxY()) / 2;
                Vector3i highest = world.getHighestPositionAt(centerX, centerZ);
                double y = highest.getY() + 1;
                Location<World> location = world.getLocation(centerX + 0.5, y, centerZ + 0.5);
                return Optional.of(location);
        }

        public static List<UUID> getTownJailedResidents(UUID townId) {
                List<UUID> jailed = new ArrayList<>();
                if (townId == null) {
                        return jailed;
                }
                for (Resident resident : RESIDENTS.values()) {
                        if (!resident.isJailed()) {
                                continue;
                        }
                        if (!townId.equals(resident.getJailTownId())) {
                                continue;
                        }
                        if (tryReleaseResidentFromJail(resident.getId())) {
                                continue;
                        }
                        jailed.add(resident.getId());
                }
                return jailed;
        }

        public static void releaseResidentsInJailPlot(UUID townId, UUID plotId) {
                if (townId == null || plotId == null) {
                        return;
                }
                for (Resident resident : RESIDENTS.values()) {
                        if (!resident.isJailed()) {
                                continue;
                        }
                        if (!townId.equals(resident.getJailTownId())) {
                                continue;
                        }
                        if (!plotId.equals(resident.getJailPlotId())) {
                                continue;
                        }
                        setResidentJailed(resident.getId(), false, null, null, 0L);
                        Sponge.getServer().getPlayer(resident.getId()).ifPresent(player ->
                                player.sendMessage(Text.of(LanguageHandler.INFO_JAIL_RELEASE)));
                }
        }

        public static void clearResidentJailRequests(UUID id) {
                Resident r = ensureResident(id);
                r.getJailRequests().clear();
                saveResidents();
        }

        // ------------------------------------------------------------
        // Spawn management
        // ------------------------------------------------------------
        public static String getResidentPreferredSpawn(UUID id) { return ensureResident(id).getPreferredSpawn(); }

        public static void setResidentPreferredSpawn(UUID id, String spawn) {
                Resident r = ensureResident(id);
                r.setPreferredSpawn(spawn);
                saveResidents();
        }

        public static long getResidentSpawnCooldown(UUID id) { return ensureResident(id).getSpawnCooldownEndsAt(); }

        public static void setResidentSpawnCooldown(UUID id, long epochMs) {
                Resident r = ensureResident(id);
                r.setSpawnCooldownEndsAt(epochMs);
                saveResidents();
        }

        public static long getResidentLastSpawn(UUID id) { return ensureResident(id).getLastSpawnAt(); }

        public static void markResidentSpawn(UUID id, UUID townId, long cooldownMillis) {
                Resident r = ensureResident(id);
                long now = System.currentTimeMillis();
                r.setLastSpawnAt(now);
                r.setLastSpawnTownId(townId);
                if (cooldownMillis > 0) {
                        r.setSpawnCooldownEndsAt(now + cooldownMillis);
                } else {
                        r.setSpawnCooldownEndsAt(0L);
                }
                saveResidents();
        }

        public static boolean isResidentSpawnAtHomeOnLogin(UUID id) { return ensureResident(id).isSpawnAtHomeOnLogin(); }

        public static void setResidentSpawnAtHomeOnLogin(UUID id, boolean enabled) {
                Resident r = ensureResident(id);
                r.setSpawnAtHomeOnLogin(enabled);
                saveResidents();
        }

        public static void setResidentBedSpawnWarmup(UUID id, long epochMs) {
                Resident r = ensureResident(id);
                r.setBedSpawnWarmupEndsAt(epochMs);
                saveResidents();
        }

        public static long getResidentBedSpawnWarmup(UUID id) { return ensureResident(id).getBedSpawnWarmupEndsAt(); }

        // ------------------------------------------------------------
        // Mode + notification management
        // ------------------------------------------------------------
        public static Set<String> getResidentModes(UUID id) {
                return new LinkedHashSet<>(ensureResident(id).getModes());
        }

        public static boolean toggleResidentMode(UUID id, String mode) {
                Resident r = ensureResident(id);
                boolean enabled = r.toggleMode(mode);
                saveResidents();
                return enabled;
        }

        public static boolean setResidentMode(UUID id, String mode, boolean enabled) {
                Resident r = ensureResident(id);
                boolean changed = r.setMode(mode, enabled);
                if (changed) saveResidents();
                return changed;
        }

        public static boolean isResidentNotificationMuted(UUID id, String notification) {
                return ensureResident(id).isNotificationMuted(notification);
        }

        public static void setResidentNotificationMuted(UUID id, String notification, boolean muted) {
                Resident r = ensureResident(id);
                r.setNotificationMuted(notification, muted);
                saveResidents();
        }

        // ------------------------------------------------------------
        // Misc flags and invites
        // ------------------------------------------------------------
        public static boolean isResidentIgnoringTowny(UUID id) { return ensureResident(id).isIgnoreTowny(); }

        public static void setResidentIgnoringTowny(UUID id, boolean ignore) {
                Resident r = ensureResident(id);
                r.setIgnoreTowny(ignore);
                saveResidents();
        }

        public static boolean isResidentDebugMode(UUID id) { return ensureResident(id).isDebugMode(); }

        public static void setResidentDebugMode(UUID id, boolean enabled) {
                Resident r = ensureResident(id);
                r.setDebugMode(enabled);
                saveResidents();
        }

        public static String getResidentProtectionStatus(UUID id) { return ensureResident(id).getProtectionStatus(); }

        public static void setResidentProtectionStatus(UUID id, String status) {
                Resident r = ensureResident(id);
                r.setProtectionStatus(status);
                saveResidents();
        }

        public static Set<String> getResidentPendingTownInvites(UUID id) {
                return new LinkedHashSet<>(ensureResident(id).getPendingTownInvites());
        }

        public static void addResidentTownInvite(UUID id, String townName) {
                Resident r = ensureResident(id);
                r.addTownInvite(townName);
                saveResidents();
        }

        public static void removeResidentTownInvite(UUID id, String townName) {
                Resident r = ensureResident(id);
                r.removeTownInvite(townName);
                saveResidents();
        }

        public static void clearResidentTownInvites(UUID id) {
                Resident r = ensureResident(id);
                r.clearTownInvites();
                saveResidents();
        }

        public static void addResidentPlotInvite(UUID id, UUID plotId) {
                Resident r = ensureResident(id);
                r.addPlotInvite(plotId);
                saveResidents();
        }

        public static void removeResidentPlotInvite(UUID id, UUID plotId) {
                Resident r = ensureResident(id);
                r.removePlotInvite(plotId);
                saveResidents();
        }

        public static Set<UUID> getResidentPendingPlotInvites(UUID id) {
                return new LinkedHashSet<>(ensureResident(id).getPendingPlotInvites());
        }

        public static void incrementResidentWarStat(UUID id, String key) {
                Resident r = ensureResident(id);
                r.incrementWarStat(key);
                saveResidents();
        }

        public static Map<String, Integer> getResidentWarStats(UUID id) {
                return new LinkedHashMap<>(ensureResident(id).getWarStats());
        }

        // Title
        public static String getResidentTitle(UUID id) {
                Resident r = ensureResident(id);
                return r.getTitle();
        }

	public static void setResidentTitle(UUID id, String title) {
		Resident r = ensureResident(id);
		r.setTitle(title == null ? "" : title);
		saveResidents();
	}

	// Auto map toggle
	public static boolean isAutoMap(UUID id) {
		return ensureResident(id).isAutoMap();
	}

	public static void setAutoMap(UUID id, boolean enabled) {
		Resident r = ensureResident(id);
		r.setAutoMap(enabled);
		if (enabled) {
			r.setLastAutoMapTs(System.currentTimeMillis());
		}
		saveResidents();
	}

	public static long getLastAutoMapTs(UUID id) {
		return ensureResident(id).getLastAutoMapTs();
	}

	public static void setLastAutoMapTs(UUID id, long epochMs) {
		Resident r = ensureResident(id);
		r.setLastAutoMapTs(epochMs);
		saveResidents();
	}

	// Friends
	public static Set<UUID> getResidentFriends(UUID id) {
		return new LinkedHashSet<>(ensureResident(id).getFriends());
	}

	public static boolean addResidentFriend(UUID id, UUID friend) {
		Resident r = ensureResident(id);
		boolean added = r.getFriends().add(friend);
		if (added) saveResidents();
		return added;
	}

	public static boolean removeResidentFriend(UUID id, UUID friend) {
		Resident r = ensureResident(id);
		boolean removed = r.getFriends().remove(friend);
		if (removed) saveResidents();
		return removed;
	}

	// ===== Resident: “about” =====
	public static String getResidentAbout(UUID id) {
		return ensureResident(id).getAbout();
	}
	public static void setResidentAbout(UUID id, String about) {
		Resident r = ensureResident(id);
		r.setAbout(about);
		saveResidents();
	}

	// ===== Resident: toggles =====
	public static boolean getResidentAutoClaim(UUID id) { return ensureResident(id).isAutoClaim(); }
	public static void setResidentAutoClaim(UUID id, boolean v) { ensureResident(id).setAutoClaim(v); saveResidents(); }

	public static boolean getResidentAutoUnclaim(UUID id) { return ensureResident(id).isAutoUnclaim(); }
	public static void setResidentAutoUnclaim(UUID id, boolean v) { ensureResident(id).setAutoUnclaim(v); saveResidents(); }

	public static boolean getResidentPreferBedSpawn(UUID id) { return ensureResident(id).isPreferBedSpawn(); }
	public static void setResidentPreferBedSpawn(UUID id, boolean v) { ensureResident(id).setPreferBedSpawn(v); saveResidents(); }

	public static boolean getResidentPlotBorder(UUID id) { return ensureResident(id).isPlotBorder(); }
	public static void setResidentPlotBorder(UUID id, boolean v) { ensureResident(id).setPlotBorder(v); saveResidents(); }

	public static boolean getResidentConstantPlotBorder(UUID id) { return ensureResident(id).isConstantPlotBorder(); }
	public static void setResidentConstantPlotBorder(UUID id, boolean v) { ensureResident(id).setConstantPlotBorder(v); saveResidents(); }

	public static boolean getResidentTownBorder(UUID id) { return ensureResident(id).isTownBorder(); }
	public static void setResidentTownBorder(UUID id, boolean v) { ensureResident(id).setTownBorder(v); saveResidents(); }

	public static boolean getResidentBorderTitles(UUID id) { return ensureResident(id).isBorderTitles(); }
	public static void setResidentBorderTitles(UUID id, boolean v) { ensureResident(id).setBorderTitles(v); saveResidents(); }

	public static boolean getResidentPvp(UUID id) { return ensureResident(id).isPvp(); }
	public static void setResidentPvp(UUID id, boolean v) { ensureResident(id).setPvp(v); saveResidents(); }

	public static boolean getResidentFire(UUID id) { return ensureResident(id).isFire(); }
	public static void setResidentFire(UUID id, boolean v) { ensureResident(id).setFire(v); saveResidents(); }

	public static boolean getResidentExplosion(UUID id) { return ensureResident(id).isExplosion(); }
	public static void setResidentExplosion(UUID id, boolean v) { ensureResident(id).setExplosion(v); saveResidents(); }

	public static boolean getResidentMobs(UUID id) { return ensureResident(id).isMobs(); }
	public static void setResidentMobs(UUID id, boolean v) { ensureResident(id).setMobs(v); saveResidents(); }

	public static boolean getResidentSpy(UUID id) { return ensureResident(id).isSpy(); }
	public static void setResidentSpy(UUID id, boolean v) { ensureResident(id).setSpy(v); saveResidents(); }

	public static boolean getResidentIgnorePlots(UUID id) { return ensureResident(id).isIgnorePlots(); }
	public static void setResidentIgnorePlots(UUID id, boolean v) { ensureResident(id).setIgnorePlots(v); saveResidents(); }

	public static boolean getResidentPlotGroupMode(UUID id) { return ensureResident(id).isPlotGroupMode(); }
	public static void setResidentPlotGroupMode(UUID id, boolean v) { ensureResident(id).setPlotGroupMode(v); saveResidents(); }

	public static boolean getResidentDistrictMode(UUID id) { return ensureResident(id).isDistrictMode(); }
	public static void setResidentDistrictMode(UUID id, boolean v) { ensureResident(id).setDistrictMode(v); saveResidents(); }

	public static boolean getResidentAdminBypass(UUID id) { return ensureResident(id).isAdminBypass(); }
	public static void setResidentAdminBypass(UUID id, boolean v) { ensureResident(id).setAdminBypass(v); saveResidents(); }

	public static boolean getResidentInfoTool(UUID id) { return ensureResident(id).isInfoTool(); }
	public static void setResidentInfoTool(UUID id, boolean v) { ensureResident(id).setInfoTool(v); saveResidents(); }

	// ===== Compatibility aliases used by your PlayerMoveListener =====
	public static boolean isResidentAutoMap(UUID id) { return ensureResident(id).isAutoMap(); }
	public static long getResidentLastMap(UUID id) { return ensureResident(id).getLastAutoMapTs(); }
	public static void setResidentLastMap(UUID id, long ts) { ensureResident(id).setLastAutoMapTs(ts); saveResidents(); }
	public static void setResidentAutoMap(UUID id, boolean enabled) { ensureResident(id).setAutoMap(enabled); saveResidents(); }

}
