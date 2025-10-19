package com.arckenver.towny.cmdexecutor.towny;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

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
				LanguageHandler.colorGold(), ((src instanceof Player) ? "" : "\n") + "--------{ ",
				LanguageHandler.colorYellow(), LanguageHandler.HEADER_TOWNCOST,
				LanguageHandler.colorGold(), " }--------",
				LanguageHandler.colorGold(), "\n", LanguageHandler.COST_MSG_TOWNCREATE, LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), ConfigHandler.getNode("prices", "townyCreationPrice").getDouble(),
				LanguageHandler.colorGold(), "\n", LanguageHandler.COST_MSG_OUTPOSTCREATE, LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), ConfigHandler.getNode("prices", "outpostCreationPrice").getDouble(),
				LanguageHandler.colorGold(), "\n", LanguageHandler.COST_MSG_UPKEEP, LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), ConfigHandler.getNode("prices", "upkeepPerCitizen").getDouble(),
                                LanguageHandler.colorGold(), "\n", LanguageHandler.COST_MSG_CLAIMPRICE, LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), ConfigHandler.getNode("prices", "chunkClaimPrice").getDouble(),
                                LanguageHandler.colorGold(), "\n", LanguageHandler.COST_MSG_EXTRAPRICE, LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), ConfigHandler.getNode("prices", "extraChunkPrice").getDouble()));
		return CommandResult.success();
	}
}
