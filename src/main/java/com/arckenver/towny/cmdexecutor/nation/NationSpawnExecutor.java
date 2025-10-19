package com.arckenver.towny.cmdexecutor.nation;

import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.TownyPlugin;
import com.arckenver.towny.cmdelement.NationNameElement;
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
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.math.BigDecimal;
import java.util.Optional;

public class NationSpawnExecutor implements CommandExecutor {
    public static void create(CommandSpec.Builder cmd) {
        cmd.child(CommandSpec.builder()
                .description(Text.of(""))
                .permission("towny.command.nation.spawn")
                .arguments(GenericArguments.optional(new NationNameElement(Text.of("nation"))))
                .executor(new NationSpawnExecutor())
                .build(), "spawn", "home");
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException {
        if (!(src instanceof Player)) {
            src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NOPLAYER));
            return CommandResult.success();
        }

        Player player = (Player) src;
        Towny playerTown = DataHandler.getTownyOfPlayer(player.getUniqueId());
        Nation targetNation;

        if (ctx.<String>getOne("nation").isPresent()) {
            targetNation = DataHandler.getNation(ctx.<String>getOne("nation").get());
            if (targetNation == null) {
                src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NATION_NOT_FOUND));
                return CommandResult.success();
            }
        } else {
            if (playerTown == null || !playerTown.hasNation()) {
                src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NONATION));
                return CommandResult.success();
            }
            targetNation = DataHandler.getNation(playerTown.getNationUUID());
            if (targetNation == null) {
                src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NATION_NOT_FOUND));
                return CommandResult.success();
            }
        }

        Location<World> spawn = targetNation.getSpawn();
        if (spawn == null) {
            src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NATION_NO_SPAWN));
            return CommandResult.success();
        }

        boolean isMember = playerTown != null && playerTown.hasNation()
                && targetNation.getUUID().equals(playerTown.getNationUUID());

        if (!isMember && !targetNation.isPublic()) {
            src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NATION_SPAWN_PRIVATE));
            return CommandResult.success();
        }

        double costValue = (!isMember) ? targetNation.getSpawnCost() : 0D;

        if (costValue > 0D) {
            if (TownyPlugin.getEcoService() == null) {
                src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NOECO));
                return CommandResult.success();
            }

            Optional<UniqueAccount> optAccount = TownyPlugin.getOrCreateUniqueAccount(player.getUniqueId());
            if (!optAccount.isPresent()) {
                src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_ECONOACCOUNT));
                return CommandResult.success();
            }

            BigDecimal cost = BigDecimal.valueOf(costValue);
            TransactionResult result = optAccount.get().withdraw(TownyPlugin.getEcoService().getDefaultCurrency(), cost,
                    TownyPlugin.getCause());
            if (result.getResult() == ResultType.ACCOUNT_NO_FUNDS) {
                src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NATION_BANK_PLAYER_FUNDS));
                return CommandResult.success();
            } else if (result.getResult() != ResultType.SUCCESS) {
                src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_ECOTRANSACTION));
                return CommandResult.success();
            }

            Optional<Account> optNationAccount = TownyPlugin.getOrCreateAccount("nation-" + targetNation.getUUID());
            optNationAccount.ifPresent(account ->
                    account.deposit(TownyPlugin.getEcoService().getDefaultCurrency(), cost, TownyPlugin.getCause()));
        }

        player.setLocation(spawn);
        src.sendMessage(Text.of(LanguageHandler.colorGreen(), LanguageHandler.INFO_NATION_SPAWN_TRAVEL));
        return CommandResult.success();
    }
}
