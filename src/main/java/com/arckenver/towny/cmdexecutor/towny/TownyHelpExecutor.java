package com.arckenver.towny.cmdexecutor.towny;

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

public class TownyHelpExecutor implements CommandExecutor
{
	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("towny.command.town.help")
				.arguments()
				.executor(new TownyHelpExecutor())
				.build(), "help", "?");
	}

	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException
	{
		List<Text> contents = new ArrayList<>();

		contents.add(Text.of(TextColors.GOLD, "/t info [towny]", TextColors.GRAY, " - ", TextColors.YELLOW, LanguageHandler.HELP_DESC_CMD_T_INFO));
		contents.add(Text.of(TextColors.GOLD, "/t here", TextColors.GRAY, " - ", TextColors.YELLOW, LanguageHandler.HELP_DESC_CMD_T_HERE));
		contents.add(Text.of(TextColors.GOLD, "/t cost", TextColors.GRAY, " - ", TextColors.YELLOW, LanguageHandler.HELP_DESC_CMD_T_COST));
		contents.add(Text.of(TextColors.GOLD, "/t see", TextColors.GRAY, " - ", TextColors.YELLOW, LanguageHandler.HELP_DESC_CMD_T_SEE));
		contents.add(Text.of(TextColors.GOLD, "/t list", TextColors.GRAY, " - ", TextColors.YELLOW, LanguageHandler.HELP_DESC_CMD_T_LIST));
		contents.add(Text.of(TextColors.GOLD, "/t create <name>", TextColors.GRAY, " - ", TextColors.YELLOW, LanguageHandler.HELP_DESC_CMD_T_CREATE));
		contents.add(Text.of(TextColors.GOLD, "/t deposit <amount>", TextColors.GRAY, " - ", TextColors.YELLOW, LanguageHandler.HELP_DESC_CMD_T_DEPOSIT));
		contents.add(Text.of(TextColors.GOLD, "/t withdraw <amount>", TextColors.GRAY, " - ", TextColors.YELLOW, LanguageHandler.HELP_DESC_CMD_T_WITHDRAW));
		contents.add(Text.of(TextColors.GOLD, "/t claim", TextColors.GRAY, " - ", TextColors.YELLOW, LanguageHandler.HELP_DESC_CMD_T_CLAIM));
		contents.add(Text.of(TextColors.GOLD, "/t unclaim", TextColors.GRAY, " - ", TextColors.YELLOW, LanguageHandler.HELP_DESC_CMD_T_UNCLAIM));
		contents.add(Text.of(TextColors.GOLD, "/t invite <player>", TextColors.GRAY, " - ", TextColors.YELLOW, LanguageHandler.HELP_DESC_CMD_T_INVITE));
		contents.add(Text.of(TextColors.GOLD, "/t join <towny>", TextColors.GRAY, " - ", TextColors.YELLOW, LanguageHandler.HELP_DESC_CMD_T_JOIN));
		contents.add(Text.of(TextColors.GOLD, "/t kick <player>", TextColors.GRAY, " - ", TextColors.YELLOW, LanguageHandler.HELP_DESC_CMD_T_KICK));
		contents.add(Text.of(TextColors.GOLD, "/t leave", TextColors.GRAY, " - ", TextColors.YELLOW, LanguageHandler.HELP_DESC_CMD_T_LEAVE));
		contents.add(Text.of(TextColors.GOLD, "/t resign <successor>", TextColors.GRAY, " - ", TextColors.YELLOW, LanguageHandler.HELP_DESC_CMD_T_RESIGN));
		contents.add(Text.of(TextColors.GOLD, "/t minister <add/remove> <player>", TextColors.GRAY, " - ", TextColors.YELLOW, LanguageHandler.HELP_DESC_CMD_T_MINISTER));
		contents.add(Text.of(TextColors.GOLD, "/t citizen <player>", TextColors.GRAY, " - ", TextColors.YELLOW, LanguageHandler.HELP_DESC_CMD_T_CITIZEN));
		contents.add(Text.of(TextColors.GOLD, "/t chat", TextColors.GRAY, " - ", TextColors.YELLOW, LanguageHandler.HELP_DESC_CMD_T_CHAT));
		contents.add(Text.of(TextColors.GOLD, "/t perm <type> <perm> [true|false]", TextColors.GRAY, " - ", TextColors.YELLOW, LanguageHandler.HELP_DESC_CMD_T_PERM));
		contents.add(Text.of(TextColors.GOLD, "/t flag <flag> <true|false>", TextColors.GRAY, " - ", TextColors.YELLOW, LanguageHandler.HELP_DESC_CMD_T_FLAG));
		contents.add(Text.of(TextColors.GOLD, "/t taxes <amount>", TextColors.GRAY, " - ", TextColors.YELLOW, LanguageHandler.HELP_DESC_CMD_T_TAXES));
                contents.add(Text.of(TextColors.GOLD, "/t spawn [name]", TextColors.GRAY, " - ", TextColors.YELLOW, LanguageHandler.HELP_DESC_CMD_T_SPAWN));
                contents.add(Text.of(TextColors.GOLD, "/t outpost [number]", TextColors.GRAY, " - ", TextColors.YELLOW, LanguageHandler.HELP_DESC_CMD_T_OUTPOST));
		contents.add(Text.of(TextColors.GOLD, "/t home", TextColors.GRAY, " - ", TextColors.YELLOW, LanguageHandler.HELP_DESC_CMD_T_HOME));
		contents.add(Text.of(TextColors.GOLD, "/t setname <name>", TextColors.GRAY, " - ", TextColors.YELLOW, LanguageHandler.HELP_DESC_CMD_SETNAME));
		contents.add(Text.of(TextColors.GOLD, "/t settag [tag]", TextColors.GRAY, " - ", TextColors.YELLOW, LanguageHandler.HELP_DESC_CMD_SETTAG));
		contents.add(Text.of(TextColors.GOLD, "/t visit <towny> [name]", TextColors.GRAY, " - ", TextColors.YELLOW, LanguageHandler.HELP_DESC_CMD_T_VISIT));
		contents.add(Text.of(TextColors.GOLD, "/t setspawn <name>", TextColors.GRAY, " - ", TextColors.YELLOW, LanguageHandler.HELP_DESC_CMD_SETSPAWN));
		contents.add(Text.of(TextColors.GOLD, "/t delspawn <name>", TextColors.GRAY, " - ", TextColors.YELLOW, LanguageHandler.HELP_DESC_CMD_DELSPAWN));
		contents.add(Text.of(TextColors.GOLD, "/t buyextra <amount>", TextColors.GRAY, " - ", TextColors.YELLOW, LanguageHandler.HELP_DESC_CMD_T_BUYEXTRA));

		PaginationList.builder()
		.title(Text.of(TextColors.GOLD, "{ ", TextColors.YELLOW, "/towny", TextColors.GOLD, " }"))
		.contents(contents)
		.padding(Text.of("-"))
		.sendTo(src);
		return CommandResult.success();
	}
}
