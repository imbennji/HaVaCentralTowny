package com.arckenver.towny.cmdexecutor.resident;

import com.arckenver.towny.DataHandler;
import org.spongepowered.api.command.*;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.*;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.Locale;
import java.util.UUID;
import com.arckenver.towny.LanguageHandler;

public class ResidentToggleExecutor implements CommandExecutor {
    public static void create(CommandSpec.Builder root) {
        root.child(spec(), "toggle");
    }
    public static CommandSpec spec() {
        return CommandSpec.builder()
                .permission("towny.command.resident.toggle")
                .arguments(
                        GenericArguments.string(Text.of("toggle")),
                        GenericArguments.optional(GenericArguments.bool(Text.of("value")))
                )
                .executor(new ResidentToggleExecutor())
                .build();
    }

    @Override public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException {
        if (!(src instanceof Player)) throw new CommandException(Text.of(LanguageHandler.colorRed(), "Players only."));
        Player p = (Player) src;
        UUID id = p.getUniqueId();

        String key = ctx.<String>getOne("toggle").get().toLowerCase(Locale.ENGLISH);
        Boolean value = ctx.<Boolean>getOne("value").orElse(null);

        switch (key) {
            case "map": setBool(src, "Auto map", value, DataHandler.isResidentAutoMap(id), v -> DataHandler.setResidentAutoMap(id, v)); break;
            case "pvp": setBool(src, "PvP", value, DataHandler.getResidentPvp(id), v -> DataHandler.setResidentPvp(id, v)); break;
            case "fire": setBool(src, "Fire", value, DataHandler.getResidentFire(id), v -> DataHandler.setResidentFire(id, v)); break;
            case "explosion": setBool(src, "Explosion", value, DataHandler.getResidentExplosion(id), v -> DataHandler.setResidentExplosion(id, v)); break;
            case "mobs": setBool(src, "Mobs", value, DataHandler.getResidentMobs(id), v -> DataHandler.setResidentMobs(id, v)); break;

            case "townclaim": setBool(src, "Auto-claim", value, DataHandler.getResidentAutoClaim(id), v -> DataHandler.setResidentAutoClaim(id, v)); break;
            case "townunclaim": setBool(src, "Auto-unclaim", value, DataHandler.getResidentAutoUnclaim(id), v -> DataHandler.setResidentAutoUnclaim(id, v)); break;

            case "bedspawn": setBool(src, "Bed spawn", value, DataHandler.getResidentPreferBedSpawn(id), v -> DataHandler.setResidentPreferBedSpawn(id, v)); break;

            case "plotborder": setBool(src, "Plot border", value, DataHandler.getResidentPlotBorder(id), v -> DataHandler.setResidentPlotBorder(id, v)); break;
            case "constantplotborder": setBool(src, "Constant plot border", value, DataHandler.getResidentConstantPlotBorder(id), v -> DataHandler.setResidentConstantPlotBorder(id, v)); break;
            case "townborder": setBool(src, "Town border", value, DataHandler.getResidentTownBorder(id), v -> DataHandler.setResidentTownBorder(id, v)); break;

            case "bordertitles": setBool(src, "Border titles", value, DataHandler.getResidentBorderTitles(id), v -> DataHandler.setResidentBorderTitles(id, v)); break;

            case "plotgroup": setBool(src, "Plot group mode", value, DataHandler.getResidentPlotGroupMode(id), v -> DataHandler.setResidentPlotGroupMode(id, v)); break;
            case "district": setBool(src, "District mode", value, DataHandler.getResidentDistrictMode(id), v -> DataHandler.setResidentDistrictMode(id, v)); break;

            case "spy": setBool(src, "Spy", value, DataHandler.getResidentSpy(id), v -> DataHandler.setResidentSpy(id, v)); break;
            case "ignoreplots": setBool(src, "Ignore plots", value, DataHandler.getResidentIgnorePlots(id), v -> DataHandler.setResidentIgnorePlots(id, v)); break;

            case "infotool": setBool(src, "Info tool", value, DataHandler.getResidentInfoTool(id), v -> DataHandler.setResidentInfoTool(id, v)); break;
            case "adminbypass":
                if (!src.hasPermission("towny.admin.bypass"))
                    throw new CommandException(Text.of(LanguageHandler.colorRed(), "You lack towny.admin.bypass"));
                setBool(src, "Admin bypass", value, DataHandler.getResidentAdminBypass(id), v -> DataHandler.setResidentAdminBypass(id, v));
                break;

            case "reset":
                DataHandler.setResidentAutoMap(id, false);
                DataHandler.setResidentPvp(id, false);
                DataHandler.setResidentFire(id, false);
                DataHandler.setResidentExplosion(id, false);
                DataHandler.setResidentMobs(id, false);
                DataHandler.setResidentAutoClaim(id, false);
                DataHandler.setResidentAutoUnclaim(id, false);
                DataHandler.setResidentPreferBedSpawn(id, false);
                DataHandler.setResidentPlotBorder(id, false);
                DataHandler.setResidentConstantPlotBorder(id, false);
                DataHandler.setResidentTownBorder(id, false);
                DataHandler.setResidentBorderTitles(id, true);
                DataHandler.setResidentPlotGroupMode(id, false);
                DataHandler.setResidentDistrictMode(id, false);
                DataHandler.setResidentSpy(id, false);
                DataHandler.setResidentIgnorePlots(id, false);
                DataHandler.setResidentInfoTool(id, false);
                DataHandler.setResidentAdminBypass(id, false);
                src.sendMessage(Text.of(LanguageHandler.colorGreen(), "Resident toggles reset."));
                break;

            default:
                throw new CommandException(Text.of(LanguageHandler.colorRed(), "Unknown toggle: " + key));
        }
        return CommandResult.success();
    }

    private interface Setter { void set(boolean v); }
    private void setBool(CommandSource src, String label, Boolean arg, boolean cur, Setter set) {
        boolean nv = (arg == null ? !cur : arg);
        set.set(nv);
        src.sendMessage(Text.of(LanguageHandler.colorGold(), label, ": ", LanguageHandler.colorYellow(), nv ? "ON" : "OFF"));
    }
}
