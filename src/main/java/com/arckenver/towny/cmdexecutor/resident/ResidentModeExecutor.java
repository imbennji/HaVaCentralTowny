package com.arckenver.towny.cmdexecutor.resident;

import com.arckenver.towny.DataHandler;
import org.spongepowered.api.command.*;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import com.arckenver.towny.LanguageHandler;

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
            throw new CommandException(Text.of(LanguageHandler.colorRed(), "Players only."));
        }
        Player player = (Player) src;
        UUID id = player.getUniqueId();

        String modesArg = args.<String>getOne("modes").orElse("").trim();
        if (modesArg.isEmpty()) {
            Set<String> active = DataHandler.getResidentModes(id);
            if (active.isEmpty()) {
                player.sendMessage(Text.of(LanguageHandler.colorGold(), "Active modes: ", LanguageHandler.colorGray(), "(none)"));
            } else {
                player.sendMessage(Text.of(LanguageHandler.colorGold(), "Active modes: ", LanguageHandler.colorYellow(), String.join(", ", active)));
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
            player.sendMessage(Text.of(LanguageHandler.colorYellow(), "No modes toggled."));
        } else {
            if (!enabled.isEmpty()) {
                player.sendMessage(Text.of(LanguageHandler.colorGreen(), "Enabled: ", LanguageHandler.colorYellow(), String.join(", ", enabled)));
            }
            if (!disabled.isEmpty()) {
                player.sendMessage(Text.of(LanguageHandler.colorRed(), "Disabled: ", LanguageHandler.colorYellow(), String.join(", ", disabled)));
            }
        }
        return CommandResult.success();
    }
}
