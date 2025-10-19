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

public class NationSetSpawnExecutor implements CommandExecutor {
    public static void create(CommandSpec.Builder cmd) {
        cmd.child(CommandSpec.builder()
                .description(Text.of(""))
                .permission("towny.command.nation.setspawn")
                .executor(new NationSetSpawnExecutor())
                .build(), "setspawn");
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

        boolean staff = nation.isKing(player.getUniqueId()) || nation.isAssistant(player.getUniqueId())
                || (nation.isCapital(town.getUUID()) && town.isPresident(player.getUniqueId()));

        if (!staff) {
            src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_PERM_NATIONSTAFF));
            return CommandResult.success();
        }

        nation.setSpawn(player.getLocation());
        DataHandler.saveNation(nation.getUUID());

        src.sendMessage(Text.of(LanguageHandler.colorGreen(), LanguageHandler.INFO_NATION_SPAWN_SET));
        return CommandResult.success();
    }
}
