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
import org.spongepowered.api.text.serializer.TextSerializers;

import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.Utils;
import com.arckenver.towny.cmdelement.TownyNameElement;
import com.arckenver.towny.object.Towny;

public class TownyInfoExecutor implements CommandExecutor
{
	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("towny.command.town.info")
				.arguments(GenericArguments.optional(new TownyNameElement(Text.of("towny"))))
				.executor(new TownyInfoExecutor())
				.build(), "info");
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException
	{
		int clicker = Utils.CLICKER_NONE;
		Towny towny;

		if (ctx.<String>getOne("towny").isPresent())
		{
			towny = DataHandler.getTowny(ctx.<String>getOne("towny").get());
			if (towny == null)
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_BADTOWNNNAME));
				return CommandResult.success();
			}
			if (src instanceof Player)
			{
				Player player = (Player) src;
				if (towny.isStaff(player.getUniqueId()))
				{
					clicker = Utils.CLICKER_DEFAULT;
				}
			}
		}
		else
		{
			if (src instanceof Player)
			{
				Player player = (Player) src;
				towny = DataHandler.getTownyOfPlayer(player.getUniqueId());
				if (towny == null)
				{
					src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NEEDTOWNNAME));
					return CommandResult.success();
				}
				if (towny.isStaff(player.getUniqueId()))
				{
					clicker = Utils.CLICKER_DEFAULT;
				}
			}
			else
			{
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOPLAYER));
				return CommandResult.success();
			}
		}

                if (clicker != Utils.CLICKER_DEFAULT && src.hasPermission("towny.command.townyadmin"))
                {
                        clicker = Utils.CLICKER_ADMIN;
                }

		// Existing description
		src.sendMessage(Utils.formatTownyDescription(towny, clicker));

		return CommandResult.success();
	}
}
