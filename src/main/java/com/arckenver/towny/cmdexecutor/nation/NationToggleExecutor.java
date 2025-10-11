package com.arckenver.towny.cmdexecutor.nation;

import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.object.Nation;
import com.arckenver.towny.object.Towny;
import com.google.common.collect.ImmutableMap;
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

public class NationToggleExecutor implements CommandExecutor {
    public static void create(CommandSpec.Builder cmd) {
        CommandSpec toggleSpec = CommandSpec.builder()
                .description(Text.of(""))
                .permission("towny.command.nation.toggle")
                .arguments(
                        GenericArguments.optional(GenericArguments.choices(Text.of("setting"),
                                ImmutableMap.of(
                                        "open", "open",
                                        "public", "public",
                                        "neutral", "neutral"
                                ))),
                        GenericArguments.optional(GenericArguments.bool(Text.of("value"))))
                .executor(new NationToggleExecutor())
                .build();

        cmd.child(toggleSpec, "toggle", "toggleopen", "open", "public", "neutral");
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException {
        if (!(src instanceof Player)) {
            src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOPLAYER));
            return CommandResult.success();
        }

        Player player = (Player) src;
        Towny town = DataHandler.getTownyOfPlayer(player.getUniqueId());
        if (town == null || !town.hasNation()) {
            src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NONATION));
            return CommandResult.success();
        }

        Nation nation = DataHandler.getNation(town.getNationUUID());
        if (nation == null) {
            src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NATION_NOT_FOUND));
            return CommandResult.success();
        }

        if (!nation.isCapital(town.getUUID()) || !town.isPresident(player.getUniqueId())) {
            src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_PERM_NATIONLEADER));
            return CommandResult.success();
        }

        String setting = ctx.<String>getOne("setting").orElse("open");
        boolean newValue;

        switch (setting.toLowerCase()) {
            case "open":
                newValue = ctx.<Boolean>getOne("value").orElse(!nation.isOpen());
                nation.setOpen(newValue);
                DataHandler.saveNation(nation.getUUID());
                src.sendMessage(Text.of(TextColors.GREEN, LanguageHandler.INFO_NATION_OPEN));
                break;
            case "public":
                newValue = ctx.<Boolean>getOne("value").orElse(!nation.isPublic());
                nation.setPublic(newValue);
                DataHandler.saveNation(nation.getUUID());
                src.sendMessage(Text.of(TextColors.GREEN, LanguageHandler.INFO_NATION_PUBLIC));
                break;
            case "neutral":
                newValue = ctx.<Boolean>getOne("value").orElse(!nation.isNeutral());
                nation.setNeutral(newValue);
                DataHandler.saveNation(nation.getUUID());
                src.sendMessage(Text.of(TextColors.GREEN, LanguageHandler.INFO_NATION_NEUTRAL));
                break;
            default:
                src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_BADARG_AR));
                return CommandResult.success();
        }

        return CommandResult.success();
    }
}
