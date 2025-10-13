package com.arckenver.towny.object;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import com.arckenver.towny.ConfigHandler;
import com.arckenver.towny.LanguageHandler;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;

public class Plot
{
	private UUID uuid;
	private String name;
	private String displayName;
	private UUID owner;
        private ArrayList<UUID> coowners;
        private Rect rect;
        private Hashtable<String, Hashtable<String, Boolean>> perms;
        private Hashtable<String, Boolean> flags;
        private BigDecimal price;
        private BigDecimal rentalPrice;

        private static final List<String> PLOT_PERMISSION_KEYS = Arrays.asList(
                        Towny.PERM_BUILD, Towny.PERM_DESTROY, Towny.PERM_SWITCH, Towny.PERM_ITEM_USE);
	
	public Plot(UUID uuid, String name, Rect rect)
	{
		this(uuid, name, rect, null);
	}
	
	@SuppressWarnings("serial")
        public Plot(UUID uuid, String name, Rect rect, UUID owner)
        {
                this.uuid = uuid;
                this.name = name;
                this.owner = owner;
		this.coowners = new ArrayList<UUID>();
		this.rect = rect;
		

                this.flags = new Hashtable<String, Boolean>();
                for (Entry<Object, ? extends CommentedConfigurationNode> e : ConfigHandler.getNode("flags", "plots").getChildrenMap().entrySet())
                {
                        flags.put(e.getKey().toString(), e.getValue().getBoolean());
                }
                this.perms = new Hashtable<>();
                ensurePlotPermContainer(Towny.TYPE_FRIEND);
                ensurePlotPermContainer(Towny.TYPE_RESIDENT);
                ensurePlotPermContainer(Towny.TYPE_ALLY);
                ensurePlotPermContainer(Towny.TYPE_OUTSIDER);
        }

        private Hashtable<String, Boolean> ensurePlotPermContainer(String type) {
                String canonical = Towny.canonicalizePlotType(type);
                return perms.computeIfAbsent(canonical, this::buildPlotPermDefaults);
        }

        private Hashtable<String, Boolean> buildPlotPermDefaults(String type) {
                Hashtable<String, Boolean> defaults = new Hashtable<>();
                for (String key : PLOT_PERMISSION_KEYS) {
                        defaults.put(key, resolvePlotPermDefault(type, key));
                }
                return defaults;
        }

        private boolean resolvePlotPermDefault(String type, String permKey) {
                CommentedConfigurationNode base = ConfigHandler.getNode("plots", "perms");
                CommentedConfigurationNode node = base.getNode(type, permKey);
                if (!node.isVirtual()) {
                        return node.getBoolean(defaultPlotPermValue(type, permKey));
                }
                if (Towny.PERM_DESTROY.equals(permKey)) {
                        return base.getNode(type, Towny.PERM_BUILD).getBoolean(defaultPlotPermValue(type, Towny.PERM_BUILD));
                }
                if (Towny.PERM_SWITCH.equals(permKey) || Towny.PERM_ITEM_USE.equals(permKey)) {
                        CommentedConfigurationNode legacy = base.getNode(type, Towny.PERM_INTERACT);
                        if (!legacy.isVirtual()) {
                                return legacy.getBoolean(defaultPlotPermValue(type, permKey));
                        }
                }

                // Legacy type fallbacks
                if (Towny.TYPE_FRIEND.equals(type)) {
                        CommentedConfigurationNode legacyType = base.getNode("coowner", permKey);
                        if (!legacyType.isVirtual()) {
                                return legacyType.getBoolean(defaultPlotPermValue(type, permKey));
                        }
                        if (Towny.PERM_SWITCH.equals(permKey) || Towny.PERM_ITEM_USE.equals(permKey)) {
                                CommentedConfigurationNode legacyInteract = base.getNode("coowner", Towny.PERM_INTERACT);
                                if (!legacyInteract.isVirtual()) {
                                        return legacyInteract.getBoolean(defaultPlotPermValue(type, permKey));
                                }
                        }
                }
                if (Towny.TYPE_RESIDENT.equals(type)) {
                        CommentedConfigurationNode legacyType = base.getNode("citizen", permKey);
                        if (!legacyType.isVirtual()) {
                                return legacyType.getBoolean(defaultPlotPermValue(type, permKey));
                        }
                        if (Towny.PERM_SWITCH.equals(permKey) || Towny.PERM_ITEM_USE.equals(permKey)) {
                                CommentedConfigurationNode legacyInteract = base.getNode("citizen", Towny.PERM_INTERACT);
                                if (!legacyInteract.isVirtual()) {
                                        return legacyInteract.getBoolean(defaultPlotPermValue(type, permKey));
                                }
                        }
                }
                return defaultPlotPermValue(type, permKey);
        }

