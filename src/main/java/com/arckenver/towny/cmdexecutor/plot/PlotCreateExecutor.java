package com.arckenver.towny.cmdexecutor.plot;

import java.util.Map.Entry;
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
import com.arckenver.towny.claim.ChunkClaimUtils;
import com.arckenver.towny.cmdelement.PlayerNameElement;
import com.arckenver.towny.object.Towny;
import com.arckenver.towny.object.Point;
import com.arckenver.towny.object.Rect;
import com.arckenver.towny.object.Plot;

public class PlotCreateExecutor implements CommandExecutor
{
	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("towny.command.plot.create")
				.arguments(
						GenericArguments.optional(GenericArguments.string(Text.of("name"))),
						GenericArguments.optional(new PlayerNameElement(Text.of("owner"))))
				.executor(new PlotCreateExecutor())
				.build(), "create", "add");
	}

	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException
	{
		if (src instanceof Player)
		{
			Player player = (Player) src;
                        ChunkClaimUtils.selectCurrentChunk(player);
                        Towny towny = DataHandler.getTowny(player.getLocation());
			if (towny == null)
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NEEDSTANDTOWN));
				return CommandResult.success();
			}
			if (!towny.isStaff(player.getUniqueId()))
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_PERM_TOWNSTAFF));
				return CommandResult.success();
			}
			String plotName = null;
			if (ctx.<String>getOne("name").isPresent())
			{
				plotName = ctx.<String>getOne("name").get();
			}
			if (plotName != null && !plotName.matches("[\\p{Alnum}\\p{IsIdeographic}\\p{IsLetter}\"_\"]*{1,30}"))
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_ALPHASPAWN
						.replaceAll("\\{MIN\\}", "1")
						.replaceAll("\\{MAX\\}", "30")));
				return CommandResult.success();
			}
			UUID owner = null;
			if (ctx.<String>getOne("owner").isPresent())
			{
				owner = DataHandler.getPlayerUUID(ctx.<String>getOne("owner").get());
				if (owner == null)
				{
					src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_BADPLAYERNAME));
					return CommandResult.success();
				}
			}
                        Point a = DataHandler.getFirstPoint(player.getUniqueId());
                        Point b = DataHandler.getSecondPoint(player.getUniqueId());
                        if (a == null || b == null)
                        {
                                src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NEEDCHUNKSELECT));
				return CommandResult.success();
			}
			Rect rect = new Rect(a, b);
			if (!towny.getRegion().isInside(rect))
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_PLOTNOTINTOWN));
				return CommandResult.success();
			}
			for (Plot plot : towny.getPlots().values())
			{
				if (plotName != null && plot.isNamed() && plot.getName().equalsIgnoreCase(plotName))
				{
					src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_PLOTNAME));
					return CommandResult.success();
				}
				if (rect.intersects(plot.getRect()))
				{
					src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_PLOTINTERSECT));
					return CommandResult.success();
				}
			}
			Plot plot = new Plot(UUID.randomUUID(), plotName, rect, owner);
			for (Entry<String, Boolean> e : towny.getFlags().entrySet())
			{
				plot.setFlag(e.getKey(), e.getValue());
			}
			towny.addPlot(plot);
			DataHandler.saveTowny(towny.getUUID());
			src.sendMessage(Text.of(TextColors.GREEN, LanguageHandler.SUCCESS_PLOTCREATE.replaceAll("\\{PLOT\\}", plot.getName())));
			Sponge.getServer().getPlayer(owner).ifPresent(
					p -> p.sendMessage(Text.of(TextColors.AQUA, LanguageHandler.SUCCESS_SETOWNER.replaceAll("\\{PLOT\\}", plot.getName()))));
		}
		else
		{
			src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOPLAYER));
		}
		return CommandResult.success();
	}
}
