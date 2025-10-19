package com.arckenver.towny.listener;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.living.humanoid.player.RespawnPlayerEvent;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.arckenver.towny.DataHandler;
import com.arckenver.towny.LanguageHandler;
import com.arckenver.towny.TownyPlugin;
import com.arckenver.towny.object.Towny;

public class JailListener {
        @Listener
        public void onRespawn(RespawnPlayerEvent event) {
                Player player = event.getTargetEntity();
                UUID id = player.getUniqueId();

                if (DataHandler.tryReleaseResidentFromJail(id)) {
                        player.sendMessage(Text.of(LanguageHandler.colorGreen(), LanguageHandler.INFO_JAIL_RELEASE));
                        return;
                }

                Optional<Location<World>> jailLocation = DataHandler.getResidentJailLocation(id);
                if (jailLocation.isPresent()) {
                        Location<World> destination = jailLocation.get();
                        event.setToTransform(event.getToTransform().setLocation(destination));
                        scheduleTeleport(id, destination);
                        String townName = DataHandler.getResidentJailTown(id)
                                        .map(DataHandler::getTowny)
                                        .filter(Objects::nonNull)
                                        .map(Towny::getDisplayName)
                                        .orElse(LanguageHandler.FORMAT_UNKNOWN);
                        player.sendMessage(Text.of(LanguageHandler.colorRed(),
                                        LanguageHandler.INFO_JAIL_TELEPORT.replace("{TOWN}", townName)));
                        return;
                }

                Location<World> deathLocation = event.getFromTransform().getLocation();
                Towny deathTown = DataHandler.getTowny(deathLocation);
                if (deathTown != null && DataHandler.isTownOutlaw(deathTown.getUUID(), id)) {
                        Location<World> spawn = deathLocation.getExtent().getSpawnLocation();
                        event.setToTransform(event.getToTransform().setLocation(spawn));
                        scheduleTeleport(id, spawn);
                        player.sendMessage(Text.of(LanguageHandler.colorRed(),
                                        LanguageHandler.INFO_OUTLAW_RESPAWN.replace("{TOWN}", deathTown.getDisplayName())));
                }
        }

        private void scheduleTeleport(UUID id, Location<World> destination) {
                Task.builder()
                                .delayTicks(1)
                                .execute(() -> Sponge.getServer().getPlayer(id)
                                                .ifPresent(p -> p.setLocation(destination)))
                                .submit(TownyPlugin.getInstance());
        }
}
