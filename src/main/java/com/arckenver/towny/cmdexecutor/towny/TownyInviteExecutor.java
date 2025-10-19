package com.arckenver.towny.cmdexecutor.towny;

import java.util.Optional;
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
import org.spongepowered.api.text.action.TextActions;

import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.object.Towny;
import com.arckenver.towny.object.Request;

public class TownyInviteExecutor implements CommandExecutor
{
	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("towny.command.town.invite")
				.arguments(GenericArguments.optional(GenericArguments.player(Text.of("player"))))
				.executor(new TownyInviteExecutor())
				.build(), "invite", "add");
	}

	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException
	{
		if (src instanceof Player)
		{
			Player hostPlayer = (Player) src;
			if (!ctx.<Player>getOne("player").isPresent())
			{
				src.sendMessage(Text.of(LanguageHandler.colorYellow(), "/t invite <player>"));
				return CommandResult.success();
			}
			Towny towny = DataHandler.getTownyOfPlayer(hostPlayer.getUniqueId());
			if (towny == null)
			{
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NOTOWN));
				return CommandResult.success();
			}
			if (!towny.isStaff(hostPlayer.getUniqueId()))
			{
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_PERM_TOWNSTAFF));
				return CommandResult.success();
			}
			Player guestPlayer = ctx.<Player>getOne("player").get();
			
			if (towny.isCitizen(guestPlayer.getUniqueId()))
			{
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_ALREADYINTOWN));
				return CommandResult.success();
			}
			
			Request req = DataHandler.getInviteRequest(towny.getUUID(), guestPlayer.getUniqueId());
			if (req != null)
			{
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_ALREADYINVITED));
				return CommandResult.success();
			}
			req = DataHandler.getJoinRequest(towny.getUUID(), guestPlayer.getUniqueId());
			if (req != null)
			{
				DataHandler.removeJoinRequest(req);
				for (UUID uuid : towny.getCitizens())
				{
					Optional<Player> optPlayer = Sponge.getServer().getPlayer(uuid);
					if (optPlayer.isPresent())
						optPlayer.get().sendMessage(Text.of(LanguageHandler.colorAqua(), LanguageHandler.INFO_JOINTOWNANNOUNCE.replaceAll("\\{PLAYER\\}", guestPlayer.getName())));
				}
				towny.addCitizen(guestPlayer.getUniqueId());
				guestPlayer.sendMessage(Text.of(LanguageHandler.colorGreen(), LanguageHandler.INFO_JOINTOWN.replaceAll("\\{TOWN\\}", towny.getName())));
				DataHandler.saveTowny(towny.getUUID());;
				return CommandResult.success();
			}
			DataHandler.addInviteRequest(new Request(towny.getUUID(), guestPlayer.getUniqueId()));

			String str = LanguageHandler.INFO_CLICK_TOWNINVITE.replaceAll("\\{TOWN\\}", towny.getName());
			guestPlayer.sendMessage(Text.builder()
					.append(Text.of(LanguageHandler.colorAqua(), str.split("\\{CLICKHERE\\}")[0]))
					.append(Text.builder(LanguageHandler.CLICKME)
							.onClick(TextActions.runCommand("/towny join " + towny.getRealName()))
							.color(LanguageHandler.colorDarkAqua())
							.build())
					.append(Text.of(LanguageHandler.colorAqua(), str.split("\\{CLICKHERE\\}")[1])).build());

			src.sendMessage(Text.of(LanguageHandler.colorGreen(), LanguageHandler.INFO_INVITSEND.replaceAll("\\{RECEIVER\\}", guestPlayer.getName())));
		}
		else
		{
			src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NOPLAYER));
		}
		return CommandResult.success();
	}
}
