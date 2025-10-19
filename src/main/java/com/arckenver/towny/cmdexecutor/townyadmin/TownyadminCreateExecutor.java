package com.arckenver.towny.cmdexecutor.townyadmin;

import com.arckenver.towny.ConfigHandler;
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

import java.util.UUID;

public class TownyadminCreateExecutor implements CommandExecutor
{
	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("towny.command.townyadmin.create")
				.arguments(GenericArguments.optional(GenericArguments.string(Text.of("name"))))
				.executor(new TownyadminCreateExecutor())
				.build(), "create");
	}

	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException {
		if (!ctx.<String>getOne("name").isPresent()) {
			src.sendMessage(Text.of(LanguageHandler.colorYellow(), "/ta create <name>"));
			return CommandResult.success();
		}

		if (src instanceof Player) {
			Player player = (Player) src;
			String townyName = ctx.<String>getOne("name").get();

			if (DataHandler.getTowny(townyName) != null) {
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NAMETAKEN));
				return CommandResult.success();
			}

			if (!townyName.matches("[\\p{Alnum}\\p{IsIdeographic}\\p{IsLetter}\"_\"]*")) {
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NAMEALPHA));
				return CommandResult.success();
			}

			if (townyName.length() < ConfigHandler.getNode("others", "minTownyNameLength").getInt() || townyName.length() > ConfigHandler.getNode("others", "maxTownyNameLength").getInt()) {
				src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NAMELENGTH
						.replaceAll("\\{MIN\\}", ConfigHandler.getNode("others", "minTownyNameLength").getString())
						.replaceAll("\\{MAX\\}", ConfigHandler.getNode("others", "maxTownyNameLength").getString())));
				return CommandResult.success();
			}

			Towny towny = new Towny(UUID.randomUUID(), townyName, true);
			DataHandler.addTowny(towny);
			TownyadminClaimExecutor claimExecutor = new TownyadminClaimExecutor();
			claimExecutor.claimLand(player, townyName);  // Pass the townyName String here
			src.sendMessage(Text.of(LanguageHandler.colorGreen(), LanguageHandler.SUCCESS_GENERAL));
		} else {
			src.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.ERROR_NOPLAYER));
		}
		return CommandResult.success();
	}
}
