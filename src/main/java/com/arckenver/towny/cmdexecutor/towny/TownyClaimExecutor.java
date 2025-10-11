package com.arckenver.towny.cmdexecutor.towny;

import java.math.BigDecimal;
import java.util.Optional;

import com.arckenver.towny.listener.GoldenAxeListener;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.arckenver.towny.ConfigHandler;
import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.TownyPlugin;
import com.arckenver.towny.Utils;
import com.arckenver.towny.object.Towny;
import com.arckenver.towny.object.Point;
import com.arckenver.towny.object.Rect;
import com.arckenver.towny.object.Region;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class TownyClaimExecutor implements CommandExecutor
{
	public static void create(CommandSpec.Builder cmd) {
		CommandSpec subCmd = CommandSpec.builder()
		.description(Text.of(""))
		.permission("towny.command.town.claim.outpost")
		.arguments()
		.executor(new TownyClaimOutpostExecutor())
		.build();

		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("towny.command.towny.claim")
				.arguments()
				.executor(new TownyClaimExecutor())
				.child(subCmd, "outpost", "o")
				.build(), "claim");
	}

	public static void CheckFirstChunk(Player player, Towny towny) {
		// Assuming 1 chunk is 16x16x256 blocks
		int blocksPerChunk = 16 * 16 * 256;
		int claimedBlocks = towny.getRegion().size();

		// Calculate the number of chunks claimed based on 256-block increments
		int claimedChunksIncremental = (int) Math.ceil((double) claimedBlocks / 256);

		// Calculate the maximum allowed chunks based on 256-block increments
		int maxChunksAllowedIncremental = (int) Math.ceil((double) towny.maxBlockSize() / 256);

		// Check if the player's towny claims at least one chunk
		if (claimedChunksIncremental >= 1) {
			// The player's towny claims at least one chunk

			// Set the spawn with the default name "home"
			handleSetSpawnCommand(player, towny, "home");
		} else {
			// No log message for the case where the towny doesn't claim the first chunk
		}
	}

	private static void handleSetSpawnCommand(Player player, Towny towny, String spawnName) {
		if (spawnName == null || spawnName.isEmpty()) {
			spawnName = "home";
		}

		if (towny == null) {
			player.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOTOWN));
			return;
		}

		if (!towny.isStaff(player.getUniqueId())) {
			player.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_PERM_TOWNSTAFF));
			return;
		}

		Location<World> newSpawn = player.getLocation();

		if (!towny.getRegion().isInside(newSpawn)) {
			player.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_BADSPAWNLOCATION));
			return;
		}

		if (towny.getNumSpawns() + 1 > towny.getMaxSpawns() && !towny.getSpawns().containsKey(spawnName)) {
			player.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_MAXSPAWNREACH
					.replaceAll("\\{MAX\\}", String.valueOf(towny.getMaxSpawns()))));
			return;
		}

		if (!spawnName.matches("[\\p{Alnum}\\p{IsIdeographic}\\p{IsLetter}]{1,30}")) {
			player.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_ALPHASPAWN
					.replaceAll("\\{MIN\\}", ConfigHandler.getNode("others", "minPlotNameLength").getString())
					.replaceAll("\\{MAX\\}", ConfigHandler.getNode("others", "maxPlotNameLength").getString())));
			return;
		}

		towny.addSpawn(spawnName, newSpawn);
		DataHandler.saveTowny(towny.getUUID());
		player.sendMessage(Text.of(TextColors.AQUA, LanguageHandler.SUCCESS_CHANGESPAWN));
	}

	public boolean claimLand(Player player, Towny townyParam) {
		// Automatically set the points based on the player's current chunk
		GoldenAxeListener.setAutomaticPoints(player);

		Towny towny = DataHandler.getTownyOfPlayer(player.getUniqueId());
		if (towny == null) {
			player.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOTOWN));
			return false;
		}
		if (!towny.isStaff(player.getUniqueId())) {
			player.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_PERM_TOWNSTAFF));
			return false;
		}
		Point a = DataHandler.getFirstPoint(player.getUniqueId());
		Point b = DataHandler.getSecondPoint(player.getUniqueId());
		if (a == null || b == null) {
			player.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NEEDAXESELECT));
			return false;
		}
		if (!ConfigHandler.getNode("worlds").getNode(a.getWorld().getName()).getNode("enabled").getBoolean()) {
			player.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_PLUGINDISABLEDINWORLD));
			return false;
		}
		Rect rect = new Rect(a, b);
		if (towny.getRegion().size() > 0 && !towny.getRegion().isAdjacent(rect)) {
			player.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NEEDADJACENT));
			return false;
		}
		if (!DataHandler.canClaim(rect, false, towny.getUUID())) {
			player.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_TOOCLOSE));
			return false;
		}
		Region claimed = towny.getRegion().copy();
		claimed.addRect(rect);

		if (claimed.size() > towny.maxBlockSize()) {
			player.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOENOUGHBLOCKS));
			return false;
		}

		// Update claimed region and other necessary data
		towny.setRegion(claimed);
		DataHandler.addToWorldChunks(towny);
		DataHandler.saveTowny(towny.getUUID());
		CheckFirstChunk(player, towny);
		player.sendMessage(Text.of(TextColors.AQUA, LanguageHandler.SUCCESS_CLAIM));
		return true;
	}



	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException
	{
		if (src instanceof Player)
		{
			Player player = (Player) src;

			// Automatically set the points based on the player's current chunk
			GoldenAxeListener.setAutomaticPoints(player);

			Towny towny = DataHandler.getTownyOfPlayer(player.getUniqueId());
			if (towny == null)
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOTOWN));
				return CommandResult.success();
			}
			if (!towny.isStaff(player.getUniqueId()))
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_PERM_TOWNSTAFF));
				return CommandResult.success();
			}
			Point a = DataHandler.getFirstPoint(player.getUniqueId());
			Point b = DataHandler.getSecondPoint(player.getUniqueId());
			if (a == null || b == null)
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NEEDAXESELECT));
				return CommandResult.success();
			}
			if (!ConfigHandler.getNode("worlds").getNode(a.getWorld().getName()).getNode("enabled").getBoolean())
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_PLUGINDISABLEDINWORLD));
				return CommandResult.success();
			}
			Rect rect = new Rect(a, b);
			if (towny.getRegion().size() > 0 && !towny.getRegion().isAdjacent(rect))
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NEEDADJACENT));
				return CommandResult.success();
			}
			if (!DataHandler.canClaim(rect, false, towny.getUUID()))
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_TOOCLOSE));
				return CommandResult.success();
			}
			Region claimed = towny.getRegion().copy();
			claimed.addRect(rect);
			
			if (claimed.size() > towny.maxBlockSize())
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOENOUGHBLOCKS));
				return CommandResult.success();
			}
			
			if (TownyPlugin.getEcoService() == null)
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOECO));
				return CommandResult.success();
			}
			Optional<Account> optAccount = TownyPlugin.getEcoService().getOrCreateAccount("towny-" + towny.getUUID().toString());
			if (!optAccount.isPresent())
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_ECONOTOWN));
				return CommandResult.success();
			}
			BigDecimal price = BigDecimal.valueOf((claimed.size() - towny.getRegion().size()) * ConfigHandler.getNode("prices", "blockClaimPrice").getDouble());
			TransactionResult result = optAccount.get().withdraw(TownyPlugin.getEcoService().getDefaultCurrency(), price, TownyPlugin.getCause());
			if (result.getResult() == ResultType.ACCOUNT_NO_FUNDS)
			{
				src.sendMessage(Text.builder()
						.append(Text.of(TextColors.RED, LanguageHandler.ERROR_NEEDMONEYTOWN.split("\\{AMOUNT\\}")[0]))
						.append(Utils.formatPrice(TextColors.RED, price))
						.append(Text.of(TextColors.RED, LanguageHandler.ERROR_NEEDMONEYTOWN.split("\\{AMOUNT\\}")[1])).build());
				return CommandResult.success();
			}
			else if (result.getResult() != ResultType.SUCCESS)
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_ECOTRANSACTION));
				return CommandResult.success();
			}
			
			towny.setRegion(claimed);
			DataHandler.addToWorldChunks(towny);
			DataHandler.saveTowny(towny.getUUID());
			CheckFirstChunk(player, towny);
			src.sendMessage(Text.of(TextColors.AQUA, LanguageHandler.SUCCESS_CLAIM));
		}
		else
		{
			src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOPLAYER));
		}
		return CommandResult.success();
	}

	public String claimLandMessages(Player player, Towny townyParam) {
		// Automatically set the points based on the player's current chunk
		GoldenAxeListener.setAutomaticPoints(player);

		Towny towny = DataHandler.getTownyOfPlayer(player.getUniqueId());
		if (towny == null) {
			return LanguageHandler.ERROR_NOTOWN;
		}
		if (!towny.isStaff(player.getUniqueId())) {
			return LanguageHandler.ERROR_PERM_TOWNSTAFF;
		}
		Point a = DataHandler.getFirstPoint(player.getUniqueId());
		Point b = DataHandler.getSecondPoint(player.getUniqueId());
		if (a == null || b == null) {
			return LanguageHandler.ERROR_NEEDAXESELECT;
		}
		if (!ConfigHandler.getNode("worlds").getNode(a.getWorld().getName()).getNode("enabled").getBoolean()) {
			return LanguageHandler.ERROR_PLUGINDISABLEDINWORLD;
		}
		Rect rect = new Rect(a, b);
		if (towny.getRegion().size() > 0 && !towny.getRegion().isAdjacent(rect)) {
			return LanguageHandler.ERROR_NEEDADJACENT;
		}
		if (!DataHandler.canClaim(rect, false, towny.getUUID())) {
			return LanguageHandler.ERROR_TOOCLOSE;
		}
		Region claimed = towny.getRegion().copy();
		claimed.addRect(rect);

		if (claimed.size() > towny.maxBlockSize()) {
			return LanguageHandler.ERROR_NOENOUGHBLOCKS;
		}

		// Update claimed region and other necessary data
		towny.setRegion(claimed);
		DataHandler.addToWorldChunks(towny);
		DataHandler.saveTowny(towny.getUUID());
		CheckFirstChunk(player, towny);
		player.sendMessage(Text.of(TextColors.AQUA, LanguageHandler.SUCCESS_CLAIM));
		return null; // Return null if the claim is successful
	}

}
