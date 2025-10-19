package com.arckenver.towny.cmdexecutor.townyworld;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import com.arckenver.towny.LanguageHandler;

public class TownyworldExecutor implements CommandExecutor
{
	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("towny.command.townyworld.help")
				.arguments()
				.executor(new TownyworldExecutor())
				.build(), "help", "?");
	}

	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException
	{
		src.sendMessage(Text.of(
				LanguageHandler.colorGold(), ((src instanceof Player) ? "" : "\n") + "--------{ ",
				LanguageHandler.colorYellow(), "/townyworld",
				LanguageHandler.colorGold(), " }--------",
				LanguageHandler.colorGold(), "\n/tw info [world]", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_TW_INFO,
				LanguageHandler.colorGold(), "\n/tw list", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_TW_LIST,
				LanguageHandler.colorGold(), "\n/tw enable <world>", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_TW_ENABLE,
				LanguageHandler.colorGold(), "\n/tw disable <world>", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_TW_DISABLE,
				LanguageHandler.colorGold(), "\n/tw perm <perm> [true|false]", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_TW_PERM,
				LanguageHandler.colorGold(), "\n/tw flag <flag> <true|false>", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_TW_FLAG
		));
		return CommandResult.success();
	}
}
