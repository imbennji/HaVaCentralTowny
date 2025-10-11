package com.arckenver.towny.object;

import java.util.Date;
import java.util.UUID;

public class Request
{
	private final UUID townyUUID;
	private final UUID playerUUID;
	private final Date date;

	public Request(UUID townyUUID, UUID playerUUID)
	{
		this.townyUUID = townyUUID;
		this.playerUUID = playerUUID;
		this.date = new Date();
	}

	public UUID getTownyUUID()
	{
		return townyUUID;
	}

	public UUID getPlayerUUID()
	{
		return playerUUID;
	}

	public Date getDate()
	{
		return date;
	}

	public boolean match(UUID townyUUID, UUID citizenUUID)
	{
		return (this.playerUUID.equals(citizenUUID) && this.townyUUID.equals(townyUUID));
	}
}
