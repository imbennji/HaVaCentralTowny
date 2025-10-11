package com.arckenver.towny.cmdexecutor.resident;

import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
import org.spongepowered.api.command.*;
import org.spongepowered.api.command.args.*;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class ResidentSetExecutor implements CommandExecutor {
    public static void create(CommandSpec.Builder root) {
        CommandSpec title = CommandSpec.builder()
                .permission("towny.command.resident.set")
                .arguments(GenericArguments.remainingJoinedStrings(Text.of("title")))
                .executor((src, args) -> {
                    if (!(src instanceof Player)) throw new CommandException(Text.of(TextColors.RED, LanguageHandler.ERROR_NOPLAYER));
                    String t = args.<String>getOne("title").orElse("").trim();
                    com.arckenver.towny.DataHandler.setResidentTitle(((Player) src).getUniqueId(), t);
                    src.sendMessage(Text.of(TextColors.GREEN, "Title updated."));
                    return CommandResult.success();
                }).build();

        root.child(CommandSpec.builder()
                .description(Text.of("Set resident options"))
                .child(title, "title")
                .build(), "set");
    }

    @Override public CommandResult execute(CommandSource src, CommandContext args) { return CommandResult.success(); }
}
