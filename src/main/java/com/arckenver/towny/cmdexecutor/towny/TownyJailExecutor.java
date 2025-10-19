package com.arckenver.towny.cmdexecutor.towny;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.cmdelement.CitizenNameElement;
import com.arckenver.towny.object.Plot;
import com.arckenver.towny.object.Towny;
import com.google.common.collect.ImmutableMap;

public class TownyJailExecutor implements CommandExecutor {
        private static final Text USAGE = Text.of(LanguageHandler.colorYellow(),
                        "/t jail add <resident> [hours]\n",
                        "/t jail release <resident>\n",
                        "/t jail list");

        public static void create(CommandSpec.Builder cmd) {
                cmd.child(CommandSpec.builder()
                                .description(Text.of(""))
                                .permission("towny.command.town.jail")
                                .arguments(
                                                GenericArguments.optional(GenericArguments.choices(Text.of("action"),
                                                                ImmutableMap.of("add", "add", "release", "release", "list", "list"))),
                                                GenericArguments.optional(new CitizenNameElement(Text.of("resident"))),
                                                GenericArguments.optional(GenericArguments.integer(Text.of("hours"))))
                                .executor(new TownyJailExecutor())
                                .build(), "jail");
        }

        @Override
        public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException {
                if (!(src instanceof Player)) {
                        src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NOPLAYER));
                        return CommandResult.success();
                }

                Player player = (Player) src;
                Towny town = DataHandler.getTownyOfPlayer(player.getUniqueId());
                if (town == null) {
                        src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NOTOWN));
                        return CommandResult.success();
                }

                if (!town.isStaff(player.getUniqueId())) {
                        src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_PERM_TOWNSTAFF));
                        return CommandResult.success();
                }

                String action = ctx.<String>getOne("action").orElse("list");
                switch (action.toLowerCase()) {
                case "add":
                        if (!ctx.<String>getOne("resident").isPresent()) {
                                src.sendMessage(USAGE);
                                return CommandResult.success();
                        }
                        handleAdd(player, town, ctx);
                        break;
                case "release":
                        if (!ctx.<String>getOne("resident").isPresent()) {
                                src.sendMessage(USAGE);
                                return CommandResult.success();
                        }
                        handleRelease(player, town, ctx);
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

        private void handleAdd(Player actor, Towny town, CommandContext ctx) {
                String residentName = ctx.<String>getOne("resident").get();
                UUID targetId = DataHandler.getPlayerUUID(residentName);
                if (targetId == null) {
                        actor.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_BADPLAYERNAME));
                        return;
                }
                if (!town.isCitizen(targetId)) {
                        actor.sendMessage(Text.of(LanguageHandler.colorRed(),
                                        LanguageHandler.ERROR_JAIL_TARGET_NOT_CITIZEN.replace("{PLAYER}", residentName)));
                        return;
                }
                if (DataHandler.isResidentJailed(targetId)) {
                        actor.sendMessage(Text.of(LanguageHandler.colorRed(),
                                        LanguageHandler.ERROR_JAIL_ALREADY.replace("{PLAYER}", residentName)));
                        return;
                }

                Optional<Plot> jailPlot = DataHandler.findPrimaryJailPlot(town);
                if (!jailPlot.isPresent()) {
                        actor.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_TOWN_NO_JAIL));
                        return;
                }

                Optional<Location<World>> jailLocation = DataHandler.getJailSpawn(town, jailPlot.get());
                if (!jailLocation.isPresent()) {
                        actor.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_TOWN_NO_JAIL));
                        return;
                }

                int hours = ctx.<Integer>getOne("hours").orElse(0);
                long releaseAt = hours > 0 ? System.currentTimeMillis() + TimeUnit.HOURS.toMillis(hours) : 0L;

                DataHandler.setResidentJailed(targetId, true, town.getUUID(), jailPlot.get().getUUID(), releaseAt);
                DataHandler.clearResidentJailRequests(targetId);

                Sponge.getServer().getPlayer(targetId).ifPresent(target -> {
                        target.setLocation(jailLocation.get());
                        String townName = town.getDisplayName();
                        target.sendMessage(Text.of(LanguageHandler.colorRed(),
                                        LanguageHandler.INFO_JAIL_TELEPORT.replace("{TOWN}", townName)));
                });

                String duration = hours > 0
                                ? LanguageHandler.INFO_JAIL_DURATION_HOURS.replace("{HOURS}", Integer.toString(hours))
                                : LanguageHandler.INFO_JAIL_DURATION_PERM;
                actor.sendMessage(Text.of(LanguageHandler.colorGreen(),
                                LanguageHandler.SUCCESS_JAIL_ADDED.replace("{PLAYER}", residentName)
                                                .replace("{DURATION}", duration)));
        }

        private void handleRelease(Player actor, Towny town, CommandContext ctx) {
                String residentName = ctx.<String>getOne("resident").get();
                UUID targetId = DataHandler.getPlayerUUID(residentName);
                if (targetId == null) {
                        actor.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_BADPLAYERNAME));
                        return;
                }
                if (!DataHandler.isResidentJailed(targetId)) {
                        actor.sendMessage(Text.of(LanguageHandler.colorRed(),
                                        LanguageHandler.ERROR_JAIL_NOT_JAILED.replace("{PLAYER}", residentName)));
                        return;
                }
                Optional<UUID> jailTown = DataHandler.getResidentJailTown(targetId);
                if (!jailTown.isPresent() || !Objects.equals(jailTown.get(), town.getUUID())) {
                        actor.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_TOWN_NO_JAIL));
                        return;
                }

                DataHandler.setResidentJailed(targetId, false, null, null, 0L);
                Sponge.getServer().getPlayer(targetId)
                                .ifPresent(p -> p.sendMessage(Text.of(LanguageHandler.colorGreen(), LanguageHandler.INFO_JAIL_RELEASE)));
                actor.sendMessage(Text.of(LanguageHandler.colorGreen(),
                                LanguageHandler.SUCCESS_JAIL_RELEASED.replace("{PLAYER}", residentName)));
        }

        private void handleList(CommandSource src, Towny town) {
                List<UUID> jailed = new ArrayList<>(DataHandler.getTownJailedResidents(town.getUUID()));
                if (jailed.isEmpty()) {
                        src.sendMessage(Text.of(LanguageHandler.colorYellow(), LanguageHandler.INFO_JAIL_LIST_EMPTY));
                        return;
                }
                String list = jailed.stream()
                                .map(DataHandler::getPlayerName)
                                .filter(Objects::nonNull)
                                .collect(Collectors.joining(", "));
                if (list.isEmpty()) {
                        list = LanguageHandler.FORMAT_UNKNOWN;
                }
                src.sendMessage(Text.of(LanguageHandler.colorAqua(),
                                LanguageHandler.INFO_JAIL_LIST.replace("{LIST}", list)));
        }
}
