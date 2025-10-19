package com.arckenver.towny.cmdexecutor.plot;

import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.TownyPlugin;
import com.arckenver.towny.Utils;
import com.arckenver.towny.object.Towny;
import com.arckenver.towny.object.Plot;
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

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.math.BigDecimal;
import java.util.Optional;

@ParametersAreNonnullByDefault
public class PlotWithdrawExecutor implements CommandExecutor {

	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("towny.command.plot.deposit") //if can deposit also can withdraw
				.arguments(GenericArguments.optional(GenericArguments.doubleNum(Text.of("amount"))))
				.executor(new PlotWithdrawExecutor())
				.build(), "withdraw");
	}

	@Nonnull
	@Override
	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException {
		if (src instanceof Player) {
			if (!ctx.<Double>getOne("amount").isPresent()) {
				src.sendMessage(Text.of(LanguageHandler.colorYellow(), "/z deposit <amount>"));
				return CommandResult.success();
			}
			Player player = (Player) src;
			BigDecimal amount = BigDecimal.valueOf(ctx.<Double>getOne("amount").get());
			if (amount.compareTo(BigDecimal.ZERO) <= 0) {
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_BADARG_P));
				return CommandResult.success();
			}
			Towny towny = DataHandler.getTowny(player.getLocation());
			if (towny == null) {
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NEEDSTANDTOWN));
				return CommandResult.success();
			}
			Plot currentPlot = towny.getPlot(player.getLocation());
			if (currentPlot == null) {
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NEEDSTANDPLOTSELF));
				return CommandResult.success();
			}//if plot owner, co owner or rather towny staff
			if (!currentPlot.isCoowner(player.getUniqueId()) && !currentPlot.isOwner(player.getUniqueId())) {
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NOOWNER));
				return CommandResult.success();
			}
			if (!currentPlot.isForRent()) {
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NOTRENTING));
				return CommandResult.success();
			}

			if (TownyPlugin.getEcoService() == null) {
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NOECO));
				return CommandResult.success();
			}
                    Optional<Account> plotAccount = TownyPlugin.getOrCreateAccount("plot-" + currentPlot.getUUID());
			if (!plotAccount.isPresent()) {
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_ECONOPLOT));
				return CommandResult.success();
			}
                    Optional<UniqueAccount> ownerAccount = TownyPlugin.getOrCreateUniqueAccount(player.getUniqueId());
			if (!ownerAccount.isPresent()) {
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_ECONOACCOUNT));
				return CommandResult.success();
			}
			TransactionResult result = plotAccount.get().transfer(ownerAccount.get(), TownyPlugin.getEcoService().getDefaultCurrency(), amount, TownyPlugin.getCause());
			if (result.getResult() == ResultType.ACCOUNT_NO_FUNDS) {
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NOENOUGHMONEY));
				return CommandResult.success();
			} else if (result.getResult() != ResultType.SUCCESS) {
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_ECOTRANSACTION));
				return CommandResult.success();
			} else {
				String[] s1 = LanguageHandler.SUCCESS_DEPOSIT_PLOT.split("\\{AMOUNT\\}");
				Text.Builder builder = Text.builder();
				if (s1[0].contains("{BALANCE}")) {
					String[] split = s1[0].split("\\{BALANCE\\}");
					builder.append(Text.of(LanguageHandler.colorGreen(), (split.length > 0) ? split[0] : ""))
							.append(Utils.formatPrice(LanguageHandler.colorYellow(), plotAccount.get().getBalance(TownyPlugin.getEcoService().getDefaultCurrency())))
							.append(Text.of(LanguageHandler.colorGreen(), (split.length > 1) ? split[1] : ""));
				} else {
					builder.append(Text.of(LanguageHandler.colorGreen(), s1[0]));
				}
				builder.append(Utils.formatPrice(LanguageHandler.colorYellow(), amount));
				if (s1[1].contains("{BALANCE}")) {
					String[] split = s1[1].split("\\{BALANCE\\}");
					builder.append(Text.of(LanguageHandler.colorGreen(), (split.length > 0) ? split[0] : ""))
							.append(Utils.formatPrice(LanguageHandler.colorYellow(), plotAccount.get().getBalance(TownyPlugin.getEcoService().getDefaultCurrency())))
							.append(Text.of(LanguageHandler.colorGreen(), (split.length > 1) ? split[1] : ""));
				} else {
					builder.append(Text.of(LanguageHandler.colorGreen(), s1[1]));
				}
				src.sendMessage(builder.build());
				return CommandResult.success();
			}
		} else {
			src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NOPLAYER));
		}
		return CommandResult.success();
	}
}
