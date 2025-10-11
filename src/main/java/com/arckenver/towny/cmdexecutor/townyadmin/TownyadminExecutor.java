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
import org.spongepowered.api.text.format.TextColors;

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

		contents.add(Text.of(TextColors.GOLD, "/ta reload", TextColors.GRAY, " - ", TextColors.YELLOW, LanguageHandler.HELP_DESC_CMD_TA_RELOAD));
		contents.add(Text.of(TextColors.GOLD, "/ta forceupkeep", TextColors.GRAY, " - ", TextColors.YELLOW, LanguageHandler.HELP_DESC_CMD_TA_FORCEKEEPUP));
		contents.add(Text.of(TextColors.GOLD, "/ta create <name>", TextColors.GRAY, " - ", TextColors.YELLOW, LanguageHandler.HELP_DESC_CMD_TA_CREATE));
		contents.add(Text.of(TextColors.GOLD, "/ta claim <towny>", TextColors.GRAY, " - ", TextColors.YELLOW, LanguageHandler.HELP_DESC_CMD_TA_CLAIM));
		contents.add(Text.of(TextColors.GOLD, "/ta unclaim <towny>", TextColors.GRAY, " - ", TextColors.YELLOW, LanguageHandler.HELP_DESC_CMD_TA_UNCLAIM));
		contents.add(Text.of(TextColors.GOLD, "/ta delete <towny>", TextColors.GRAY, " - ", TextColors.YELLOW, LanguageHandler.HELP_DESC_CMD_TA_DELETE));
		contents.add(Text.of(TextColors.GOLD, "/ta setname <towny> <name>", TextColors.GRAY, " - ", TextColors.YELLOW, LanguageHandler.HELP_DESC_CMD_SETNAME));
		contents.add(Text.of(TextColors.GOLD, "/ta settag <towny> <tag>", TextColors.GRAY, " - ", TextColors.YELLOW, LanguageHandler.HELP_DESC_CMD_SETTAG));
		contents.add(Text.of(TextColors.GOLD, "/ta setpres <towny> <player>", TextColors.GRAY, " - ", TextColors.YELLOW, LanguageHandler.HELP_DESC_CMD_TA_SETPRES));
		contents.add(Text.of(TextColors.GOLD, "/ta setspawn <towny> <name>", TextColors.GRAY, " - ", TextColors.YELLOW, LanguageHandler.HELP_DESC_CMD_SETSPAWN));
		contents.add(Text.of(TextColors.GOLD, "/ta delspawn <towny> <name>", TextColors.GRAY, " - ", TextColors.YELLOW, LanguageHandler.HELP_DESC_CMD_DELSPAWN));
		contents.add(Text.of(TextColors.GOLD, "/ta forcejoin <towny> <player>", TextColors.GRAY, " - ", TextColors.YELLOW, LanguageHandler.HELP_DESC_CMD_TA_FORCEJOIN));
		contents.add(Text.of(TextColors.GOLD, "/ta forceleave <towny> <player>", TextColors.GRAY, " - ", TextColors.YELLOW, LanguageHandler.HELP_DESC_CMD_TA_FORCELEAVE));
		contents.add(Text.of(TextColors.GOLD, "/ta eco <give|take|set> <towny> <amount>", TextColors.GRAY, " - ", TextColors.YELLOW, LanguageHandler.HELP_DESC_CMD_TA_ECO));
		contents.add(Text.of(TextColors.GOLD, "/ta perm <towny> <type> <perm> [true|false]", TextColors.GRAY, " - ", TextColors.YELLOW, LanguageHandler.HELP_DESC_CMD_TA_PERM));
		contents.add(Text.of(TextColors.GOLD, "/ta flag <towny> <flag> [true|false]", TextColors.GRAY, " - ", TextColors.YELLOW, LanguageHandler.HELP_DESC_CMD_TA_FLAG));
		contents.add(Text.of(TextColors.GOLD, "/ta spy", TextColors.GRAY, " - ", TextColors.YELLOW, LanguageHandler.HELP_DESC_CMD_TA_SPY));
		contents.add(Text.of(TextColors.GOLD, "/ta extra <give|take|set> <towny> <amount>", TextColors.GRAY, " - ", TextColors.YELLOW, LanguageHandler.HELP_DESC_CMD_TA_EXTRA));
		contents.add(Text.of(TextColors.GOLD, "/ta extraplayer <give|take|set> <player> <amount>", TextColors.GRAY, " - ", TextColors.YELLOW, LanguageHandler.HELP_DESC_CMD_TA_EXTRAPLAYER));
		contents.add(Text.of(TextColors.GOLD, "/ta extraspawn <give|take|set> <towny> <amount>", TextColors.GRAY, " - ", TextColors.YELLOW, LanguageHandler.HELP_DESC_CMD_TA_EXTRASPAWN));
		contents.add(Text.of(TextColors.GOLD, "/ta extraspawnplayer <give|take|set> <player> <amount>", TextColors.GRAY, " - ", TextColors.YELLOW, LanguageHandler.HELP_DESC_CMD_TA_EXTRASPAWNPLAYER));

		PaginationList
.builder()
		.title(Text.of(TextColors.GOLD, "{ ", TextColors.YELLOW, "/townyadmin", TextColors.GOLD, " }"))
		.contents(contents)
		.padding(Text.of("-"))
		.sendTo(src);
		return CommandResult.success();
	}
}
