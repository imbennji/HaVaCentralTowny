package com.arckenver.towny.cmdexecutor.plot;

import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.object.Towny;
import com.arckenver.towny.object.Plot;
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

public class PlotSetDisplayNameExecutor implements CommandExecutor
{
	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("towny.command.plot.setdisplayname")
				.arguments(GenericArguments.optional(GenericArguments.string(Text.of("name"))))
				.executor(new PlotSetDisplayNameExecutor())
				.build(), "setdisplayname", "setdname", "setdisplay");
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
			Player player = (Player) src;
			Towny towny = DataHandler.getTowny(player.getLocation());
			if (towny == null)
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NEEDSTANDTOWN));
				return CommandResult.success();
			}
			Plot currentPlot = towny.getPlot(player.getLocation());
			if (currentPlot == null)
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NEEDSTANDPLOTSELF));
				return CommandResult.success();
			}//if plot owner, co owner or rather towny staff
			if (!currentPlot.isCoowner(player.getUniqueId()) && !currentPlot.isOwner(player.getUniqueId()) && !towny.isStaff(player.getUniqueId()))
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOOWNER));
				return CommandResult.success();
			}
			currentPlot.setDisplayName(plotName);
			DataHandler.saveTowny(towny.getUUID());
			src.sendMessage(Text.of(TextColors.GREEN, LanguageHandler.SUCCESS_PLOTRENAME.replaceAll("\\{PLOT\\}", currentPlot.getDisplayName())));
		}
		else
		{
			src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOPLAYER));
		}
		return CommandResult.success();
	}
}
