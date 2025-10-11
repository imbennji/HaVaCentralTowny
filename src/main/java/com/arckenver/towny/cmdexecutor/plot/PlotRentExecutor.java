package com.arckenver.towny.cmdexecutor.plot;

import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.TownyPlugin;
import com.arckenver.towny.Utils;
import com.arckenver.towny.object.Towny;
import com.arckenver.towny.object.Plot;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public class PlotRentExecutor implements CommandExecutor {
	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("towny.command.plot.rent")
				.arguments()
				.executor(new PlotRentExecutor())
				.build(), "rent", "lease");
	}

	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException {
		if (src instanceof Player) {
			Player player = (Player) src;
			Towny towny = DataHandler.getTowny(player.getLocation());
			if (towny == null) {
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NEEDSTANDTOWN));
				return CommandResult.success();
			}
			Plot plot = towny.getPlot(player.getLocation());
			if (plot == null) {
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NEEDSTANDPLOT));
				return CommandResult.success();
			}
			Towny playerTowny = DataHandler.getTownyOfPlayer(player.getUniqueId());
			if (!towny.isAdmin() && !plot.getFlag("public") && (playerTowny == null || !towny.getUUID().equals(playerTowny.getUUID()))) {
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_PERM_PLOTRENT));
				return CommandResult.success();
			}
			if (!plot.isForRent() || plot.isOwned()) {
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_PLOTNFR));
				return CommandResult.success();
			}
			if (TownyPlugin.getEcoService() == null) {
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOECO));
				return CommandResult.success();
			}
			Optional<UniqueAccount> optAccount = TownyPlugin.getEcoService().getOrCreateAccount(player.getUniqueId());
			if (!optAccount.isPresent()) {
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_ECONOACCOUNT));
				return CommandResult.success();
			}
			Account receiver;
			Optional<Account> optReceiver = TownyPlugin.getEcoService().getOrCreateAccount("towny-" + towny.getUUID());
			if (!optReceiver.isPresent())
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_ECONOTOWN));
				return CommandResult.success();
			}
			receiver = optReceiver.get();
			BigDecimal price = plot.getRentalPrice();
			TransactionResult result = optAccount.get().transfer(receiver, TownyPlugin.getEcoService().getDefaultCurrency(), price, TownyPlugin.getCause());
			if (result.getResult() == ResultType.ACCOUNT_NO_FUNDS) {
				src.sendMessage(Text.builder()
						.append(Text.of(TextColors.RED, LanguageHandler.ERROR_NEEDMONEY.split("\\{AMOUNT\\}")[0]))
						.append(Utils.formatPrice(TextColors.YELLOW, price))
						.append(Text.of(TextColors.RED, LanguageHandler.ERROR_NEEDMONEY.split("\\{AMOUNT\\}")[1])).build());
				return CommandResult.success();
			} else if (result.getResult() != ResultType.SUCCESS) {
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_ECOTRANSACTION));
				return CommandResult.success();
			}
			plot.setOwner(player.getUniqueId());
			plot.setRentalPrice(plot.getRentalPrice());
			DataHandler.saveTowny(towny.getUUID());
			src.sendMessage(Text.of(TextColors.GREEN, LanguageHandler.SUCCESS_SETOWNER.replaceAll("\\{PLOT\\}", plot.getName())));
			String str = LanguageHandler.INFO_PLOTRENT.replaceAll("\\{PLAYER\\}", player.getName()).replaceAll("\\{PLOT\\}", plot.getDisplayName());
			towny.getChannel().send(Text.of(TextColors.AQUA, str));
		} else {
			src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOPLAYER));
		}
		return CommandResult.success();
	}
}
