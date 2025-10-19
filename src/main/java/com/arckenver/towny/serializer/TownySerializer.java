package com.arckenver.towny.serializer;

import java.lang.reflect.Type;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.UUID;

import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.arckenver.towny.object.Towny;
import com.arckenver.towny.object.Rect;
import com.arckenver.towny.object.Plot;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class TownySerializer implements JsonSerializer<Towny> {
	@Override
	public JsonElement serialize(Towny towny, Type type, JsonSerializationContext ctx) {
		JsonObject json = new JsonObject();

		json.add("uuid", new JsonPrimitive(towny.getUUID().toString()));
		json.add("name", new JsonPrimitive(towny.getRealName()));
		json.add("admin", new JsonPrimitive(towny.isAdmin()));
		json.add("rentInterval", new JsonPrimitive(towny.getRentInterval()));
		json.add("lastRentCollectYear", new JsonPrimitive(towny.getLastRentCollectTime().getYear()));
		json.add("lastRentCollectDay", new JsonPrimitive(towny.getLastRentCollectTime().getDayOfYear()));
		json.add("lastRentCollectHour", new JsonPrimitive(towny.getLastRentCollectTime().getHour()));

		// NEW: persist town board (always present; empty string if none)
		json.add("board", new JsonPrimitive(towny.getBoard()));

		if (!towny.isAdmin()) {
			json.add("taxes", new JsonPrimitive(towny.getTaxes()));
			json.add("extras", new JsonPrimitive(towny.getExtras()));
			json.add("extraspawns", new JsonPrimitive(towny.getExtraSpawns()));
		}

		if (towny.hasTag()) {
			json.add("tag", new JsonPrimitive(towny.getTag()));
		}

		if (towny.hasDisplayName()) {
			json.add("displayname", new JsonPrimitive(towny.getDisplayName()));
		}

		JsonObject flags = new JsonObject();
		for (Entry<String, Boolean> e : towny.getFlags().entrySet()) {
			flags.add(e.getKey(), new JsonPrimitive(e.getValue()));
		}
		json.add("flags", flags);

		JsonObject perms = new JsonObject();
		for (Entry<String, Hashtable<String, Boolean>> e : towny.getPerms().entrySet()) {
			JsonObject obj = new JsonObject();
			for (Entry<String, Boolean> en : e.getValue().entrySet()) {
				obj.add(en.getKey(), new JsonPrimitive(en.getValue()));
			}
			perms.add(e.getKey(), obj);
		}
		json.add("perms", perms);

		JsonArray rectArray = new JsonArray();
		for (Rect r : towny.getRegion().getRects()) {
			JsonObject rectJson = new JsonObject();
			rectJson.add("world", new JsonPrimitive(r.getWorld().toString()));
			rectJson.add("minX", new JsonPrimitive(r.getMinX()));
			rectJson.add("maxX", new JsonPrimitive(r.getMaxX()));
			rectJson.add("minY", new JsonPrimitive(r.getMinY()));
			rectJson.add("maxY", new JsonPrimitive(r.getMaxY()));
			rectArray.add(rectJson);
		}
		json.add("rects", rectArray);

		JsonArray plotsArray = new JsonArray();
		for (Plot plot : towny.getPlots().values()) {
			JsonObject plotObj = new JsonObject();

			plotObj.add("uuid", new JsonPrimitive(plot.getUUID().toString()));
			if (plot.isNamed())
				plotObj.add("name", new JsonPrimitive(plot.getRealName()));
                        if (plot.hasDisplayName())
                                plotObj.add("displayname", new JsonPrimitive(plot.getDisplayName()));

                        plotObj.add("type", new JsonPrimitive(plot.getTypeId()));

                        JsonObject rectJson = new JsonObject();
                        rectJson.add("world", new JsonPrimitive(plot.getRect().getWorld().toString()));
			rectJson.add("minX", new JsonPrimitive(plot.getRect().getMinX()));
			rectJson.add("maxX", new JsonPrimitive(plot.getRect().getMaxX()));
			rectJson.add("minY", new JsonPrimitive(plot.getRect().getMinY()));
			rectJson.add("maxY", new JsonPrimitive(plot.getRect().getMaxY()));
			plotObj.add("rect", rectJson);

			if (plot.getOwner() != null) {
				plotObj.add("owner", new JsonPrimitive(plot.getOwner().toString()));
			}

			JsonArray coownersArray = new JsonArray();
			for (UUID coowner : plot.getCoowners()) {
				coownersArray.add(new JsonPrimitive(coowner.toString()));
			}
			plotObj.add("coowners", coownersArray);

			JsonObject plotFlags = new JsonObject();
			for (Entry<String, Boolean> e : plot.getFlags().entrySet()) {
				plotFlags.add(e.getKey(), new JsonPrimitive(e.getValue()));
			}
			plotObj.add("flags", plotFlags);

			JsonObject plotPerms = new JsonObject();
			for (Entry<String, Hashtable<String, Boolean>> e : plot.getPerms().entrySet()) {
				JsonObject obj = new JsonObject();
				for (Entry<String, Boolean> en : e.getValue().entrySet()) {
					obj.add(en.getKey(), new JsonPrimitive(en.getValue()));
				}
				plotPerms.add(e.getKey(), obj);
			}
			plotObj.add("perms", plotPerms);

			if (plot.isForSale()) {
				plotObj.add("price", new JsonPrimitive(plot.getPrice()));
			}
			if (plot.isForRent()) {
				plotObj.add("rentalPrice", new JsonPrimitive(plot.getRentalPrice()));
			}

			plotsArray.add(plotObj);
		}
		json.add("plots", plotsArray);

		JsonObject spawns = new JsonObject();
		for (Entry<String, Location<World>> e : towny.getSpawns().entrySet()) {
			JsonObject loc = new JsonObject();
			loc.add("world", new JsonPrimitive(e.getValue().getExtent().getUniqueId().toString()));
			loc.add("x", new JsonPrimitive(e.getValue().getX()));
			loc.add("y", new JsonPrimitive(e.getValue().getY()));
			loc.add("z", new JsonPrimitive(e.getValue().getZ()));

			spawns.add(e.getKey(), loc);
		}
		json.add("spawns", spawns);

                if (towny.getPresident() != null)
                        json.add("mayor", new JsonPrimitive(towny.getPresident().toString()));

                JsonArray comayorArray = new JsonArray();
                for (UUID minister : towny.getMinisters()) {
			comayorArray.add(new JsonPrimitive(minister.toString()));
		}
		json.add("comayor", comayorArray);

		JsonArray citizensArray = new JsonArray();
                for (UUID citizen : towny.getCitizens()) {
                        citizensArray.add(new JsonPrimitive(citizen.toString()));
                }
                json.add("citizens", citizensArray);

		JsonArray outlawsArray = new JsonArray();
                for (UUID outlaw : towny.getOutlaws()) {
                        outlawsArray.add(new JsonPrimitive(outlaw.toString()));
                }
                json.add("outlaws", outlawsArray);

                if (towny.hasNation()) {
                        json.add("nation", new JsonPrimitive(towny.getNationUUID().toString()));
                }

                return json;
        }
}
