package com.arckenver.towny.cmdexecutor.resident;

import com.arckenver.towny.DataHandler;
import org.spongepowered.api.command.*;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public class ResidentModeExecutor implements CommandExecutor {

    public static void create(CommandSpec.Builder root) {
        root.child(spec(), "mode", "modes");
    }

    public static CommandSpec spec() {
        return CommandSpec.builder()
                .permission("towny.command.resident.mode")
                .arguments(GenericArguments.optional(GenericArguments.remainingJoinedStrings(Text.of("modes"))))
                .executor(new ResidentModeExecutor())
                .build();
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!(src instanceof Player)) {
            throw new CommandException(Text.of(TextColors.RED, "Players only."));
        }
        Player player = (Player) src;
        UUID id = player.getUniqueId();

        String modesArg = args.<String>getOne("modes").orElse("").trim();
        if (modesArg.isEmpty()) {
            Set<String> active = DataHandler.getResidentModes(id);
            if (active.isEmpty()) {
                player.sendMessage(Text.of(TextColors.GOLD, "Active modes: ", TextColors.GRAY, "(none)"));
            } else {
                player.sendMessage(Text.of(TextColors.GOLD, "Active modes: ", TextColors.YELLOW, String.join(", ", active)));
            }
            return CommandResult.success();
        }

        Set<String> toggled = new LinkedHashSet<>();
        Set<String> enabled = new LinkedHashSet<>();
        Set<String> disabled = new LinkedHashSet<>();

        Arrays.stream(modesArg.split("\\s+"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .forEach(mode -> {
                    boolean nowEnabled = DataHandler.toggleResidentMode(id, mode);
                    toggled.add(mode.toLowerCase());
                    if (nowEnabled) enabled.add(mode.toLowerCase());
                    else disabled.add(mode.toLowerCase());
                });

        if (toggled.isEmpty()) {
            player.sendMessage(Text.of(TextColors.YELLOW, "No modes toggled."));
        } else {
            if (!enabled.isEmpty()) {
                player.sendMessage(Text.of(TextColors.GREEN, "Enabled: ", TextColors.YELLOW, String.join(", ", enabled)));
            }
            if (!disabled.isEmpty()) {
                player.sendMessage(Text.of(TextColors.RED, "Disabled: ", TextColors.YELLOW, String.join(", ", disabled)));
            }
        }
        return CommandResult.success();
    }
}
