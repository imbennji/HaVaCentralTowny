package com.arckenver.towny.object;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Hashtable;
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
		this.perms = new Hashtable<String, Hashtable<String, Boolean>>()
		{{
			put(Towny.TYPE_OUTSIDER, new Hashtable<String, Boolean>()
			{{
				put(Towny.PERM_BUILD, ConfigHandler.getNode("plots", "perms").getNode(Towny.TYPE_OUTSIDER).getNode(Towny.PERM_BUILD).getBoolean());
				put(Towny.PERM_INTERACT, ConfigHandler.getNode("plots", "perms").getNode(Towny.TYPE_OUTSIDER).getNode(Towny.PERM_INTERACT).getBoolean());
			}});
			put(Towny.TYPE_CITIZEN, new Hashtable<String, Boolean>()
			{{
				put(Towny.PERM_BUILD, ConfigHandler.getNode("plots", "perms").getNode(Towny.TYPE_CITIZEN).getNode(Towny.PERM_BUILD).getBoolean());
				put(Towny.PERM_INTERACT, ConfigHandler.getNode("plots", "perms").getNode(Towny.TYPE_CITIZEN).getNode(Towny.PERM_INTERACT).getBoolean());
			}});
			put(Towny.TYPE_COOWNER, new Hashtable<String, Boolean>()
			{{
				put(Towny.PERM_BUILD, ConfigHandler.getNode("plots", "perms").getNode(Towny.TYPE_COOWNER).getNode(Towny.PERM_BUILD).getBoolean());
				put(Towny.PERM_INTERACT, ConfigHandler.getNode("plots", "perms").getNode(Towny.TYPE_COOWNER).getNode(Towny.PERM_INTERACT).getBoolean());
			}});
		}};
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
		return perms.get(type).get(perm);
	}

	public Hashtable<String, Hashtable<String, Boolean>> getPerms()
	{
		return perms;
	}

	public void setPerm(String type, String perm, boolean bool)
	{
		perms.get(type).put(perm, bool);
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
