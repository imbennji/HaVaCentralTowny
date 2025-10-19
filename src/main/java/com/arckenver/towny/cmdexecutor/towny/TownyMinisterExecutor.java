package com.arckenver.towny.cmdexecutor.towny;

import java.util.UUID;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.cmdelement.CitizenNameElement;
import com.arckenver.towny.object.Towny;
import com.google.common.collect.ImmutableMap;

public class TownyMinisterExecutor implements CommandExecutor
{
	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("towny.command.town.minister")
				.arguments(
						GenericArguments.optional(GenericArguments.choices(Text.of("add|remove"),
								ImmutableMap.<String, String> builder()
										.put("add", "add")
										.put("remove", "remove")
										.build())),
						GenericArguments.optional(new CitizenNameElement(Text.of("citizen"))))
				.executor(new TownyMinisterExecutor())
				.build(), "minister");
	}

	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException
	{
		if (src instanceof Player)
		{
			Player player = (Player) src;
			Towny towny = DataHandler.getTownyOfPlayer(player.getUniqueId());
			if (towny == null)
			{
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NOTOWN));
				return CommandResult.success();
			}
			if (!towny.isPresident(player.getUniqueId()))
			{
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_PERM_TOWNPRES));
				return CommandResult.success();
			}
			if (!ctx.<String>getOne("add|remove").isPresent() || !ctx.<String>getOne("citizen").isPresent())
			{
				src.sendMessage(Text.of(LanguageHandler.colorYellow(), "/t minister add <citizen>\n/t minister remove <citizen>"));
				return CommandResult.success();
			}
			String addOrRemove = ctx.<String>getOne("add|remove").get();
			String playerName = ctx.<String>getOne("citizen").get();
			UUID uuid = DataHandler.getPlayerUUID(playerName);
			if (uuid == null)
			{
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_BADPLAYERNAME));
				return CommandResult.success();
			}
			if (player.getUniqueId().equals(uuid))
			{
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_PERM_HANDLECOMAYOR));
				return CommandResult.success();
			}
			if (addOrRemove.equalsIgnoreCase("add"))
			{
				if (towny.isMinister(uuid))
				{
					src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_ALREADYCOMAYOR.replaceAll("\\{PLAYER\\}", playerName)));
					return CommandResult.success();
				}
				towny.addMinister(uuid);
				DataHandler.saveTowny(towny.getUUID());
				src.sendMessage(Text.of(LanguageHandler.colorAqua(), LanguageHandler.SUCCESS_ADDCOMAYOR.replaceAll("\\{PLAYER\\}", playerName)));
				Sponge.getServer().getPlayer(uuid).ifPresent(
						p -> p.sendMessage(Text.of(LanguageHandler.colorAqua(), LanguageHandler.INFO_ADDCOMAYOR.replaceAll("\\{PLAYER\\}", player.getName()))));
			}
			else if (addOrRemove.equalsIgnoreCase("remove"))
			{
				if (!towny.isMinister(uuid))
				{
					src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NOCOMAYOR.replaceAll("\\{PLAYER\\}", playerName)));
					return CommandResult.success();
				}
				towny.removeMinister(uuid);
				DataHandler.saveTowny(towny.getUUID());
				src.sendMessage(Text.of(LanguageHandler.colorAqua(), LanguageHandler.SUCCESS_DELCOMAYOR.replaceAll("\\{PLAYER\\}", playerName)));
				Sponge.getServer().getPlayer(uuid).ifPresent(
						p -> p.sendMessage(Text.of(LanguageHandler.colorAqua(), LanguageHandler.INFO_DELCOMAYOR.replaceAll("\\{PLAYER\\}", player.getName()))));
			}
			else
			{
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_BADARG_AR));
			}
		}
		else
		{
			src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NOPLAYER));
		}
		return CommandResult.success();
	}
}
