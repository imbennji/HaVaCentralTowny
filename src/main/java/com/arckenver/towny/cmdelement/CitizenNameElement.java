package com.arckenver.towny.cmdelement;

import java.util.Collections;
import java.util.stream.Collectors;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.PatternMatchingCommandElement;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import com.arckenver.towny.DataHandler;
import com.arckenver.towny.object.Towny;

public class CitizenNameElement extends PatternMatchingCommandElement
{
	public CitizenNameElement(Text key)
	{
		super(key);
	}
	
	@Override
	protected Iterable<String> getChoices(CommandSource src)
	{
		if (!(src instanceof Player))
		{
			return Collections.emptyList();
		}
		Player player = (Player) src;
		Towny towny = DataHandler.getTownyOfPlayer(player.getUniqueId());
		if (towny == null)
		{
			return Collections.emptyList();
		}
		return towny
				.getCitizens()
				.stream()
				.map(uuid -> DataHandler.getPlayerName(uuid))
				.collect(Collectors.toList());
	}

	@Override
	protected Object getValue(String choice) throws IllegalArgumentException
	{
		return choice;
	}

	public Text getUsage(CommandSource src)
	{
		return Text.EMPTY;
	}
}
