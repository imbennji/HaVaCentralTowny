package com.arckenver.towny.cmdexecutor.nation;

import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
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
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;
import java.util.UUID;

public class NationAssistantExecutor implements CommandExecutor {
    public static void create(CommandSpec.Builder cmd) {
        cmd.child(CommandSpec.builder()
                .description(Text.of(""))
                .permission("towny.command.nation.assistant")
                .arguments(
                        GenericArguments.optional(GenericArguments.choices(Text.of("action"),
                                ImmutableMap.of("add", "add", "remove", "remove"))),
                        GenericArguments.optional(GenericArguments.string(Text.of("player"))))
                .executor(new NationAssistantExecutor())
                .build(), "assistant", "assistants");
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException {
        if (!(src instanceof Player)) {
            src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOPLAYER));
            return CommandResult.success();
        }

        if (!ctx.<String>getOne("action").isPresent() || !ctx.<String>getOne("player").isPresent()) {
            src.sendMessage(Text.of(TextColors.YELLOW, "/nation assistant add <player>\n/nation assistant remove <player>"));
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

        if (!nation.isKing(player.getUniqueId())) {
            src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_PERM_NATIONLEADER));
            return CommandResult.success();
        }

        String action = ctx.<String>getOne("action").get();
        String targetName = ctx.<String>getOne("player").get();
        UUID targetId = DataHandler.getPlayerUUID(targetName);
        if (targetId == null) {
            src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_BADPLAYERNAME));
            return CommandResult.success();
        }

        Optional<UUID> targetTownId = DataHandler.getResidentTownId(targetId);
        if (!targetTownId.isPresent()) {
            src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_PLAYERNOTPARTOFTOWN));
            return CommandResult.success();
        }

        Towny targetTown = DataHandler.getTowny(targetTownId.get());
        if (targetTown == null || !targetTown.hasNation() || !nation.getUUID().equals(targetTown.getNationUUID())) {
            src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NATION_NO_TARGET_PLAYER));
            return CommandResult.success();
        }

        if (action.equalsIgnoreCase("add")) {
            if (nation.isAssistant(targetId) || nation.isKing(targetId)) {
                src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_PERM_NATIONSTAFF));
                return CommandResult.success();
            }
            nation.addAssistant(targetId);
            DataHandler.saveNation(nation.getUUID());
            src.sendMessage(Text.of(TextColors.GREEN, LanguageHandler.INFO_NATION_ASSISTANT_ADDED));
        } else if (action.equalsIgnoreCase("remove")) {
            if (!nation.isAssistant(targetId)) {
                src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NATION_NO_TARGET_PLAYER));
                return CommandResult.success();
            }
            nation.removeAssistant(targetId);
            DataHandler.saveNation(nation.getUUID());
            src.sendMessage(Text.of(TextColors.GREEN, LanguageHandler.INFO_NATION_ASSISTANT_REMOVED));
        } else {
            src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_BADARG_AR));
        }

        return CommandResult.success();
    }
}
