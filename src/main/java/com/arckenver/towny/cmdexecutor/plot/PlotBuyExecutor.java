package com.arckenver.towny.cmdexecutor.plot;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

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

import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.TownyPlugin;
import com.arckenver.towny.Utils;
import com.arckenver.towny.object.Towny;
import com.arckenver.towny.object.Plot;

public class PlotBuyExecutor implements CommandExecutor
{
	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("towny.command.plot.buy")
				.arguments()
				.executor(new PlotBuyExecutor())
				.build(), "buy", "claim");
	}

	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException
	{
		if (src instanceof Player)
		{
			Player player = (Player) src;
			Towny towny = DataHandler.getTowny(player.getLocation());
			if (towny == null)
			{
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NEEDSTANDTOWN));
				return CommandResult.success();
			}
			Plot plot = towny.getPlot(player.getLocation());
			if (plot == null)
			{
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NEEDSTANDPLOT));
				return CommandResult.success();
			}
			Towny playerTowny = DataHandler.getTownyOfPlayer(player.getUniqueId());
                        boolean sameTown = playerTowny != null && towny.getUUID().equals(playerTowny.getUUID());
                        boolean foreignAllowed = plot.getType().allowsForeignOwnership();
                        if (!towny.isAdmin() && !plot.getFlag("public") && !sameTown && !foreignAllowed)
                        {
                                src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_PERM_PLOTBUY));
                                return CommandResult.success();
                        }
                        if (!towny.isAdmin() && !sameTown && !foreignAllowed)
                        {
                                src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_PLOT_FOREIGN_OWNERSHIP));
                                return CommandResult.success();
                        }
			if (!plot.isForSale())
			{
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_PLOTNFS));
				return CommandResult.success();
			}
			UUID oldOwner = plot.getOwner();
			
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
			Account receiver;
			if (oldOwner == null)
			{
                            Optional<Account> optReceiver = TownyPlugin.getOrCreateAccount("towny-" + towny.getUUID());
				if (!optReceiver.isPresent())
				{
					src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_ECONOTOWN));
					return CommandResult.success();
				}
				receiver = optReceiver.get();
			}
			else
			{
                            Optional<UniqueAccount> optReceiver = TownyPlugin.getOrCreateUniqueAccount(oldOwner);
				if (!optReceiver.isPresent())
				{
					src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_ECONOOWNER));
					return CommandResult.success();
				}
				receiver = optReceiver.get();
			}
			BigDecimal price = plot.getPrice();
			TransactionResult result = optAccount.get().transfer(receiver, TownyPlugin.getEcoService().getDefaultCurrency(), price, TownyPlugin.getCause());
			if (result.getResult() == ResultType.ACCOUNT_NO_FUNDS)
			{
				src.sendMessage(Text.builder()
						.append(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NEEDMONEY.split("\\{AMOUNT\\}")[0]))
						.append(Utils.formatPrice(LanguageHandler.colorRed(), price))
						.append(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NEEDMONEY.split("\\{AMOUNT\\}")[1])).build());
				return CommandResult.success();
			}
			else if (result.getResult() != ResultType.SUCCESS)
			{
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_ECOTRANSACTION));
				return CommandResult.success();
			}
			plot.setOwner(player.getUniqueId());
			plot.setPrice(null);
			DataHandler.saveTowny(towny.getUUID());
			src.sendMessage(Text.of(LanguageHandler.colorGreen(), LanguageHandler.SUCCESS_SETOWNER.replaceAll("\\{PLOT\\}", plot.getName())));
			if (oldOwner != null)
			{
				Sponge.getServer().getPlayer(oldOwner).ifPresent(
						p -> {
							String str = LanguageHandler.INFO_PLOTBUY.replaceAll("\\{PLAYER\\}",  player.getName()).replaceAll("\\{PLOT\\}", plot.getName());
							String[] splited = str.split("\\{AMOUNT\\}");
							src.sendMessage(Text.builder()
									.append(Text.of(LanguageHandler.colorAqua(), (splited.length > 0) ? splited[0] : ""))
									.append(Utils.formatPrice(LanguageHandler.colorAqua(), price))
									.append(Text.of(LanguageHandler.colorAqua(), (splited.length > 1) ? splited[1] : "")).build());
						});
			}
		}
		else
		{
			src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NOPLAYER));
		}
		return CommandResult.success();
	}
}
