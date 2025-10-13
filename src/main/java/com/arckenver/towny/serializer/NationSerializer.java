package com.arckenver.towny.serializer;

import com.arckenver.towny.object.Nation;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.lang.reflect.Type;
import java.util.UUID;

public class NationSerializer implements JsonSerializer<Nation> {
    @Override
    public JsonElement serialize(Nation nation, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject json = new JsonObject();
        json.add("uuid", new JsonPrimitive(nation.getUUID().toString()));
        json.add("name", new JsonPrimitive(nation.getRealName()));
        if (nation.hasTag()) {
            json.add("tag", new JsonPrimitive(nation.getTag()));
        }
        json.add("board", new JsonPrimitive(nation.getBoard()));
        if (nation.getCapital() != null) {
            json.add("capital", new JsonPrimitive(nation.getCapital().toString()));
        }
        if (nation.getKing() != null) {
            json.add("king", new JsonPrimitive(nation.getKing().toString()));
        }
        json.add("open", new JsonPrimitive(nation.isOpen()));
        json.add("neutral", new JsonPrimitive(nation.isNeutral()));
        json.add("public", new JsonPrimitive(nation.isPublic()));
        json.add("taxes", new JsonPrimitive(nation.getTaxes()));
        json.add("taxPercentage", new JsonPrimitive(nation.isTaxPercentage()));
        json.add("spawnCost", new JsonPrimitive(nation.getSpawnCost()));
        json.add("government", new JsonPrimitive(nation.getGovernment().name().toLowerCase()));

        Location<World> spawn = nation.getSpawn();
        if (spawn != null) {
            JsonObject spawnObj = new JsonObject();
            spawnObj.add("world", new JsonPrimitive(spawn.getExtent().getUniqueId().toString()));
            spawnObj.add("x", new JsonPrimitive(spawn.getX()));
            spawnObj.add("y", new JsonPrimitive(spawn.getY()));
            spawnObj.add("z", new JsonPrimitive(spawn.getZ()));
            json.add("spawn", spawnObj);
        }

        JsonArray towns = new JsonArray();
        for (UUID town : nation.getTowns()) {
            towns.add(new JsonPrimitive(town.toString()));
        }
        json.add("towns", towns);

        JsonArray allies = new JsonArray();
        for (UUID ally : nation.getAllies()) {
            allies.add(new JsonPrimitive(ally.toString()));
        }
        json.add("allies", allies);

        JsonArray enemies = new JsonArray();
        for (UUID enemy : nation.getEnemies()) {
            enemies.add(new JsonPrimitive(enemy.toString()));
        }
        json.add("enemies", enemies);

        JsonArray assistants = new JsonArray();
        for (UUID assistant : nation.getAssistants()) {
            assistants.add(new JsonPrimitive(assistant.toString()));
        }
        json.add("assistants", assistants);
        return json;
    }
}
