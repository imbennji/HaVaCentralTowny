package com.arckenver.towny.cmdexecutor.towny;

import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.object.Towny;

public class TownyFlagsExecutor implements CommandExecutor {

        public static void create(CommandSpec.Builder cmd) {
                cmd.child(CommandSpec.builder()
                                .description(Text.of(""))
                                .permission("towny.command.town.flags")
                                .executor(new TownyFlagsExecutor())
                                .build(), "flags", "toggles");
        }

        @Override
        public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException {
                if (!(src instanceof Player)) {
                        src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOPLAYER));
                        return CommandResult.success();
                }

                Player player = (Player) src;
                Towny towny = DataHandler.getTownyOfPlayer(player.getUniqueId());
                if (towny == null) {
                        src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOTOWN));
                        return CommandResult.success();
                }
                if (!towny.isStaff(player.getUniqueId())) {
                        src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_PERM_TOWNSTAFF));
                        return CommandResult.success();
                }

                Text.Builder builder = Text.builder("");
                builder.append(Text.of(TextColors.GOLD, "----------{ "));
                builder.append(Text.of(TextColors.YELLOW, LanguageHandler.FORMAT_FLAGS));
                builder.append(Text.of(TextColors.GOLD, " }----------"));

                List<Entry<String, Boolean>> sortedFlags = towny.getFlags().entrySet().stream()
                                .sorted(Comparator.comparing(flagEntry -> flagEntry.getKey().toLowerCase()))
                                .collect(Collectors.toList());

                for (Entry<String, Boolean> entry : sortedFlags) {
                        boolean enabled = entry.getValue();
                        String flag = entry.getKey();

                        builder.append(Text.of(TextColors.GOLD,
                                        "\n    " + StringUtils.capitalize(flag.toLowerCase()) + ": "));
                        builder.append(Text.builder(LanguageHandler.FLAG_ENABLED)
                                        .color(enabled ? TextColors.YELLOW : TextColors.DARK_GRAY)
                                        .onClick(TextActions.runCommand("/town flag " + flag + " true"))
                                        .onHover(TextActions.showText(Text.of(TextColors.YELLOW,
                                                        LanguageHandler.CLICKME)))
                                        .build());
                        builder.append(Text.of(TextColors.GOLD, "/"));
                        builder.append(Text.builder(LanguageHandler.FLAG_DISABLED)
                                        .color(enabled ? TextColors.DARK_GRAY : TextColors.YELLOW)
                                        .onClick(TextActions.runCommand("/town flag " + flag + " false"))
                                        .onHover(TextActions.showText(Text.of(TextColors.YELLOW,
                                                        LanguageHandler.CLICKME)))
                                        .build());
                        builder.append(Text.of(TextColors.DARK_GRAY, " <- ", LanguageHandler.CLICK));
                }

                builder.append(Text.of(TextColors.GRAY,
                                "\n" + LanguageHandler.CLICKME + " " + LanguageHandler.FORMAT_FLAGS.toLowerCase()
                                                + " or use /town flag <flag> [true|false]"));

                src.sendMessage(builder.build());
                return CommandResult.success();
        }
}
