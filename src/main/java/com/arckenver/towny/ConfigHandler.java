package com.arckenver.towny;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.World;

import com.arckenver.towny.claim.ChunkClaimUtils;
import com.arckenver.towny.object.Towny;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import com.arckenver.towny.LanguageHandler;

public class ConfigHandler
{
	private static File configFile;
	private static ConfigurationLoader<CommentedConfigurationNode> configManager;
	private static CommentedConfigurationNode config;

        public static void init(File rootDir)
        {
                configFile = new File(rootDir, "TownsConfig.conf");
                configManager = HoconConfigurationLoader.builder().setPath(configFile.toPath()).build();
        }

        private static void ensureTownPermNode(CommentedConfigurationNode node, boolean defaultValue) {
                Utils.ensureBoolean(node, defaultValue);
        }

	public static void load()
	{
		load(null);
	}

        public static void load(CommandSource src)
        {
                // load file
                try
                {
			if (!configFile.exists())
			{
				configFile.getParentFile().mkdirs();
				configFile.createNewFile();
				config = configManager.load();
				configManager.save(config);
			}
			config = configManager.load();
		}
		catch (IOException e)
		{
			TownyPlugin.getLogger().error(LanguageHandler.ERROR_CONFIGFILE);
			e.printStackTrace();
			if (src != null)
			{
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_CONFIGFILE));
			}
                }

                migrateLegacyKeys();

                // check integrity
                Utils.ensurePositiveNumber(config.getNode("prices", "townyCreationPrice"), 2500);
                Utils.ensurePositiveNumber(config.getNode("prices", "upkeepPerCitizen"), 100);
                Utils.ensurePositiveNumber(config.getNode("prices", "unclaimRefundPercentage"), 0);
                Utils.ensurePositiveNumber(config.getNode("prices", "extraChunkPrice"), ChunkClaimUtils.CHUNK_AREA * 0.5);
                Utils.ensurePositiveNumber(config.getNode("prices", "chunkClaimPrice"), ChunkClaimUtils.CHUNK_AREA * 0.3);
                Utils.ensurePositiveNumber(config.getNode("prices", "outpostCreationPrice"), 1000);
                Utils.ensurePositiveNumber(config.getNode("prices", "nationCreationPrice"), 75000);
                Utils.ensurePositiveNumber(config.getNode("prices", "plotTaxPerDay"), 10);
                Utils.ensurePositiveNumber(config.getNode("others", "chunksPerCitizen"), 4);
		Utils.ensurePositiveNumber(config.getNode("others", "maxTownySpawns"), 3);
		Utils.ensurePositiveNumber(config.getNode("others", "minTownyDistance"), 500);
                Utils.ensurePositiveNumber(config.getNode("others", "maxExtraChunks"), 20);
                Utils.ensurePositiveNumber(config.getNode("others", "minTownyNameLength"), 3);
                Utils.ensurePositiveNumber(config.getNode("others", "maxTownyNameLength"), 13);
                Utils.ensurePositiveNumber(config.getNode("others", "minTownyTagLength"), 3);
                Utils.ensurePositiveNumber(config.getNode("others", "maxTownyTagLength"), 5);
                Utils.ensurePositiveNumber(config.getNode("others", "minNationNameLength"), 3);
                Utils.ensurePositiveNumber(config.getNode("others", "maxNationNameLength"), 13);
                Utils.ensurePositiveNumber(config.getNode("others", "minNationTagLength"), 2);
                Utils.ensurePositiveNumber(config.getNode("others", "maxNationTagLength"), 6);
                Utils.ensurePositiveNumber(config.getNode("others", "minTownyDisplayLength"), 3);
		Utils.ensurePositiveNumber(config.getNode("others", "maxTownyDisplayLength"), 32);
		Utils.ensurePositiveNumber(config.getNode("others", "minPlotNameLength"), 3);
		Utils.ensurePositiveNumber(config.getNode("others", "maxPlotNameLength"), 13);

                Utils.ensureBoolean(config.getNode("others", "enableTownyRanks"), true);
                Utils.ensureBoolean(config.getNode("others", "enableTownyTag"), true);

		// chat formats (use {TOWN}, not {NATION})
		Utils.ensureString(
				config.getNode("others", "publicChatFormat"),
				" &r[&3{TOWN}&r] &5{TITLE} &r"
		);
		Utils.ensureString(
				config.getNode("others", "townyChatFormat"),
				" &r{&eNC&r} "
		);
		Utils.ensureString(
				config.getNode("others", "townySpyChatTag"),
				" &r[&cSPY&r]"
		);

		// toast defaults (use {TOWN} and FORMATPLOT* tokens)
		Utils.ensureString(
				config.getNode("toast", "wild"),
				"&2{WILD} &7- {FORMATPVP}"
		);
		Utils.ensureString(
				config.getNode("toast", "towny"),
				"&3{TOWN}{FORMATPRESIDENT} &7- {FORMATPVP}"
		);
		Utils.ensureString(
				config.getNode("toast", "plot"),
				"&3{TOWN}{FORMATPRESIDENT} &7~ {FORMATPLOTNAME}{FORMATPLOTOWNER}{FORMATPLOTPRICE}{FORMATPVP}"
		);

		Utils.ensureString(
				config.getNode("toast", "formatPresident"),
				"&7 - &e{TITLE} {NAME}"
		);
		Utils.ensureString(
				config.getNode("toast", "formatPlotName"),
				"&a{ARG} &7-"
		);
		Utils.ensureString(
				config.getNode("toast", "formatPlotOwner"),
				"&e{ARG} &7-"
		);
		Utils.ensureString(
				config.getNode("toast", "formatPlotPrice"),
				"&e[{ARG}] &7-"
		);
		Utils.ensureString(
				config.getNode("toast", "formatPvp"),
				"&4({ARG})"
		);
		Utils.ensureString(
				config.getNode("toast", "formatNoPvp"),
				"&2({ARG})"
		);

                Utils.ensureBoolean(config.getNode("towny", "canEditTaxes"), true);
                Utils.ensurePositiveNumber(config.getNode("towny", "defaultTaxes"), 50);
                Utils.ensurePositiveNumber(config.getNode("towny", "maxTaxes"), 100);
                Utils.ensurePositiveNumber(config.getNode("towny", "defaultRentInterval"), 24);

                Utils.ensurePositiveNumber(config.getNode("nation", "defaultTaxes"), 0);
                Utils.ensurePositiveNumber(config.getNode("nation", "maxTaxes"), 1000);
                Utils.ensureBoolean(config.getNode("nation", "flags", "open"), false);
                Utils.ensureBoolean(config.getNode("nation", "flags", "neutral"), false);

                Utils.ensureBoolean(config.getNode("towny", "flags", "pvp"), false);
                Utils.ensureBoolean(config.getNode("towny", "flags", "mobs"), false);
                Utils.ensureBoolean(config.getNode("towny", "flags", "fire"), false);
                Utils.ensureBoolean(config.getNode("towny", "flags", "explosions"), false);
                Utils.ensureBoolean(config.getNode("towny", "flags", "open"), false);
                Utils.ensureBoolean(config.getNode("towny", "flags", "public"), false);
                Utils.ensureBoolean(config.getNode("towny", "flags", "taxpercent"), false);
                Utils.ensureBoolean(config.getNode("towny", "flags", "jail"), false);

                CommentedConfigurationNode townPermRoot = config.getNode("towny", "perms");
                CommentedConfigurationNode legacyCitizen = townPermRoot.getNode("citizen");
                ensureTownPermNode(townPermRoot.getNode(Towny.TYPE_RESIDENT, Towny.PERM_BUILD), legacyCitizen.getNode(Towny.PERM_BUILD).getBoolean(false));
                ensureTownPermNode(townPermRoot.getNode(Towny.TYPE_RESIDENT, Towny.PERM_DESTROY), legacyCitizen.getNode(Towny.PERM_BUILD).getBoolean(false));
                ensureTownPermNode(townPermRoot.getNode(Towny.TYPE_RESIDENT, Towny.PERM_SWITCH), legacyCitizen.getNode(Towny.PERM_INTERACT).getBoolean(true));
                ensureTownPermNode(townPermRoot.getNode(Towny.TYPE_RESIDENT, Towny.PERM_ITEM_USE), legacyCitizen.getNode(Towny.PERM_INTERACT).getBoolean(true));

                ensureTownPermNode(townPermRoot.getNode(Towny.TYPE_ALLY, Towny.PERM_BUILD), false);
                ensureTownPermNode(townPermRoot.getNode(Towny.TYPE_ALLY, Towny.PERM_DESTROY), false);
                ensureTownPermNode(townPermRoot.getNode(Towny.TYPE_ALLY, Towny.PERM_SWITCH), true);
                ensureTownPermNode(townPermRoot.getNode(Towny.TYPE_ALLY, Towny.PERM_ITEM_USE), true);

                ensureTownPermNode(townPermRoot.getNode(Towny.TYPE_NATION, Towny.PERM_BUILD), false);
                ensureTownPermNode(townPermRoot.getNode(Towny.TYPE_NATION, Towny.PERM_DESTROY), false);
                ensureTownPermNode(townPermRoot.getNode(Towny.TYPE_NATION, Towny.PERM_SWITCH), true);
                ensureTownPermNode(townPermRoot.getNode(Towny.TYPE_NATION, Towny.PERM_ITEM_USE), true);

                ensureTownPermNode(townPermRoot.getNode(Towny.TYPE_OUTSIDER, Towny.PERM_BUILD), false);
                ensureTownPermNode(townPermRoot.getNode(Towny.TYPE_OUTSIDER, Towny.PERM_DESTROY), false);
                ensureTownPermNode(townPermRoot.getNode(Towny.TYPE_OUTSIDER, Towny.PERM_SWITCH), false);
                ensureTownPermNode(townPermRoot.getNode(Towny.TYPE_OUTSIDER, Towny.PERM_ITEM_USE), false);

                CommentedConfigurationNode plotPermRoot = config.getNode("plots", "perms");
                CommentedConfigurationNode legacyCoowner = plotPermRoot.getNode("coowner");
                CommentedConfigurationNode legacyCitizenPlot = plotPermRoot.getNode("citizen");

                ensureTownPermNode(plotPermRoot.getNode(Towny.TYPE_FRIEND, Towny.PERM_BUILD), legacyCoowner.getNode(Towny.PERM_BUILD).getBoolean(true));
                ensureTownPermNode(plotPermRoot.getNode(Towny.TYPE_FRIEND, Towny.PERM_DESTROY), legacyCoowner.getNode(Towny.PERM_BUILD).getBoolean(true));
                ensureTownPermNode(plotPermRoot.getNode(Towny.TYPE_FRIEND, Towny.PERM_SWITCH), legacyCoowner.getNode(Towny.PERM_INTERACT).getBoolean(true));
                ensureTownPermNode(plotPermRoot.getNode(Towny.TYPE_FRIEND, Towny.PERM_ITEM_USE), legacyCoowner.getNode(Towny.PERM_INTERACT).getBoolean(true));

                ensureTownPermNode(plotPermRoot.getNode(Towny.TYPE_RESIDENT, Towny.PERM_BUILD), legacyCitizenPlot.getNode(Towny.PERM_BUILD).getBoolean(false));
                ensureTownPermNode(plotPermRoot.getNode(Towny.TYPE_RESIDENT, Towny.PERM_DESTROY), legacyCitizenPlot.getNode(Towny.PERM_BUILD).getBoolean(false));
                ensureTownPermNode(plotPermRoot.getNode(Towny.TYPE_RESIDENT, Towny.PERM_SWITCH), legacyCitizenPlot.getNode(Towny.PERM_INTERACT).getBoolean(true));
                ensureTownPermNode(plotPermRoot.getNode(Towny.TYPE_RESIDENT, Towny.PERM_ITEM_USE), legacyCitizenPlot.getNode(Towny.PERM_INTERACT).getBoolean(true));

                ensureTownPermNode(plotPermRoot.getNode(Towny.TYPE_NATION, Towny.PERM_BUILD), false);
                ensureTownPermNode(plotPermRoot.getNode(Towny.TYPE_NATION, Towny.PERM_DESTROY), false);
                ensureTownPermNode(plotPermRoot.getNode(Towny.TYPE_NATION, Towny.PERM_SWITCH), false);
                ensureTownPermNode(plotPermRoot.getNode(Towny.TYPE_NATION, Towny.PERM_ITEM_USE), false);

                ensureTownPermNode(plotPermRoot.getNode(Towny.TYPE_ALLY, Towny.PERM_BUILD), false);
                ensureTownPermNode(plotPermRoot.getNode(Towny.TYPE_ALLY, Towny.PERM_DESTROY), false);
                ensureTownPermNode(plotPermRoot.getNode(Towny.TYPE_ALLY, Towny.PERM_SWITCH), false);
                ensureTownPermNode(plotPermRoot.getNode(Towny.TYPE_ALLY, Towny.PERM_ITEM_USE), false);

                ensureTownPermNode(plotPermRoot.getNode(Towny.TYPE_OUTSIDER, Towny.PERM_BUILD), false);
                ensureTownPermNode(plotPermRoot.getNode(Towny.TYPE_OUTSIDER, Towny.PERM_DESTROY), false);
                ensureTownPermNode(plotPermRoot.getNode(Towny.TYPE_OUTSIDER, Towny.PERM_SWITCH), false);
                ensureTownPermNode(plotPermRoot.getNode(Towny.TYPE_OUTSIDER, Towny.PERM_ITEM_USE), false);

		if (!config.getNode("whitelist", "build").hasListChildren() || config.getNode("whitelist", "build").getChildrenList().isEmpty())
		{
			Utils.ensureString(config.getNode("whitelist", "build").getAppendedNode(), "gravestone:gravestone");
			Utils.ensureString(config.getNode("whitelist", "build").getAppendedNode(), "modname:blockname");
        }

                if (!config.getNode("whitelist", "break").hasListChildren() || config.getNode("whitelist", "break").getChildrenList().isEmpty())
                {
                        Utils.ensureString(config.getNode("whitelist", "break").getAppendedNode(), "modname:blockname");
		}

		if (!config.getNode("whitelist", "use").hasListChildren() || config.getNode("whitelist", "use").getChildrenList().isEmpty())
		{
			Utils.ensureString(config.getNode("whitelist", "use").getAppendedNode(), "modname:blockname");
		}

		if (!config.getNode("whitelist", "spawn").hasListChildren() || config.getNode("whitelist", "spawn").getChildrenList().isEmpty())
		{
			Utils.ensureString(config.getNode("whitelist", "spawn").getAppendedNode(), "modname:entity");
		}

		if (config.getNode("others", "enableTownyRanks").getBoolean())
		{
			if (!config.getNode("townyRanks").hasListChildren() || config.getNode("townyRanks").getChildrenList().isEmpty())
			{
				CommentedConfigurationNode rank;

				rank = config.getNode("townyRanks").getAppendedNode();
				rank.getNode("numCitizens").setValue(1);
				rank.getNode("townyTitle").setValue("Land");
				rank.getNode("mayorTitle").setValue("Leader");

				rank = config.getNode("townyRanks").getAppendedNode();
				rank.getNode("numCitizens").setValue(3);
				rank.getNode("townyTitle").setValue("Federation");
				rank.getNode("mayorTitle").setValue("Count");

				rank = config.getNode("townyRanks").getAppendedNode();
				rank.getNode("numCitizens").setValue(6);
				rank.getNode("townyTitle").setValue("Dominion");
				rank.getNode("mayorTitle").setValue("Duke");

				rank = config.getNode("townyRanks").getAppendedNode();
				rank.getNode("numCitizens").setValue(10);
				rank.getNode("townyTitle").setValue("Kingdom");
				rank.getNode("mayorTitle").setValue("King");

				rank = config.getNode("townyRanks").getAppendedNode();
				rank.getNode("numCitizens").setValue(15);
				rank.getNode("townyTitle").setValue("Empire");
				rank.getNode("mayorTitle").setValue("Emperor");
			}

			boolean defaultRankMissing = true;
			for (CommentedConfigurationNode rank : config.getNode("townyRanks").getChildrenList())
			{
				Utils.ensurePositiveNumber(rank.getNode("numCitizens"), 1_000_000);
				Utils.ensureString(rank.getNode("townyTitle"), "NO_TITLE");
				Utils.ensureString(rank.getNode("mayorTitle"), "NO_TITLE");
				if (rank.getNode("numCitizens").getInt() == 0)
				{
					defaultRankMissing = false;
				}
			}
			if (defaultRankMissing)
			{
				CommentedConfigurationNode rank = config.getNode("townyRanks").getAppendedNode();
				rank.getNode("numCitizens").setValue(0);
				rank.getNode("townyTitle").setValue("Virtual");
				rank.getNode("mayorTitle").setValue("Leader");
			}
		}

		for (World world : Sponge.getServer().getWorlds())
		{
			CommentedConfigurationNode node = config.getNode("worlds").getNode(world.getName());

			Utils.ensureBoolean(node.getNode("enabled"), true);
			if (node.getNode("enabled").getBoolean())
			{
                            Utils.ensureBoolean(node.getNode("perms").getNode(Towny.PERM_BUILD), true);
                            Utils.ensureBoolean(node.getNode("perms").getNode(Towny.PERM_DESTROY), true);
                            Utils.ensureBoolean(node.getNode("perms").getNode(Towny.PERM_SWITCH), true);
                            Utils.ensureBoolean(node.getNode("perms").getNode(Towny.PERM_ITEM_USE), true);
                            Utils.ensureBoolean(node.getNode("perms").getNode(Towny.PERM_INTERACT), true);

				Utils.ensureBoolean(node.getNode("flags", "pvp"), true);
				Utils.ensureBoolean(node.getNode("flags", "mobs"), true);
				Utils.ensureBoolean(node.getNode("flags", "fire"), true);
				Utils.ensureBoolean(node.getNode("flags", "explosions"), true);
			}
			else
			{
				node.removeChild("perms");
				node.removeChild("flags");
			}
		}

                save();
                if (src != null)
                {
                        src.sendMessage(Text.of(LanguageHandler.colorGreen(), LanguageHandler.INFO_CONFIGRELOADED));
                }
        }

        private static void migrateLegacyKeys()
        {
                migratePriceNode(config.getNode("prices"), "blockClaimPrice", "chunkClaimPrice");
                migratePriceNode(config.getNode("prices"), "extraPrice", "extraChunkPrice");
                migrateChunkCountNode(config.getNode("others"), "blocksPerCitizen", "chunksPerCitizen");
                migrateChunkCountNode(config.getNode("others"), "maxExtra", "maxExtraChunks");
        }

        private static void migratePriceNode(CommentedConfigurationNode parent, String oldKey, String newKey)
        {
                CommentedConfigurationNode legacy = parent.getNode(oldKey);
                CommentedConfigurationNode modern = parent.getNode(newKey);
                if (!modern.isVirtual())
                {
                        return;
                }
                if (!legacy.isVirtual())
                {
                        modern.setValue(legacy.getDouble() * ChunkClaimUtils.CHUNK_AREA);
                }
        }

        private static void migrateChunkCountNode(CommentedConfigurationNode parent, String oldKey, String newKey)
        {
                CommentedConfigurationNode legacy = parent.getNode(oldKey);
                CommentedConfigurationNode modern = parent.getNode(newKey);
                if (!modern.isVirtual())
                {
                        return;
                }
                if (!legacy.isVirtual())
                {
                        int legacyValue = legacy.getInt();
                        int converted = (int) Math.max(0, Math.ceil(legacyValue / (double) ChunkClaimUtils.CHUNK_AREA));
                        modern.setValue(converted);
                }
        }

        public static void save()
        {
                try
                {
			configManager.save(config);
		}
		catch (IOException e)
		{
			TownyPlugin.getLogger().error("Could not save config file !");
		}
	}

	public static CommentedConfigurationNode getNode(String... path)
	{
		return config.getNode((Object[]) path);
	}

	public static CommentedConfigurationNode getTownyRank(int numCitizens)
	{
		return config.getNode("townyRanks")
				.getChildrenList()
				.stream()
				.filter(node -> node.getNode("numCitizens").getInt() <= numCitizens)
				.max(Comparator.comparingInt((CommentedConfigurationNode a) -> a.getNode("numCitizens").getInt()))
				.get();
	}

	public static boolean isWhitelisted(String type, String id)
	{
		if (!config.getNode("whitelist", type).hasListChildren())
			return false;

		for (CommentedConfigurationNode item : config.getNode("whitelist", type).getChildrenList())
		{
			if (id.startsWith(item.getString()))
				return true;
		}
		return false;
	}

	public static class Utils
	{
		public static void ensureString(CommentedConfigurationNode node, String def)
		{
			if (node.getString() == null)
			{
				node.setValue(def);
			}
		}

		public static void ensurePositiveNumber(CommentedConfigurationNode node, Number def)
		{
			if (!(node.getValue() instanceof Number) || node.getDouble(-1) < 0)
			{
				node.setValue(def);
			}
		}

		public static void ensureBoolean(CommentedConfigurationNode node, boolean def)
		{
			if (!(node.getValue() instanceof Boolean))
			{
				node.setValue(def);
			}
		}
	}
}
