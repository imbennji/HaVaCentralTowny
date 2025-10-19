package com.arckenver.towny.cmdexecutor.plot;

import java.math.BigDecimal;

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
import com.arckenver.towny.Utils;
import com.arckenver.towny.object.Towny;
import com.arckenver.towny.object.Plot;

public class PlotSellExecutor implements CommandExecutor
{
	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("towny.command.plot.sell")
				.arguments(GenericArguments.optional(GenericArguments.doubleNum(Text.of("price"))))
				.executor(new PlotSellExecutor())
				.build(), "sell", "forsale", "fs");
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
			Plot plot = towny.getPlot(player.getLocation());
			if (plot == null)
			{
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NEEDSTANDPLOTSELF));
				return CommandResult.success();
			}
			if ((!plot.isOwner(player.getUniqueId()) || towny.isAdmin()) && !towny.isStaff(player.getUniqueId()))
			{
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NOOWNER));
				return CommandResult.success();
			}
			if (!ctx.<Double>getOne("price").isPresent())
			{
				src.sendMessage(Text.of(LanguageHandler.colorYellow(), "/z sell <price>"));
				return CommandResult.success();
			}
			BigDecimal price = BigDecimal.valueOf(ctx.<Double>getOne("price").get());
			if (price.compareTo(BigDecimal.ZERO) == -1)
			{
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_BADARG_P));
				return CommandResult.success();
			}
			plot.setPrice(price);
			DataHandler.saveTowny(towny.getUUID());
			towny.getCitizens().forEach(
				uuid -> Sponge.getServer().getPlayer(uuid).ifPresent(
						p -> {
							String str = LanguageHandler.INFO_PLOTFORSALE.replaceAll("\\{PLAYER\\}",  player.getName()).replaceAll("\\{PLOT\\}", plot.getName());
							String[] splited = str.split("\\{AMOUNT\\}");
							src.sendMessage(Text.builder()
									.append(Text.of(LanguageHandler.colorAqua(), (splited.length > 0) ? splited[0] : ""))
									.append(Utils.formatPrice(LanguageHandler.colorAqua(), price))
									.append(Text.of(LanguageHandler.colorAqua(), (splited.length > 1) ? splited[1] : "")).build());
						}));
		}
		else
		{
			src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NOPLAYER));
		}
		return CommandResult.success();
	}
}
