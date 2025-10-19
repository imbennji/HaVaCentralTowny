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
import com.google.common.collect.ImmutableMap;

public class PlotPermExecutor implements CommandExecutor
{
	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("towny.command.plot.perm")
                                .arguments(
                                                GenericArguments.choices(Text.of("type"),
                                                                ImmutableMap.<String, String> builder()
                                                                                .put("friend", Towny.TYPE_FRIEND)
                                                                                .put("coowner", Towny.TYPE_FRIEND)
                                                                                .put("resident", Towny.TYPE_RESIDENT)
                                                                                .put("citizen", Towny.TYPE_RESIDENT)
                                                                                .put("ally", Towny.TYPE_ALLY)
                                                                                .put("outsider", Towny.TYPE_OUTSIDER)
                                                                                .build()),
                                                GenericArguments.choices(Text.of("perm"),
                                                                ImmutableMap.<String, String> builder()
                                                                                .put("build", Towny.PERM_BUILD)
                                                                                .put("destroy", Towny.PERM_DESTROY)
                                                                                .put("break", Towny.PERM_DESTROY)
                                                                                .put("switch", Towny.PERM_SWITCH)
                                                                                .put("itemuse", Towny.PERM_ITEM_USE)
                                                                                .put("item_use", Towny.PERM_ITEM_USE)
                                                                                .put("use", Towny.PERM_ITEM_USE)
                                                                                .put("interact", Towny.PERM_SWITCH)
                                                                                .build()),
						GenericArguments.optional(GenericArguments.bool(Text.of("bool"))))
				.executor(new PlotPermExecutor())
				.build(), "perm");
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
                        String type = ctx.<String>getOne("type").get();
                        String perm = ctx.<String>getOne("perm").get();
                        if (!plot.canSetPerm(type, perm)) {
                                src.sendMessage(Text.of(LanguageHandler.colorRed(),
                                                LanguageHandler.ERROR_PLOT_TYPE_PERM_LOCKED
                                                                .replace("{PERM}", perm)
                                                                .replace("{TYPE}", plot.getType().getDisplayName())));
                                return CommandResult.success();
                        }
                        boolean bool = (ctx.<Boolean>getOne("bool").isPresent()) ? ctx.<Boolean>getOne("bool").get() : !plot.getPerm(type, perm);
                        plot.setPerm(type, perm, bool);
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
