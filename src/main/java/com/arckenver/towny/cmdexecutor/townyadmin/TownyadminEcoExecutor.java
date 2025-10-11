package com.arckenver.towny.cmdexecutor.townyadmin;

import java.math.BigDecimal;
import java.util.Optional;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.TownyPlugin;
import com.arckenver.towny.cmdelement.TownyNameElement;
import com.arckenver.towny.object.Towny;
import com.google.common.collect.ImmutableMap;

public class TownyadminEcoExecutor implements CommandExecutor
{
	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("towny.command.townyadmin.eco")
				.arguments(
						GenericArguments.optional(GenericArguments.choices(Text.of("give|take|set"),
								ImmutableMap.<String, String> builder()
										.put("give", "give")
										.put("take", "take")
										.put("set", "set")
										.build())),
						GenericArguments.optional(new TownyNameElement(Text.of("towny"))),
						GenericArguments.optional(GenericArguments.doubleNum(Text.of("amount"))))
				.executor(new TownyadminEcoExecutor())
				.build(), "eco");
	}

	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException
	{
		if (!ctx.<String>getOne("towny").isPresent() || !ctx.<String>getOne("give|take|set").isPresent() || !ctx.<String>getOne("amount").isPresent())
		{
			src.sendMessage(Text.of(TextColors.YELLOW, "/ta eco <give|take|set> <towny> <amount>"));
			return CommandResult.success();
		}
		String townyName = ctx.<String>getOne("towny").get();
		BigDecimal amount = BigDecimal.valueOf(ctx.<Double>getOne("amount").get());
		String operation = ctx.<String>getOne("give|take|set").get();
		
		Towny towny = DataHandler.getTowny(townyName);
		if (towny == null)
		{
			src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_BADTOWNNNAME));
			return CommandResult.success();
		}
		if (TownyPlugin.getEcoService() == null)
		{
			src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOECO));
			return CommandResult.success();
		}
            Optional<Account> optAccount = TownyPlugin.getOrCreateAccount("towny-" + towny.getUUID().toString());
		if (!optAccount.isPresent())
		{
			src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_ECONOACCOUNT));
			return CommandResult.success();
		}
		TransactionResult result;
		if (operation.equalsIgnoreCase("give"))
		{
			result = optAccount.get().deposit(TownyPlugin.getEcoService().getDefaultCurrency(), amount, TownyPlugin.getCause());
		}
		else if (operation.equalsIgnoreCase("take"))
		{
			result = optAccount.get().withdraw(TownyPlugin.getEcoService().getDefaultCurrency(), amount, TownyPlugin.getCause());
		}
		else if (operation.equalsIgnoreCase("set"))
		{
			result = optAccount.get().setBalance(TownyPlugin.getEcoService().getDefaultCurrency(), amount, TownyPlugin.getCause());
		}
		else
		{
			src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_BADARG_GTS));
			return CommandResult.success();
		}
		if (result.getResult() != ResultType.SUCCESS)
		{
			src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_ECOTRANSACTION));
			return CommandResult.success();
		}
		src.sendMessage(Text.of(TextColors.GREEN, LanguageHandler.SUCCESS_GENERAL));
		return CommandResult.success();
	}
}
