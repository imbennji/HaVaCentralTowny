package com.arckenver.towny;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.arckenver.towny.channel.AdminSpyMessageChannel;
import com.arckenver.towny.channel.TownyMessageChannel;
import com.arckenver.towny.object.*;
import com.arckenver.towny.serializer.TownyDeserializer;
import com.arckenver.towny.serializer.TownySerializer;
import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.math.IntMath;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

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
	private static Gson gson;

	// Residents file + storage
	private static final Map<UUID, Resident> RESIDENTS = new HashMap<>();
	private static File residentsFile;

	// --- Existing storage ---
	private static Hashtable<UUID, Towny> towny;
	private static Hashtable<UUID, Hashtable<Vector2i, ArrayList<Towny>>> worldChunks;
	private static HashMap<UUID, Towny> lastTownyWalkedOn;
	private static HashMap<UUID, Plot> lastPlotWalkedOn;
	private static Hashtable<UUID, Point> firstPoints;
	private static Hashtable<UUID, Point> secondPoints;
	private static Hashtable<UUID, UUID> markJobs;
	private static ArrayList<Request> inviteRequests;
	private static ArrayList<Request> joinRequests;
	private static AdminSpyMessageChannel spyChannel;

	// ------------------------------------------------------------
	// Lifecycle
	// ------------------------------------------------------------
	public static void init(File rootDir)
	{
		townyDir = new File(rootDir, "towns");

		gson = (new GsonBuilder())
				.registerTypeAdapter(Towny.class, new TownySerializer())
				.registerTypeAdapter(Towny.class, new TownyDeserializer())
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

		// Load residents
		loadResidents();

		calculateWorldChunks();
		lastTownyWalkedOn = new HashMap<>();
		lastPlotWalkedOn = new HashMap<>();
		firstPoints = new Hashtable<>();
		secondPoints = new Hashtable<>();
		markJobs = new Hashtable<>();
		inviteRequests = new ArrayList<>();
		joinRequests = new ArrayList<>();
		spyChannel = new AdminSpyMessageChannel();
	}

	public static void save()
	{
		for (UUID uuid : towny.keySet()) {
			saveTowny(uuid);
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

	public static void removeTowny(UUID uuid)
	{
		Towny oldTowny = getTowny(uuid);
		if (oldTowny != null) {
			MessageChannel.TO_CONSOLE.send(Text.of("Removing Towny " + uuid + ": "));
			MessageChannel.TO_CONSOLE.send(Utils.formatTownyDescription(oldTowny, Utils.CLICKER_ADMIN));
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
		Towny t = getTowny(loc);
		if (t == null)
		{
			return ConfigHandler.getNode("worlds")
					.getNode(loc.getExtent().getName())
					.getNode("perms").getNode(perm).getBoolean();
		}
		Plot plot = t.getPlot(loc);
		if (plot == null)
		{
			if (t.isCitizen(playerUUID))
			{
				if (t.isStaff(playerUUID))
				{
					return true;
				}
				return t.getPerm(Towny.TYPE_CITIZEN, perm);
			}
			return t.getPerm(Towny.TYPE_OUTSIDER, perm);
		}

		if (t.isStaff(playerUUID) || plot.isOwner(playerUUID))
			return true;
		if (plot.isCoowner(playerUUID))
			return plot.getPerm(Towny.TYPE_COOWNER, perm);
		if (t.isCitizen(playerUUID))
			return plot.getPerm(Towny.TYPE_CITIZEN, perm);

		return plot.getPerm(Towny.TYPE_OUTSIDER, perm);
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
		if (ConfigHandler.getNode("others", "enableGoldenAxe").getBoolean(true))
		{
			return firstPoints.get(uuid);
		}
		Optional<Player> player = Sponge.getServer().getPlayer(uuid);
		if (!player.isPresent())
		{
			return null;
		}
		Vector3i chunk = player.get().getLocation().getChunkPosition();
		return new Point(player.get().getWorld(), chunk.getX() * 16, chunk.getZ() * 16);
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
		if (ConfigHandler.getNode("others", "enableGoldenAxe").getBoolean(true))
		{
			return secondPoints.get(uuid);
		}
		Optional<Player> player = Sponge.getServer().getPlayer(uuid);
		if (!player.isPresent())
		{
			return null;
		}
		Vector3i chunk = player.get().getLocation().getChunkPosition();
		return new Point(player.get().getWorld(), chunk.getX() * 16 + 15, chunk.getZ() * 16 + 15);
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
						if (src == null) src = new Resident(id);
						// normalize to ensure UUID is set
                                                Resident fixed = new Resident(id);
                                                fixed.setTitle(src.getTitle());
                                                fixed.setAbout(src.getAbout());
                                                fixed.setAutoMap(src.isAutoMap());
                                                fixed.setLastAutoMapTs(src.getLastAutoMapTs());
                                                fixed.getFriends().addAll(src.getFriends());
                                                fixed.setAutoClaim(src.isAutoClaim());
                                                fixed.setAutoUnclaim(src.isAutoUnclaim());
                                                fixed.setPreferBedSpawn(src.isPreferBedSpawn());
                                                fixed.setPlotBorder(src.isPlotBorder());
                                                fixed.setConstantPlotBorder(src.isConstantPlotBorder());
                                                fixed.setTownBorder(src.isTownBorder());
                                                fixed.setBorderTitles(src.isBorderTitles());
                                                fixed.setPvp(src.isPvp());
                                                fixed.setFire(src.isFire());
                                                fixed.setExplosion(src.isExplosion());
                                                fixed.setMobs(src.isMobs());
                                                fixed.setSpy(src.isSpy());
                                                fixed.setIgnorePlots(src.isIgnorePlots());
                                                fixed.setPlotGroupMode(src.isPlotGroupMode());
                                                fixed.setDistrictMode(src.isDistrictMode());
                                                fixed.setAdminBypass(src.isAdminBypass());
                                                fixed.setInfoTool(src.isInfoTool());
                                                RESIDENTS.put(id, fixed);
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
		return RESIDENTS.computeIfAbsent(id, Resident::new);
	}

	public static Optional<Resident> getResident(UUID id) {
		return Optional.ofNullable(RESIDENTS.get(id));
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
