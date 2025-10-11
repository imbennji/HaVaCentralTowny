package com.arckenver.towny.cmdexecutor.townyadmin;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.channel.TownyMessageChannel;

public class TownyadminSpyExecutor implements CommandExecutor
{
	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("towny.command.townyadmin.spy")
				.arguments()
				.executor(new TownyadminSpyExecutor())
				.build(), "spy", "spychat");
	}

	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException
	{
		if (src instanceof Player)
		{
			TownyMessageChannel channel = DataHandler.getSpyChannel();
			if (channel.getMembers().contains(src))
			{
				channel.removeMember(src);
				src.sendMessage(Text.of(TextColors.YELLOW, LanguageHandler.INFO_TOWNSPY_OFF));
			}
			else
			{
				channel.addMember(src);
				src.sendMessage(Text.of(TextColors.YELLOW, LanguageHandler.INFO_TOWNSPY_ON));
			}
		}
		else
		{
			src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOPLAYER));
		}
		return CommandResult.success();
	}
}
