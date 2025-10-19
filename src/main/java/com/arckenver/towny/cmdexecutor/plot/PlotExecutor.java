package com.arckenver.towny.cmdexecutor.plot;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import com.arckenver.towny.LanguageHandler;

public class PlotExecutor implements CommandExecutor
{
	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("towny.command.plot.help")
				.arguments()
				.executor(new PlotExecutor())
				.build(), "help", "?");
	}

	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException
	{
		src.sendMessage(Text.of(
				LanguageHandler.colorGold(), ((src instanceof Player) ? "" : "\n") + "--------{ ",
				LanguageHandler.colorYellow(), "/plot",
				LanguageHandler.colorGold(), " }--------",
				LanguageHandler.colorGold(), "\n/z info [plot]", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_P_INFO,
				LanguageHandler.colorGold(), "\n/z list", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_P_LIST,
				LanguageHandler.colorGold(), "\n/z create <name> [owner]", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_P_CREATE,
				LanguageHandler.colorGold(), "\n/z delete [plot]", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_P_DELETE,
				LanguageHandler.colorGold(), "\n/z coowner <add/remove> <player>", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_P_COOWNER,
                                LanguageHandler.colorGold(), "\n/z setowner <player>", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_P_SETOWNER,
                                LanguageHandler.colorGold(), "\n/z delowner", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_P_DELOWNER,
                                LanguageHandler.colorGold(), "\n/z rename", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_P_RENAME,
                                LanguageHandler.colorGold(), "\n/z set <type>", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_P_SETTYPE,
                                LanguageHandler.colorGold(), "\n/z perm <type> <perm> [true/false]", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_P_PERM,
                                LanguageHandler.colorGold(), "\n/z flag <flag> [true/false]", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_P_FLAG,
				LanguageHandler.colorGold(), "\n/z sell <price>", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_P_SELL,
				LanguageHandler.colorGold(), "\n/z buy", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_P_BUY));
		return CommandResult.success();
	}
}
