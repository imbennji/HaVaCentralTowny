package com.arckenver.towny.cmdexecutor.townyadmin;

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
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.arckenver.towny.ConfigHandler;
import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.claim.ChunkClaimUtils;
import com.arckenver.towny.object.Towny;
import com.arckenver.towny.object.Point;
import com.arckenver.towny.object.Rect;
import com.arckenver.towny.object.Region;
import com.arckenver.towny.object.Plot;
import com.flowpowered.math.vector.Vector2i;

public class TownyadminUnclaimExecutor implements CommandExecutor
{
	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("towny.command.townyadmin.unclaim")
				.arguments(GenericArguments.optional(GenericArguments.string(Text.of("towny"))))
				.executor(new TownyadminUnclaimExecutor())
				.build(), "unclaim");
	}

	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException
	{
		if (src instanceof Player)
		{
			if (!ctx.<String>getOne("towny").isPresent())
			{
				src.sendMessage(Text.of(TextColors.YELLOW, "/ta unclaim <towny>"));
				return CommandResult.success();
			}
                        Player player = (Player) src;
                        ChunkClaimUtils.selectCurrentChunk(player);
			String townyName = ctx.<String>getOne("towny").get();
			Towny towny = DataHandler.getTowny(townyName);
			if (towny == null)
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_BADTOWNNNAME));
				return CommandResult.success();
			}
                        Point a = DataHandler.getFirstPoint(player.getUniqueId());
                        Point b = DataHandler.getSecondPoint(player.getUniqueId());
                        if (a == null || b == null)
                        {
                                src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NEEDCHUNKSELECT));
				return CommandResult.success();
			}
			if (!ConfigHandler.getNode("worlds").getNode(a.getWorld().getName()).getNode("enabled").getBoolean())
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_PLUGINDISABLEDINWORLD));
				return CommandResult.success();
			}
			Rect rect = new Rect(a, b);
			if (!towny.getRegion().intersects(rect))
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NEEDINTERSECT));
				return CommandResult.success();
			}
			for (Location<World> spawn : towny.getSpawns().values())
			{
				if (rect.isInside(new Vector2i(spawn.getBlockX(), spawn.getBlockZ())))
				{
					src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_AREACONTAINSPAWN));
					return CommandResult.success();
				}
			}
			for (Plot plot : towny.getPlots().values())
			{
				if (plot.getRect().intersects(rect))
				{
					src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_SELECTIONCONTAINPLOT));
					return CommandResult.success();
				}
			}
			Region claimed = towny.getRegion().copy();
			claimed.removeRect(rect);

			towny.setRegion(claimed);
			DataHandler.addToWorldChunks(towny);
			DataHandler.saveTowny(towny.getUUID());
			src.sendMessage(Text.of(TextColors.AQUA, LanguageHandler.SUCCESS_GENERAL));
		}
		else
		{
			src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOPLAYER));
		}
		return CommandResult.success();
	}
}
