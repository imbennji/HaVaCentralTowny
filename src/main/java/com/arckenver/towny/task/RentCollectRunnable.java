package com.arckenver.towny.task;

import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.TownyPlugin;
import com.arckenver.towny.Utils;
import com.arckenver.towny.object.Towny;
import com.arckenver.towny.object.Plot;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.format.TextColors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

public class RentCollectRunnable implements Runnable {

	@Override
	public void run() {

		if (TownyPlugin.getEcoService() == null)
		{
			TownyPlugin.getLogger().error(LanguageHandler.ERROR_NOECO);
			return;
		}

		EventContext context = EventContext.builder()
				.build();

		Cause cause = Cause.builder()
				.append(Sponge.getServer().getConsole())
				.append(TownyPlugin.getInstance())
				.build(context);

		Text rentTimeMessage = Text.of(TextColors.AQUA, LanguageHandler.INFO_RENTTIME);

			MessageChannel.TO_CONSOLE.send(Text.of(TextColors.AQUA, LanguageHandler.INFO_RENTTIME));
		if (TownyPlugin.getEcoService() == null) {
			MessageChannel.TO_CONSOLE.send(Text.of(TextColors.RED, LanguageHandler.ERROR_NOECO));
			return;
		}

		LocalDateTime nowHour = LocalDateTime.of(LocalDate.now(),LocalTime.of(LocalTime.now().getHour(), 0));

		for (Towny towny : DataHandler.getTowny().values()) {

			LocalDateTime targetHour = towny.getLastRentCollectTime().plusHours(towny.getRentInterval());
			if(nowHour.isAfter(targetHour) || nowHour.isEqual(targetHour)) {

				if (towny.getPresident() != null) {
					Sponge.getServer().getPlayer(towny.getPresident()).ifPresent(p -> {
						p.sendMessage(rentTimeMessage);
					});
				}

				Optional<Account> townyAccount = TownyPlugin.getEcoService().getOrCreateAccount("towny-" + towny.getUUID());
				if (!townyAccount.isPresent()) {
					MessageChannel.TO_CONSOLE.send(Text.of(TextColors.RED, LanguageHandler.ERROR_ECONOTOWN));
					continue;
				}

				for (Plot plot : towny.getPlots().values()) {
					if (!plot.isForRent()) continue;
					if (!plot.isOwned()) continue;
					Optional<Player> owner = Sponge.getServer().getPlayer(plot.getOwner());
					if (!owner.isPresent())
						continue; //probably something went wrong
					owner.get().sendMessage(rentTimeMessage);
					BigDecimal rentPrice = plot.getRentalPrice();

					BigDecimal plotBalance = BigDecimal.ZERO;
					Optional<Account> plotAccount = TownyPlugin.getEcoService().getOrCreateAccount("plot-" + plot.getUUID());
					if (plotAccount.isPresent()) {
						plotBalance = plotAccount.get().getBalance(TownyPlugin.getEcoService().getDefaultCurrency());
					} else {
						MessageChannel.TO_CONSOLE.send(Text.of(TextColors.RED, LanguageHandler.ERROR_ECONOPLOT));
						continue;
					}

					Optional<UniqueAccount> ownerAccount = TownyPlugin.getEcoService().getOrCreateAccount(plot.getOwner());
					BigDecimal ownerBalance = BigDecimal.ZERO;
					if (ownerAccount.isPresent()) {
						ownerBalance = ownerAccount.get().getBalance(TownyPlugin.getEcoService().getDefaultCurrency());
					} else {
						MessageChannel.TO_CONSOLE.send(Text.of(TextColors.RED, LanguageHandler.ERROR_ECONOACCOUNT));
						continue;
					}
					if (plotBalance.compareTo(rentPrice) >= 0) { //y didn't java devs just make actual operators for big decimals
						TransactionResult result = plotAccount.get().transfer(townyAccount.get(), TownyPlugin.getEcoService().getDefaultCurrency(), rentPrice, cause);
						if (result.getResult() != ResultType.SUCCESS) {
							MessageChannel.TO_CONSOLE.send(Text.of(TextColors.RED, LanguageHandler.ERROR_ECOTRANSACTION));
						} else {
							String str = LanguageHandler.INFO_PAYRENTPLOTBALANCE.replaceAll("\\{PLOT\\}", plot.getDisplayName());
							String split[] = str.split("\\{VALUE\\}");
							owner.get().sendMessage(Text.of(TextColors.AQUA, split[0], Utils.formatPrice(TextColors.YELLOW, rentPrice),split[1]));
						}
					} else if (plotBalance.add(ownerBalance).compareTo(rentPrice) >= 0) {
						TransactionResult result = plotAccount.get().transfer(townyAccount.get(), TownyPlugin.getEcoService().getDefaultCurrency(), plotBalance, cause);
						if (result.getResult() != ResultType.SUCCESS) {
							MessageChannel.TO_CONSOLE.send(Text.of(TextColors.RED, LanguageHandler.ERROR_ECOTRANSACTION));
						} else if(plotBalance.compareTo(BigDecimal.ZERO) > 0){ //only shows if it really paid with it, so it won't flood chat
							String str = LanguageHandler.INFO_PAYRENTPLOTBALANCE.replaceAll("\\{PLOT\\}", plot.getDisplayName());
							String split[] = str.split("\\{VALUE\\}");
							owner.get().sendMessage(Text.of(TextColors.AQUA, split[0], Utils.formatPrice(TextColors.YELLOW, plotBalance),split[1]));
						}
						result = ownerAccount.get().transfer(townyAccount.get(), TownyPlugin.getEcoService().getDefaultCurrency(), rentPrice.subtract(plotBalance), cause);
						if (result.getResult() != ResultType.SUCCESS) {
							MessageChannel.TO_CONSOLE.send(Text.of(TextColors.RED, LanguageHandler.ERROR_ECOTRANSACTION));
						} else {
							String str = LanguageHandler.INFO_PAYRENTPLOTPLAYER.replaceAll("\\{PLOT\\}", plot.getDisplayName());
							String split[] = str.split("\\{VALUE\\}");
							owner.get().sendMessage(Text.of(TextColors.AQUA, split[0], Utils.formatPrice(TextColors.YELLOW, rentPrice.subtract(plotBalance)),split[1]));
						}
					} else { //return plot: make plot bal go to owner and make ownerless
						TransactionResult result = plotAccount.get().transfer(ownerAccount.get(), TownyPlugin.getEcoService().getDefaultCurrency(), plotBalance, cause);
						if (result.getResult() != ResultType.SUCCESS) {
							MessageChannel.TO_CONSOLE.send(Text.of(TextColors.RED, LanguageHandler.ERROR_ECOTRANSACTION));
						}
						String oldName = plot.getDisplayName();
						plot.resetCoowners();
						plot.setOwner(null);
						plot.setDisplayName(null);
						owner.get().sendMessage(Text.of(TextColors.RED, LanguageHandler.INFO_FAILEDRENT.replaceAll("\\{PLOT\\}}", oldName)));
						for (UUID player : towny.getCitizens()) {
							if (player != owner.get().getUniqueId()) {
								Sponge.getServer().getPlayer(player).ifPresent(p -> {
									p.sendMessage(Text.of(TextColors.AQUA, LanguageHandler.INFO_RETURNRENT
											.replaceAll("\\{PLAYER\\}", owner.get().getName())
											.replaceAll("\\{PLOT\\}", oldName)));
								});
							}
						}
					}
				}
				towny.setLastRentCollectTime(nowHour);
			}
		}
		//saves everything after operation
		DataHandler.save();
	}
}
