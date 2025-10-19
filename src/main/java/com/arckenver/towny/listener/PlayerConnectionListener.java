package com.arckenver.towny.listener;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.object.Towny;

import java.util.Objects;
import java.util.Optional;

public class PlayerConnectionListener
{
	@Listener
        public void onPlayerJoin(ClientConnectionEvent.Join event)
        {
                event.getTargetEntity();
                Player player = event.getTargetEntity();
                DataHandler.markResidentLogin(player.getUniqueId(), player.getName());
                Towny towny = DataHandler.getTownyOfPlayer(player.getUniqueId());
                if (towny != null)
                        towny.getMessageChannel().addMember(player);

                if (DataHandler.tryReleaseResidentFromJail(player.getUniqueId())) {
                        player.sendMessage(Text.of(LanguageHandler.colorGreen(), LanguageHandler.INFO_JAIL_RELEASE));
                }
                Optional<Location<World>> jailLocation = DataHandler.getResidentJailLocation(player.getUniqueId());
                if (jailLocation.isPresent()) {
                        player.setLocation(jailLocation.get());
                        String townName = DataHandler.getResidentJailTown(player.getUniqueId())
                                        .map(DataHandler::getTowny)
                                        .filter(Objects::nonNull)
                                        .map(Towny::getDisplayName)
                                        .orElse(LanguageHandler.FORMAT_UNKNOWN);
                        player.sendMessage(Text.of(LanguageHandler.colorRed(), LanguageHandler.INFO_JAIL_TELEPORT.replace("{TOWN}", townName)));
                }

                player.setMessageChannel(MessageChannel.TO_ALL);
                if (player.hasPermission("towny.admin.spychat"))
			DataHandler.getSpyChannel().addMember(player);
		Towny my = DataHandler.getTownyOfPlayer(player.getUniqueId());
		if (my != null && !my.getBoard().isEmpty()) {
			player.sendMessage(Text.of(LanguageHandler.colorAqua(), "Town Board: ", LanguageHandler.colorWhite(), my.getBoard()));
		}
	}

	@Listener
        public void onPlayerLeave(ClientConnectionEvent.Disconnect event)
        {
                event.getTargetEntity();
                Player player = event.getTargetEntity();
                DataHandler.markResidentLogout(player.getUniqueId());
                DataHandler.removeFirstPoint(player.getUniqueId());
                DataHandler.removeSecondPoint(player.getUniqueId());
                Towny towny = DataHandler.getTownyOfPlayer(player.getUniqueId());
                if (towny != null)
                        towny.getMessageChannel().removeMember(player);
		DataHandler.getSpyChannel().removeMember(player);
	}
}
