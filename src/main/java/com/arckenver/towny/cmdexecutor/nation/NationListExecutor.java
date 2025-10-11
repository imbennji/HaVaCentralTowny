package com.arckenver.towny.cmdexecutor.nation;

import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.object.Nation;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NationListExecutor implements CommandExecutor {
    public static void create(CommandSpec.Builder cmd) {
        cmd.child(CommandSpec.builder()
                .description(Text.of(""))
                .permission("towny.command.nation.list")
                .arguments()
                .executor(new NationListExecutor())
                .build(), "list", "l");
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException {
        List<Text> contents = new ArrayList<>();
        Iterator<Nation> iter = DataHandler.getNations().iterator();
        if (!iter.hasNext()) {
            contents.add(Text.of(TextColors.YELLOW, LanguageHandler.ERROR_NONATIONYET));
        } else {
            while (iter.hasNext()) {
                Nation nation = iter.next();
                contents.add(Text.of(TextColors.YELLOW, nation.getName(), TextColors.GOLD, " [" + nation.getTowns().size() + "]"));
            }
        }

        PaginationList.builder()
                .title(Text.of(TextColors.GOLD, "{ ", TextColors.YELLOW, LanguageHandler.HEADER_NATIONLIST, TextColors.GOLD, " }"))
                .contents(contents)
                .padding(Text.of("-"))
                .sendTo(src);
        return CommandResult.success();
    }
}
