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

public class PlotSetownerExecutor implements CommandExecutor
{
	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("towny.command.plot.setowner")
				.arguments(GenericArguments.optional(new PlayerNameElement(Text.of("owner"))))
				.executor(new PlotSetownerExecutor())
				.build(), "setowner");
	}

	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException
	{
		if (src instanceof Player)
		{
			if (!ctx.<String>getOne("owner").isPresent())
			{
				src.sendMessage(Text.of(TextColors.RED, "/z setowner <owner>"));
				return CommandResult.success();
			}
			String newOwnerName = ctx.<String>getOne("owner").get();
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
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NEEDSTANDPLOTSELF));
				return CommandResult.success();
			}
			if (!plot.isOwner(player.getUniqueId()) && !towny.isStaff(player.getUniqueId()))
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOOWNER));
				return CommandResult.success();
			}
			UUID newOwner = DataHandler.getPlayerUUID(newOwnerName);
			if (newOwner == null)
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_BADPLAYERNAME));
				return CommandResult.success();
			}
			if (newOwner.equals(plot.getOwner()))
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_ALREADYOWNER));
				return CommandResult.success();
			}
			if (!towny.isCitizen(newOwner))
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_OWNERNEEDTOWN));
				return CommandResult.success();
			}
			plot.setOwner(newOwner);
			DataHandler.saveTowny(towny.getUUID());
			final String plotName = plot.getName();
			if (newOwner.equals(player.getUniqueId()))
			{
				src.sendMessage(Text.of(TextColors.GREEN, LanguageHandler.SUCCESS_SETOWNER.replaceAll("\\{PLOT\\}", plotName)));
			}
			else
			{
				src.sendMessage(Text.of(TextColors.GREEN, LanguageHandler.SUCCESS_CHANGEOWNER.replaceAll("\\{PLAYER\\}", newOwnerName).replaceAll("\\{PLOT\\}", plotName)));
				Sponge.getServer().getPlayer(newOwner).ifPresent(
						p -> p.sendMessage(Text.of(TextColors.AQUA, LanguageHandler.INFO_CHANGEOWNER.replaceAll("\\{PLAYER\\}", player.getName()).replaceAll("\\{PLOT\\}", plotName))));
			}
		}
		else
		{
			src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOPLAYER));
		}
		return CommandResult.success();
	}
}
