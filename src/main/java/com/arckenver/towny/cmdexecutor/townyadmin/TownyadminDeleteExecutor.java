package com.arckenver.towny.cmdexecutor.townyadmin;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.format.TextColors;

import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.cmdelement.TownyNameElement;
import com.arckenver.towny.object.Towny;

public class TownyadminDeleteExecutor implements CommandExecutor
{
	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("towny.command.townyadmin.delete")
				.arguments(GenericArguments.optional(new TownyNameElement(Text.of("towny"))))
				.executor(new TownyadminDeleteExecutor())
				.build(), "delete");
	}

	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException
	{
		if (!ctx.<String>getOne("towny").isPresent())
		{
			src.sendMessage(Text.of(TextColors.YELLOW, "/ta delete <towny>"));
			return CommandResult.success();
		}
		String townyName = ctx.<String>getOne("towny").get();
		Towny towny = DataHandler.getTowny(townyName);
		if (towny == null)
		{
			src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_BADTOWNNNAME));
			return CommandResult.success();
		}
		DataHandler.removeTowny(towny.getUUID());
		MessageChannel.TO_ALL.send(Text.of(TextColors.AQUA, LanguageHandler.INFO_TOWNFALL.replaceAll("\\{TOWN\\}", towny.getName())));
		return CommandResult.success();
	}
}
