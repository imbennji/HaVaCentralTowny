# Town Feature Parity Checklist

This document tracks the Sponge port's `Towny` and plot implementations against the Towny for Spigot feature set. Use it to confirm which mechanics already match player expectations and where further work is required.

## Town parity highlights (Sponge port)

* **Four-action permission grid** – Towns now canonicalise resident/ally/nation/outsider groups and expose Spigot’s `build`/`destroy`/`switch`/`itemuse` booleans. Legacy `interact` keys expand into the modern switch + item-use pair so existing saves continue to load.【F:src/main/java/com/arckenver/towny/object/Towny.java†L22-L177】
* **Plot contexts** – Plot permissions mirror the four-action matrix for friend/resident/nation/ally/outsider roles, folding in historic co-owner/citizen flags so previously claimed land keeps its protections.【F:src/main/java/com/arckenver/towny/object/Plot.java†L31-L142】
* **Config scaffolding** – `ConfigHandler` now seeds all town and plot permission nodes plus the Spigot flag set (`pvp`, `mobs`, `fire`, `explosions`, `open`, `public`, `taxpercent`, `jail`) so administrators inherit sensible defaults without manual editing.【F:src/main/java/com/arckenver/towny/ConfigHandler.java†L159-L212】
* **Command surface parity** – `/town perm`, `/plot perm`, and `/tw perm` accept the full range of permission groups/actions, automatically toggling legacy aliases like `interact` for world configs.【F:src/main/java/com/arckenver/towny/cmdexecutor/towny/TownyPermExecutor.java†L24-L90】【F:src/main/java/com/arckenver/towny/cmdexecutor/plot/PlotPermExecutor.java†L23-L87】【F:src/main/java/com/arckenver/towny/cmdexecutor/townyworld/TownyworldPermExecutor.java†L23-L79】
* **Clickable UI** – Build/interaction listeners respect the expanded permission checks, while `/town flags` and world descriptions show clickable toggles for every flag/permission, matching Spigot’s UX.【F:src/main/java/com/arckenver/towny/listener/BuildPermListener.java†L32-L172】【F:src/main/java/com/arckenver/towny/listener/InteractPermListener.java†L30-L103】【F:src/main/java/com/arckenver/towny/cmdexecutor/towny/TownyFlagsExecutor.java†L24-L88】【F:src/main/java/com/arckenver/towny/Utils.java†L564-L624】
* **World enable defaults** – `/tw enable` seeds all four permission booleans and common wilderness flags so a newly enabled world behaves like Spigot’s default wilderness until adjusted.【F:src/main/java/com/arckenver/towny/cmdexecutor/townyworld/TownyworldEnableExecutor.java†L40-L82】

## Remaining Towny gaps

* **Jails & siege mechanics** – Although the `jail` flag now exists, the Sponge port still lacks the jail plot workflow, siege warfare, and upkeep modifiers that Spigot ties to those toggles.【F:docs/spigot_parity_review.md†L27-L44】
* **War scoring & notifications** – Town war/neutrality handling is limited to simple toggles; siege scoring, broadcast messages, and wartime restrictions have not been ported yet.【F:docs/spigot_parity_review.md†L46-L55】
* **Data migration polish** – Runtime alias expansion keeps legacy saves working, but a dedicated migration pass would prevent the server from rewriting defaults every startup, keeping admin configs tidy.【F:src/main/java/com/arckenver/towny/object/Towny.java†L108-L177】【F:src/main/java/com/arckenver/towny/object/Plot.java†L74-L142】
