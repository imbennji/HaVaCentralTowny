package com.arckenver.towny.cmdexecutor.resident;

import com.arckenver.towny.DataHandler;
import org.spongepowered.api.command.*;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.*;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class ResidentSetAboutExecutor implements CommandExecutor {
    public static void create(CommandSpec.Builder root) {
        root.child(CommandSpec.builder()
                .permission("towny.command.resident.about")
                .arguments(GenericArguments.optional(GenericArguments.remainingJoinedStrings(Text.of("about"))))
                .executor(new ResidentSetAboutExecutor())
                .build(), "about", "setabout");
    }

    @Override public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException {
        if (!(src instanceof Player)) throw new CommandException(Text.of(TextColors.RED, "Players only."));
        Player p = (Player) src;
        String about = ctx.<String>getOne("about").orElse("");
        if (about.equalsIgnoreCase("clear") || about.equalsIgnoreCase("reset")) about = "";
        DataHandler.setResidentAbout(p.getUniqueId(), about);
        p.sendMessage(Text.of(TextColors.GREEN, "About set."));
        return CommandResult.success();
    }
}
