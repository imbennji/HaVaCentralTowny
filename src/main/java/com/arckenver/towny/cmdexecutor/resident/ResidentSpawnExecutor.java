package com.arckenver.towny.cmdexecutor.resident;

import com.arckenver.towny.ConfigHandler;
import com.arckenver.towny.DataHandler;
import com.arckenver.towny.TownyPlugin;
import com.arckenver.towny.object.Towny;
import org.spongepowered.api.command.*;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.*;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ResidentSpawnExecutor implements CommandExecutor {
    public static void create(CommandSpec.Builder root) {
        root.child(spec(), "spawn");
    }
    public static CommandSpec spec() {
        return CommandSpec.builder()
                .permission("towny.command.resident.spawn")
                .executor(new ResidentSpawnExecutor())
                .build();
    }

    @Override public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException {
        if (!(src instanceof Player)) throw new CommandException(Text.of(TextColors.RED, "Players only."));
        Player p = (Player) src;
        UUID id = p.getUniqueId();

        Towny t = DataHandler.getTownyOfPlayer(id);
        if (t == null) throw new CommandException(Text.of(TextColors.RED, "You’re not in a town."));

        long now = System.currentTimeMillis();
        long cooldownEnds = DataHandler.getResidentSpawnCooldown(id);
        if (cooldownEnds > now) {
            long remaining = cooldownEnds - now;
            throw new CommandException(Text.of(TextColors.RED, "Spawn on cooldown for ", TextColors.GOLD, formatDuration(remaining)));
        }

        boolean preferBed = DataHandler.getResidentPreferBedSpawn(id);
        Optional<Location<World>> bedLocation = Optional.empty();
        if (preferBed) {
            try {
                bedLocation = p.getBedLocation();
            } catch (Exception ignored) {}
        }

        Location<World> dest;
        boolean usingBed = bedLocation.isPresent();
        if (usingBed) {
            dest = bedLocation.get();
        } else {
            dest = t.getSpawns().get("home");
            if (dest == null && !t.getSpawns().isEmpty()) {
                for (Map.Entry<String, Location<World>> e : t.getSpawns().entrySet()) { dest = e.getValue(); break; }
            }
            if (dest == null) throw new CommandException(Text.of(TextColors.RED, "Your town has no spawn set."));
        }

        long warmupSeconds = ConfigHandler.getNode("others", "residentSpawnWarmupSeconds").getLong(5L);
        long cooldownSeconds = ConfigHandler.getNode("others", "residentSpawnCooldownSeconds").getLong(300L);

        long warmupEnd = DataHandler.getResidentBedSpawnWarmup(id);
        if (warmupEnd > now) {
            long remaining = warmupEnd - now;
            throw new CommandException(Text.of(TextColors.RED, "Spawn warmup in progress: ", TextColors.GOLD, formatDuration(remaining)));
        }

        if (warmupSeconds > 0) {
            long warmupTarget = now + TimeUnit.SECONDS.toMillis(warmupSeconds);
            DataHandler.setResidentBedSpawnWarmup(id, warmupTarget);
            p.sendMessage(Text.of(TextColors.AQUA, "Preparing to teleport in ", TextColors.YELLOW, warmupSeconds, TextColors.AQUA, "s..."));
            Location<World> finalDest = dest;
            boolean finalUsingBed = usingBed;
            Towny finalTown = t;
            org.spongepowered.api.Sponge.getScheduler().createTaskBuilder()
                    .delay(warmupSeconds, TimeUnit.SECONDS)
                    .execute(() -> performTeleport(id, finalDest, finalUsingBed, finalTown, cooldownSeconds))
                    .submit(TownyPlugin.getInstance());
        } else {
            performTeleport(id, dest, usingBed, t, cooldownSeconds);
        }
        return CommandResult.success();
    }

    private void performTeleport(UUID id, Location<World> dest, boolean usingBed, Towny town, long cooldownSeconds) {
        Optional<Player> optPlayer = org.spongepowered.api.Sponge.getServer().getPlayer(id);
        if (!optPlayer.isPresent()) {
            return;
        }
        Player target = optPlayer.get();
        target.setLocation(dest);
        DataHandler.setResidentBedSpawnWarmup(id, 0L);
        DataHandler.markResidentSpawn(id, town != null ? town.getUUID() : null, TimeUnit.SECONDS.toMillis(cooldownSeconds));
        Text message = usingBed
                ? Text.of(TextColors.GREEN, "Teleported to bed spawn.")
                : Text.of(TextColors.GREEN, "Teleported to town spawn.");
        target.sendMessage(message);
    }

    private static String formatDuration(long millis) {
        if (millis <= 0) return "0s";
        Duration d = Duration.ofMillis(millis);
        long minutes = d.toMinutes();
        long seconds = d.minusMinutes(minutes).getSeconds();
        if (minutes > 0) {
            return minutes + "m " + seconds + "s";
        }
        return seconds + "s";
    }
}
