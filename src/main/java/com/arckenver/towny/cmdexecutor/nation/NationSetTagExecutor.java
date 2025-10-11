package com.arckenver.towny.cmdexecutor.nation;

import com.arckenver.towny.ConfigHandler;
import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
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

public class NationSetTagExecutor implements CommandExecutor {
    public static void create(CommandSpec.Builder cmd) {
        cmd.child(CommandSpec.builder()
                .description(Text.of(""))
                .permission("towny.command.nation.settag")
                .arguments(GenericArguments.optional(GenericArguments.string(Text.of("tag"))))
                .executor(new NationSetTagExecutor())
                .build(), "settag", "tag");
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

        String tag = ctx.<String>getOne("tag").orElse("");
        if (tag.isEmpty()) {
            nation.setTag(null);
        } else {
            int min = ConfigHandler.getNode("others", "minNationTagLength").getInt(2);
            int max = ConfigHandler.getNode("others", "maxNationTagLength").getInt(6);
            if (tag.length() < min || tag.length() > max) {
                src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NATION_TAG_LENGTH
                        .replace("{MIN}", String.valueOf(min))
                        .replace("{MAX}", String.valueOf(max))));
                return CommandResult.success();
            }
            Nation existing = DataHandler.getNationByTag(tag);
            if (existing != null && !existing.getUUID().equals(nation.getUUID())) {
                src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NATION_TAG_TAKEN));
                return CommandResult.success();
            }
            nation.setTag(tag);
        }

        DataHandler.saveNation(nation.getUUID());
        src.sendMessage(Text.of(TextColors.GREEN, LanguageHandler.INFO_NATION_TAG));
        return CommandResult.success();
    }
}
