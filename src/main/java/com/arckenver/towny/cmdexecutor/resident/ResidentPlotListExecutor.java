package com.arckenver.towny.cmdexecutor.resident;

import com.arckenver.towny.DataHandler;
import com.arckenver.towny.object.Plot;
import com.arckenver.towny.object.Towny;
import org.spongepowered.api.command.*;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.*;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.*;
import java.util.stream.Collectors;

public class ResidentPlotListExecutor implements CommandExecutor {
    public static void create(CommandSpec.Builder root) {
        root.child(spec(), "plotlist");
    }
    public static CommandSpec spec() {
        return CommandSpec.builder()
                .permission("towny.command.resident.plotlist")
                .arguments(GenericArguments.optional(GenericArguments.string(Text.of("player"))))
                .executor(new ResidentPlotListExecutor())
                .build();
    }

    @Override public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException {
        String who = ctx.<String>getOne("player").orElse(null);
        UUID id;
        if (who == null) throw new CommandException(Text.of(TextColors.RED, "Usage: /res plotlist <player>"));
        id = DataHandler.getPlayerUUID(who);
        if (id == null) throw new CommandException(Text.of(TextColors.RED, "Unknown player."));

        List<String> lines = new ArrayList<>();
        for (Towny t : DataHandler.getTowny().values()) {
            List<Plot> mine = t.getPlots().values().stream()
                    .filter(p -> id.equals(p.getOwner()))
                    .collect(Collectors.toList());
            if (mine.isEmpty()) continue;
            lines.add("§6Town §e" + t.getDisplayName() + "§6:");
            for (Plot p : mine) {
                lines.add("  §e" + p.getDisplayName() + " §7(" + p.getRect().getMinX() + "," + p.getRect().getMinY() + ")");
            }
        }
        if (lines.isEmpty()) {
            src.sendMessage(Text.of(TextColors.GRAY, "(no plots)"));
        } else {
            for (String s : lines) src.sendMessage(Text.of(TextColors.WHITE, s.replace('§', '§')));
        }
        return CommandResult.success();
    }
}
