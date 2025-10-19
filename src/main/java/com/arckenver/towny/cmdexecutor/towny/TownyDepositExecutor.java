package com.arckenver.towny.cmdexecutor.towny;

import java.math.BigDecimal;
import java.util.Optional;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Text.Builder;

import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.TownyPlugin;
import com.arckenver.towny.Utils;
import com.arckenver.towny.object.Towny;

public class TownyDepositExecutor implements CommandExecutor
{
	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("towny.command.town.deposit")
				.arguments(GenericArguments.optional(GenericArguments.doubleNum(Text.of("amount"))))
				.executor(new TownyDepositExecutor())
				.build(), "deposit", "give");
	}

	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException
	{
		if (src instanceof Player)
		{
			if (!ctx.<Double>getOne("amount").isPresent())
			{
				src.sendMessage(Text.of(LanguageHandler.colorYellow(), "/t deposit <amount>\n/t withdraw <amount>"));
				return CommandResult.success();
			}
			
			Player player = (Player) src;
			Towny towny = DataHandler.getTownyOfPlayer(player.getUniqueId());
			if (towny == null)
			{
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NOTOWN));
				return CommandResult.success();
			}
			
			if (TownyPlugin.getEcoService() == null)
			{
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NOECO));
				return CommandResult.success();
			}
                    Optional<UniqueAccount> optAccount = TownyPlugin.getOrCreateUniqueAccount(player.getUniqueId());
			if (!optAccount.isPresent())
			{
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_ECONOACCOUNT));
				return CommandResult.success();
			}
                    Optional<Account> optTownyAccount = TownyPlugin.getOrCreateAccount("towny-" + towny.getUUID().toString());
			if (!optTownyAccount.isPresent())
			{
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_ECONOTOWN));
				return CommandResult.success();
			}
			BigDecimal amount = BigDecimal.valueOf(ctx.<Double>getOne("amount").get());
			TransactionResult result = optAccount.get().transfer(optTownyAccount.get(), TownyPlugin.getEcoService().getDefaultCurrency(), amount, TownyPlugin.getCause());
			if (result.getResult() == ResultType.ACCOUNT_NO_FUNDS)
			{
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NOENOUGHMONEY));
				return CommandResult.success();
			}
			else if (result.getResult() != ResultType.SUCCESS)
			{
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_ECOTRANSACTION));
				return CommandResult.success();
			}
			
			String[] s1 = LanguageHandler.SUCCESS_DEPOSIT.split("\\{AMOUNT\\}");
			Builder builder = Text.builder();
			if (s1[0].indexOf("{BALANCE}") >= 0)
			{
				String[] splited0 = s1[0].split("\\{BALANCE\\}");
				builder
				.append(Text.of(LanguageHandler.colorGreen(), (splited0.length > 0) ? splited0[0] : ""))
				.append(Utils.formatPrice(LanguageHandler.colorGreen(), optTownyAccount.get().getBalance(TownyPlugin.getEcoService().getDefaultCurrency())))
				.append(Text.of(LanguageHandler.colorGreen(), (splited0.length > 1) ? splited0[1] : ""));
			}
			else
			{
				builder.append(Text.of(LanguageHandler.colorGreen(), s1[0]));
			}
			builder.append(Utils.formatPrice(LanguageHandler.colorGreen(), amount));
			if (s1[1].indexOf("{BALANCE}") >= 0)
			{
				String[] splited1 = s1[1].split("\\{BALANCE\\}");
				builder
				.append(Text.of(LanguageHandler.colorGreen(), (splited1.length > 0) ? splited1[0] : ""))
				.append(Utils.formatPrice(LanguageHandler.colorGreen(), optTownyAccount.get().getBalance(TownyPlugin.getEcoService().getDefaultCurrency())))
				.append(Text.of(LanguageHandler.colorGreen(), (splited1.length > 1) ? splited1[1] : ""));
			}
			else
			{
				builder.append(Text.of(LanguageHandler.colorGreen(), s1[1]));
			}
			src.sendMessage(builder.build());
		}
		else
		{
			src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NOPLAYER));
		}
		return CommandResult.success();
	}
}
