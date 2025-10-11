package com.arckenver.towny.serializer;

import com.arckenver.towny.object.Nation;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.World;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.UUID;

public class NationDeserializer implements JsonDeserializer<Nation> {
    @Override
    public Nation deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        UUID uuid = UUID.fromString(obj.get("uuid").getAsString());
        String name = obj.get("name").getAsString();
        Nation nation = new Nation(uuid, name);

        if (obj.has("tag")) {
            nation.setTag(obj.get("tag").getAsString());
        }

        if (obj.has("board")) {
            nation.setBoard(obj.get("board").getAsString());
        }

        if (obj.has("capital")) {
            try {
                nation.setCapital(UUID.fromString(obj.get("capital").getAsString()));
            } catch (IllegalArgumentException ignored) {
                nation.setCapital(null);
            }
        }

        if (obj.has("open")) {
            nation.setOpen(obj.get("open").getAsBoolean());
        }

        if (obj.has("neutral")) {
            nation.setNeutral(obj.get("neutral").getAsBoolean());
        }

        if (obj.has("taxes")) {
            nation.setTaxes(obj.get("taxes").getAsDouble());
        }

        if (obj.has("spawn")) {
            JsonObject spawnObj = obj.get("spawn").getAsJsonObject();
            try {
                UUID worldUUID = UUID.fromString(spawnObj.get("world").getAsString());
                Optional<World> optWorld = Sponge.getServer().getWorld(worldUUID);
                if (optWorld.isPresent()) {
                    nation.setSpawn(optWorld.get().getLocation(
                            spawnObj.get("x").getAsDouble(),
                            spawnObj.get("y").getAsDouble(),
                            spawnObj.get("z").getAsDouble()));
                }
            } catch (Exception ignored) {
                nation.setSpawn(null);
            }
        }

        if (obj.has("towns")) {
            for (JsonElement element : obj.get("towns").getAsJsonArray()) {
                try {
                    nation.addTown(UUID.fromString(element.getAsString()));
                } catch (IllegalArgumentException ignored) {
                    // skip invalid entry
                }
            }
        }

        if (obj.has("allies")) {
            for (JsonElement element : obj.get("allies").getAsJsonArray()) {
                try {
                    nation.addAlly(UUID.fromString(element.getAsString()));
                } catch (IllegalArgumentException ignored) {
                    // skip invalid entry
                }
            }
        }

        if (obj.has("enemies")) {
            for (JsonElement element : obj.get("enemies").getAsJsonArray()) {
                try {
                    nation.addEnemy(UUID.fromString(element.getAsString()));
                } catch (IllegalArgumentException ignored) {
                    // skip invalid entry
                }
            }
        }

        return nation;
    }
}
