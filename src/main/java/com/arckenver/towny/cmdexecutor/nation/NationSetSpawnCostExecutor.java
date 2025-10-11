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
import org.spongepowered.api.text.TextColors;

public class NationSetSpawnCostExecutor implements CommandExecutor {
    public static void create(CommandSpec.Builder cmd) {
        cmd.child(CommandSpec.builder()
                .description(Text.of(""))
                .permission("towny.command.nation.setspawncost")
                .arguments(GenericArguments.optional(GenericArguments.doubleNum(Text.of("cost"))))
                .executor(new NationSetSpawnCostExecutor())
                .build(), "setspawncost", "spawncost");
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException {
        if (!(src instanceof Player)) {
            src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOPLAYER));
            return CommandResult.success();
        }

        if (!ctx.<Double>getOne("cost").isPresent()) {
            src.sendMessage(Text.of(TextColors.YELLOW, "/nation setspawncost <amount>"));
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

        double cost = ctx.<Double>getOne("cost").get();
        if (cost < 0D) {
            src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_BADARG_P));
            return CommandResult.success();
        }

        double max = ConfigHandler.getNode("nation", "maxSpawnCost").getDouble(1000D);
        if (cost > max) {
            src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NATION_SPAWN_COST.replace("{MAX}", String.valueOf(max))));
            return CommandResult.success();
        }

        nation.setSpawnCost(cost);
        DataHandler.saveNation(nation.getUUID());

        src.sendMessage(Text.of(TextColors.GREEN, LanguageHandler.INFO_NATION_SPAWN_COST));
        return CommandResult.success();
    }
}
