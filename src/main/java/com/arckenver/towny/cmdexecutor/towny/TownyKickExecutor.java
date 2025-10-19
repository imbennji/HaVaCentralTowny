package com.arckenver.towny.cmdexecutor.towny;

import java.util.UUID;

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

import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.cmdelement.CitizenNameElement;
import com.arckenver.towny.object.Towny;

public class TownyKickExecutor implements CommandExecutor
{
	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("towny.command.town.kick")
				.arguments(GenericArguments.optional(new CitizenNameElement(Text.of("player"))))
				.executor(new TownyKickExecutor())
				.build(), "kick");
	}

	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException
	{
		if (src instanceof Player)
		{
			Player player = (Player) src;
			Towny towny = DataHandler.getTownyOfPlayer(player.getUniqueId());
			if (towny == null)
			{
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NOTOWN));
				return CommandResult.success();
			}
			if (!towny.isPresident(player.getUniqueId()))
			{
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_PERM_TOWNPRES));
				return CommandResult.success();
			}
			if (!ctx.<String>getOne("player").isPresent())
			{
				src.sendMessage(Text.of(LanguageHandler.colorYellow(), "/t kick <player>"));
				return CommandResult.success();
			}
			String toKick = ctx.<String>getOne("player").get();
			UUID uuid = DataHandler.getPlayerUUID(toKick);
			if (uuid == null)
			{
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_BADPLAYERNAME));
				return CommandResult.success();
			}
			if (!towny.isCitizen(uuid))
			{
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NOTINTOWN));
				return CommandResult.success();
			}
			if (player.getUniqueId().equals(uuid))
			{
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NOKICKSELF));
				return CommandResult.success();
			}
			if (towny.isPresident(uuid))
			{
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_KICKMAYOR));
				return CommandResult.success();
			}
			if (towny.isMinister(uuid) && towny.isMinister(player.getUniqueId()))
			{
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_KICKCOMAYOR));
				return CommandResult.success();
			}
			towny.removeCitizen(uuid);
			src.sendMessage(Text.of(LanguageHandler.colorYellow(), "/t kick <player>" + towny.getUUID()));
			DataHandler.saveTowny(towny.getUUID());
			src.sendMessage(Text.of(LanguageHandler.colorGreen(), LanguageHandler.SUCCESS_KICK.replaceAll("\\{PLAYER\\}", toKick)));
			Sponge.getServer().getPlayer(uuid).ifPresent(
					p -> p.sendMessage(Text.of(LanguageHandler.colorAqua(), LanguageHandler.SUCCESS_KICK.replaceAll("\\{PLAYER\\}", player.getName()))));
		}
		else
		{
			src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NOPLAYER));
		}
		return CommandResult.success();
	}
}
