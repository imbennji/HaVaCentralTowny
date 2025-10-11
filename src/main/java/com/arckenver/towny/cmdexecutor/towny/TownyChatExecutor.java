package com.arckenver.towny.cmdexecutor.towny;

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
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import com.arckenver.towny.ConfigHandler;
import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.TownyPlugin;
import com.arckenver.towny.channel.TownyMessageChannel;
import com.arckenver.towny.object.Towny;

public class TownyChatExecutor implements CommandExecutor
{
	public static void create(CommandSpec.Builder cmd) {
		CommandSpec chatCmd = CommandSpec.builder()
				.description(Text.of(""))
				.permission("towny.command.town.chat")
				.arguments(GenericArguments.optional(GenericArguments.remainingJoinedStrings(Text.of("msg"))))
				.executor(new TownyChatExecutor())
				.build();
		
		cmd.child(chatCmd, "chat", "c");
		
		Sponge.getCommandManager().register(TownyPlugin.getInstance(), chatCmd, "townychat", "nc");
	}

	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException
	{
		if (src instanceof Player)
		{
			Player player = (Player) src;
			Towny towny = DataHandler.getTownyOfPlayer(player.getUniqueId());
			if (towny == null)
			{
				player.setMessageChannel(MessageChannel.TO_ALL);
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOTOWN));
				return CommandResult.success();
			}
			TownyMessageChannel channel = towny.getMessageChannel();

			if (!ctx.<String>getOne("msg").isPresent())
			{
				if (player.getMessageChannel().equals(channel)) {
					player.setMessageChannel(MessageChannel.TO_ALL);
					src.sendMessage(Text.of(TextColors.YELLOW, LanguageHandler.INFO_TOWNCHAT_OFF));
				} else {
					player.setMessageChannel(channel);
					src.sendMessage(Text.of(TextColors.YELLOW, LanguageHandler.INFO_TOWNCHATON_ON));
				}
			}
			else
			{
				Text header = TextSerializers.FORMATTING_CODE.deserialize(ConfigHandler.getNode("others", "townyChatFormat").getString().replaceAll("\\{TOWN\\}", towny.getTag()).replaceAll("\\{TITLE\\}", DataHandler.getCitizenTitle(player.getUniqueId())));
				
				Text msg = Text.of(header, TextColors.RESET, player.getName(), TextColors.WHITE, ": ", TextColors.YELLOW, ctx.<String>getOne("msg").get());
				channel.send(player, msg);
				DataHandler.getSpyChannel().send(Text.of(TextSerializers.FORMATTING_CODE.deserialize(ConfigHandler.getNode("others", "townySpyChatTag").getString()), TextColors.RESET, msg));
			}

		}
		else
		{
			src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOPLAYER));
		}
		return CommandResult.success();
	}
}
