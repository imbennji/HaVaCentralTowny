package com.arckenver.towny.cmdexecutor.towny;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.arckenver.towny.ConfigHandler;
import com.arckenver.towny.LanguageHandler;

public class TownyCostExecutor implements CommandExecutor
{
	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
			.description(Text.of(""))
			.permission("towny.command.town.cost")
			.arguments()
			.executor(new TownyCostExecutor())
			.build(), "cost", "costs", "price", "prices", "fare", "fares");
	}

	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException
	{
		src.sendMessage(Text.of(
				TextColors.GOLD, ((src instanceof Player) ? "" : "\n") + "--------{ ",
				TextColors.YELLOW, LanguageHandler.HEADER_TOWNCOST,
				TextColors.GOLD, " }--------",
				TextColors.GOLD, "\n", LanguageHandler.COST_MSG_TOWNCREATE, TextColors.GRAY, " - ", TextColors.YELLOW, ConfigHandler.getNode("prices", "townyCreationPrice").getDouble(),
				TextColors.GOLD, "\n", LanguageHandler.COST_MSG_OUTPOSTCREATE, TextColors.GRAY, " - ", TextColors.YELLOW, ConfigHandler.getNode("prices", "outpostCreationPrice").getDouble(),
				TextColors.GOLD, "\n", LanguageHandler.COST_MSG_UPKEEP, TextColors.GRAY, " - ", TextColors.YELLOW, ConfigHandler.getNode("prices", "upkeepPerCitizen").getDouble(),
                                TextColors.GOLD, "\n", LanguageHandler.COST_MSG_CLAIMPRICE, TextColors.GRAY, " - ", TextColors.YELLOW, ConfigHandler.getNode("prices", "chunkClaimPrice").getDouble(),
                                TextColors.GOLD, "\n", LanguageHandler.COST_MSG_EXTRAPRICE, TextColors.GRAY, " - ", TextColors.YELLOW, ConfigHandler.getNode("prices", "extraChunkPrice").getDouble()));
		return CommandResult.success();
	}
}
