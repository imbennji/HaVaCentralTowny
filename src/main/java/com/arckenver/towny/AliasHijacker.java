package com.arckenver.towny;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandMapping;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.command.TabCompleteEvent;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.Locatable;
import org.spongepowered.api.world.World;

@NonnullByDefault
public final class AliasHijacker {

    public AliasHijacker(TownyPlugin plugin) {
        Sponge.getEventManager().registerListeners(plugin, this);
    }

    // ---- helpers ----
    private static boolean hasCmd(String root) {
        // API 7 returns Optional<? extends CommandMapping>
        Optional<? extends CommandMapping> m = Sponge.getCommandManager().get(root);
        return m.isPresent();
    }

    /** Returns a fully-formed command line to forward to, or null to ignore. */
    private static String resolveForward(String rawCmd, String args) {
        final String cmd = rawCmd.toLowerCase(Locale.ENGLISH);

        // Respect namespaced commands like other:cmd
        if (cmd.indexOf(':') >= 0) return null;

        switch (cmd) {
            // Town root
            case "t":
                return args.isEmpty() ? "town" : "town " + args;

            // Town chat
            case "tc": {
                final boolean hasTownyChat = hasCmd("townychat");
                return hasTownyChat
                        ? (args.isEmpty() ? "townychat" : "townychat " + args)
                        : (args.isEmpty() ? "town chat"  : "town chat "  + args);
            }

            // Admin
            case "ta": {
                final boolean hasTownyAdmin = hasCmd("townyadmin");
                return hasTownyAdmin
                        ? (args.isEmpty() ? "townyadmin" : "townyadmin " + args)
                        : (args.isEmpty() ? "town admin"  : "town admin "  + args);
            }

            // Town world
            case "tw": {
                final boolean hasTownWorld = hasCmd("townworld");
                return hasTownWorld
                        ? (args.isEmpty() ? "townworld" : "townworld " + args)
                        : (args.isEmpty() ? "town world" : "town world " + args);
            }

            // Plot
            case "p": {
                final boolean hasPlot = hasCmd("plot");
                return hasPlot
                        ? (args.isEmpty() ? "plot" : "plot " + args)
                        : (args.isEmpty() ? "town plot" : "town plot " + args);
            }

            default:
                return null;
        }
    }

    // ---- intercept actual dispatch ----
    @Listener(order = Order.FIRST)
    public void onSendCommand(SendCommandEvent event) {
        final CommandSource src = event.getCause().first(CommandSource.class).orElse(null);
        if (src == null) return;

        final String forward = resolveForward(event.getCommand(), event.getArguments());
        if (forward == null) return;

        event.setCancelled(true); // stop whoever else would claim the alias
        Sponge.getCommandManager().process(src, forward); // run your command
    }

    // ---- fix tab-completion too ----
    @Listener(order = Order.FIRST)
    public void onTabComplete(TabCompleteEvent.Command event) {
        final CommandSource src = event.getCause().first(CommandSource.class).orElse(null);
        if (src == null) return;

        final String cmd = event.getCommand();
        String forward = resolveForward(cmd, event.getArguments());
        if (forward == null) return;

        // Ensure a trailing space so completions occur on the correct segment
        if (!forward.endsWith(" ") && !event.getArguments().isEmpty())
            forward += " ";

        Location<World> target = (src instanceof Locatable) ? ((Locatable) src).getLocation() : null;

        // API 7: 3-arg getSuggestions
        List<String> suggestions = Sponge.getCommandManager().getSuggestions(src, forward, target);

        // Mutate in place (API 7 lists are live)
        event.getTabCompletions().clear();
        event.getTabCompletions().addAll(suggestions);
    }
}
