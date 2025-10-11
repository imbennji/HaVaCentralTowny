package com.arckenver.towny.object;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.Map.Entry;

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
	public static final String TYPE_CITIZEN = "citizen";
	public static final String TYPE_COOWNER = "coowner";

	public static final String PERM_BUILD = "build";
	public static final String PERM_INTERACT = "interact";

	private UUID uuid;
	private String name;
	private String tag;
	private String displayName;
	private boolean isAdmin;
	private Hashtable<String, Location<World>> spawns;
	private Region region;
	private UUID mayor;
	private ArrayList<UUID> comayor;
	private ArrayList<UUID> citizens;
        private Hashtable<String, Hashtable<String, Boolean>> perms;
        private Hashtable<String, Boolean> flags;
        private Hashtable<UUID, Plot> plots;
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
		this.perms = new Hashtable<String, Hashtable<String, Boolean>>() {{
			put(TYPE_OUTSIDER, new Hashtable<String, Boolean>() {{
				put(PERM_BUILD, ConfigHandler.getNode("towny", "perms").getNode(TYPE_OUTSIDER).getNode(PERM_BUILD).getBoolean());
				put(PERM_INTERACT, ConfigHandler.getNode("towny", "perms").getNode(TYPE_OUTSIDER).getNode(PERM_INTERACT).getBoolean());
			}});
			put(TYPE_CITIZEN, new Hashtable<String, Boolean>() {{
				put(PERM_BUILD, ConfigHandler.getNode("towny", "perms").getNode(TYPE_CITIZEN).getNode(PERM_BUILD).getBoolean());
				put(PERM_INTERACT, ConfigHandler.getNode("towny", "perms").getNode(TYPE_CITIZEN).getNode(PERM_INTERACT).getBoolean());
			}});
		}};
		this.plots = new Hashtable<>();
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
		return perms.get(type).get(perm);
	}

	public Hashtable<String, Hashtable<String, Boolean>> getPerms() {
		return perms;
	}

	public void setPerm(String type, String perm, boolean bool) {
		perms.get(type).put(perm, bool);
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
