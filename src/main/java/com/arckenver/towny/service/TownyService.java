package com.arckenver.towny.service;

import java.util.Optional;
import java.util.UUID;

import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.arckenver.towny.DataHandler;
import com.arckenver.towny.object.Towny;

public class TownyService
{
	public Optional<String> getTownyNameOfPlayer(UUID uuid)
	{
		Towny towny = DataHandler.getTownyOfPlayer(uuid);
		if (towny == null)
		{
			return Optional.empty();
		}
		return Optional.of(towny.getRealName());
	}
	
	public Optional<String> getTownyNameAtLocation(Location<World> loc)
	{
		Towny towny = DataHandler.getTowny(loc);
		if (towny == null)
		{
			return Optional.empty();
		}
		return Optional.of(towny.getRealName());
	}
	
	public boolean hasTowny(UUID uuid)
	{
		return DataHandler.getTownyOfPlayer(uuid) != null;
	}
	
	public boolean isPresident(UUID uuid)
	{
		Towny towny = DataHandler.getTownyOfPlayer(uuid);
		if (towny == null)
		{
			return false;
		}
		return towny.isPresident(uuid);
	}
	
	public boolean isMinister(UUID uuid)
	{
		Towny towny = DataHandler.getTownyOfPlayer(uuid);
		if (towny == null)
		{
			return false;
		}
		return towny.isMinister(uuid);
	}
}
