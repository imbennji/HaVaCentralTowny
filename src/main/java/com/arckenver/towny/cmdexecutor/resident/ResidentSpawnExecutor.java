package com.arckenver.towny.cmdexecutor.resident;

import com.arckenver.towny.ConfigHandler;
import com.arckenver.towny.DataHandler;
import com.arckenver.towny.TownyPlugin;
import com.arckenver.towny.object.Towny;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.*;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.*;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import com.arckenver.towny.LanguageHandler;

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
        if (!(src instanceof Player)) throw new CommandException(Text.of(LanguageHandler.colorRed(), "Players only."));
        Player p = (Player) src;
        UUID id = p.getUniqueId();

        Towny t = DataHandler.getTownyOfPlayer(id);
        if (t == null) throw new CommandException(Text.of(LanguageHandler.colorRed(), "You’re not in a town."));

        long now = System.currentTimeMillis();
        long cooldownEnds = DataHandler.getResidentSpawnCooldown(id);
        if (cooldownEnds > now) {
            long remaining = cooldownEnds - now;
            throw new CommandException(Text.of(LanguageHandler.colorRed(), "Spawn on cooldown for ", LanguageHandler.colorGold(), formatDuration(remaining)));
        }

        boolean preferBed = DataHandler.getResidentPreferBedSpawn(id);
        Optional<Location<World>> bedLocation = preferBed ? findBedSpawn(p) : Optional.empty();

        Location<World> dest;
        boolean usingBed = bedLocation.isPresent();
        if (usingBed) {
            dest = bedLocation.get();
        } else {
            dest = t.getSpawns().get("home");
            if (dest == null && !t.getSpawns().isEmpty()) {
                for (Map.Entry<String, Location<World>> e : t.getSpawns().entrySet()) { dest = e.getValue(); break; }
            }
            if (dest == null) throw new CommandException(Text.of(LanguageHandler.colorRed(), "Your town has no spawn set."));
        }

        long warmupSeconds = ConfigHandler.getNode("others", "residentSpawnWarmupSeconds").getLong(5L);
        long cooldownSeconds = ConfigHandler.getNode("others", "residentSpawnCooldownSeconds").getLong(300L);

        long warmupEnd = DataHandler.getResidentBedSpawnWarmup(id);
        if (warmupEnd > now) {
            long remaining = warmupEnd - now;
            throw new CommandException(Text.of(LanguageHandler.colorRed(), "Spawn warmup in progress: ", LanguageHandler.colorGold(), formatDuration(remaining)));
        }

        if (warmupSeconds > 0) {
            long warmupTarget = now + TimeUnit.SECONDS.toMillis(warmupSeconds);
            DataHandler.setResidentBedSpawnWarmup(id, warmupTarget);
            p.sendMessage(Text.of(LanguageHandler.colorAqua(), "Preparing to teleport in ", LanguageHandler.colorYellow(), warmupSeconds, LanguageHandler.colorAqua(), "s..."));
            Location<World> finalDest = dest;
            boolean finalUsingBed = usingBed;
            Towny finalTown = t;
            Sponge.getScheduler().createTaskBuilder()
                    .delay(warmupSeconds, TimeUnit.SECONDS)
                    .execute(() -> performTeleport(id, finalDest, finalUsingBed, finalTown, cooldownSeconds))
                    .submit(TownyPlugin.getInstance());
        } else {
            performTeleport(id, dest, usingBed, t, cooldownSeconds);
        }
        return CommandResult.success();
    }

    private void performTeleport(UUID id, Location<World> dest, boolean usingBed, Towny town, long cooldownSeconds) {
        Optional<Player> optPlayer = Sponge.getServer().getPlayer(id);
        if (!optPlayer.isPresent()) {
            return;
        }
        Player target = optPlayer.get();
        target.setLocation(dest);
        DataHandler.setResidentBedSpawnWarmup(id, 0L);
        DataHandler.markResidentSpawn(id, town != null ? town.getUUID() : null, TimeUnit.SECONDS.toMillis(cooldownSeconds));
        Text message = usingBed
                ? Text.of(LanguageHandler.colorGreen(), "Teleported to bed spawn.")
                : Text.of(LanguageHandler.colorGreen(), "Teleported to town spawn.");
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

    private static Optional<Location<World>> findBedSpawn(Player player) {
        return player.getBedLocation();
    }
}
