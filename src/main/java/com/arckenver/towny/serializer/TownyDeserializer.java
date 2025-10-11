package com.arckenver.towny.serializer;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.World;

import com.arckenver.towny.object.Towny;
import com.arckenver.towny.object.Rect;
import com.arckenver.towny.object.Region;
import com.arckenver.towny.object.Plot;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class TownyDeserializer implements JsonDeserializer<Towny> {
	@Override
	public Towny deserialize(JsonElement json, Type type, JsonDeserializationContext ctx) throws JsonParseException {
		JsonObject obj = json.getAsJsonObject();
		UUID uuid = UUID.fromString(obj.get("uuid").getAsString());
		String name = obj.get("name").getAsString();
		boolean isAdmin = obj.get("admin").getAsBoolean();
		Towny towny = new Towny(uuid, name, isAdmin);

		// --- timing / rent ---
		if (obj.has("rentInterval"))
			towny.setRentInterval(obj.get("rentInterval").getAsInt());
		if (obj.has("lastRentCollectYear") && obj.has("lastRentCollectDay") && obj.has("lastRentCollectHour")) {
			int lastRCYear = obj.get("lastRentCollectYear").getAsInt();
			int lastRCDay = obj.get("lastRentCollectDay").getAsInt();
			int lastRCHour = obj.get("lastRentCollectHour").getAsInt();
			LocalDateTime lastRentCollect = LocalDateTime.of(LocalDate.ofYearDay(lastRCYear, lastRCDay), LocalTime.of(lastRCHour, 0));
			towny.setLastRentCollectTime(lastRentCollect);
		}

		// --- NEW: town board (defaults to empty string if absent) ---
		if (obj.has("board")) {
			towny.setBoard(obj.get("board").getAsString());
		} else {
			towny.setBoard("");
		}

		// --- misc strings ---
		if (obj.has("tag"))
			towny.setTag(obj.get("tag").getAsString());
		if (obj.has("displayname"))
			towny.setDisplayName(obj.get("displayname").getAsString());

		// --- flags ---
		if (obj.has("flags")) {
			for (Entry<String, JsonElement> e : obj.get("flags").getAsJsonObject().entrySet()) {
				towny.setFlag(e.getKey(), e.getValue().getAsBoolean());
			}
		}

		// --- perms ---
		if (obj.has("perms")) {
			for (Entry<String, JsonElement> e : obj.get("perms").getAsJsonObject().entrySet()) {
				for (Entry<String, JsonElement> en : e.getValue().getAsJsonObject().entrySet()) {
					towny.setPerm(e.getKey(), en.getKey(), en.getValue().getAsBoolean());
				}
			}
		}

		// --- region / rects ---
		Region region = new Region();
		if (obj.has("rects")) {
			for (JsonElement e : obj.get("rects").getAsJsonArray()) {
				JsonObject rectObj = e.getAsJsonObject();
				Rect rect = new Rect(
						UUID.fromString(rectObj.get("world").getAsString()),
						rectObj.get("minX").getAsInt(),
						rectObj.get("maxX").getAsInt(),
						rectObj.get("minY").getAsInt(),
						rectObj.get("maxY").getAsInt());
				region.addRect(rect);
			}
		}
		towny.setRegion(region);

		// --- plots ---
		if (obj.has("plots")) {
			for (JsonElement e : obj.get("plots").getAsJsonArray()) {
				JsonObject plotObj = e.getAsJsonObject();
				UUID plotUUID = UUID.fromString(plotObj.get("uuid").getAsString());

				String plotName = null;
				if (plotObj.has("name"))
					plotName = plotObj.get("name").getAsString();

				String plotDisplayName = null;
				if (plotObj.has("displayname"))
					plotDisplayName = plotObj.get("displayname").getAsString();

				JsonObject rectObj = plotObj.get("rect").getAsJsonObject();
				Rect rect = new Rect(
						UUID.fromString(rectObj.get("world").getAsString()),
						rectObj.get("minX").getAsInt(),
						rectObj.get("maxX").getAsInt(),
						rectObj.get("minY").getAsInt(),
						rectObj.get("maxY").getAsInt());

				Plot plot = new Plot(plotUUID, plotName, rect);
				plot.setDisplayName(plotDisplayName);

				if (plotObj.has("owner")) {
					plot.setOwner(UUID.fromString(plotObj.get("owner").getAsString()));
				}

				if (plotObj.has("coowners")) {
					for (JsonElement el : plotObj.get("coowners").getAsJsonArray()) {
						plot.addCoowner(UUID.fromString(el.getAsString()));
					}
				}

				if (plotObj.has("flags")) {
					for (Entry<String, JsonElement> en : plotObj.get("flags").getAsJsonObject().entrySet()) {
						plot.setFlag(en.getKey(), en.getValue().getAsBoolean());
					}
				}

				if (plotObj.has("perms")) {
					for (Entry<String, JsonElement> en : plotObj.get("perms").getAsJsonObject().entrySet()) {
						for (Entry<String, JsonElement> ent : en.getValue().getAsJsonObject().entrySet()) {
							plot.setPerm(en.getKey(), ent.getKey(), ent.getValue().getAsBoolean());
						}
					}
				}

				if (plotObj.has("price")) {
					plot.setPrice(plotObj.get("price").getAsBigDecimal());
				}
				if (plotObj.has("rentalPrice")) {
					plot.setRentalPrice(plotObj.get("rentalPrice").getAsBigDecimal());
				}

				towny.addPlot(plot);
			}
		}

		// --- spawns ---
		if (obj.has("spawns")) {
			for (Entry<String, JsonElement> e : obj.get("spawns").getAsJsonObject().entrySet()) {
				JsonObject spawnObj = e.getValue().getAsJsonObject();
				Optional<World> optWorld = Sponge.getServer().getWorld(UUID.fromString(spawnObj.get("world").getAsString()));
				if (optWorld.isPresent()) {
					towny.addSpawn(e.getKey(), optWorld.get().getLocation(
							spawnObj.get("x").getAsDouble(),
							spawnObj.get("y").getAsDouble(),
							spawnObj.get("z").getAsDouble()));
				}
			}
		}

		// --- non-admin data ---
                if (!isAdmin) {
                        if (obj.has("mayor"))
                                towny.setPresident(UUID.fromString(obj.get("mayor").getAsString()));

			if (obj.has("comayor")) {
				for (JsonElement element : obj.get("comayor").getAsJsonArray()) {
					towny.addMinister(UUID.fromString(element.getAsString()));
				}
			}

			if (obj.has("citizens")) {
				for (JsonElement element : obj.get("citizens").getAsJsonArray()) {
					towny.addCitizen(UUID.fromString(element.getAsString()));
				}
			}

			if (obj.has("taxes"))
				towny.setTaxes(obj.get("taxes").getAsDouble());
                        if (obj.has("extras"))
                                towny.setExtras(obj.get("extras").getAsInt());
                        if (obj.has("extraspawns"))
                                towny.setExtraSpawns(obj.get("extraspawns").getAsInt());
                }

                if (obj.has("nation")) {
                        try {
                                towny.setNationUUID(UUID.fromString(obj.get("nation").getAsString()));
                        } catch (IllegalArgumentException ignored) {
                                towny.clearNation();
                        }
                }
                return towny;
        }
}
