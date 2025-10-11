package com.arckenver.towny.task;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.format.TextColors;

import com.arckenver.towny.ConfigHandler;
import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.TownyPlugin;
import com.arckenver.towny.Utils;             // <-- NEW (for formatPricePlain)
import com.arckenver.towny.object.Towny;
import com.arckenver.towny.object.Plot;       // <-- ensure this import exists

public class TaxesCollectRunnable implements Runnable
{
	public void run()
	{
		final double upkeepPerCitizen = ConfigHandler.getNode("prices").getNode("upkeepPerCitizen").getDouble(0D);
		final boolean allTownTaxesZero = DataHandler.getTowny().values().stream().allMatch(n -> n.getTaxes() == 0D);
		final double cfgPlotTax = ConfigHandler.getNode("prices").getNode("plotTax").getDouble(0D);

		// nothing to do?
		if (upkeepPerCitizen == 0D && allTownTaxesZero && cfgPlotTax == 0D) {
			return;
		}

		if (TownyPlugin.getEcoService() == null)
		{
			TownyPlugin.getLogger().error(LanguageHandler.ERROR_NOECO);
			return;
		}

		EventContext context = EventContext.builder().build();
		Cause cause = Cause.builder()
				.append(Sponge.getServer().getConsole())
				.append(TownyPlugin.getInstance())
				.build(context);

		Text upkeepAnnounceMassage = Text.of(TextColors.AQUA, LanguageHandler.INFO_UPKEEPANNOUNCE);

		ArrayList<UUID> townyToRemove = new ArrayList<>();

		for (Towny towny : DataHandler.getTowny().values())
		{
			if (towny.isAdmin()) continue;

			Optional<Account> optAccount = TownyPlugin.getEcoService().getOrCreateAccount("towny-" + towny.getUUID().toString());
			if (!optAccount.isPresent())
			{
				TownyPlugin.getLogger().error("Towny " + towny.getName() + " doesn't have an account on the economy plugin of this server");
				continue;
			}

			// --- RESIDENT TAXES (head tax) ---
			BigDecimal taxes = BigDecimal.valueOf(towny.getTaxes());
			ArrayList<UUID> citizensToRemove = new ArrayList<>();

			UserStorageService userStorage = Sponge.getServiceManager().provide(UserStorageService.class).get();

			for (UUID uuid : towny.getCitizens())
			{
				Optional<User> user = userStorage.get(uuid);
				Sponge.getServer().getPlayer(uuid).ifPresent(p -> p.sendMessage(upkeepAnnounceMassage));

				// exemptions and staff skip
				if (user.isPresent() && user.get().hasPermission("towny.admin.towny.exempt")) continue;
				if (towny.isStaff(uuid)) continue;

				Optional<UniqueAccount> optCitizenAccount = TownyPlugin.getEcoService().getOrCreateAccount(uuid);
				if (!optCitizenAccount.isPresent()) {
					TownyPlugin.getLogger().error("Player " + uuid + " has no economy account for taxes.");
					continue;
				}

				TransactionResult result = optCitizenAccount.get().transfer(
						optAccount.get(),
						TownyPlugin.getEcoService().getDefaultCurrency(),
						taxes,
						cause
				);

				if (result.getResult() == ResultType.ACCOUNT_NO_FUNDS)
				{
					citizensToRemove.add(uuid);
					Sponge.getServer().getPlayer(uuid).ifPresent(p ->
							p.sendMessage(Text.of(TextColors.RED, LanguageHandler.INFO_KICKUPKEEP)));
				}
				else if (result.getResult() != ResultType.SUCCESS)
				{
					MessageChannel.TO_CONSOLE.send(Text.of("Something bad happened: ", result.getResult()));
					TownyPlugin.getLogger().error("Error while taking taxes from player " + uuid.toString() + " for towny " + towny.getName());
				}
			}

			for (UUID uuid : citizensToRemove)
			{
				MessageChannel.TO_CONSOLE.send(Text.of("Removing player for towny (no funds): ", uuid));
				towny.removeCitizen(uuid);
			}

			// --- PLOT TAXES (flat per owned plot) ---
			final BigDecimal perPlotTax = BigDecimal.valueOf(cfgPlotTax);
			BigDecimal plotTaxTotal = BigDecimal.ZERO;
			int plotsTaxed = 0;

			if (perPlotTax.signum() > 0)
			{
				ArrayList<Plot> plotsToForfeit = new ArrayList<>();

				for (Plot plot : towny.getPlots().values()) {
					final UUID owner = plot.getOwner();
					if (owner == null) continue;

					Optional<UniqueAccount> optOwnerAcc = TownyPlugin.getEcoService().getOrCreateAccount(owner);
					if (!optOwnerAcc.isPresent()) {
						TownyPlugin.getLogger().error("No account for owner " + owner + " while collecting plot tax.");
						continue;
					}

					TransactionResult tr = optOwnerAcc.get().transfer(
							optAccount.get(),
							TownyPlugin.getEcoService().getDefaultCurrency(),
							perPlotTax,
							cause
					);

					if (tr.getResult() == ResultType.SUCCESS) {
						plotsTaxed++;
						plotTaxTotal = plotTaxTotal.add(perPlotTax);

						// Tell online owner about the plot tax payment
						Sponge.getServer().getPlayer(owner).ifPresent(p ->
								p.sendMessage(Text.of(TextColors.GREEN,
										LanguageHandler.INFO_PLOT_TAX_PAID
												.replace("{AMOUNT}", Utils.formatPricePlain(perPlotTax))
												.replace("{PLOT}", plot.getDisplayName())
												.replace("{TOWN}", towny.getDisplayName())
								)));
					} else if (tr.getResult() == ResultType.ACCOUNT_NO_FUNDS) {
						plotsToForfeit.add(plot);

						// Owner failed to pay: notify if online
						Sponge.getServer().getPlayer(owner).ifPresent(p ->
								p.sendMessage(Text.of(TextColors.RED,
										LanguageHandler.INFO_PLOTTAX_FAIL
												.replace("{PLOT}", plot.getDisplayName())
								)));
					} else {
						MessageChannel.TO_CONSOLE.send(Text.of("Plot tax error: ", tr.getResult()));
						TownyPlugin.getLogger().error("Error taking plot tax for plot " + plot.getName()
								+ " in town " + towny.getName());
					}
				}

				// Forfeit failed plots (make them unowned)
				for (Plot plot : plotsToForfeit) {
					plot.setOwner(null);
					// Optional: clear auxiliaries like coowners/for-sale/rent if desired.
				}

				// Town staff summary if anything was collected
				if (plotsTaxed > 0) {
					String sum = LanguageHandler.INFO_PLOT_TAX_TOWN_SUMMARY
							.replace("{AMOUNT}", Utils.formatPricePlain(plotTaxTotal))
							.replace("{COUNT}", String.valueOf(plotsTaxed));

					// Mayor
					UUID mayor = towny.getPresident();
					if (mayor != null) {
						Sponge.getServer().getPlayer(mayor).ifPresent(p -> p.sendMessage(Text.of(TextColors.AQUA, sum)));
					}
					// Ministers
					for (UUID m : towny.getMinisters()) {
						Sponge.getServer().getPlayer(m).ifPresent(p -> p.sendMessage(Text.of(TextColors.AQUA, sum)));
					}
				}
			}

			// --- TOWN UPKEEP ---
			BigDecimal upkeep = BigDecimal.valueOf(towny.getUpkeep());
			TransactionResult result = optAccount.get().withdraw(
					TownyPlugin.getEcoService().getDefaultCurrency(),
					upkeep,
					cause);

			if (result.getResult() == ResultType.ACCOUNT_NO_FUNDS)
			{
				townyToRemove.add(towny.getUUID());
			}
			else if (result.getResult() != ResultType.SUCCESS)
			{
				MessageChannel.TO_CONSOLE.send(Text.of("Error occured: ", result.getResult()));
				TownyPlugin.getLogger().error("Error while taking upkeep from towny " + towny.getName());
			}
		}

		for (UUID uuid : townyToRemove)
		{
			String name = DataHandler.getTowny(uuid).getName();
			DataHandler.removeTowny(uuid);
			MessageChannel.TO_ALL.send(Text.of(TextColors.RED, LanguageHandler.INFO_TOWNFAILUPKEEP.replace("{TOWN}", name)));
		}
	}
}
