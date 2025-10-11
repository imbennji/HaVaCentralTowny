package com.arckenver.towny.cmdexecutor.nation;

import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.TownyPlugin;
import com.arckenver.towny.Utils;
import com.arckenver.towny.object.Nation;
import com.arckenver.towny.object.Towny;
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
import org.spongepowered.api.text.format.TextColors;

import java.math.BigDecimal;
import java.util.Optional;

public class NationWithdrawExecutor implements CommandExecutor {
    public static void create(CommandSpec.Builder cmd) {
        cmd.child(CommandSpec.builder()
                .description(Text.of(""))
                .permission("towny.command.nation.withdraw")
                .arguments(GenericArguments.optional(GenericArguments.doubleNum(Text.of("amount"))))
                .executor(new NationWithdrawExecutor())
                .build(), "withdraw");
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException {
        if (!(src instanceof Player)) {
            src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOPLAYER));
            return CommandResult.success();
        }

        if (!ctx.<Double>getOne("amount").isPresent()) {
            src.sendMessage(Text.of(TextColors.YELLOW, "/nation withdraw <amount>"));
            return CommandResult.success();
        }

        Player player = (Player) src;
        Towny town = DataHandler.getTownyOfPlayer(player.getUniqueId());
        if (town == null || !town.hasNation()) {
            src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NONATION));
            return CommandResult.success();
        }

        Nation nation = DataHandler.getNation(town.getNationUUID());
        if (nation == null) {
            src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NATION_NOT_FOUND));
            return CommandResult.success();
        }

        if (!(nation.isKing(player.getUniqueId()) || nation.isAssistant(player.getUniqueId()))) {
            src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_PERM_NATIONSTAFF));
            return CommandResult.success();
        }

        if (TownyPlugin.getEcoService() == null) {
            src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOECO));
            return CommandResult.success();
        }

        Optional<Account> optNationAccount = TownyPlugin.getEcoService().getOrCreateAccount("nation-" + nation.getUUID());
        if (!optNationAccount.isPresent()) {
            src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NATION_BANK));
            return CommandResult.success();
        }

        Optional<UniqueAccount> optPlayerAccount = TownyPlugin.getEcoService().getOrCreateAccount(player.getUniqueId());
        if (!optPlayerAccount.isPresent()) {
            src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_ECONOACCOUNT));
            return CommandResult.success();
        }

        BigDecimal amount = BigDecimal.valueOf(ctx.<Double>getOne("amount").get());
        if (amount.doubleValue() <= 0) {
            src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_BADARG_P));
            return CommandResult.success();
        }

        TransactionResult result = optNationAccount.get().transfer(optPlayerAccount.get(),
                TownyPlugin.getEcoService().getDefaultCurrency(), amount, TownyPlugin.getCause());

        if (result.getResult() == ResultType.ACCOUNT_NO_FUNDS) {
            src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NATION_BANK_FUNDS));
            return CommandResult.success();
        } else if (result.getResult() != ResultType.SUCCESS) {
            src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_ECOTRANSACTION));
            return CommandResult.success();
        }

        String[] parts = LanguageHandler.INFO_NATION_WITHDRAW.split("\\{AMOUNT\\}");
        Text message = Text.builder()
                .append(Text.of(TextColors.GREEN, parts.length > 0 ? parts[0] : ""))
                .append(Utils.formatPrice(TextColors.GREEN, amount))
                .append(Text.of(TextColors.GREEN, parts.length > 1 ? parts[1] : ""))
                .build();
        src.sendMessage(message);
        return CommandResult.success();
    }
}
