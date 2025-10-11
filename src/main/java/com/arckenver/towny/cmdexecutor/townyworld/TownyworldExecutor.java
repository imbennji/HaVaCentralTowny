package com.arckenver.towny.cmdexecutor.townyworld;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

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
				TextColors.GOLD, ((src instanceof Player) ? "" : "\n") + "--------{ ",
				TextColors.YELLOW, "/townyworld",
				TextColors.GOLD, " }--------",
				TextColors.GOLD, "\n/tw info [world]", TextColors.GRAY, " - ", TextColors.YELLOW, LanguageHandler.HELP_DESC_CMD_TW_INFO,
				TextColors.GOLD, "\n/tw list", TextColors.GRAY, " - ", TextColors.YELLOW, LanguageHandler.HELP_DESC_CMD_TW_LIST,
				TextColors.GOLD, "\n/tw enable <world>", TextColors.GRAY, " - ", TextColors.YELLOW, LanguageHandler.HELP_DESC_CMD_TW_ENABLE,
				TextColors.GOLD, "\n/tw disable <world>", TextColors.GRAY, " - ", TextColors.YELLOW, LanguageHandler.HELP_DESC_CMD_TW_DISABLE,
				TextColors.GOLD, "\n/tw perm <perm> [true|false]", TextColors.GRAY, " - ", TextColors.YELLOW, LanguageHandler.HELP_DESC_CMD_TW_PERM,
				TextColors.GOLD, "\n/tw flag <flag> <true|false>", TextColors.GRAY, " - ", TextColors.YELLOW, LanguageHandler.HELP_DESC_CMD_TW_FLAG
		));
		return CommandResult.success();
	}
}
