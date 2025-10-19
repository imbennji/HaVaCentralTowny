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
import com.arckenver.towny.object.Towny;
import com.arckenver.towny.object.Plot;

public class PlotRenameExecutor implements CommandExecutor
{
	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("towny.command.plot.rename")
				.arguments(GenericArguments.optional(GenericArguments.string(Text.of("name"))))
				.executor(new PlotRenameExecutor())
				.build(), "rename");
	}

	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException
	{
		if (src instanceof Player)
		{
			String plotName = null;
			if (ctx.<String>getOne("name").isPresent())
			{
				plotName = ctx.<String>getOne("name").get();
			}
			if (plotName != null && !plotName.matches("[\\p{Alnum}\\p{IsIdeographic}\\p{IsLetter}\"_\"]*{1,30}"))
			{
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_ALPHASPAWN
						.replaceAll("\\{MIN\\}", "1")
						.replaceAll("\\{MAX\\}", "30")));
				return CommandResult.success();
			}
			Player player = (Player) src;
			Towny towny = DataHandler.getTowny(player.getLocation());
			if (towny == null)
			{
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NEEDSTANDTOWN));
				return CommandResult.success();
			}
			Plot currentPlot = towny.getPlot(player.getLocation());
			if (currentPlot == null)
			{
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NEEDSTANDPLOTSELF));
				return CommandResult.success();
			}
			if (!towny.isStaff(player.getUniqueId()))
			{
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NOOWNER));
				return CommandResult.success();
			}
			if (plotName != null)
			{
				for (Plot plot : towny.getPlots().values())
				{
					if (plot.isNamed() && plot.getRealName().equalsIgnoreCase(plotName))
					{
						src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_PLOTNAME));
						return CommandResult.success();
					}
				}
			}
			currentPlot.setName(plotName);
			DataHandler.saveTowny(towny.getUUID());
			src.sendMessage(Text.of(LanguageHandler.colorGreen(), LanguageHandler.SUCCESS_PLOTRENAME.replaceAll("\\{PLOT\\}", currentPlot.getName())));
		}
		else
		{
			src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NOPLAYER));
		}
		return CommandResult.success();
	}
}
