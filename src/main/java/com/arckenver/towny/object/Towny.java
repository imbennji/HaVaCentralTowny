package com.arckenver.towny.object;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.arckenver.towny.ConfigHandler;
import com.arckenver.towny.claim.ChunkClaimUtils;
import com.arckenver.towny.channel.TownyMessageChannel;
import com.flowpowered.math.vector.Vector2i;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;

public class Towny {
        public static final String TYPE_OUTSIDER = "outsider";
        public static final String TYPE_RESIDENT = "resident";
        public static final String TYPE_CITIZEN = TYPE_RESIDENT;
        public static final String TYPE_ALLY = "ally";
        public static final String TYPE_NATION = "nation";
        public static final String TYPE_FRIEND = "friend";
        public static final String TYPE_COOWNER = TYPE_FRIEND;

        public static final String PERM_BUILD = "build";
        public static final String PERM_DESTROY = "destroy";
        public static final String PERM_SWITCH = "switch";
        public static final String PERM_ITEM_USE = "itemuse";
        public static final String PERM_INTERACT = "interact"; // legacy alias used by Sponge world config

        private static final List<String> TOWN_PERMISSION_KEYS = Arrays.asList(
                        PERM_BUILD, PERM_DESTROY, PERM_SWITCH, PERM_ITEM_USE);

        private static final Map<String, String> TOWN_TYPE_ALIASES = new HashMap<>();
        private static final Map<String, String> PLOT_TYPE_ALIASES = new HashMap<>();
        private static final Map<String, String> PERM_ALIASES = new HashMap<>();

        static {
                // Town context groups
                TOWN_TYPE_ALIASES.put(TYPE_RESIDENT, TYPE_RESIDENT);
                TOWN_TYPE_ALIASES.put("citizens", TYPE_RESIDENT);
                TOWN_TYPE_ALIASES.put("citizen", TYPE_RESIDENT);
                TOWN_TYPE_ALIASES.put("res", TYPE_RESIDENT);
                TOWN_TYPE_ALIASES.put(TYPE_ALLY, TYPE_ALLY);
                TOWN_TYPE_ALIASES.put("allies", TYPE_ALLY);
                TOWN_TYPE_ALIASES.put(TYPE_NATION, TYPE_NATION);
                TOWN_TYPE_ALIASES.put("nationals", TYPE_NATION);
                TOWN_TYPE_ALIASES.put(TYPE_OUTSIDER, TYPE_OUTSIDER);
                TOWN_TYPE_ALIASES.put("outsiders", TYPE_OUTSIDER);

                // Plot context groups (friends == co-owners)
                PLOT_TYPE_ALIASES.put(TYPE_FRIEND, TYPE_FRIEND);
                PLOT_TYPE_ALIASES.put("friends", TYPE_FRIEND);
                PLOT_TYPE_ALIASES.put("coowner", TYPE_FRIEND);
                PLOT_TYPE_ALIASES.put("coowners", TYPE_FRIEND);
                PLOT_TYPE_ALIASES.put(TYPE_RESIDENT, TYPE_RESIDENT);
                PLOT_TYPE_ALIASES.put("citizen", TYPE_RESIDENT);
                PLOT_TYPE_ALIASES.put("citizens", TYPE_RESIDENT);
                PLOT_TYPE_ALIASES.put(TYPE_ALLY, TYPE_ALLY);
                PLOT_TYPE_ALIASES.put("allies", TYPE_ALLY);
                PLOT_TYPE_ALIASES.put(TYPE_OUTSIDER, TYPE_OUTSIDER);
                PLOT_TYPE_ALIASES.put("outsiders", TYPE_OUTSIDER);

                // Permission aliases (legacy support + shorthand)
                PERM_ALIASES.put(PERM_BUILD, PERM_BUILD);
                PERM_ALIASES.put("place", PERM_BUILD);
                PERM_ALIASES.put(PERM_DESTROY, PERM_DESTROY);
                PERM_ALIASES.put("break", PERM_DESTROY);
                PERM_ALIASES.put(PERM_SWITCH, PERM_SWITCH);
                PERM_ALIASES.put("toggle", PERM_SWITCH);
                PERM_ALIASES.put("lever", PERM_SWITCH);
                PERM_ALIASES.put(PERM_ITEM_USE, PERM_ITEM_USE);
                PERM_ALIASES.put("item_use", PERM_ITEM_USE);
                PERM_ALIASES.put("use", PERM_ITEM_USE);
                PERM_ALIASES.put(PERM_INTERACT, PERM_SWITCH);
        }

