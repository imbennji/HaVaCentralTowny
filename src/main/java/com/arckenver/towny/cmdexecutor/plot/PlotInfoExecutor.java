package com.arckenver.towny.cmdexecutor.plot;

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
import com.arckenver.towny.Utils;
import com.arckenver.towny.object.Towny;
import com.arckenver.towny.object.Plot;

public class PlotInfoExecutor implements CommandExecutor
{
	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("towny.command.plot.info")
				.arguments(GenericArguments.optional(GenericArguments.string(Text.of("plot"))))
				.executor(new PlotInfoExecutor())
				.build(), "info");
	}

	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException
	{
		if (src instanceof Player)
		{
			Player player = (Player) src;
			Towny towny = DataHandler.getTowny(player.getLocation());
			if (towny == null)
			{
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NEEDSTANDTOWN));
				return CommandResult.success();
			}
			Plot plot = null;
			if (!ctx.<String>getOne("plot").isPresent())
			{
				plot = towny.getPlot(player.getLocation());
				if (plot == null)
				{
					src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NEEDPLOT));
					return CommandResult.success();
				}
			}
			else
			{
				String plotName = ctx.<String>getOne("plot").get();
				for (Plot z : towny.getPlots().values())
				{
					if (z.isNamed() && z.getRealName().equalsIgnoreCase(plotName))
					{
						plot = z;
					}
				}
				if (plot == null)
				{
					src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_BADPLOTNNAME));
					return CommandResult.success();
				}
			}
			int clicker = Utils.CLICKER_NONE;
			if (plot.isOwner(player.getUniqueId()) || plot.isCoowner(player.getUniqueId()) || towny.isStaff(player.getUniqueId()))
			{
				clicker = Utils.CLICKER_DEFAULT;
			}
			src.sendMessage(Utils.formatPlotDescription(plot, towny, clicker));
		}
		else
		{
			src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NOPLAYER));
		}
		return CommandResult.success();
	}
}
