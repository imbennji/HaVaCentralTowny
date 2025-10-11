package com.arckenver.towny.cmdexecutor.towny;

import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.cmdelement.TownyNameElement;
import com.arckenver.towny.object.Request;
import com.arckenver.towny.object.Towny;
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
import org.spongepowered.api.text.format.TextColors;

import java.util.*;
import java.util.stream.Collectors;

public class TownyJoinExecutor implements CommandExecutor {
	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("towny.command.town.join")
				.arguments(GenericArguments.optional(new TownyNameElement(Text.of("towny"))))
				.executor(new TownyJoinExecutor())
				.build(), "join", "apply");
	}

	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException {
		if (src instanceof Player) {
			Player guestPlayer = (Player) src;
			if (!ctx.<String>getOne("towny").isPresent()) {
				src.sendMessage(Text.of(TextColors.YELLOW, "/t join <towny>"));
				return CommandResult.success();
			}
			if (DataHandler.getTownyOfPlayer(guestPlayer.getUniqueId()) != null) {
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NEEDLEAVE));
				return CommandResult.success();
			}
			String townyName = ctx.<String>getOne("towny").get();
			Towny towny = DataHandler.getTowny(townyName);
			if (towny == null) {
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_BADTOWNNNAME));
				return CommandResult.success();
			}

			Request req = DataHandler.getJoinRequest(towny.getUUID(), guestPlayer.getUniqueId());
			if (req != null) {
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_ALREADYASKED));
				return CommandResult.success();
			}
			req = DataHandler.getInviteRequest(towny.getUUID(), guestPlayer.getUniqueId());
			if (towny.getFlag("open") || req != null) {
				if (req != null) {
					DataHandler.removeInviteRequest(req);
				}
				for (UUID uuid : towny.getCitizens()) {
					Optional<Player> optPlayer = Sponge.getServer().getPlayer(uuid);
					if (optPlayer.isPresent())
						optPlayer.get().sendMessage(Text.of(TextColors.GREEN, LanguageHandler.INFO_JOINTOWNANNOUNCE.replaceAll("\\{PLAYER\\}", guestPlayer.getName())));
				}
				towny.addCitizen(guestPlayer.getUniqueId());
				DataHandler.getTownyOfPlayer(guestPlayer.getUniqueId());
				             DataHandler.saveTowny(towny.getUUID());
				guestPlayer.sendMessage(Text.of(TextColors.GREEN, LanguageHandler.INFO_JOINTOWN.replaceAll("\\{TOWN\\}", towny.getName())));
				return CommandResult.success();
			}
			ArrayList<UUID> townyStaff = towny.getStaff();
			List<Player> townyStaffPlayers = townyStaff
					.stream()
					.filter(uuid -> Sponge.getServer().getPlayer(uuid).isPresent())
					.map(uuid -> Sponge.getServer().getPlayer(uuid).get())
					.collect(Collectors.toList());

			if (townyStaffPlayers.isEmpty()) {
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOSTAFFONLINE));
				return CommandResult.success();
			}
			DataHandler.addJoinRequest(new Request(towny.getUUID(), guestPlayer.getUniqueId()));
			for (Player p : townyStaffPlayers) {
				String str = LanguageHandler.INFO_CLICK_JOINREQUEST.replaceAll("\\{PLAYER\\}", guestPlayer.getName());
				p.sendMessage(Text.builder()
						.append(Text.of(TextColors.AQUA, str.split("\\{CLICKHERE\\}")[0]))
						.append(Text.builder(LanguageHandler.CLICKME)
								.onClick(TextActions.runCommand("/towny invite " + guestPlayer.getName()))
								.color(TextColors.DARK_AQUA)
								.build())
						.append(Text.of(TextColors.AQUA, str.split("\\{CLICKHERE\\}")[1])).build());
			}
			src.sendMessage(Text.of(TextColors.GREEN, LanguageHandler.INFO_INVITSEND.replaceAll("\\{RECEIVER\\}", townyName)));
		} else {
			src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOPLAYER));
		}
		return CommandResult.success();
	}
}
