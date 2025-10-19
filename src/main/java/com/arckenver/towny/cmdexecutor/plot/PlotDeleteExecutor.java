package com.arckenver.towny.cmdexecutor.plot;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.format.TextColors;

import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.object.Towny;
import com.arckenver.towny.object.Plot;
import com.arckenver.towny.object.PlotType;

public class PlotDeleteExecutor implements CommandExecutor
{
	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("towny.command.plot.delete")
				.arguments()
				.executor(new PlotDeleteExecutor())
				.build(), "delete", "remove");
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
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_BADPLOTNNAME));
				return CommandResult.success();
			}
			String plotName = plot.getName();
			boolean wasJail = plot.getType() == PlotType.JAIL;
			if (!towny.isStaff(player.getUniqueId()))
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_PERM_TOWNSTAFF));
				return CommandResult.success();
			}
			towny.removePlot(plot.getUUID());
			DataHandler.saveTowny(towny.getUUID());
			if (wasJail) {
				DataHandler.releaseResidentsInJailPlot(towny.getUUID(), plot.getUUID());
			}
			src.sendMessage(Text.of(TextColors.GREEN, LanguageHandler.SUCCESS_DELPLOT.replaceAll("\\{PLOT\\}", plotName)));
			MessageChannel.TO_CONSOLE.send(Text.of(player.getName(), " > ", towny.getName(), ": ", LanguageHandler.SUCCESS_DELPLOT.replaceAll("\\{PLOT\\}", plotName)));
		}
		else
		{
			src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOPLAYER));
		}
		return CommandResult.success();
	}
}
