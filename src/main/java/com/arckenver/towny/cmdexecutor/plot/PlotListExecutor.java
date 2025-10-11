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
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.Utils;
import com.arckenver.towny.cmdelement.TownyNameElement;
import com.arckenver.towny.object.Towny;

public class PlotListExecutor implements CommandExecutor
{
	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("towny.command.plot.list")
				.arguments(GenericArguments.optional(new TownyNameElement(Text.of("towny"))))
				.executor(new PlotListExecutor())
				.build(), "list");
	}

	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException
	{
		Towny towny;
		if (ctx.<String>getOne("towny").isPresent())
		{
			if (!src.hasPermission("towny.admin.plot.listall"))
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_PERM_LISTPLOTS));
				return CommandResult.success();
			}
			towny = DataHandler.getTowny(ctx.<String>getOne("towny").get());
			if (towny == null)
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_BADTOWNNNAME));
				return CommandResult.success();
			}
		}
		else
		{
			if (src instanceof Player)
			{
				Player player = (Player) src;
				towny = DataHandler.getTowny(player.getLocation());
				if (towny == null)
				{
					src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NEEDTOWNNAME));
					return CommandResult.success();
				}
			}
			else
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NEEDTOWNNAME));
				return CommandResult.success();
			}
		}
		
		String str = LanguageHandler.HEADER_PLOTLIST.replaceAll("\\{TOWN\\}", towny.getName());
		String[] splited = str.split("\\{PLOTLIST\\}");
		src.sendMessage(Utils.structureX(
				towny.getPlots().values().iterator(),
				Text.builder(splited[0]).color(TextColors.AQUA), 
				(b) -> b.append(Text.of(TextColors.GRAY, LanguageHandler.FORMAT_NONE)),
				(b, plot) -> b.append(Text.builder(plot.getName()).color(TextColors.YELLOW).onClick(TextActions.runCommand("/z info " + plot.getRealName())).build()),
				(b) -> b.append(Text.of(TextColors.AQUA, ", "))).append(Text.of(TextColors.AQUA, (splited.length > 1) ? splited[1] : "")).build());
		
		return CommandResult.success();
	}
}
