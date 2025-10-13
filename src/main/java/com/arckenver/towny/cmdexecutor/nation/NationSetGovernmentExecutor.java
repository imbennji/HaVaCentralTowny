package com.arckenver.towny.cmdexecutor.nation;

import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.object.Nation;
import com.arckenver.towny.object.Nation.GovernmentType;
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

import java.util.Locale;
import java.util.Map;

public class NationSetGovernmentExecutor implements CommandExecutor {
    public static void create(CommandSpec.Builder cmd) {
        ImmutableMap.Builder<String, GovernmentType> builder = ImmutableMap.builder();
        for (GovernmentType type : GovernmentType.values()) {
            builder.put(type.name().toLowerCase(Locale.ENGLISH), type);
        }
        Map<String, GovernmentType> options = builder.build();

        cmd.child(CommandSpec.builder()
                .description(Text.of(""))
                .permission("towny.command.nation.government")
                .arguments(GenericArguments.choices(Text.of("government"), options))
                .executor(new NationSetGovernmentExecutor())
                .build(), "setgovernment", "government");
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException {
        if (!(src instanceof Player)) {
            src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOPLAYER));
            return CommandResult.success();
        }

        if (!ctx.<GovernmentType>getOne("government").isPresent()) {
            src.sendMessage(Text.of(TextColors.YELLOW, "/nation government <type>"));
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

        if (!nation.isKing(player.getUniqueId())) {
            src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_PERM_NATIONLEADER));
            return CommandResult.success();
        }

        GovernmentType newGovernment = ctx.<GovernmentType>getOne("government").get();
        nation.setGovernment(newGovernment);
        DataHandler.saveNation(nation.getUUID());
        src.sendMessage(Text.of(TextColors.GREEN,
                LanguageHandler.INFO_NATION_GOVERNMENT.replace("{GOVERNMENT}", newGovernment.getDisplayName())));
        return CommandResult.success();
    }
}
