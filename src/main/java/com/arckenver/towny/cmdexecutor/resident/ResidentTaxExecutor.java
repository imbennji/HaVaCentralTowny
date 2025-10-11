package com.arckenver.towny.cmdexecutor.resident;

import com.arckenver.towny.ConfigHandler;
import com.arckenver.towny.DataHandler;
import com.arckenver.towny.object.Plot;
import com.arckenver.towny.object.Towny;
import org.spongepowered.api.command.*;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.*;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.math.BigDecimal;
import java.util.UUID;

public class ResidentTaxExecutor implements CommandExecutor {
    public static void create(CommandSpec.Builder root) {
        root.child(spec(), "tax");
    }
    public static CommandSpec spec() {
        return CommandSpec.builder()
                .permission("towny.command.resident.tax")
                .executor(new ResidentTaxExecutor())
                .build();
    }

    @Override public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException {
        if (!(src instanceof Player)) throw new CommandException(Text.of(TextColors.RED, "Players only."));
        Player p = (Player) src;
        UUID id = p.getUniqueId();

        Towny t = DataHandler.getTownyOfPlayer(id);
        if (t == null) throw new CommandException(Text.of(TextColors.RED, "You’re not in a town."));

        double townTax = t.getTaxes();
        int owned = (int) t.getPlots().values().stream().filter(pl -> id.equals(pl.getOwner())).count();
        double perPlot = ConfigHandler.getNode("prices", "plotTaxPerPlot").getDouble(0.0);
        double total = townTax + (perPlot * owned);

        src.sendMessage(Text.of(
                TextColors.GOLD, "Town tax: ", TextColors.YELLOW, townTax,
                TextColors.GOLD, " | Plot tax: ", TextColors.YELLOW, perPlot, " x ", owned,
                TextColors.GOLD, " = ", TextColors.YELLOW, BigDecimal.valueOf(total)
        ));
        return CommandResult.success();
    }
}
