package com.arckenver.towny.cmdexecutor.townyadmin;

import com.arckenver.towny.claim.ChunkClaimUtils;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import com.arckenver.towny.ConfigHandler;
import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.object.Towny;
import com.arckenver.towny.object.Point;
import com.arckenver.towny.object.Rect;

public class TownyadminClaimExecutor implements CommandExecutor
{
	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("towny.command.townyadmin.claim")
				.arguments(GenericArguments.optional(GenericArguments.string(Text.of("towny"))))
				.executor(new TownyadminClaimExecutor())
				.build(), "claim");
	}

	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException
	{
		if (!ctx.<String>getOne("towny").isPresent())
		{
			src.sendMessage(Text.of(LanguageHandler.colorYellow(), "/ta claim <towny>"));
			return CommandResult.success();
		}
		if (src instanceof Player)
		{
			Player player = (Player) src;
                        // Automatically set the points based on the player's current chunk
                        ChunkClaimUtils.selectCurrentChunk(player);
			String townyName = ctx.<String>getOne("towny").get();
			Towny towny = DataHandler.getTowny(townyName);
			if (towny == null)
			{
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_BADTOWNNNAME));
				return CommandResult.success();
			}
                        Point a = DataHandler.getFirstPoint(player.getUniqueId());
                        Point b = DataHandler.getSecondPoint(player.getUniqueId());
                        if (a == null || b == null)
                        {
                                src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NEEDCHUNKSELECT));
				return CommandResult.success();
			}
			if (!ConfigHandler.getNode("worlds").getNode(a.getWorld().getName()).getNode("enabled").getBoolean())
			{
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_PLUGINDISABLEDINWORLD));
				return CommandResult.success();
			}
			Rect rect = new Rect(a, b);

			if (!DataHandler.canClaim(rect, true, towny.getUUID()))
			{
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_TOOCLOSE));
				return CommandResult.success();
			}

			towny.getRegion().addRect(rect);
			DataHandler.addToWorldChunks(towny);
			DataHandler.saveTowny(towny.getUUID());
			src.sendMessage(Text.of(LanguageHandler.colorGreen(), LanguageHandler.SUCCESS_GENERAL));
		}
		else
		{
			src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NOPLAYER));
		}
		return CommandResult.success();
	}

	public CommandResult claimLand(Player player, String townyName) {
                // Automatically set the points based on the player's current chunk
                ChunkClaimUtils.selectCurrentChunk(player);

		Towny towny = DataHandler.getTowny(townyName);
		if (towny == null) {
			player.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_BADTOWNNNAME));
			return CommandResult.success();
		}

		Point a = DataHandler.getFirstPoint(player.getUniqueId());
		Point b = DataHandler.getSecondPoint(player.getUniqueId());

                if (a == null || b == null) {
                        player.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NEEDCHUNKSELECT));
			return CommandResult.success();
		}

		if (!ConfigHandler.getNode("worlds").getNode(a.getWorld().getName()).getNode("enabled").getBoolean()) {
			player.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_PLUGINDISABLEDINWORLD));
			return CommandResult.success();
		}

		Rect rect = new Rect(a, b);

		if (!DataHandler.canClaim(rect, true, towny.getUUID())) {
			player.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_TOOCLOSE));
			return CommandResult.success();
		}

		towny.getRegion().addRect(rect);
		DataHandler.addToWorldChunks(towny);
		DataHandler.saveTowny(towny.getUUID());
		player.sendMessage(Text.of(LanguageHandler.colorGreen(), LanguageHandler.SUCCESS_GENERAL));

		return CommandResult.success();
	}
}
