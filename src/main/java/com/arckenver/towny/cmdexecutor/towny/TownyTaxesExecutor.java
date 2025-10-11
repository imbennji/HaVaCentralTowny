package com.arckenver.towny.cmdexecutor.towny;

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

import com.arckenver.towny.ConfigHandler;
import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.object.Towny;

public class TownyTaxesExecutor implements CommandExecutor
{
	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("towny.command.town.taxes")
				.arguments(GenericArguments.optional(GenericArguments.doubleNum(Text.of("amount"))))
				.executor(new TownyTaxesExecutor())
				.build(), "taxes");
	}

	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException
	{
		if (src instanceof Player)
		{
			Player player = (Player) src;
			Towny towny = DataHandler.getTownyOfPlayer(player.getUniqueId());
			if (towny == null)
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOTOWN));
				return CommandResult.success();
			}
			if (!towny.isStaff(player.getUniqueId()))
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_PERM_TOWNSTAFF));
				return CommandResult.success();
			}
			if (!ctx.<Double>getOne("amount").isPresent())
			{
				src.sendMessage(Text.of(TextColors.YELLOW, "/t taxes <amount>"));
				return CommandResult.success();
			}
			double newTaxes = ctx.<Double>getOne("amount").get();
			if (!ConfigHandler.getNode("towny", "canEditTaxes").getBoolean())
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_TAXEDIT));
				return CommandResult.success();
			}
			if (newTaxes > ConfigHandler.getNode("towny", "maxTaxes").getDouble())
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_TAXMAX.replaceAll("\\{AMOUNT\\}", String.valueOf(ConfigHandler.getNode("towny", "maxTaxes").getDouble()))));
				return CommandResult.success();
			}
			towny.setTaxes(newTaxes);
			DataHandler.saveTowny(towny.getUUID());
			src.sendMessage(Text.of(TextColors.GREEN, LanguageHandler.SUCCESS_CHANGETAX));
		}
		else
		{
			src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOPLAYER));
		}
		return CommandResult.success();
	}
}
