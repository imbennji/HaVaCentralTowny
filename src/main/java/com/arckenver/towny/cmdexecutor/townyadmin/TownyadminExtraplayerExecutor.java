package com.arckenver.towny.cmdexecutor.townyadmin;

import java.util.UUID;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.cmdelement.PlayerNameElement;
import com.arckenver.towny.object.Towny;
import com.google.common.collect.ImmutableMap;

public class TownyadminExtraplayerExecutor implements CommandExecutor
{
	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("towny.command.townyadmin.extraplayer")
				.arguments(
						GenericArguments.optional(GenericArguments.choices(Text.of("give|take|set"),
								ImmutableMap.<String, String> builder()
										.put("give", "give")
										.put("take", "take")
										.put("set", "set")
										.build())),
						GenericArguments.optional(new PlayerNameElement(Text.of("player"))),
						GenericArguments.optional(GenericArguments.integer(Text.of("amount"))))
				.executor(new TownyadminExtraplayerExecutor())
				.build(), "extraplayer");
	}

	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException
	{
		if (!ctx.<String>getOne("player").isPresent() || !ctx.<String>getOne("give|take|set").isPresent() || !ctx.<String>getOne("amount").isPresent())
		{
			src.sendMessage(Text.of(LanguageHandler.colorYellow(), "/ta extraplayer <give|take|set> <player> <amount>"));
			return CommandResult.success();
		}
		String playerName = ctx.<String>getOne("player").get();
		Integer amount = Integer.valueOf(ctx.<Integer>getOne("amount").get());
		String operation = ctx.<String>getOne("give|take|set").get();
		
		UUID playerUUID = DataHandler.getPlayerUUID(playerName);
		if (playerUUID == null)
		{
			src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_BADPLAYERNAME));
			return CommandResult.success();
		}
		
		Towny towny = DataHandler.getTownyOfPlayer(playerUUID);
		if (towny == null)
		{
			src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_PLAYERNOTINTOWN));
			return CommandResult.success();
		}
		if (operation.equalsIgnoreCase("give"))
		{
			towny.addExtras(amount);
		}
		else if (operation.equalsIgnoreCase("take"))
		{
			towny.removeExtras(amount);
		}
		else if (operation.equalsIgnoreCase("set"))
		{
			towny.setExtras(amount);
		}
		else
		{
			src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_BADARG_GTS));
			return CommandResult.success();
		}
		DataHandler.saveTowny(towny.getUUID());
		src.sendMessage(Text.of(LanguageHandler.colorGreen(), LanguageHandler.SUCCESS_GENERAL));
		return CommandResult.success();
	}
}
