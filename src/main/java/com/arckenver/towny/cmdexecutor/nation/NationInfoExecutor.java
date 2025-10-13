package com.arckenver.towny.cmdexecutor.nation;

import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.Utils;
import com.arckenver.towny.cmdelement.NationNameElement;
import com.arckenver.towny.object.Nation;
import com.arckenver.towny.object.Towny;
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

public class NationInfoExecutor implements CommandExecutor {
    public static void create(CommandSpec.Builder cmd) {
        cmd.executor(new NationInfoExecutor());
        cmd.child(CommandSpec.builder()
                .description(Text.of(""))
                .permission("towny.command.nation.info")
                .arguments(GenericArguments.optional(new NationNameElement(Text.of("nation"))))
                .executor(new NationInfoExecutor())
                .build(), "info", "i");

        NationListExecutor.create(cmd);
        NationCreateExecutor.create(cmd);
        NationInviteExecutor.create(cmd);
        NationJoinExecutor.create(cmd);
        NationLeaveExecutor.create(cmd);
        NationKickExecutor.create(cmd);
        NationSetBoardExecutor.create(cmd);
        NationSetTagExecutor.create(cmd);
        NationSetGovernmentExecutor.create(cmd);
        NationToggleOpenExecutor.create(cmd);
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException {
        Nation nation = null;

        if (ctx.<String>getOne("nation").isPresent()) {
            nation = DataHandler.getNation(ctx.<String>getOne("nation").get());
            if (nation == null) {
                src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NATION_NOT_FOUND));
                return CommandResult.success();
            }
        } else if (src instanceof Player) {
            Player player = (Player) src;
            Towny town = DataHandler.getTownyOfPlayer(player.getUniqueId());
            if (town == null || !town.hasNation()) {
                src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NONATION));
                return CommandResult.success();
            }
            nation = DataHandler.getNation(town.getNationUUID());
            if (nation == null) {
                src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NATION_NOT_FOUND));
                return CommandResult.success();
            }
        } else {
            src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NEEDNATIONNAME));
            return CommandResult.success();
        }

        src.sendMessage(Utils.formatNationDescription(nation));
        return CommandResult.success();
    }
}
