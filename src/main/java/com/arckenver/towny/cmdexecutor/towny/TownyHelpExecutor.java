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

		contents.add(Text.of(LanguageHandler.colorGold(), "/t info [towny]", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_T_INFO));
		contents.add(Text.of(LanguageHandler.colorGold(), "/t here", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_T_HERE));
		contents.add(Text.of(LanguageHandler.colorGold(), "/t cost", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_T_COST));
		contents.add(Text.of(LanguageHandler.colorGold(), "/t see", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_T_SEE));
		contents.add(Text.of(LanguageHandler.colorGold(), "/t list", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_T_LIST));
		contents.add(Text.of(LanguageHandler.colorGold(), "/t create <name>", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_T_CREATE));
		contents.add(Text.of(LanguageHandler.colorGold(), "/t deposit <amount>", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_T_DEPOSIT));
		contents.add(Text.of(LanguageHandler.colorGold(), "/t withdraw <amount>", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_T_WITHDRAW));
		contents.add(Text.of(LanguageHandler.colorGold(), "/t claim", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_T_CLAIM));
		contents.add(Text.of(LanguageHandler.colorGold(), "/t unclaim", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_T_UNCLAIM));
		contents.add(Text.of(LanguageHandler.colorGold(), "/t invite <player>", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_T_INVITE));
		contents.add(Text.of(LanguageHandler.colorGold(), "/t join <towny>", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_T_JOIN));
		contents.add(Text.of(LanguageHandler.colorGold(), "/t kick <player>", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_T_KICK));
		contents.add(Text.of(LanguageHandler.colorGold(), "/t leave", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_T_LEAVE));
		contents.add(Text.of(LanguageHandler.colorGold(), "/t resign <successor>", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_T_RESIGN));
		contents.add(Text.of(LanguageHandler.colorGold(), "/t minister <add/remove> <player>", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_T_MINISTER));
		contents.add(Text.of(LanguageHandler.colorGold(), "/t citizen <player>", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_T_CITIZEN));
		contents.add(Text.of(LanguageHandler.colorGold(), "/t chat", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_T_CHAT));
		contents.add(Text.of(LanguageHandler.colorGold(), "/t perm <type> <perm> [true|false]", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_T_PERM));
		contents.add(Text.of(LanguageHandler.colorGold(), "/t flag <flag> <true|false>", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_T_FLAG));
		contents.add(Text.of(LanguageHandler.colorGold(), "/t taxes <amount>", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_T_TAXES));
                contents.add(Text.of(LanguageHandler.colorGold(), "/t spawn [name]", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_T_SPAWN));
                contents.add(Text.of(LanguageHandler.colorGold(), "/t outpost [number]", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_T_OUTPOST));
		contents.add(Text.of(LanguageHandler.colorGold(), "/t home", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_T_HOME));
		contents.add(Text.of(LanguageHandler.colorGold(), "/t setname <name>", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_SETNAME));
		contents.add(Text.of(LanguageHandler.colorGold(), "/t settag [tag]", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_SETTAG));
		contents.add(Text.of(LanguageHandler.colorGold(), "/t visit <towny> [name]", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_T_VISIT));
		contents.add(Text.of(LanguageHandler.colorGold(), "/t setspawn <name>", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_SETSPAWN));
		contents.add(Text.of(LanguageHandler.colorGold(), "/t delspawn <name>", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_DELSPAWN));
		contents.add(Text.of(LanguageHandler.colorGold(), "/t buyextra <amount>", LanguageHandler.colorGray(), " - ", LanguageHandler.colorYellow(), LanguageHandler.HELP_DESC_CMD_T_BUYEXTRA));

		PaginationList.builder()
		.title(Text.of(LanguageHandler.colorGold(), "{ ", LanguageHandler.colorYellow(), "/towny", LanguageHandler.colorGold(), " }"))
		.contents(contents)
		.padding(Text.of("-"))
		.sendTo(src);
		return CommandResult.success();
	}
}
