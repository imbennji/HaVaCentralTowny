package com.arckenver.towny.cmdexecutor.nation;

import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.cmdelement.NationNameElement;
import com.arckenver.towny.object.Nation;
import com.arckenver.towny.object.Towny;
import com.google.common.collect.ImmutableMap;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

public class NationAllyExecutor implements CommandExecutor {
    public static void create(CommandSpec.Builder cmd) {
        cmd.child(CommandSpec.builder()
                .description(Text.of(""))
                .permission("towny.command.nation.ally")
                .arguments(
                        GenericArguments.optional(GenericArguments.choices(Text.of("action"),
                                ImmutableMap.of("add", "add", "remove", "remove"))),
                        GenericArguments.optional(new NationNameElement(Text.of("nation"))))
                .executor(new NationAllyExecutor())
                .build(), "ally", "allies");
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException {
        if (!(src instanceof Player)) {
            src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NOPLAYER));
            return CommandResult.success();
        }

        if (!ctx.<String>getOne("action").isPresent() || !ctx.<String>getOne("nation").isPresent()) {
            src.sendMessage(Text.of(LanguageHandler.colorYellow(), "/nation ally add <nation>\n/nation ally remove <nation>"));
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

        if (!nation.isStaff(player.getUniqueId())) {
            src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_PERM_NATIONSTAFF));
            return CommandResult.success();
        }

        Nation target = DataHandler.getNation(ctx.<String>getOne("nation").get());
        if (target == null) {
            src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NATION_NOT_FOUND));
            return CommandResult.success();
        }

        if (nation.getUUID().equals(target.getUUID())) {
            src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NATION_SELF_RELATION));
            return CommandResult.success();
        }

        String action = ctx.<String>getOne("action").get();
        if (action.equalsIgnoreCase("add")) {
            if (nation.getAllies().contains(target.getUUID())) {
                src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NATION_ALREADY_ALLY));
                return CommandResult.success();
            }

            nation.addAlly(target.getUUID());
            target.addAlly(nation.getUUID());
            nation.removeEnemy(target.getUUID());
            target.removeEnemy(nation.getUUID());

            DataHandler.saveNation(nation.getUUID());
            DataHandler.saveNation(target.getUUID());
            src.sendMessage(Text.of(LanguageHandler.colorGreen(), LanguageHandler.INFO_NATION_ALLY_ADDED));
        } else if (action.equalsIgnoreCase("remove")) {
            if (!nation.getAllies().contains(target.getUUID())) {
                src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NATION_NOT_ALLY));
                return CommandResult.success();
            }

            nation.removeAlly(target.getUUID());
            target.removeAlly(nation.getUUID());
            DataHandler.saveNation(nation.getUUID());
            DataHandler.saveNation(target.getUUID());
            src.sendMessage(Text.of(LanguageHandler.colorGreen(), LanguageHandler.INFO_NATION_ALLY_REMOVED));
        } else {
            src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_BADARG_AR));
        }

        return CommandResult.success();
    }
}
