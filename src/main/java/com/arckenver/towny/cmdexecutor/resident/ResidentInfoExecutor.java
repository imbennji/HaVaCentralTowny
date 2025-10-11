package com.arckenver.towny.cmdexecutor.resident;

import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.Utils;
import org.spongepowered.api.command.*;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.*;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.UUID;

public class ResidentInfoExecutor implements CommandExecutor {
    public static void create(CommandSpec.Builder root) {
        root.child(spec(), "info", "");
    }
    public static CommandSpec spec() {
        return CommandSpec.builder()
                .permission("towny.command.resident.info")
                .arguments(GenericArguments.optional(GenericArguments.string(Text.of("player"))))
                .executor(new ResidentInfoExecutor())
                .build();
    }

    @Override public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        String who = args.<String>getOne("player").orElse(null);
        if (who == null) {
            if (!(src instanceof Player)) throw new CommandException(Text.of(TextColors.RED, LanguageHandler.ERROR_NOPLAYER));
            who = ((Player) src).getName();
        }
        UUID id = DataHandler.getPlayerUUID(who);
        if (id == null) throw new CommandException(Text.of(TextColors.RED, LanguageHandler.FORMAT_UNKNOWN));

        Text base = Utils.formatCitizenDescription(who);
        String about = DataHandler.getResidentAbout(id);
        if (about != null && !about.trim().isEmpty()) {
            base = Text.of(base, Text.NEW_LINE, TextColors.GOLD, "About: ", TextColors.YELLOW, about);
        }
        src.sendMessage(base);
        return CommandResult.success();
    }
}
