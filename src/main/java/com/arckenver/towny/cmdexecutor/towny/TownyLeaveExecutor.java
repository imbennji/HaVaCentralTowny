package com.arckenver.towny.cmdexecutor.towny;

import java.util.UUID;

import org.spongepowered.api.Sponge;
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

public class TownyLeaveExecutor implements CommandExecutor
{
	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("towny.command.town.leave")
				.arguments()
				.executor(new TownyLeaveExecutor())
				.build(), "leave", "quit");
	}

	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException
	{
		if (src instanceof Player)
		{
			Player player = (Player) src;
			Towny towny = DataHandler.getTownyOfPlayer(player.getUniqueId());
			if (towny == null)
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOTOWN));
				return CommandResult.success();
			}
			if (towny.isPresident(player.getUniqueId()))
			{
				if (towny.getNumCitizens() > 1)
				{
					src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NEEDRESIGN));
					return CommandResult.success();
				}
				towny.removeCitizen(player.getUniqueId());
				src.sendMessage(Text.of(TextColors.GREEN, LanguageHandler.SUCCESS_LEAVETOWN));
				DataHandler.removeTowny(towny.getUUID());
				MessageChannel.TO_ALL.send(Text.of(TextColors.AQUA, LanguageHandler.INFO_TOWNFALL.replaceAll("\\{TOWN\\}", towny.getName())));
				return CommandResult.success();
			}
			towny.removeCitizen(player.getUniqueId());
			DataHandler.saveTowny(towny.getUUID());
			src.sendMessage(Text.of(TextColors.GREEN, LanguageHandler.SUCCESS_LEAVETOWN));
			for (UUID citizen : towny.getCitizens())
			{
				Sponge.getServer().getPlayer(citizen).ifPresent(
						p -> p.sendMessage(Text.of(TextColors.AQUA, LanguageHandler.INFO_LEAVETOWN.replaceAll("\\{PLAYER\\}", player.getName()))));
			}
		}
		else
		{
			src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOPLAYER));
		}
		return CommandResult.success();
	}
}
