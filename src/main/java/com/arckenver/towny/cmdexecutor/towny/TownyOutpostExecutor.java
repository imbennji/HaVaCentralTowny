package com.arckenver.towny.cmdexecutor.towny;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.TownyPlugin;
import com.arckenver.towny.Utils;
import com.arckenver.towny.event.PlayerTeleportEvent;
import com.arckenver.towny.object.Towny;

public class TownyOutpostExecutor implements CommandExecutor {
        public static void create(CommandSpec.Builder cmd) {
                cmd.child(CommandSpec.builder()
                                .description(Text.of(""))
                                .permission("towny.command.town.outpost")
                                .arguments(GenericArguments.optional(GenericArguments.integer(Text.of("index"))))
                                .executor(new TownyOutpostExecutor())
                                .build(), "outpost", "outposts");
        }

        @Override
        public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException {
                if (!(src instanceof Player)) {
                        src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOPLAYER));
                        return CommandResult.success();
                }

                Player player = (Player) src;
                Towny towny = DataHandler.getTownyOfPlayer(player.getUniqueId());
                if (towny == null) {
                        src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOTOWN));
                        return CommandResult.success();
                }

                if (!ctx.<Integer>getOne("index").isPresent()) {
                        sendOutpostList(src, towny);
                        return CommandResult.success();
                }

                int index = ctx.<Integer>getOne("index").get();
                if (index <= 0) {
                        sendInvalidIndexMessage(src, towny);
                        return CommandResult.success();
                }

                Location<World> spawn = towny.getOutpostSpawn(index);
                if (spawn == null) {
                        sendInvalidIndexMessage(src, towny);
                        return CommandResult.success();
                }

                if (player.hasPermission("towny.bypass.teleport.warmup")) {
                        teleportPlayer(player, spawn, src);
                        return CommandResult.success();
                }

                src.sendMessage(Text.of(TextColors.AQUA, LanguageHandler.INFO_TELEPORTCOOLDOWN));

                Scheduler scheduler = Sponge.getScheduler();
                Task.Builder taskBuilder = scheduler.createTaskBuilder();
                taskBuilder.execute(new Consumer<Task>() {

                        @Override
                        public void accept(Task t) {
                                t.cancel();
                                teleportPlayer(player, spawn, src);
                        }
                }).delay(10, TimeUnit.SECONDS).submit(TownyPlugin.getInstance());

                return CommandResult.success();
        }

        private static void sendOutpostList(CommandSource src, Towny towny) {
                List<Integer> indices = towny.getOutpostIndices();
                if (indices.isEmpty()) {
                        src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NO_OUTPOST_SPAWNS));
                        return;
                }

                src.sendMessage(Text.builder()
                                .append(Text.of(TextColors.AQUA,
                                                LanguageHandler.INFO_OUTPOST_LIST.split("\\{OUTPOSTLIST\\}")[0]))
                                .append(Utils.formatTownyOutpostSpawns(towny, TextColors.YELLOW))
                                .append(Text.of(TextColors.AQUA,
                                                LanguageHandler.INFO_OUTPOST_LIST.split("\\{OUTPOSTLIST\\}")[1]))
                                .append(Text.of(TextColors.DARK_GRAY, " <- " + LanguageHandler.CLICK)).build());
        }

        private static void sendInvalidIndexMessage(CommandSource src, Towny towny) {
                if (!towny.hasOutpostSpawns()) {
                        src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NO_OUTPOST_SPAWNS));
                        return;
                }

                src.sendMessage(Text.builder()
                                .append(Text.of(TextColors.RED,
                                                LanguageHandler.ERROR_OUTPOST_NUMBER.split("\\{OUTPOSTLIST\\}")[0]))
                                .append(Utils.formatTownyOutpostSpawns(towny, TextColors.YELLOW))
                                .append(Text.of(TextColors.RED,
                                                LanguageHandler.ERROR_OUTPOST_NUMBER.split("\\{OUTPOSTLIST\\}")[1]))
                                .append(Text.of(TextColors.DARK_GRAY, " <- " + LanguageHandler.CLICK)).build());
        }

        private static void teleportPlayer(Player player, Location<World> spawn, CommandSource src) {
                PlayerTeleportEvent event = new PlayerTeleportEvent(player, spawn, TownyPlugin.getCause());
                Sponge.getEventManager().post(event);
                if (event.isCancelled()) {
                        return;
                }

                player.setLocation(spawn);
                src.sendMessage(Text.of(TextColors.AQUA, LanguageHandler.INFO_TELEPORTED));
        }
}
