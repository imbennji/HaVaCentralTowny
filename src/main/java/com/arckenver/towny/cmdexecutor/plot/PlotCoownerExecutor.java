package com.arckenver.towny.cmdexecutor.plot;

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
import org.spongepowered.api.text.format.TextColors;

import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.cmdelement.PlayerNameElement;
import com.arckenver.towny.object.Towny;
import com.arckenver.towny.object.Plot;
import com.google.common.collect.ImmutableMap;

public class PlotCoownerExecutor implements CommandExecutor
{
	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("towny.command.plot.coowner")
				.arguments(
						GenericArguments.optional(GenericArguments.choices(Text.of("add|remove"),
								ImmutableMap.<String, String> builder()
										.put("add", "add")
										.put("remove", "remove")
										.build())),
						GenericArguments.optional(new PlayerNameElement(Text.of("citizen"))))
				.executor(new PlotCoownerExecutor())
				.build(), "coowner");
	}

	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException
	{
		if (src instanceof Player)
		{
			Player player = (Player) src;
			Towny towny = DataHandler.getTowny(player.getLocation());
			if (towny == null)
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NEEDSTANDTOWN));
				return CommandResult.success();
			}
			Plot plot = towny.getPlot(player.getLocation());
			if (plot == null)
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOSTANDPLOTTOWN));
				return CommandResult.success();
			}
			final String plotName = plot.getName();
			if (!plot.isOwner(player.getUniqueId()))
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_PERM_NOTOWNER));
				return CommandResult.success();
			}
			if (!ctx.<String>getOne("add|remove").isPresent() || !ctx.<String>getOne("citizen").isPresent())
			{
				src.sendMessage(Text.of(TextColors.YELLOW, "/z coowner add <citizen>\n/z coowner remove <citizen>"));
				return CommandResult.success();
			}
			String addOrRemove = ctx.<String>getOne("add|remove").get();
			String playerName = ctx.<String>getOne("citizen").get();
			UUID uuid = DataHandler.getPlayerUUID(playerName);
			if (uuid == null)
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_BADPLAYERNAME));
				return CommandResult.success();
			}
			if (player.getUniqueId().equals(uuid))
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_PERM_MANAGECOOWNER));
				return CommandResult.success();
			}
			if (addOrRemove.equalsIgnoreCase("add"))
			{
				if (plot.isCoowner(uuid))
				{
					src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_ALREADYCOOWNER.replaceAll("\\{PLAYER\\}", playerName)));
					return CommandResult.success();
				}
				plot.addCoowner(uuid);
				DataHandler.saveTowny(towny.getUUID());
				src.sendMessage(Text.of(TextColors.AQUA, LanguageHandler.SUCCESS_ADDCOOWNER.replaceAll("\\{PLAYER\\}", playerName)));
				Sponge.getServer().getPlayer(uuid).ifPresent(
						p -> p.sendMessage(Text.of(TextColors.AQUA, LanguageHandler.INFO_ADDCOOWNER.replaceAll("\\{PLAYER\\}", player.getName()).replaceAll("\\{PLOT\\}", plotName))));
			}
			else if (addOrRemove.equalsIgnoreCase("remove"))
			{
				if (!towny.isMinister(uuid))
				{
					src.sendMessage(Text.of(TextColors.RED, LanguageHandler.INFO_ALREADYNOCOOWNER.replaceAll("\\{PLAYER\\}", playerName)));
					return CommandResult.success();
				}
				plot.removeCoowner(uuid);
				DataHandler.saveTowny(towny.getUUID());
				src.sendMessage(Text.of(TextColors.AQUA, LanguageHandler.SUCCESS_DELCOOWNER.replaceAll("\\{PLAYER\\}", playerName)));
				Sponge.getServer().getPlayer(uuid).ifPresent(
						p -> p.sendMessage(Text.of(TextColors.AQUA, LanguageHandler.INFO_DELCOOWNER.replaceAll("\\{PLAYER\\}", player.getName()).replaceAll("\\{PLOT\\}", plotName))));
			}
			else
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_BADARG_AR));
			}
		}
		else
		{
			src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOPLAYER));
		}
		return CommandResult.success();
	}
}
