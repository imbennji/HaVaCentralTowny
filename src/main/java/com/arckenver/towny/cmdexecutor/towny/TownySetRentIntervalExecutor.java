package com.arckenver.towny.cmdexecutor.towny;

import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
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

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class TownySetRentIntervalExecutor implements CommandExecutor {

	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("towny.command.town.setrentinterval")
				.arguments(GenericArguments.optional(GenericArguments.integer(Text.of("interval"))))
				.executor(new TownySetRentIntervalExecutor())
				.build(), "setrentinterval", "rentinterval");
	}

	@Nonnull
	@Override
	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException {
		if (src instanceof Player) {

			Player player = (Player) src;
			Towny towny = DataHandler.getTownyOfPlayer(player.getUniqueId());
			if (towny == null) {
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NOTOWN));
				return CommandResult.success();
			}
			if (!towny.isStaff(player.getUniqueId())) {
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_PERM_TOWNPRES));
				return CommandResult.success();
			}
			if (!ctx.<String>getOne("interval").isPresent()) {
				src.sendMessage(Text.of(LanguageHandler.colorYellow(), "/t setrentinterval <interval>"));
				return CommandResult.success();
			}
			int n = ctx.<Integer>getOne("interval").get();
			if(n < 0) {
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NEGATIVEINTERVAL));
				return CommandResult.success();
			}
			towny.setRentInterval(n);
			DataHandler.saveTowny(towny.getUUID());
			src.sendMessage(Text.of(LanguageHandler.colorAqua(), LanguageHandler.INFO_RENTINTERVAL.replaceAll("\\{NUMBER\\}", n + "")));
		} else {
			src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NOPLAYER));
		}
		return CommandResult.success();
	}
}
