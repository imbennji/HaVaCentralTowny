package com.arckenver.towny.cmdexecutor.townyadmin;

import com.arckenver.towny.ConfigHandler;
import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.cmdelement.TownyNameElement;
import com.arckenver.towny.cmdexecutor.towny.TownyBuyextraExecutor;
import com.arckenver.towny.object.Towny;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class TownyadminSetRentIntervalExecutor implements CommandExecutor {

	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("towny.command.townyadmin.setrentinterval")
				.arguments(
						new TownyNameElement(Text.of("towny")),
						GenericArguments.optional(GenericArguments.integer(Text.of("interval"))))
				.executor(new TownyadminSetRentIntervalExecutor())
				.build(), "setrentinterval", "rentinterval");
	}

	@Nonnull
	@Override
	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException {
		Towny towny = DataHandler.getTowny(ctx.<String>getOne("towny").get());
		if (towny == null) {
			src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NEEDTOWNNAME));
			return CommandResult.success();
		}
		int n = ConfigHandler.getNode("towny", "defaultRentInterval").getInt();
		if (ctx.<String>getOne("interval").isPresent()) {
			n = ctx.<Integer>getOne("interval").get();
		}
		if(n < 0) {
			src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NEGATIVEINTERVAL));
			return CommandResult.success();
		}
		towny.setRentInterval(n);
		DataHandler.saveTowny(towny.getUUID());
		src.sendMessage(Text.of(TextColors.AQUA, LanguageHandler.INFO_RENTINTERVAL.replaceAll("\\{NUMBER\\}", n + "")));
		return CommandResult.success();
	}
}
