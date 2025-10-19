package com.arckenver.towny.cmdexecutor.townyadmin;

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
import com.arckenver.towny.cmdelement.TownyNameElement;
import com.arckenver.towny.object.Towny;
import com.google.common.collect.ImmutableMap;

public class TownyadminExtraspawnExecutor implements CommandExecutor
{
	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("towny.command.townyadmin.extraspawn")
				.arguments(
						GenericArguments.optional(GenericArguments.choices(Text.of("give|take|set"),
								ImmutableMap.<String, String> builder()
										.put("give", "give")
										.put("take", "take")
										.put("set", "set")
										.build())),
						GenericArguments.optional(new TownyNameElement(Text.of("towny"))),
						GenericArguments.optional(GenericArguments.integer(Text.of("amount"))))
				.executor(new TownyadminExtraspawnExecutor())
				.build(), "extraspawn");
	}

	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException
	{
		if (!ctx.<String>getOne("towny").isPresent() || !ctx.<String>getOne("give|take|set").isPresent() || !ctx.<String>getOne("amount").isPresent())
		{
			src.sendMessage(Text.of(LanguageHandler.colorYellow(), "/ta extraspawn <give|take|set> <towny> <amount>"));
			return CommandResult.success();
		}
		String townyName = ctx.<String>getOne("towny").get();
		Integer amount = Integer.valueOf(ctx.<Integer>getOne("amount").get());
		String operation = ctx.<String>getOne("give|take|set").get();
		
		Towny towny = DataHandler.getTowny(townyName);
		if (towny == null)
		{
			src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_BADTOWNNNAME));
			return CommandResult.success();
		}
		if (operation.equalsIgnoreCase("give"))
		{
			towny.addExtraSpawns(amount);
		}
		else if (operation.equalsIgnoreCase("take"))
		{
			towny.addExtraSpawns(amount);
		}
		else if (operation.equalsIgnoreCase("set"))
		{
			towny.addExtraSpawns(amount);
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