        public static String canonicalizeTownType(String type) {
                return canonicalize(type, TOWN_TYPE_ALIASES, TYPE_OUTSIDER);
        }

        public static String canonicalizePlotType(String type) {
                return canonicalize(type, PLOT_TYPE_ALIASES, TYPE_OUTSIDER);
        }

        public static String canonicalizePerm(String perm) {
                if (perm == null) {
                        return null;
                }
                String lowered = perm.toLowerCase(Locale.ENGLISH);
                return PERM_ALIASES.getOrDefault(lowered, lowered);
        }

        private static String canonicalize(String raw, Map<String, String> aliases, String fallback) {
                if (raw == null) {
                        return fallback;
                }
                String lowered = raw.toLowerCase(Locale.ENGLISH);
                return aliases.getOrDefault(lowered, lowered);
        }

        public static Collection<String> expandPermKeys(String perm) {
                String canonical = canonicalizePerm(perm);
                if (canonical == null) {
                        return Collections.emptyList();
                }
                if (PERM_SWITCH.equals(canonical) && PERM_INTERACT.equalsIgnoreCase(perm)) {
                        return Arrays.asList(PERM_SWITCH, PERM_ITEM_USE);
                }
                return Collections.singletonList(canonical);
        }

        private Hashtable<String, Boolean> ensureTownPermContainer(String type) {
                String canonical = canonicalizeTownType(type);
                return perms.computeIfAbsent(canonical, this::buildTownPermDefaults);
        }

        private Hashtable<String, Boolean> buildTownPermDefaults(String type) {
                Hashtable<String, Boolean> defaults = new Hashtable<>();
                for (String key : TOWN_PERMISSION_KEYS) {
                        defaults.put(key, resolveTownPermDefault(type, key));
                }
                return defaults;
        }

        private boolean resolveTownPermDefault(String type, String permKey) {
                CommentedConfigurationNode base = ConfigHandler.getNode("towny", "perms");
                CommentedConfigurationNode node = base.getNode(type, permKey);
                if (!node.isVirtual()) {
                        return node.getBoolean(defaultTownPermValue(type, permKey));
                }
                if (PERM_DESTROY.equals(permKey)) {
                        return base.getNode(type, PERM_BUILD).getBoolean(defaultTownPermValue(type, PERM_BUILD));
                }
                if (PERM_SWITCH.equals(permKey) || PERM_ITEM_USE.equals(permKey)) {
                        CommentedConfigurationNode legacy = base.getNode(type, PERM_INTERACT);
                        if (!legacy.isVirtual()) {
                                return legacy.getBoolean(defaultTownPermValue(type, permKey));
                        }
                }
                return defaultTownPermValue(type, permKey);
        }

        private boolean defaultTownPermValue(String type, String permKey) {
                if (TYPE_RESIDENT.equals(type)) {
                        if (PERM_BUILD.equals(permKey) || PERM_DESTROY.equals(permKey)) {
                                return false;
                        }
                        if (PERM_SWITCH.equals(permKey) || PERM_ITEM_USE.equals(permKey)) {
                                return true;
                        }
                }
                if (TYPE_ALLY.equals(type) || TYPE_NATION.equals(type)) {
                        return PERM_SWITCH.equals(permKey) || PERM_ITEM_USE.equals(permKey);
                }
                return false;
        }

        private boolean getTownPermInternal(String type, String permKey) {
                Hashtable<String, Boolean> map = ensureTownPermContainer(type);
                if (!map.containsKey(permKey)) {
                        map.put(permKey, resolveTownPermDefault(type, permKey));
                }
                return map.get(permKey);
        }

        private void setTownPermInternal(String type, String permKey, boolean value) {
                Hashtable<String, Boolean> map = ensureTownPermContainer(type);
                map.put(permKey, value);
        }

	private UUID uuid;
	private String name;
	private String tag;
	private String displayName;
	private boolean isAdmin;
        private static final Pattern OUTPOST_SPAWN_PATTERN = Pattern.compile("^outpost(\\d+)$", Pattern.CASE_INSENSITIVE);

