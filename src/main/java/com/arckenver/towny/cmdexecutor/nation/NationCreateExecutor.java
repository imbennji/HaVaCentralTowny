package com.arckenver.towny.cmdexecutor.nation;

import com.arckenver.towny.ConfigHandler;
import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.Utils;
import com.arckenver.towny.TownyPlugin;
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
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public class NationCreateExecutor implements CommandExecutor {
    public static void create(CommandSpec.Builder cmd) {
        cmd.child(CommandSpec.builder()
                .description(Text.of(""))
                .permission("towny.command.nation.create")
                .arguments(GenericArguments.string(Text.of("name")))
                .executor(new NationCreateExecutor())
                .build(), "create", "new");
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException {
        if (!(src instanceof Player)) {
            src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOPLAYER));
            return CommandResult.success();
        }

        Player player = (Player) src;
        Towny town = DataHandler.getTownyOfPlayer(player.getUniqueId());
        if (town == null) {
            src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOTOWN));
            return CommandResult.success();
        }

        if (!town.isPresident(player.getUniqueId())) {
            src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_PERM_TOWNPRES));
            return CommandResult.success();
        }

        if (town.hasNation()) {
            src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_TOWN_HAS_NATION));
            return CommandResult.success();
        }

        String rawName = ctx.<String>getOne("name").orElse("");
        if (rawName.isEmpty()) {
            src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NEEDNATIONNAME));
            return CommandResult.success();
        }

        if (DataHandler.getNation(rawName) != null) {
            src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NATION_NAME_TAKEN));
            return CommandResult.success();
        }

        if (!rawName.matches("[\\p{Alnum}\\p{IsIdeographic}\\p{IsLetter}\"_\"]*")) {
            src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_BADTOWNNNAME));
            return CommandResult.success();
        }

        int minLength = ConfigHandler.getNode("others", "minNationNameLength").getInt(3);
        int maxLength = ConfigHandler.getNode("others", "maxNationNameLength").getInt(13);
        if (rawName.length() < minLength || rawName.length() > maxLength) {
            src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NATION_NAME_LENGTH
                    .replace("{MIN}", String.valueOf(minLength))
                    .replace("{MAX}", String.valueOf(maxLength))));
            return CommandResult.success();
        }

        if (TownyPlugin.getEcoService() == null) {
            src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOECO));
            return CommandResult.success();
        }

        Optional<UniqueAccount> optAccount = TownyPlugin.getOrCreateUniqueAccount(player.getUniqueId());
        if (!optAccount.isPresent()) {
            src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_ECONOACCOUNT));
            return CommandResult.success();
        }

        BigDecimal price = BigDecimal.valueOf(ConfigHandler.getNode("prices", "nationCreationPrice").getDouble());
        TransactionResult result = optAccount.get().withdraw(TownyPlugin.getEcoService().getDefaultCurrency(), price, TownyPlugin.getCause());
        if (result.getResult() == ResultType.ACCOUNT_NO_FUNDS) {
            src.sendMessage(Text.builder()
                    .append(Text.of(TextColors.RED, LanguageHandler.ERROR_NEEDMONEY.split("\\{AMOUNT\\}")[0]))
                    .append(Utils.formatPrice(TextColors.RED, price))
                    .append(Text.of(TextColors.RED, LanguageHandler.ERROR_NEEDMONEY.split("\\{AMOUNT\\}")[1]))
                    .build());
            return CommandResult.success();
        } else if (result.getResult() != ResultType.SUCCESS) {
            src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_ECOTRANSACTION));
            return CommandResult.success();
        }

        Nation nation = new Nation(UUID.randomUUID(), rawName);
        nation.setCapital(town.getUUID());
        nation.addTown(town.getUUID());
        nation.setOpen(ConfigHandler.getNode("nation", "flags", "open").getBoolean());
        nation.setNeutral(ConfigHandler.getNode("nation", "flags", "neutral").getBoolean());
        nation.setTaxes(ConfigHandler.getNode("nation", "defaultTaxes").getDouble());

        DataHandler.addNation(nation);
        town.setNationUUID(nation.getUUID());
        DataHandler.saveTowny(town.getUUID());

        src.sendMessage(Text.of(TextColors.GREEN, LanguageHandler.INFO_NATION_CREATED.replace("{NATION}", nation.getName())));
        return CommandResult.success();
    }
}
