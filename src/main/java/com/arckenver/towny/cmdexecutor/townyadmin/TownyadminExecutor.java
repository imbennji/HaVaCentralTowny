package com.arckenver.towny.cmdexecutor.townyadmin;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;

import com.arckenver.towny.LanguageHandler;

public class TownyadminExecutor implements CommandExecutor
{
	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("towny.command.townyadmin.help")
				.arguments()
				.executor(new TownyadminExecutor())
				.build(), "help", "?");
	}

	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException
	{
		List<Text> contents = new ArrayList<>();

		contents.add(Text.of(LanguageHandler.colorGold(), "/ta reload", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_TA_RELOAD));
		contents.add(Text.of(LanguageHandler.colorGold(), "/ta forceupkeep", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_TA_FORCEKEEPUP));
		contents.add(Text.of(LanguageHandler.colorGold(), "/ta create <name>", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_TA_CREATE));
		contents.add(Text.of(LanguageHandler.colorGold(), "/ta claim <towny>", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_TA_CLAIM));
		contents.add(Text.of(LanguageHandler.colorGold(), "/ta unclaim <towny>", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_TA_UNCLAIM));
		contents.add(Text.of(LanguageHandler.colorGold(), "/ta delete <towny>", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_TA_DELETE));
		contents.add(Text.of(LanguageHandler.colorGold(), "/ta setname <towny> <name>", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_SETNAME));
		contents.add(Text.of(LanguageHandler.colorGold(), "/ta settag <towny> <tag>", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_SETTAG));
		contents.add(Text.of(LanguageHandler.colorGold(), "/ta setpres <towny> <player>", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_TA_SETPRES));
		contents.add(Text.of(LanguageHandler.colorGold(), "/ta setspawn <towny> <name>", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_SETSPAWN));
		contents.add(Text.of(LanguageHandler.colorGold(), "/ta delspawn <towny> <name>", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_DELSPAWN));
		contents.add(Text.of(LanguageHandler.colorGold(), "/ta forcejoin <towny> <player>", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_TA_FORCEJOIN));
		contents.add(Text.of(LanguageHandler.colorGold(), "/ta forceleave <towny> <player>", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_TA_FORCELEAVE));
		contents.add(Text.of(LanguageHandler.colorGold(), "/ta eco <give|take|set> <towny> <amount>", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_TA_ECO));
		contents.add(Text.of(LanguageHandler.colorGold(), "/ta perm <towny> <type> <perm> [true|false]", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_TA_PERM));
		contents.add(Text.of(LanguageHandler.colorGold(), "/ta flag <towny> <flag> [true|false]", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_TA_FLAG));
		contents.add(Text.of(LanguageHandler.colorGold(), "/ta spy", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_TA_SPY));
		contents.add(Text.of(LanguageHandler.colorGold(), "/ta extra <give|take|set> <towny> <amount>", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_TA_EXTRA));
		contents.add(Text.of(LanguageHandler.colorGold(), "/ta extraplayer <give|take|set> <player> <amount>", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_TA_EXTRAPLAYER));
		contents.add(Text.of(LanguageHandler.colorGold(), "/ta extraspawn <give|take|set> <towny> <amount>", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_TA_EXTRASPAWN));
		contents.add(Text.of(LanguageHandler.colorGold(), "/ta extraspawnplayer <give|take|set> <player> <amount>", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_TA_EXTRASPAWNPLAYER));

		PaginationList
.builder()
		.title(Text.of(LanguageHandler.colorGold(), "{ ", LanguageHandler.colorYellow(), "/townyadmin", LanguageHandler.colorGold(), " }"))
		.contents(contents)
		.padding(Text.of("-"))
		.sendTo(src);
		return CommandResult.success();
	}
}
