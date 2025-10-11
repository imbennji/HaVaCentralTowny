package com.arckenver.towny.cmdexecutor.plot;

import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.Utils;
import com.arckenver.towny.object.Towny;
import com.arckenver.towny.object.Plot;
import org.spongepowered.api.Sponge;
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

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.UUID;

@SuppressWarnings("RegExpRedundantEscape")
@ParametersAreNonnullByDefault
public class PlotPutRentExecutor implements CommandExecutor {
	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("towny.command.plot.putrent")
				.arguments(GenericArguments.optional(GenericArguments.doubleNum(Text.of("price"))))
				.executor(new PlotPutRentExecutor())
				.build(), "putrent", "forrent", "fr");
	}

	@Nonnull
	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException {
		if (src instanceof Player) {
			Player player = (Player) src;
			Towny towny = DataHandler.getTowny(player.getLocation());
			if (towny == null) {
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NEEDSTANDTOWN));
				return CommandResult.success();
			}
			Plot plot = towny.getPlot(player.getLocation());
			if (plot == null) {
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NEEDSTANDPLOTSELF));
				return CommandResult.success();
			}
			if ((!plot.isOwner(player.getUniqueId()) || towny.isAdmin()) && !towny.isStaff(player.getUniqueId())) {
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOOWNER));
				return CommandResult.success();
			}
			if (!ctx.<Double>getOne("price").isPresent()) {
				src.sendMessage(Text.of(TextColors.YELLOW, "/z putrent <price>"));
				return CommandResult.success();
			}
			BigDecimal price = BigDecimal.valueOf(ctx.<Double>getOne("price").get());
			if (price.compareTo(BigDecimal.ZERO) == -1) {
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_BADARG_P));
				return CommandResult.success();
			}
			plot.setRentalPrice(price);
			DataHandler.saveTowny(towny.getUUID());
			ArrayList<UUID> targets = towny.getCitizens();
			if (!targets.contains(player.getUniqueId()))
				targets.add(player.getUniqueId());
			targets.forEach(
					uuid -> Sponge.getServer().getPlayer(uuid).ifPresent(
							p -> {
								String str = LanguageHandler.INFO_PLOTFORRENT.replaceAll("\\{PLOT\\}", plot.getDisplayName());
								String[] splited = str.split("\\{AMOUNT\\}");
								src.sendMessage(Text.builder()
										.append(Text.of(TextColors.AQUA, (splited.length > 0) ? splited[0] : ""))
										.append(Utils.formatPrice(TextColors.GREEN, price))
										.append(Text.of(TextColors.AQUA, (splited.length > 1) ? splited[1] : "")).build());
							}));
		} else {
			src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOPLAYER));
		}
		return CommandResult.success();
	}
}
