package com.arckenver.towny.listener;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.arckenver.towny.ConfigHandler;
import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.Utils;
import com.arckenver.towny.object.Towny;
import com.arckenver.towny.object.Plot;

public class PlayerMoveListener
{
	@Listener
	public void onPlayerMove(MoveEntityEvent event, @First Player player)
	{
		// only react to X/Z changes (chunk-ish movement)
		if (event.getFromTransform().getLocation().getBlockX() == event.getToTransform().getLocation().getBlockX() &&
				event.getFromTransform().getLocation().getBlockZ() == event.getToTransform().getLocation().getBlockZ())
		{
			return;
		}

		if (!ConfigHandler.getNode("worlds")
				.getNode(event.getToTransform().getExtent().getName())
				.getNode("enabled").getBoolean())
		{
			return;
		}

		Location<World> loc = event.getToTransform().getLocation();
		Towny towny = DataHandler.getTowny(loc);
		Towny lastTownyWalkedOn = DataHandler.getLastTownyWalkedOn(player.getUniqueId());

		Plot plot = (towny == null) ? null : towny.getPlot(loc);
		Plot lastPlotWalkedOn = DataHandler.getLastPlotWalkedOn(player.getUniqueId());

		// if neither the town nor the plot changed, ignore
		if ((towny == null && lastTownyWalkedOn == null) ||
				(towny != null && lastTownyWalkedOn != null && towny.getUUID().equals(lastTownyWalkedOn.getUUID())))
		{
			if ((plot == null && lastPlotWalkedOn == null) ||
					(plot != null && lastPlotWalkedOn != null && plot.getUUID().equals(lastPlotWalkedOn.getUUID())))
			{
				return;
			}
		}

		// update last stepped
		DataHandler.setLastTownyWalkedOn(player.getUniqueId(), towny);
		DataHandler.setLastPlotWalkedOn(player.getUniqueId(), plot);

		// choose toast template
		String toast;
		if (towny == null) {
			toast = ConfigHandler.getNode("toast", "wild").getString();
		} else {
			toast = (plot == null
					? ConfigHandler.getNode("toast", "towny").getString()
					: ConfigHandler.getNode("toast", "plot").getString());
		}

		// --- Auto-map (resident toggle) ---
		if (player != null && com.arckenver.towny.DataHandler.isResidentAutoMap(player.getUniqueId())) {
			// Only when chunk changed:
			org.spongepowered.api.world.Location<org.spongepowered.api.world.World> from = event.getFromTransform().getLocation();
			org.spongepowered.api.world.Location<org.spongepowered.api.world.World> to   = event.getToTransform().getLocation();
			if (!from.getChunkPosition().equals(to.getChunkPosition())) {

				long now = System.currentTimeMillis();
				long last = com.arckenver.towny.DataHandler.getResidentLastMap(player.getUniqueId());
				if (now - last >= 700) { // 0.7s throttle
					com.arckenver.towny.DataHandler.setResidentLastMap(player.getUniqueId(), now);

					// Reuse your TownMapExecutor renderer with a compact radius (avoid large packets)
					com.arckenver.towny.cmdexecutor.towny.TownMapExecutor renderer =
							new com.arckenver.towny.cmdexecutor.towny.TownMapExecutor();

					// expose a small helper in TownMapExecutor: renderFor(Player p, int radius) -> Text
					org.spongepowered.api.text.Text map = renderer.renderFor(player, 4); // 9x9
					if (map != null) {
						player.sendMessage(map);
					}
				}
			}
		}


		// president format
		String formatPresident = "";
		if (towny != null && !towny.isAdmin()) {
			formatPresident = ConfigHandler.getNode("toast", "formatPresident").getString()
					.replaceAll("\\{TITLE\\}", DataHandler.getCitizenTitle(towny.getPresident()))
					.replaceAll("\\{NAME\\}", DataHandler.getPlayerName(towny.getPresident()));
		}

		// plot formats
		String formatPlotName = "";
		String formatPlotOwner = "";
		String formatPlotPrice = "";

		if (plot != null) {
			if (plot.hasDisplayName())
				formatPlotName = ConfigHandler.getNode("toast", "formatPlotName").getString()
						.replaceAll("\\{ARG\\}", plot.getDisplayName()) + " ";
			else if (plot.isNamed())
				formatPlotName = ConfigHandler.getNode("toast", "formatPlotName").getString()
						.replaceAll("\\{ARG\\}", plot.getName()) + " ";

			if (plot.isOwned())
				formatPlotOwner = ConfigHandler.getNode("toast", "formatPlotOwner").getString()
						.replaceAll("\\{ARG\\}", DataHandler.getPlayerName(plot.getOwner())) + " ";

			if (plot.isForSale())
				formatPlotPrice = ConfigHandler.getNode("toast", "formatPlotPrice").getString()
						.replaceAll("\\{ARG\\}", Utils.formatPricePlain(plot.getPrice())) + " ";
		}

		// pvp format
		String formatPvp = DataHandler.getFlag("pvp", loc)
				? ConfigHandler.getNode("toast", "formatPvp").getString().replaceAll("\\{ARG\\}", LanguageHandler.TOAST_PVP)
				: ConfigHandler.getNode("toast", "formatNoPvp").getString().replaceAll("\\{ARG\\}", LanguageHandler.TOAST_NOPVP);

		// town/wild token
		if (towny != null) {
			toast = toast.replaceAll("\\{TOWN\\}", towny.getDisplayName());
		} else {
			toast = toast.replaceAll("\\{WILD\\}", LanguageHandler.TOAST_WILDNAME);
		}

		// show board when entering a *different* town
		if (towny != null &&
				(lastTownyWalkedOn == null || !towny.getUUID().equals(lastTownyWalkedOn.getUUID())))
		{
			String board = towny.getBoard();
			if (board != null && !board.trim().isEmpty()) {
				// support & color codes
				player.sendMessage(Text.of(
						TextColors.AQUA, "Board [",
						TextColors.YELLOW, towny.getDisplayName(),
						TextColors.AQUA, "]: ",
						TextSerializers.FORMATTING_CODE.deserialize(board)
				));
			}
		}

		Text finalToast = TextSerializers.FORMATTING_CODE.deserialize(toast
				.replaceAll("\\{FORMATPRESIDENT\\}", formatPresident)
				.replaceAll("\\{FORMATPLOTNAME\\}", formatPlotName)
				.replaceAll("\\{FORMATPLOTOWNER\\}", formatPlotOwner)
				.replaceAll("\\{FORMATPLOTPRICE\\}", formatPlotPrice)
				.replaceAll("\\{FORMATPVP\\}", formatPvp));

		player.sendMessage(ChatTypes.ACTION_BAR, finalToast);
		MessageChannel.TO_CONSOLE.send(Text.of(player.getName(), " entered area ", finalToast));
	}
}
