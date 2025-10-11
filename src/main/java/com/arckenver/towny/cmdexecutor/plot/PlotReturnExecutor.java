package com.arckenver.towny.cmdexecutor.plot;

import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.TownyPlugin;
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

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@ParametersAreNonnullByDefault
public class PlotReturnExecutor implements CommandExecutor {

	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("towny.command.plot.rent") //if can rent, also can return or else would be extortion
				.arguments()
				.executor(new PlotReturnExecutor())
				.build(), "return", "release"); //release pun with lease and also has a meaning like this
	}

	@Nonnull
	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		if(src instanceof Player) {
			Player player = (Player) src;
			Towny towny = DataHandler.getTowny(player.getLocation());
			if (towny == null)
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NEEDSTANDTOWN));
				return CommandResult.success();
			}
			Plot plot = towny.getPlot(player.getLocation());
			if (plot == null)
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NEEDSTANDPLOTSELF));
				return CommandResult.success();
			}
			if (!plot.isOwner(player.getUniqueId()) && !towny.isStaff(player.getUniqueId()))
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOOWNER));
				return CommandResult.success();
			}
			if(!plot.isForRent()) {
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_ISBOUGHT));
				return CommandResult.success();
			}
			//give back money to owner
			if (TownyPlugin.getEcoService() == null)
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOECO));
				return CommandResult.success();
			}
			Optional<Account> optAccount = TownyPlugin.getEcoService().getOrCreateAccount("plot-" + plot.getUUID().toString());
			if (!optAccount.isPresent())
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_ECONOPLOT));
				return CommandResult.success();
			}
			Optional<UniqueAccount> receiver = TownyPlugin.getEcoService().getOrCreateAccount(player.getUniqueId());
			if (!receiver.isPresent()) {
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_ECONOACCOUNT));
				return CommandResult.success();
			}
			BigDecimal balance = optAccount.get().getBalance(TownyPlugin.getEcoService().getDefaultCurrency());
			TransactionResult result = optAccount.get().transfer(receiver.get(), TownyPlugin.getEcoService().getDefaultCurrency(), balance, TownyPlugin.getCause());
			if (result.getResult() != ResultType.SUCCESS)
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_ECOTRANSACTION));
				return CommandResult.success();
			}
			String oldName = plot.getDisplayName();
			//then make ownerless
			plot.resetCoowners();
			plot.setOwner(null);
			plot.setDisplayName(null);
			DataHandler.saveTowny(towny.getUUID());
			String str = LanguageHandler.INFO_RETURNRENT.replaceAll("\\{PLAYER\\}", player.getName()).replaceAll("\\{PLOT\\}", oldName);
			towny.getChannel().send(Text.of(TextColors.AQUA, str));
			if(!towny.getCitizens().contains(player.getUniqueId())){
				player.sendMessage(Text.of(TextColors.AQUA, str));
			}
		} else {
			src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOPLAYER));
		}
		return CommandResult.success();
	}
}
