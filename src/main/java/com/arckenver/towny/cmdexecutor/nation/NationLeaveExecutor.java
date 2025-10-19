package com.arckenver.towny.cmdexecutor.nation;

import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.object.Nation;
import com.arckenver.towny.object.Towny;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

public class NationLeaveExecutor implements CommandExecutor {
    public static void create(CommandSpec.Builder cmd) {
        cmd.child(CommandSpec.builder()
                .description(Text.of(""))
                .permission("towny.command.nation.leave")
                .arguments()
                .executor(new NationLeaveExecutor())
                .build(), "leave");
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
            src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_TOWN_NO_NATION));
            return CommandResult.success();
        }

        if (!town.isPresident(player.getUniqueId())) {
            src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_PERM_TOWNPRES));
            return CommandResult.success();
        }

        Nation nation = DataHandler.getNation(town.getNationUUID());
        if (nation == null) {
            town.clearNation();
            DataHandler.saveTowny(town.getUUID());
            src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NATION_NOT_FOUND));
            return CommandResult.success();
        }

        if (nation.isCapital(town.getUUID())) {
            if (nation.getTowns().size() > 1) {
                src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NATION_CAPITAL_REQUIRED));
                return CommandResult.success();
            }
            DataHandler.removeNation(nation.getUUID());
            town.clearNation();
            DataHandler.saveTowny(town.getUUID());
            src.sendMessage(Text.of(LanguageHandler.colorYellow(), LanguageHandler.INFO_NATION_DISBANDED.replace("{NATION}", nation.getName())));
            return CommandResult.success();
        }

        nation.removeTown(town.getUUID());
        DataHandler.saveNation(nation.getUUID());
        town.clearNation();
        DataHandler.saveTowny(town.getUUID());
        src.sendMessage(Text.of(LanguageHandler.colorGreen(), LanguageHandler.INFO_NATION_LEFT.replace("{TOWN}", town.getName())));
        return CommandResult.success();
    }
}
