package com.arckenver.towny.cmdexecutor.townyadmin;

import java.util.UUID;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.cmdelement.TownyNameElement;
import com.arckenver.towny.cmdelement.PlayerNameElement;
import com.arckenver.towny.object.Towny;

public class TownyadminForcejoinExecutor implements CommandExecutor
{
	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("towny.command.townyadmin.forcejoin")
				.arguments(
						GenericArguments.optional(new TownyNameElement(Text.of("towny"))),
						GenericArguments.optional(new PlayerNameElement(Text.of("player"))))
				.executor(new TownyadminForcejoinExecutor())
				.build(), "forcejoin");
	}

	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException
	{
		if (!ctx.<String>getOne("towny").isPresent() || !ctx.<String>getOne("player").isPresent())
		{
			src.sendMessage(Text.of(TextColors.YELLOW, "/ta forcejoin <towny> <player>"));
			return CommandResult.success();
		}
		String townyName = ctx.<String>getOne("towny").get();
		String playerName = ctx.<String>getOne("player").get();
		
		Towny towny = DataHandler.getTowny(townyName);
		if (towny == null)
		{
			src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_BADTOWNNNAME));
			return CommandResult.success();
		}
		UUID playerUUID = DataHandler.getPlayerUUID(playerName);
		if (playerUUID == null)
		{
			src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_BADPLAYERNAME));
			return CommandResult.success();
		}
		
		Towny playerTowny = DataHandler.getTownyOfPlayer(playerUUID);
		if (playerTowny != null)
		{
			playerTowny.removeCitizen(playerUUID);
			for (UUID uuid : playerTowny.getCitizens())
			{
				Sponge.getServer().getPlayer(uuid).ifPresent(p -> 
					p.sendMessage(Text.of(TextColors.AQUA, LanguageHandler.INFO_LEAVETOWN.replaceAll("\\{PLAYER\\}", playerName))));
			}
		}
		
		for (UUID uuid : towny.getCitizens())
		{
			Sponge.getServer().getPlayer(uuid).ifPresent(p -> 
				p.sendMessage(Text.of(TextColors.AQUA, LanguageHandler.INFO_JOINTOWNANNOUNCE.replaceAll("\\{PLAYER\\}", playerName))));
		}
		towny.addCitizen(playerUUID);
		DataHandler.saveTowny(towny.getUUID());
		Sponge.getServer().getPlayer(playerUUID).ifPresent(p -> 
			p.sendMessage(Text.of(TextColors.AQUA, LanguageHandler.INFO_JOINTOWN.replaceAll("\\{TOWN\\}", townyName))));
		src.sendMessage(Text.of(TextColors.GREEN, LanguageHandler.SUCCESS_GENERAL));
		return CommandResult.success();
	}
}
