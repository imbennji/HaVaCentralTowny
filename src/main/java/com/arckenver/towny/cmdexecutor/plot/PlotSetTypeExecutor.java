package com.arckenver.towny.cmdexecutor.plot;

import java.util.Map;
import java.util.stream.Collectors;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.Utils;
import com.arckenver.towny.object.Plot;
import com.arckenver.towny.object.PlotType;
import com.arckenver.towny.object.Towny;

public class PlotSetTypeExecutor implements CommandExecutor {
        public static void create(CommandSpec.Builder cmd) {
                Map<String, String> aliases = PlotType.getAliases().stream()
                                .collect(Collectors.toMap(alias -> alias, alias -> alias));
                cmd.child(CommandSpec.builder()
                                .description(Text.of(""))
                                .permission("towny.command.plot.settype")
                                .arguments(GenericArguments.choices(Text.of("type"), aliases))
                                .executor(new PlotSetTypeExecutor())
                                .build(), "set", "type");
        }

        @Override
        public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException {
                if (!(src instanceof Player)) {
                        src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NOPLAYER));
                        return CommandResult.success();
                }
                Player player = (Player) src;
                Towny towny = DataHandler.getTowny(player.getLocation());
                if (towny == null) {
                        src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NEEDSTANDTOWN));
                        return CommandResult.success();
                }
                Plot plot = towny.getPlot(player.getLocation());
                if (plot == null) {
                        src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NEEDSTANDPLOTSELF));
                        return CommandResult.success();
                }
                if (!plot.isOwner(player.getUniqueId()) && !towny.isStaff(player.getUniqueId())) {
                        src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NOOWNER));
                        return CommandResult.success();
                }
                String rawType = ctx.<String>getOne("type").orElse(null);
                PlotType newType = PlotType.fromString(rawType);
                if (newType == null) {
                        src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_PLOT_TYPE_INVALID.replace("{TYPE}", rawType)));
                        return CommandResult.success();
                }
                if (newType == plot.getType()) {
                        src.sendMessage(Text.of(LanguageHandler.colorYellow(), LanguageHandler.INFO_PLOT_TYPE_UNCHANGED
                                        .replace("{TYPE}", plot.getType().getDisplayName())));
                        return CommandResult.success();
                }

                PlotType oldType = plot.getType();
                plot.setType(newType);
                plot.enforceTypeRules();
                DataHandler.saveTowny(towny.getUUID());
                if (oldType == PlotType.JAIL && newType != PlotType.JAIL) {
                        DataHandler.releaseResidentsInJailPlot(towny.getUUID(), plot.getUUID());
                }
                src.sendMessage(Text.of(LanguageHandler.colorGreen(),
                                LanguageHandler.SUCCESS_PLOT_TYPE_SET.replace("{TYPE}", newType.getDisplayName())));
                src.sendMessage(Utils.formatPlotDescription(plot, towny, Utils.CLICKER_DEFAULT));
                return CommandResult.success();
        }
}
