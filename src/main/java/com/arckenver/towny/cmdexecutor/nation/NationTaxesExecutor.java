package com.arckenver.towny.cmdexecutor.nation;

import com.arckenver.towny.ConfigHandler;
import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
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
import org.spongepowered.api.text.Text;

public class NationTaxesExecutor implements CommandExecutor {
    public static void create(CommandSpec.Builder cmd) {
        cmd.child(CommandSpec.builder()
                .description(Text.of(""))
                .permission("towny.command.nation.taxes")
                .arguments(
                        GenericArguments.optional(GenericArguments.string(Text.of("mode"))),
                        GenericArguments.optional(GenericArguments.doubleNum(Text.of("amount"))))
                .executor(new NationTaxesExecutor())
                .build(), "taxes");
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException {
        if (!(src instanceof Player)) {
            src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NOPLAYER));
            return CommandResult.success();
        }

        Player player = (Player) src;
        Towny town = DataHandler.getTownyOfPlayer(player.getUniqueId());
        if (town == null || !town.hasNation()) {
            src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NONATION));
            return CommandResult.success();
        }

        Nation nation = DataHandler.getNation(town.getNationUUID());
        if (nation == null) {
            src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NATION_NOT_FOUND));
            return CommandResult.success();
        }

        if (!nation.isKing(player.getUniqueId())) {
            src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_PERM_NATIONLEADER));
            return CommandResult.success();
        }

        if (!ctx.<String>getOne("mode").isPresent()) {
            String mode = nation.isTaxPercentage() ? "percentage" : "flat";
            src.sendMessage(Text.of(LanguageHandler.colorYellow(),
                    "Nation taxes: " + nation.getTaxes() + " (" + mode + ")"));
            return CommandResult.success();
        }

        String mode = ctx.<String>getOne("mode").get();
        double max = ConfigHandler.getNode("nation", "maxTaxes").getDouble(1000D);

        switch (mode.toLowerCase()) {
            case "flat":
                if (!ctx.<Double>getOne("amount").isPresent()) {
                    src.sendMessage(Text.of(LanguageHandler.colorYellow(), "/nation taxes flat <amount>"));
                    return CommandResult.success();
                }
                double flat = ctx.<Double>getOne("amount").get();
                if (flat < 0 || flat > max) {
                    src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NATION_TAX_RANGE.replace("{MAX}", String.valueOf(max))));
                    return CommandResult.success();
                }
                nation.setTaxPercentage(false);
                nation.setTaxes(flat);
                DataHandler.saveNation(nation.getUUID());
                src.sendMessage(Text.of(LanguageHandler.colorGreen(), LanguageHandler.INFO_NATION_TAXES));
                break;
            case "percent":
                if (!ctx.<Double>getOne("amount").isPresent()) {
                    src.sendMessage(Text.of(LanguageHandler.colorYellow(), "/nation taxes percent <amount>"));
                    return CommandResult.success();
                }
                double percent = ctx.<Double>getOne("amount").get();
                if (percent < 0 || percent > max) {
                    src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NATION_TAX_RANGE.replace("{MAX}", String.valueOf(max))));
                    return CommandResult.success();
                }
                nation.setTaxPercentage(true);
                nation.setTaxes(percent);
                DataHandler.saveNation(nation.getUUID());
                src.sendMessage(Text.of(LanguageHandler.colorGreen(), LanguageHandler.INFO_NATION_TAXES));
                break;
            case "toggle":
                nation.setTaxPercentage(!nation.isTaxPercentage());
                DataHandler.saveNation(nation.getUUID());
                src.sendMessage(Text.of(LanguageHandler.colorGreen(), LanguageHandler.INFO_NATION_TAXES_PERCENT));
                break;
            default:
                src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_BADARG_AR));
                break;
        }

        return CommandResult.success();
    }
}
