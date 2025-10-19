package com.arckenver.towny.cmdexecutor.towny;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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

import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.object.Towny;
import com.google.common.collect.ImmutableMap;

public class TownyOutlawExecutor implements CommandExecutor {
        private static final Text USAGE = Text.of(TextColors.YELLOW,
                        "/t outlaw add <player>\n",
                        "/t outlaw remove <player>\n",
                        "/t outlaw list");

        public static void create(CommandSpec.Builder cmd) {
                cmd.child(CommandSpec.builder()
                                .description(Text.of(""))
                                .permission("towny.command.town.outlaw")
                                .arguments(
                                                GenericArguments.optional(GenericArguments.choices(Text.of("action"),
                                                                ImmutableMap.of("add", "add", "remove", "remove", "list", "list"))),
                                                GenericArguments.optional(GenericArguments.string(Text.of("player"))))
                                .executor(new TownyOutlawExecutor())
                                .build(), "outlaw");
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

                if (!town.isStaff(player.getUniqueId())) {
                        src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_PERM_TOWNSTAFF));
                        return CommandResult.success();
                }

                String action = ctx.<String>getOne("action").orElse("list");
                switch (action.toLowerCase()) {
                case "add":
                        if (!ctx.<String>getOne("player").isPresent()) {
                                src.sendMessage(USAGE);
                                return CommandResult.success();
                        }
                        handleAdd(player, town, ctx.<String>getOne("player").get());
                        break;
                case "remove":
                        if (!ctx.<String>getOne("player").isPresent()) {
                                src.sendMessage(USAGE);
                                return CommandResult.success();
                        }
                        handleRemove(player, town, ctx.<String>getOne("player").get());
                        break;
                case "list":
                        handleList(src, town);
                        break;
                default:
                        src.sendMessage(USAGE);
                        break;
                }
                return CommandResult.success();
        }

        private void handleAdd(Player actor, Towny town, String targetName) {
                UUID targetId = DataHandler.getPlayerUUID(targetName);
                if (targetId == null) {
                        actor.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_BADPLAYERNAME));
                        return;
                }
                if (Objects.equals(targetId, actor.getUniqueId())) {
                        actor.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_OUTLAW_SELF));
                        return;
                }
                if (DataHandler.addTownOutlaw(town.getUUID(), targetId)) {
                                actor.sendMessage(Text.of(TextColors.GREEN,
                                                LanguageHandler.SUCCESS_OUTLAW_ADDED.replace("{PLAYER}", targetName)));
                } else {
                        actor.sendMessage(Text.of(TextColors.RED,
                                        LanguageHandler.ERROR_OUTLAW_ALREADY.replace("{PLAYER}", targetName)));
                }
        }

        private void handleRemove(Player actor, Towny town, String targetName) {
                UUID targetId = DataHandler.getPlayerUUID(targetName);
                if (targetId == null) {
                        actor.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_BADPLAYERNAME));
                        return;
                }
                if (!DataHandler.removeTownOutlaw(town.getUUID(), targetId)) {
                        actor.sendMessage(Text.of(TextColors.RED,
                                        LanguageHandler.ERROR_OUTLAW_NOT_LISTED.replace("{PLAYER}", targetName)));
                        return;
                }
                actor.sendMessage(Text.of(TextColors.GREEN,
                                LanguageHandler.SUCCESS_OUTLAW_REMOVED.replace("{PLAYER}", targetName)));
        }

        private void handleList(CommandSource src, Towny town) {
                Set<UUID> outlaws = DataHandler.getTownOutlaws(town.getUUID());
                if (outlaws.isEmpty()) {
                        src.sendMessage(Text.of(TextColors.YELLOW, LanguageHandler.INFO_OUTLAW_LIST_EMPTY));
                        return;
                }
                String list = outlaws.stream()
                                .map(DataHandler::getPlayerName)
                                .filter(Objects::nonNull)
                                .collect(Collectors.joining(", "));
                if (list.isEmpty()) {
                        list = LanguageHandler.FORMAT_UNKNOWN;
                }
                src.sendMessage(Text.of(TextColors.AQUA,
                                LanguageHandler.INFO_OUTLAW_LIST.replace("{LIST}", list)));
        }
}