        private boolean defaultPlotPermValue(String type, String permKey) {
                if (Towny.TYPE_FRIEND.equals(type)) {
                        return true;
                }
                if (Towny.TYPE_RESIDENT.equals(type)) {
                        if (Towny.PERM_BUILD.equals(permKey) || Towny.PERM_DESTROY.equals(permKey)) {
                                return false;
                        }
                        return true;
                }
                return false;
        }

        private boolean getPlotPermInternal(String type, String permKey) {
                Hashtable<String, Boolean> map = ensurePlotPermContainer(type);
                if (!map.containsKey(permKey)) {
                        map.put(permKey, resolvePlotPermDefault(type, permKey));
                }
                return map.get(permKey);
        }

        private void setPlotPermInternal(String type, String permKey, boolean value) {
                Hashtable<String, Boolean> map = ensurePlotPermContainer(type);
                map.put(permKey, value);
        }
	
	public UUID getUUID()
	{
		return uuid;
	}

	public String getName()
	{
      if (name == null)
			return LanguageHandler.DEFAULT_PLOTNAME;
		return name.replace("_", " ");
	}
	
	public String getRealName()
	{
		return name;
	}

	public boolean hasDisplayName() {
		return this.displayName != null;
	}

	public String getDisplayName() {
		if(this.displayName == null)
			return this.getName();
		return this.displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public UUID getOwner()
	{
		return owner;
	}

	public void setOwner(UUID owner)
	{
		this.owner = owner;
	}

	public boolean isOwner(UUID uuid)
	{
		return owner != null && owner.equals(uuid);
	}
	
	public boolean isOwned()
	{
		return owner != null;
	}
	
	public boolean isNamed()
	{
		return name != null;
	}

	public ArrayList<UUID> getCoowners()
	{
		return coowners;
	}

	public void addCoowner(UUID coowner)
	{
		this.coowners.add(coowner);
	}

	public void removeCoowner(UUID coowner)
	{
		this.coowners.remove(coowner);
	}

	public void resetCoowners()
	{
		this.coowners = new ArrayList<UUID>();
	}

	public boolean isCoowner(UUID uuid)
	{
		return coowners.contains(uuid);
	}
	
	public Rect getRect()
	{
		return rect;
	}

	public void setRect(Rect rect)
	{
		this.rect = rect;
	}

	public boolean getFlag(String flag)
	{
		return flags.get(flag);
	}

	public Hashtable<String, Boolean> getFlags()
	{
		return flags;
	}

	public void setFlag(String flag, boolean b)
	{
		flags.put(flag, b);
	}

	public boolean hasFlag(String flag)
	{
		return flags.containsKey(flag);
	}
	
        public boolean getPerm(String type, String perm)
        {
                Collection<String> keys = Towny.expandPermKeys(perm);
                if (keys.isEmpty()) {
                        return false;
                }
                String canonicalType = Towny.canonicalizePlotType(type);
                boolean allowed = true;
                for (String key : keys) {
                        allowed = allowed && getPlotPermInternal(canonicalType, key);
                }
                return allowed;
        }

        public Hashtable<String, Hashtable<String, Boolean>> getPerms()
        {
                ensurePlotPermContainer(Towny.TYPE_FRIEND);
                ensurePlotPermContainer(Towny.TYPE_RESIDENT);
                ensurePlotPermContainer(Towny.TYPE_ALLY);
                ensurePlotPermContainer(Towny.TYPE_OUTSIDER);
                return perms;
        }

        public void setPerm(String type, String perm, boolean bool)
        {
                String canonicalType = Towny.canonicalizePlotType(type);
                Collection<String> keys = Towny.expandPermKeys(perm);
                if (keys.isEmpty()) {
                        return;
                }
                for (String key : keys) {
                        setPlotPermInternal(canonicalType, key, bool);
                }
        }
	
	public BigDecimal getPrice()
	{
		return price;
	}
	
	public void setPrice(BigDecimal price)
	{
		this.price = price;
		this.rentalPrice = null;
	}
	
	public boolean isForSale()
	{
		return price != null;
	}

	public BigDecimal getRentalPrice() {
		return rentalPrice;
	}

	public void setRentalPrice(BigDecimal rentalPrice) {
		this.rentalPrice = rentalPrice;
		this.price = null;
	}

	public boolean isForRent() {
		return this.rentalPrice != null;
	}
}
