package com.arckenver.towny.cmdexecutor.resident;

import com.arckenver.towny.DataHandler;
import com.arckenver.towny.object.Towny;
import org.spongepowered.api.command.*;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.*;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Map;

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

        Towny t = DataHandler.getTownyOfPlayer(p.getUniqueId());
        if (t == null) throw new CommandException(Text.of(TextColors.RED, "You’re not in a town."));

        Location<World> dest = t.getSpawns().get("home");
        if (dest == null && !t.getSpawns().isEmpty()) {
            for (Map.Entry<String, Location<World>> e : t.getSpawns().entrySet()) { dest = e.getValue(); break; }
        }
        if (dest == null) throw new CommandException(Text.of(TextColors.RED, "Your town has no spawn set."));

        p.setLocation(dest);
        p.sendMessage(Text.of(TextColors.GREEN, "Teleported to town spawn."));
        return CommandResult.success();
    }
}
