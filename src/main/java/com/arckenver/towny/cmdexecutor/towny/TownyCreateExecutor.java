package com.arckenver.towny.cmdexecutor.towny;

import com.arckenver.towny.*;
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
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.format.TextColors;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public class TownyCreateExecutor implements CommandExecutor {
	public static void create(CommandSpec.Builder cmd) {
		cmd.child(CommandSpec.builder()
				.description(Text.of(""))
				.permission("towny.command.town.create")
				.arguments(GenericArguments.optional(GenericArguments.string(Text.of("name"))))
				.executor(new TownyCreateExecutor())
				.build(), "create", "new");
	}

	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException {
		if (!ctx.<String>getOne("name").isPresent()) {
			src.sendMessage(Text.of(TextColors.YELLOW, "/t create <name>"));
			return CommandResult.success();
		}

		if (!src.hasPermission("towny.command.town.create")) {
			src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NO_PERMISSION));
			return CommandResult.success();
		}

		if (src instanceof Player) {
			Player player = (Player) src;

			if (!ConfigHandler.getNode("worlds").getNode(player.getWorld().getName()).getNode("enabled").getBoolean()) {
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_PLUGINDISABLEDINWORLD));
				return CommandResult.success();
			}

			if (DataHandler.getTownyOfPlayer(player.getUniqueId()) != null) {
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NEEDLEAVE));
				return CommandResult.success();
			}

			String townyName = ctx.<String>getOne("name").get();

			if (DataHandler.getTowny(townyName) != null) {
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NAMETAKEN));
				return CommandResult.success();
			}

			if (!townyName.matches("[\\p{Alnum}\\p{IsIdeographic}\\p{IsLetter}\"_\"]*")) {
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NAMEALPHA));
				return CommandResult.success();
			}

			int minNameLength = ConfigHandler.getNode("others", "minTownyNameLength").getInt();
			int maxNameLength = ConfigHandler.getNode("others", "maxTownyNameLength").getInt();

			if (townyName.length() < minNameLength || townyName.length() > maxNameLength) {
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NAMELENGTH
						.replaceAll("\\{MIN\\}", String.valueOf(minNameLength))
						.replaceAll("\\{MAX\\}", String.valueOf(maxNameLength))));
				return CommandResult.success();
			}

			if (TownyPlugin.getEcoService() == null) {
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOECO));
				return CommandResult.success();
			}

			Optional<UniqueAccount> optAccount = TownyPlugin.getEcoService().getOrCreateAccount(player.getUniqueId());

			if (!optAccount.isPresent()) {
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_ECONOACCOUNT));
				return CommandResult.success();
			}

			BigDecimal creationPrice = BigDecimal.valueOf(ConfigHandler.getNode("prices", "townyCreationPrice").getDouble());

			if (optAccount.get().getBalance(TownyPlugin.getEcoService().getDefaultCurrency()).compareTo(creationPrice) < 0) {
				src.sendMessage(Text.builder()
						.append(Text.of(TextColors.RED, LanguageHandler.ERROR_NEEDMONEY.split("\\{AMOUNT\\}")[0]))
						.append(Utils.formatPrice(TextColors.RED, creationPrice))
						.append(Text.of(TextColors.RED, LanguageHandler.ERROR_NEEDMONEY.split("\\{AMOUNT\\}")[1])).build());
				return CommandResult.success();
			}

			TransactionResult result = optAccount.get().withdraw(TownyPlugin.getEcoService().getDefaultCurrency(), creationPrice, TownyPlugin.getCause());

			if (result.getResult() == ResultType.ACCOUNT_NO_FUNDS) {
				src.sendMessage(Text.builder()
						.append(Text.of(TextColors.RED, LanguageHandler.ERROR_NEEDMONEY.split("\\{AMOUNT\\}")[0]))
						.append(Utils.formatPrice(TextColors.RED, creationPrice))
						.append(Text.of(TextColors.RED, LanguageHandler.ERROR_NEEDMONEY.split("\\{AMOUNT\\}")[1])).build());
				return CommandResult.success();
			} else if (result.getResult() != ResultType.SUCCESS) {
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_ECOTRANSACTION));
				return CommandResult.success();
			}

			Towny towny = new Towny(UUID.randomUUID(), townyName);
			towny.addCitizen(player.getUniqueId());
			towny.setPresident(player.getUniqueId());

			Optional<Account> optTownyAccount = TownyPlugin.getEcoService().getOrCreateAccount("towny-" + towny.getUUID().toString());

			if (!optTownyAccount.isPresent()) {
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_CREATEECOTOWN));
				TownyPlugin.getLogger().error("Could not create towny's account on the economy service !");
				return CommandResult.success();
			}

			if (optTownyAccount.get().setBalance(TownyPlugin.getEcoService().getDefaultCurrency(), BigDecimal.ZERO, TownyPlugin.getCause()).getResult() != ResultType.SUCCESS) {
				src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_CREATEECOTOWN));
				TownyPlugin.getLogger().error("Could not fund towny's account on the economy service !");
				return CommandResult.success();
			}


			DataHandler.addTowny(towny);

			TownyClaimExecutor claimExecutor = new TownyClaimExecutor();
			String claimReason = claimExecutor.claimLandMessages(player, towny);


			if (claimReason == null) {
				// The claim was successful
			} else {
				// Execute "/town leave" command for the player
				Sponge.getCommandManager().process(src, "towny leave");

				Sponge.getCommandManager().process(src, "cc");
				// Delay the execution of "cc" commands by 5 seconds
				Sponge.getScheduler().createTaskBuilder()
						.delayTicks(1 * 3) // (1 second = 20 ticks)
						.execute(() -> {
							// Execute "cc" commands
							Sponge.getCommandManager().process(src, "cc");
							Sponge.getCommandManager().process(src, "cc");
							Sponge.getCommandManager().process(src, "cc");
							Sponge.getCommandManager().process(src, "cc");
							Sponge.getCommandManager().process(src, "cc");
							// The claim failed
							player.sendMessage(Text.of(TextColors.RED, claimReason));
						})
						.submit(TownyPlugin.getInstance());


				// Pay the player with the specified amount (creationPrice)
				Sponge.getCommandManager().process(Sponge.getServer().getConsole(), "eco add " + src.getName() + " " + creationPrice);

			}

			MessageChannel.TO_ALL.send(Text.of(TextColors.AQUA, LanguageHandler.INFO_NEWTOWNANNOUNCE
					.replaceAll("\\{PLAYER\\}", player.getName())
					.replaceAll("\\{TOWN\\}", towny.getName())));

			src.sendMessage(Text.of(TextColors.GREEN, LanguageHandler.INFO_NEWTOWN.replaceAll("\\{TOWN\\}", towny.getName())));
		} else {
			src.sendMessage(Text.of(TextColors.RED, LanguageHandler.ERROR_NOPLAYER));
		}

		return CommandResult.success();
	}
}
