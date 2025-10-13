package com.arckenver.towny.cmdexecutor.nation;

import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.cmdelement.TownyNameElement;
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
import org.spongepowered.api.text.format.TextColors;

public class NationKickExecutor implements CommandExecutor {
    public static void create(CommandSpec.Builder cmd) {
        cmd.child(CommandSpec.builder()
                .description(Text.of(""))
                .permission("towny.command.nation.kick")
                .arguments(new TownyNameElement(Text.of("town")))
                .executor(new NationKickExecutor())
                .build(), "kick", "remove");
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException {
        if (!(src instanceof Player)) {
            src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOPLAYER));
            return CommandResult.success();
        }

        Player player = (Player) src;
        Towny playerTown = DataHandler.getTownyOfPlayer(player.getUniqueId());
        if (playerTown == null || !playerTown.hasNation()) {
            src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NONATION));
            return CommandResult.success();
        }

        Nation nation = DataHandler.getNation(playerTown.getNationUUID());
        if (nation == null) {
            src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NATION_NOT_FOUND));
            return CommandResult.success();
        }

        if (!nation.isStaff(player.getUniqueId())) {
            src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_PERM_NATIONSTAFF));
            return CommandResult.success();
        }

        String townName = ctx.<String>getOne("town").orElse("");
        Towny targetTown = DataHandler.getTowny(townName);
        if (targetTown == null || !targetTown.hasNation() || !nation.getUUID().equals(targetTown.getNationUUID())) {
            src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_TOWN_NO_NATION));
            return CommandResult.success();
        }

        if (nation.isCapital(targetTown.getUUID())) {
            src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NATION_CAPITAL_REQUIRED));
            return CommandResult.success();
        }

        nation.removeTown(targetTown.getUUID());
        DataHandler.saveNation(nation.getUUID());
        targetTown.clearNation();
        DataHandler.saveTowny(targetTown.getUUID());

        src.sendMessage(Text.of(TextColors.GREEN, LanguageHandler.INFO_NATION_LEFT.replace("{TOWN}", targetTown.getName())));
        return CommandResult.success();
    }
}