        private Hashtable<String, Location<World>> spawns;
	private Region region;
	private UUID mayor;
	private ArrayList<UUID> comayor;
	private ArrayList<UUID> citizens;
        private Hashtable<String, Hashtable<String, Boolean>> perms;
        private Hashtable<String, Boolean> flags;
        private Hashtable<UUID, Plot> plots;
        private LinkedHashSet<UUID> outlaws;
        private UUID nationUUID;
	private int extras;
	private int extraspawns;
	private double taxes;
	private int rentInterval;// hours
	private LocalDateTime lastRentCollectTime;

	private TownyMessageChannel channel = new TownyMessageChannel();

	public Towny(UUID uuid, String name) {
		this(uuid, name, false);
	}

	@SuppressWarnings("serial")
	public Towny(UUID uuid, String name, boolean isAdmin) {
		this.uuid = uuid;
		this.name = name;
		this.tag = null;
		this.isAdmin = isAdmin;
		this.spawns = new Hashtable<>();
		this.region = new Region();
		this.mayor = null;
		this.comayor = new ArrayList<>();
		this.citizens = new ArrayList<>();
                this.flags = new Hashtable<>();
                this.rentInterval = ConfigHandler.getNode("towny", "defaultRentInterval").getInt();
                this.lastRentCollectTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(LocalTime.now().getHour(), 0)); //just hours

                for (Entry<Object, ? extends CommentedConfigurationNode> e : ConfigHandler.getNode("towny", "flags").getChildrenMap().entrySet()) {
                        flags.put(e.getKey().toString(), e.getValue().getBoolean());
                }

