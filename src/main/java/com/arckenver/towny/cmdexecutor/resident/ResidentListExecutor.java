package com.arckenver.towny.cmdexecutor.resident;

import com.arckenver.towny.DataHandler;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.*;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.*;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.stream.Collectors;

public class ResidentListExecutor implements CommandExecutor {
    public static void create(CommandSpec.Builder root) {
        root.child(spec(), "list");
    }
    public static CommandSpec spec() {
        return CommandSpec.builder()
                .permission("towny.command.resident.list")
                .executor(new ResidentListExecutor())
                .build();
    }

    @Override public CommandResult execute(CommandSource src, CommandContext args) {
        String online = Sponge.getServer().getOnlinePlayers().stream()
                .map(p -> p.getName())
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.joining(", "));
        if (online.isEmpty()) online = "(none)";
        src.sendMessage(Text.of(TextColors.GOLD, "Online residents: ", TextColors.YELLOW, online));
        return CommandResult.success();
    }
}
