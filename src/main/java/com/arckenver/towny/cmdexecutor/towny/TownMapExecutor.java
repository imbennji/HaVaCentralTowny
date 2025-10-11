package com.arckenver.towny.cmdexecutor.towny;

import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.object.Plot;
import com.arckenver.towny.object.Towny;
import com.flowpowered.math.vector.Vector3i;
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
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.List;

/**
 * /t map (and renderer for auto-map)
 */
public final class TownMapExecutor implements CommandExecutor {

    private static final int DEFAULT_RADIUS = 5; // 11x11 fits one chat page
    private static final int MIN_RADIUS = 2;
    private static final int MAX_RADIUS = 7;

    /** Hook for your reflection loader to register under /town map */
    public static void create(CommandSpec.Builder root) {
        root.child(
                CommandSpec.builder()
                        .description(Text.of("Show a Towny-style chunk map around you"))
                        .permission("towny.command.town.map")
                        .arguments(GenericArguments.optional(GenericArguments.integer(Text.of("radius"))))
                        .executor(new TownMapExecutor())
                        .build(),
                "map", "m"
        );
    }

    // ---------------- Command ----------------

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!(src instanceof Player)) {
            throw new CommandException(Text.of(TextColors.RED, LanguageHandler.ERROR_NOPLAYER));
        }
        Player p = (Player) src;

        int r = args.<Integer>getOne("radius").orElse(DEFAULT_RADIUS);
        src.sendMessage(renderFor(p, r));
        return CommandResult.success();
    }

    // ---------------- Public renderer API (used by PlayerMoveListener auto-map) ----------------

    /** Instance-friendly for your existing code: renderer.renderFor(player, 4). */
    public Text renderFor(Player player, int radius) {
        return renderForStatic(player, radius);
    }

    /** Static convenience: TownMapExecutor.renderFor(player, 4). */
    public static Text renderForStatic(Player player, int radius) {
        int r = clampRadius(radius);
        Location<World> loc = player.getLocation();
        Vector3i cc = loc.getChunkPosition();

        List<Text> lines = new ArrayList<>();
        // Header
        lines.add(Text.of(
                TextColors.GRAY, "Map ",
                TextColors.WHITE, "(", cc.getX(), ",", cc.getZ(), ") ",
                TextColors.DARK_GRAY, "[r=", r, "]  ",
                TextColors.GRAY, "N↑"
        ));

        // Grid: north (top) to south (bottom)
        for (int dz = r; dz >= -r; dz--) {
            lines.add(buildRow(player, cc, dz, r));
        }

        // Compact legend
        lines.add(Text.of(
                TextColors.DARK_GRAY, "Legend: ",
                TextColors.GOLD, "@", TextColors.GRAY, "=You  ",
                TextColors.GREEN, "H", TextColors.GRAY, "=Home  ",
                TextColors.GREEN, "■", TextColors.GRAY, "=YourTown  ",
                TextColors.BLUE, "■", TextColors.GRAY, "=OtherTown  ",
                TextColors.YELLOW, "◆", TextColors.GRAY, "=YourPlot  ",
                TextColors.AQUA, "◆", TextColors.GRAY, "=OtherPlot  ",
                TextColors.DARK_GRAY, "·", TextColors.GRAY, "=Wild"
        ));

        return Text.joinWith(Text.NEW_LINE, lines);
    }

    private static int clampRadius(int r) {
        if (r < MIN_RADIUS) return MIN_RADIUS;
        if (r > MAX_RADIUS) return MAX_RADIUS;
        return r;
    }

    // ---------------- Internals ----------------

    private static Text buildRow(Player viewer, Vector3i centerChunk, int dz, int r) {
        World w = viewer.getWorld();
        List<Text> parts = new ArrayList<>();

        for (int dx = -r; dx <= r; dx++) {
            int cx = centerChunk.getX() + dx;
            int cz = centerChunk.getZ() + dz;

            // center of chunk
            int bx = cx * 16 + 8;
            int bz = cz * 16 + 8;
            Location<World> probe = w.getLocation(bx, viewer.getLocation().getBlockY(), bz);

            parts.add(symbolFor(viewer, probe, cx, cz, centerChunk));
        }
        return Text.join(parts);
    }

    private static Text symbolFor(Player viewer, Location<World> tile, int cx, int cz, Vector3i center) {
        // You
        if (cx == center.getX() && cz == center.getZ()) {
            return Text.of(TextColors.GOLD, "@");
        }

        Towny t = DataHandler.getTowny(tile);
        if (t == null) {
            return Text.of(TextColors.DARK_GRAY, "·"); // wilderness
        }

        boolean ownTown = t.isCitizen(viewer.getUniqueId());
        Plot plot = t.getPlot(tile);

        // Plot marker
        if (plot != null) {
            return Text.of(ownTown ? TextColors.YELLOW : TextColors.AQUA, "◆");
        }

        // Home marker if present in this chunk
        if (homeInChunk(t, tile.getExtent(), cx, cz)) {
            return Text.of(TextColors.GREEN, "H");
        }

        // Regular town claim
        return Text.of(ownTown ? TextColors.GREEN : TextColors.BLUE, "■");
    }

    private static boolean homeInChunk(Towny towny, World world, int cx, int cz) {
        try {
            Object spawns = towny.getClass().getMethod("getSpawns").invoke(towny);
            if (spawns instanceof java.util.Map) {
                Object home = ((java.util.Map<?, ?>) spawns).get("home");
                Location<World> loc = toLocation(world, home);
                if (loc != null) {
                    Vector3i c = loc.getChunkPosition();
                    return c.getX() == cx && c.getZ() == cz;
                }
            }
        } catch (Throwable ignored) {
            try {
                Object home = towny.getClass().getMethod("getSpawn", String.class).invoke(towny, "home");
                Location<World> loc = toLocation(world, home);
                if (loc != null) {
                    Vector3i c = loc.getChunkPosition();
                    return c.getX() == cx && c.getZ() == cz;
                }
            } catch (Throwable ignored2) {}
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private static Location<World> toLocation(World w, Object obj) {
        if (obj == null) return null;
        if (obj instanceof Location) return (Location<World>) obj;
        try {
            Class<?> c = obj.getClass();
            int x = (int) c.getMethod("getX").invoke(obj);
            int z = (int) c.getMethod("getY").invoke(obj); // 2D point (x,y) -> (x,z)
            return w.getLocation(x, 64, z);
        } catch (Throwable ignored) {
            return null;
        }
    }
}
