package com.arckenver.towny.cmdexecutor.resident;

import com.arckenver.towny.ConfigHandler;
import com.arckenver.towny.DataHandler;
import com.arckenver.towny.TownyPlugin;
import com.arckenver.towny.object.Towny;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.*;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.*;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.api.world.teleport.RespawnLocation;

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
        Player player = (Player) src;
        UUID id = player.getUniqueId();

        Towny town = DataHandler.getTownyOfPlayer(id);
        if (town == null) throw new CommandException(Text.of(TextColors.RED, "You’re not in a town."));

        long now = System.currentTimeMillis();
        long cooldownEnds = DataHandler.getResidentSpawnCooldown(id);
        if (cooldownEnds > now) {
            long remaining = cooldownEnds - now;
            throw new CommandException(Text.of(TextColors.RED, "Spawn on cooldown for ", TextColors.GOLD, formatDuration(remaining)));
        }

        long warmupEnd = DataHandler.getResidentBedSpawnWarmup(id);
        if (warmupEnd > now) {
            long remaining = warmupEnd - now;
            throw new CommandException(Text.of(TextColors.RED, "Spawn warmup in progress: ", TextColors.GOLD, formatDuration(remaining)));
        }

        SpawnTarget target = determineSpawnTarget(player, town, id);

        long warmupSeconds = ConfigHandler.getNode("others", "residentSpawnWarmupSeconds").getLong(5L);
        long cooldownMillis = TimeUnit.SECONDS.toMillis(ConfigHandler.getNode("others", "residentSpawnCooldownSeconds").getLong(300L));

        if (warmupSeconds > 0) {
            long warmupTarget = now + TimeUnit.SECONDS.toMillis(warmupSeconds);
            DataHandler.setResidentBedSpawnWarmup(id, warmupTarget);
            player.sendMessage(Text.of(TextColors.AQUA, "Preparing to teleport in ", TextColors.YELLOW, warmupSeconds, TextColors.AQUA, "s..."));
            Sponge.getScheduler().createTaskBuilder()
                    .delay(warmupSeconds, TimeUnit.SECONDS)
                    .execute(() -> performTeleport(id, target, cooldownMillis))
                    .submit(TownyPlugin.getInstance());
        } else {
            performTeleport(id, target, cooldownMillis);
        }
        return CommandResult.success();
    }

    private SpawnTarget determineSpawnTarget(Player player, Towny town, UUID residentId) throws CommandException {
        boolean preferBed = DataHandler.getResidentPreferBedSpawn(residentId);
        if (preferBed) {
            Optional<Location<World>> bed = resolveBedSpawn(player);
            if (bed.isPresent()) {
                return SpawnTarget.toBed(bed.get());
            }
            player.sendMessage(Text.of(TextColors.YELLOW, "No bed spawn found; using town spawn instead."));
        }

        String preferredSpawn = DataHandler.getResidentPreferredSpawn(residentId);
        Location<World> dest = null;
        String spawnName = null;
        if (preferredSpawn != null && !preferredSpawn.trim().isEmpty()) {
            dest = town.getSpawn(preferredSpawn);
            if (dest == null) {
                DataHandler.setResidentPreferredSpawn(residentId, "");
                player.sendMessage(Text.of(TextColors.RED, "Preferred spawn '", preferredSpawn, "' no longer exists."));
            } else {
                spawnName = preferredSpawn;
            }
        }

        if (dest == null) {
            dest = town.getSpawns().get("home");
            spawnName = "home";
        }

        if (dest == null && !town.getSpawns().isEmpty()) {
            for (Map.Entry<String, Location<World>> entry : town.getSpawns().entrySet()) {
                dest = entry.getValue();
                spawnName = entry.getKey();
                if (dest != null) {
                    break;
                }
            }
        }

        if (dest == null) {
            throw new CommandException(Text.of(TextColors.RED, "Your town has no spawn set."));
        }

        return SpawnTarget.toTown(dest, spawnName, town.getUUID());
    }

    private void performTeleport(UUID id, SpawnTarget target, long cooldownMillis) {
        DataHandler.setResidentBedSpawnWarmup(id, 0L);

        Optional<Player> optPlayer = Sponge.getServer().getPlayer(id);
        if (!optPlayer.isPresent()) {
            return;
        }

        Player targetPlayer = optPlayer.get();
        targetPlayer.setLocation(target.location);
        DataHandler.markResidentSpawn(id, target.townId, cooldownMillis);

        Text message;
        if (target.isBed) {
            message = Text.of(TextColors.GREEN, "Teleported to bed spawn.");
        } else if (target.spawnName != null && !target.spawnName.isEmpty()) {
            message = Text.of(TextColors.GREEN, "Teleported to town spawn ", TextColors.GOLD, target.spawnName, TextColors.GREEN, ".");
        } else {
            message = Text.of(TextColors.GREEN, "Teleported to town spawn.");
        }
        targetPlayer.sendMessage(message);
    }

    private Optional<Location<World>> resolveBedSpawn(Player player) {
        return player.get(Keys.RESPAWN_LOCATIONS).flatMap(map -> {
            RespawnLocation sameWorld = map.get(player.getWorld().getUniqueId());
            if (sameWorld != null) {
                Optional<Location<World>> sameWorldLocation = toLocation(sameWorld);
                if (sameWorldLocation.isPresent()) {
                    return sameWorldLocation;
                }
            }

            for (RespawnLocation respawn : map.values()) {
                Optional<Location<World>> location = toLocation(respawn);
                if (location.isPresent()) {
                    return location;
                }
            }
            return Optional.empty();
        });
    }

    private Optional<Location<World>> toLocation(RespawnLocation respawn) {
        Optional<World> world = Sponge.getServer().getWorld(respawn.getWorldUniqueId());
        if (!world.isPresent()) {
            Optional<WorldProperties> props = Sponge.getServer().getWorldProperties(respawn.getWorldUniqueId());
            if (props.isPresent()) {
                world = Sponge.getServer().loadWorld(props.get());
            }
        }
        return world.map(w -> new Location<>(w, respawn.getPosition()));
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

    private static final class SpawnTarget {
        private final Location<World> location;
        private final boolean isBed;
        private final String spawnName;
        private final UUID townId;

        private SpawnTarget(Location<World> location, boolean isBed, String spawnName, UUID townId) {
            this.location = location;
            this.isBed = isBed;
            this.spawnName = spawnName;
            this.townId = townId;
        }

        private static SpawnTarget toBed(Location<World> location) {
            return new SpawnTarget(location, true, null, null);
        }

        private static SpawnTarget toTown(Location<World> location, String spawnName, UUID townId) {
            return new SpawnTarget(location, false, spawnName, townId);
        }
    }
}
