package com.arckenver.towny.cmdexecutor.plot;

import java.util.stream.Collectors;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import com.arckenver.towny.ConfigHandler;
import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.Utils;
import com.arckenver.towny.object.Towny;
import com.arckenver.towny.object.Plot;

public class PlotFlagExecutor implements CommandExecutor
{
	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("towny.command.plot.flag")
				.arguments(
						GenericArguments.choices(Text.of("flag"), ConfigHandler.getNode("towny", "flags")
								.getChildrenMap()
								.keySet()
								.stream()
								.map(o -> o.toString())
								.collect(Collectors.toMap(flag -> flag, flag -> flag))),
						GenericArguments.optional(GenericArguments.bool(Text.of("bool"))))
				.executor(new PlotFlagExecutor())
				.build(), "flag");
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
			if (!plot.isOwner(player.getUniqueId()) && !towny.isStaff(player.getUniqueId()))
			{
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NOOWNER));
				return CommandResult.success();
			}
                        String flag = ctx.<String>getOne("flag").get();
                        if (!plot.canSetFlag(flag)) {
                                src.sendMessage(Text.of(LanguageHandler.colorRed(),
                                                LanguageHandler.ERROR_PLOT_TYPE_FLAG_LOCKED
                                                                .replace("{FLAG}", flag)
                                                                .replace("{TYPE}", plot.getType().getDisplayName())));
                                return CommandResult.success();
                        }
                        boolean bool = (ctx.<Boolean>getOne("bool").isPresent()) ? ctx.<Boolean>getOne("bool").get() : !plot.getFlag(flag);
                        plot.setFlag(flag, bool);
			DataHandler.saveTowny(towny.getUUID());
			src.sendMessage(Utils.formatPlotDescription(plot, towny, Utils.CLICKER_DEFAULT));
		}
		else
		{
			src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NOPLAYER));
		}
		return CommandResult.success();
	}
}
