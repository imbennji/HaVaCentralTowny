package com.arckenver.towny.listener;

import java.util.Optional;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.event.message.MessageEvent.MessageFormatter;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.serializer.TextSerializers;

import com.arckenver.towny.ConfigHandler;
import com.arckenver.towny.DataHandler;
import com.arckenver.towny.channel.TownyMessageChannel;
import com.arckenver.towny.object.Towny;
import com.arckenver.towny.LanguageHandler;

public class ChatListener
{

	@Listener(order = Order.LATE)
	public void onPlayerChat(MessageChannelEvent.Chat e, @First Player p)
	{
		Towny towny = DataHandler.getTownyOfPlayer(p.getUniqueId());
		if (towny == null)
		{
			return;
		}
		MessageChannel chan = MessageChannel.TO_ALL;
		Optional<MessageChannel> channel = e.getChannel();
		if (channel.isPresent())
		{
			chan = channel.get();
		}
		
		MessageFormatter formater = e.getFormatter();
		
		if (chan.equals(MessageChannel.TO_ALL) && ConfigHandler.getNode("others", "enableTownyTag").getBoolean(true))
		{
			e.setMessage(Text.of(TextSerializers.FORMATTING_CODE.deserialize(ConfigHandler.getNode("others", "publicChatFormat").getString().replaceAll("\\{TOWN\\}", towny.getTag()).replaceAll("\\{TITLE\\}", DataHandler.getCitizenTitle(p.getUniqueId()))), formater.getHeader().toText()), formater.getBody().toText());
		}
		else if (chan instanceof TownyMessageChannel)
		{
			e.setMessage(Text.of(TextSerializers.FORMATTING_CODE.deserialize(ConfigHandler.getNode("others", "townyChatFormat").getString().replaceAll("\\{TOWN\\}", towny.getTag()).replaceAll("\\{TITLE\\}", DataHandler.getCitizenTitle(p.getUniqueId()))), formater.getHeader().toText()), Text.of(LanguageHandler.colorYellow(), formater.getBody().toText()));
			DataHandler.getSpyChannel().send(p, Text.of(TextSerializers.FORMATTING_CODE.deserialize(ConfigHandler.getNode("others", "townySpyChatTag").getString()), LanguageHandler.colorReset(), e.getMessage()));
		}
	}
}