                this.perms = new Hashtable<>();
                ensureTownPermContainer(TYPE_RESIDENT);
                ensureTownPermContainer(TYPE_ALLY);
                ensureTownPermContainer(TYPE_NATION);
                ensureTownPermContainer(TYPE_OUTSIDER);
		this.plots = new Hashtable<>();
		this.outlaws = new LinkedHashSet<>();
		this.extras = 0;
		this.extraspawns = 0;
                this.taxes = ConfigHandler.getNode("towny", "defaultTaxes").getDouble();
                this.nationUUID = null;
        }

	public UUID getUUID() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public String getName() {
		return name.replace("_", " ");
	}

	public String getRealName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean hasTag() {
		return tag != null;
	}

	public String getTag() {
		if (tag == null)
			return getName();
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public boolean hasDisplayName() {
		return this.displayName != null;
	}

	public String getDisplayName() {
		if (this.displayName == null) {
			return this.name;
		}
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public boolean isAdmin() {
		return isAdmin;
	}

	public double getTaxes() {
		return taxes;
	}

	public void setTaxes(double taxes) {
		this.taxes = taxes;
	}

	public double getUpkeep() {
		return ConfigHandler.getNode("prices", "upkeepPerCitizen").getDouble() * citizens.size();
	}

        public Location<World> getSpawn(String name) {
                return spawns.get(name);
        }

        public void addSpawn(String name, Location<World> spawn) {
                this.spawns.put(name, spawn);
        }

        public void removeSpawn(String name) {
                this.spawns.remove(name);
        }

        public Hashtable<String, Location<World>> getSpawns() {
                return spawns;
        }

        public int getNumSpawns() {
                return spawns.size();
        }

        public int getMaxSpawns() {
                return ConfigHandler.getNode("others", "maxTownySpawns").getInt() + extraspawns;
        }

        public boolean hasOutpostSpawns() {
                return !getOutpostIndices().isEmpty();
        }

        public List<Integer> getOutpostIndices() {
                return spawns.keySet().stream()
                                .map(Towny::parseOutpostIndex)
                                .filter(i -> i > 0)
                                .sorted()
                                .collect(Collectors.toList());
        }

        public int getNextOutpostIndex() {
                int candidate = 1;
                while (spawns.containsKey(outpostSpawnKey(candidate))) {
                        candidate++;
                }
                return candidate;
        }

        public Location<World> getOutpostSpawn(int index) {
                return getSpawn(outpostSpawnKey(index));
        }

        public void setOutpostSpawn(int index, Location<World> spawn) {
                addSpawn(outpostSpawnKey(index), spawn);
        }

        private static String outpostSpawnKey(int index) {
                return "outpost" + index;
        }

        private static int parseOutpostIndex(String name) {
                if (name == null) {
                        return -1;
                }
                Matcher matcher = OUTPOST_SPAWN_PATTERN.matcher(name);
                if (!matcher.matches()) {
                        return -1;
                }
                try {
                        return Integer.parseInt(matcher.group(1));
                } catch (NumberFormatException ignored) {
                        return -1;
                }
        }

	public int getExtraSpawns() {
		return extraspawns;
	}

	public void setExtraSpawns(int extraspawns) {
		this.extraspawns = extraspawns;
		if (this.extraspawns < 0)
			this.extraspawns = 0;
	}

	public void addExtraSpawns(int extraspawns) {
		this.extraspawns += extraspawns;
	}

	public void removeExtraSpawns(int extraspawns) {
		this.extraspawns -= extraspawns;
		if (this.extraspawns < 0)
			this.extraspawns = 0;
	}

	public Region getRegion() {
		return region;
	}

	public void setRegion(Region region) {
		this.region = region;
	}

	public UUID getPresident() {
		return mayor;
	}

	public void setPresident(UUID mayor) {
		this.mayor = mayor;
	}

	public boolean isPresident(UUID uuid) {
		return uuid.equals(mayor);
	}

	public ArrayList<UUID> getMinisters() {
		return comayor;
	}

	public void addMinister(UUID uuid) {
		comayor.add(uuid);
	}

	public void removeMinister(UUID uuid) {
		comayor.remove(uuid);
	}

	public boolean isMinister(UUID uuid) {
		return comayor.contains(uuid);
	}

	public ArrayList<UUID> getStaff() {
		ArrayList<UUID> staff = new ArrayList<UUID>();
		staff.add(mayor);
		staff.addAll(comayor);
		return staff;
	}

	public boolean isStaff(UUID uuid) {
		if (isPresident(uuid) || isMinister(uuid))
			return true;
		if (!isAdmin())
			return false;
		Optional<Player> player = Sponge.getServer().getPlayer(uuid);
		if (player.isPresent() && player.get().hasPermission("towny.admin.plot.staff"))
			return true;
		return false;
	}

	// Your existing getCitizens() method
	public ArrayList<UUID> getCitizens() {
		return citizens;
	}

	public void addCitizen(UUID uuid) {
		citizens.add(uuid);
		Optional<Player> player = Sponge.getServer().getPlayer(uuid);
		player.ifPresent(player1 -> channel.addMember(player1));
	}

	public boolean isCitizen(UUID uuid) {
		return citizens.contains(uuid);
	}

	public int getNumCitizens() {
		return citizens.size();
	}

	public void removeCitizen(UUID uuid) {
		plots.values().stream()
				.filter(plot -> uuid.equals(plot.getOwner()))
				.forEach(plot -> plot.setOwner(null));
		comayor.remove(uuid);
		citizens.remove(uuid);
		Optional<Player> player = Sponge.getServer().getPlayer(uuid);
		if (player.isPresent()) {
			channel.removeMember(player.get());
			player.get().setMessageChannel(MessageChannel.TO_ALL);
		}
	}

	public Hashtable<String, Boolean> getFlags() {
		return flags;
	}

	public void setFlag(String flag, boolean b) {
		flags.put(flag, b);
	}

	public boolean getFlag(String flag) {
		return flags.get(flag);
	}

	public boolean getFlag(String flag, Location<World> loc) {
		Plot plot = getPlot(loc);
		if (plot == null || !plot.hasFlag(flag)) {
			return getFlag(flag);
		}
		return plot.getFlag(flag);
	}

        public boolean getPerm(String type, String perm) {
                Collection<String> keys = expandPermKeys(perm);
                if (keys.isEmpty()) {
                        return false;
                }
                String canonicalType = canonicalizeTownType(type);
                boolean allowed = true;
                for (String key : keys) {
                        allowed = allowed && getTownPermInternal(canonicalType, key);
                }
                return allowed;
        }

        public Hashtable<String, Hashtable<String, Boolean>> getPerms() {
                ensureTownPermContainer(TYPE_RESIDENT);
                ensureTownPermContainer(TYPE_ALLY);
                ensureTownPermContainer(TYPE_NATION);
                ensureTownPermContainer(TYPE_OUTSIDER);
                return perms;
        }

        public void setPerm(String type, String perm, boolean bool) {
                String canonicalType = canonicalizeTownType(type);
                Collection<String> keys = expandPermKeys(perm);
                if (keys.isEmpty()) {
                        return;
                }
                for (String key : keys) {
                        setTownPermInternal(canonicalType, key, bool);
                }
        }

	public Hashtable<UUID, Plot> getPlots() {
		return plots;
	}

	public Plot getPlot(Location<World> loc) {
		Vector2i p = new Vector2i(loc.getBlockX(), loc.getBlockZ());
		for (Plot plot : plots.values()) {
			if (plot.getRect().isInside(p)) {
				return plot;
			}
		}
		return null;
	}

	public void addPlot(Plot plot) {
		plots.put(plot.getUUID(), plot);
	}

	public void removePlot(UUID uuid) {
		plots.remove(uuid);
	}

	public List<Plot> getPlotsOfType(PlotType type) {
		if (type == null) {
			return Collections.emptyList();
		}
		return plots.values().stream()
			.filter(plot -> plot.getType() == type)
			.collect(Collectors.toList());
	}

	public boolean hasPlotOfType(PlotType type) {
		if (type == null) {
			return false;
		}
		return plots.values().stream().anyMatch(plot -> plot.getType() == type);
	}

	public Set<UUID> getOutlaws() {
		return Collections.unmodifiableSet(outlaws);
	}

	public boolean isOutlaw(UUID uuid) {
		return uuid != null && outlaws.contains(uuid);
	}

	public boolean addOutlaw(UUID uuid) {
		return uuid != null && outlaws.add(uuid);
	}

	public boolean removeOutlaw(UUID uuid) {
		return uuid != null && outlaws.remove(uuid);
	}

        public int getExtras() {
                return extras;
        }

        public void setExtras(int extras) {
                this.extras = normalizeChunkCount(extras);
                if (this.extras < 0)
                        this.extras = 0;
        }

        public boolean hasNation() {
                return nationUUID != null;
        }

        public UUID getNationUUID() {
                return nationUUID;
        }

        public void setNationUUID(UUID nationUUID) {
                this.nationUUID = nationUUID;
        }

        public void clearNation() {
                this.nationUUID = null;
        }

        public void addExtras(int extras) {
                this.extras += normalizeChunkCount(extras);
        }

        public void removeExtras(int extras) {
                this.extras -= normalizeChunkCount(extras);
                if (this.extras < 0)
                        this.extras = 0;
        }

        private int normalizeChunkCount(int value) {
                if (value >= ChunkClaimUtils.CHUNK_AREA) {
                        return (int) Math.ceil(value / (double) ChunkClaimUtils.CHUNK_AREA);
                }
                return value;
        }

        public int maxClaimArea() {
                int perCitizenChunks = ConfigHandler.getNode("others", "chunksPerCitizen").getInt();
                return (extras + citizens.size() * perCitizenChunks) * ChunkClaimUtils.CHUNK_AREA;
        }

        public int maxChunkAllowance() {
                int perCitizenChunks = ConfigHandler.getNode("others", "chunksPerCitizen").getInt();
                return extras + citizens.size() * perCitizenChunks;
        }

	public int getRentInterval() {
		return rentInterval;
	}

	public void setRentInterval(int rentInterval) {
		this.rentInterval = rentInterval;
	}

	// NEW: Town board
	private String board = ""; // keep empty, never null

	public String getBoard() {
		return board == null ? "" : board;
	}

	public void setBoard(String board) {
		this.board = board == null ? "" : board.trim();
	}


	public LocalDateTime getLastRentCollectTime() {
		return lastRentCollectTime;
	}

	public void setLastRentCollectTime(LocalDateTime lastRentCollectTime) {
		this.lastRentCollectTime = lastRentCollectTime;
	}

	public TownyMessageChannel getChannel() {
		return channel;
	}

	public void setChannel(TownyMessageChannel channel) {
		this.channel = channel;
	}

	public TownyMessageChannel getMessageChannel() {
		return channel;
	}

}
