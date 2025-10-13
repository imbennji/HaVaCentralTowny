# Spigot Parity Review for the Sponge Towny Remake

This document summarises how closely the Sponge rewrite mirrors the behaviour of the Spigot Towny plugin and lists the most visible divergences. It is based on a pass over the current codebase and public knowledge of how Towny behaves on Spigot.

## Areas that already feel familiar

* **Command surface** – The command dispatcher registers the same top-level namespaces that Towny exposes on Spigot (`/town`, `/nation`, `/resident`, `/plot`, `/townyadmin`, `/townyworld`) and wires them to dedicated executors, so most chat interactions carry over without retraining players.【F:src/main/java/com/arckenver/towny/cmdexecutor/TownyCmds.java†L29-L78】
* **Flag toggles UX** – `/town flags` renders the Spigot-style clickable enable/disable pairs for each boolean flag, including hover text and automatic sorting, giving staff the familiar UI they expect from `/town toggle` on Spigot. The view now automatically lists all configured flags, including the newly added `taxpercent` and `jail` toggles.【F:src/main/java/com/arckenver/towny/cmdexecutor/towny/TownyFlagsExecutor.java†L24-L88】【F:src/main/java/com/arckenver/towny/ConfigHandler.java†L159-L167】
* **Activity toasts and automap** – The movement listener still emits entry toasts, displays boards, and honours the automatic town map toggle, mimicking the situational awareness cues Spigot players rely on.【F:src/main/java/com/arckenver/towny/listener/PlayerMoveListener.java†L27-L165】

## Structural differences compared to Spigot Towny

* **Platform runtime** – The plugin is built for Sponge (`@Plugin(id = "towny-relaunched")`) and registers listeners/commands through Sponge’s APIs, so it cannot be dropped into a Bukkit/Spigot server the way the original jar can.【F:src/main/java/com/arckenver/towny/TownyPlugin.java†L36-L115】
* **Persistence format** – Towns, nations, and residents are serialised as individual JSON files inside the plugin config directory instead of using Towny’s flatfile or SQL data source layer.【F:src/main/java/com/arckenver/towny/DataHandler.java†L72-L150】
* **Nation model scope** – The `Nation` class now tracks assistants, government types, tax mode and spawn cost alongside the legacy fields, but Spigot options such as nation map colours, conquest metadata, or government-specific modifiers are still absent.【F:src/main/java/com/arckenver/towny/object/Nation.java†L18-L218】
* **Town/plot permissions** – Permission tables have been expanded to Spigot’s four-action grid for every context group, yet legacy data without the newer keys still relies on runtime defaults. A migration helper may be worthwhile for extremely old backups.【F:src/main/java/com/arckenver/towny/object/Towny.java†L22-L177】【F:src/main/java/com/arckenver/towny/object/Plot.java†L31-L142】【F:src/main/java/com/arckenver/towny/ConfigHandler.java†L168-L212】
* **Available town flags** – The config scaffolding seeds Spigot’s core toggles (`pvp`, `mobs`, `fire`, `explosions`, `open`, `public`, `taxpercent`, `jail`), and `/town flags` renders them with clickable enable/disable controls, but siege-related switches still need bespoke behaviour.【F:src/main/java/com/arckenver/towny/ConfigHandler.java†L159-L167】【F:src/main/java/com/arckenver/towny/cmdexecutor/towny/TownyFlagsExecutor.java†L24-L88】

## Behavioural gaps to plan for

* **Economy provider lifecycle** – The Sponge service hooks guard against missing economy services, but Spigot Towny ships with vault integration and fallbacks for offline calculations. Equivalent handling (or a bundled economy bridge) would improve parity when an economy plugin disappears mid-session.【F:src/main/java/com/arckenver/towny/TownyPlugin.java†L76-L158】
* **Nation gameplay depth** – War mechanics, government-based bonuses, and nation map colours remain unimplemented despite the broader staff/command surface. Implementing those systems will require additional persistence and event hooks similar to Spigot’s siege features.【F:src/main/java/com/arckenver/towny/object/Nation.java†L18-L218】【F:src/main/java/com/arckenver/towny/cmdexecutor/nation/NationToggleExecutor.java†L19-L99】
* **Town governance extras** – Jail blocks, siege warfare, and plot-level upkeep modifiers still need dedicated executors and listeners even though the toggle schema now exposes the requisite flags.【F:src/main/java/com/arckenver/towny/ConfigHandler.java†L159-L212】【F:docs/resident_parity.md†L33-L38】

## Suggested next steps

1. Build out town jails, siege/war rules, and resident mode side-effects so the new parity checklists can flip outstanding items to “done.”【F:docs/town_parity.md†L20-L58】【F:docs/nation_parity.md†L19-L61】
2. Implement migration routines that rewrite legacy `interact` flags into discrete `switch/itemuse` keys during load, avoiding repeated default-filling at runtime.【F:src/main/java/com/arckenver/towny/object/Towny.java†L108-L177】【F:src/main/java/com/arckenver/towny/object/Plot.java†L74-L142】
3. Reintroduce nation war systems (siege timers, war spoils, nation zones) and hook government types into those modifiers to finish matching Spigot’s diplomacy depth.【F:src/main/java/com/arckenver/towny/object/Nation.java†L18-L218】【F:docs/nation_parity.md†L63-L84】

This checklist should make it easier to prioritise what still needs to be brought over for a near drop-in experience.
