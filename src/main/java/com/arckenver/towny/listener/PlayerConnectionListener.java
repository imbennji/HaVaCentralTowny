package com.arckenver.towny.listener;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;

import com.arckenver.towny.DataHandler;
import com.arckenver.towny.object.Towny;
import org.spongepowered.api.text.format.TextColors;

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
                player.setMessageChannel(MessageChannel.TO_ALL);
                if (player.hasPermission("towny.admin.spychat"))
			DataHandler.getSpyChannel().addMember(player);
		Towny my = DataHandler.getTownyOfPlayer(player.getUniqueId());
		if (my != null && !my.getBoard().isEmpty()) {
			player.sendMessage(Text.of(TextColors.AQUA, "Town Board: ", TextColors.WHITE, my.getBoard()));
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
