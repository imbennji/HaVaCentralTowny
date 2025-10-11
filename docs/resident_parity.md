# Resident Feature Parity Checklist

This document tracks the differences between the current Sponge port's `Resident` data model and the feature set available in the Spigot Towny plugin. Use it to plan future work toward parity.

## Resident parity highlights (Sponge port)

The Sponge port now mirrors the Spigot resident feature-set (excluding nations) in the following areas:

* **Identity & profile** – Track last-known name, name history, surname/prefix/suffix, locale, biography and title strings. 【F:src/main/java/com/arckenver/towny/object/Resident.java†L24-L120】【F:src/main/java/com/arckenver/towny/DataHandler.java†L704-L760】
* **Town membership** – Persist the player’s town UUID, rank set, promotion history and helper lookups like `isMayor`/`isAssistant`. Resident ranks are displayed in `/resident info` and `/resident`. 【F:src/main/java/com/arckenver/towny/object/Resident.java†L130-L203】【F:src/main/java/com/arckenver/towny/DataHandler.java†L762-L830】【F:src/main/java/com/arckenver/towny/cmdexecutor/resident/ResidentInfoExecutor.java†L63-L118】
* **Lifecycle tracking** – Registration time, last online/logout timestamps and name history power the improved `/resident info` and `/resident list` activity summaries. 【F:src/main/java/com/arckenver/towny/object/Resident.java†L205-L236】【F:src/main/java/com/arckenver/towny/DataHandler.java†L708-L741】【F:src/main/java/com/arckenver/towny/cmdexecutor/resident/ResidentListExecutor.java†L35-L94】
* **Economy & taxes** – Residents maintain an internal balance ledger, bankruptcy flag, tax timestamps and tax exemption timers exposed through `/resident tax`. 【F:src/main/java/com/arckenver/towny/object/Resident.java†L238-L288】【F:src/main/java/com/arckenver/towny/DataHandler.java†L832-L886】【F:src/main/java/com/arckenver/towny/cmdexecutor/resident/ResidentTaxExecutor.java†L30-L73】
* **Jail system** – Jail status, town/plot references, release time, escape counter and jail requests are saved and surfaced in `/resident info`. 【F:src/main/java/com/arckenver/towny/object/Resident.java†L290-L338】【F:src/main/java/com/arckenver/towny/DataHandler.java†L888-L938】【F:src/main/java/com/arckenver/towny/Utils.java†L232-L249】
* **Spawn management** – Spawn cooldown, preferred spawn, warmup timer and last-spawn metadata drive the enhanced `/resident spawn` command. 【F:src/main/java/com/arckenver/towny/object/Resident.java†L340-L386】【F:src/main/java/com/arckenver/towny/DataHandler.java†L940-L991】【F:src/main/java/com/arckenver/towny/cmdexecutor/resident/ResidentSpawnExecutor.java†L37-L143】
* **Modes and notifications** – Spigot-style resident modes (managed via `/resident mode`) and per-notification mute flags replace the legacy boolean-only toggles. 【F:src/main/java/com/arckenver/towny/object/Resident.java†L388-L480】【F:src/main/java/com/arckenver/towny/DataHandler.java†L993-L1037】【F:src/main/java/com/arckenver/towny/cmdexecutor/resident/ResidentModeExecutor.java†L1-L71】
* **Miscellaneous parity** – Pending invites, war statistics, debug/ignore flags and info UI improvements align with Spigot behaviour. 【F:src/main/java/com/arckenver/towny/object/Resident.java†L482-L600】【F:src/main/java/com/arckenver/towny/DataHandler.java†L1039-L1117】【F:src/main/java/com/arckenver/towny/Utils.java†L174-L275】

## Remaining Towny gaps

* **Nation data** – Nation membership, allied invites and king-specific rank helpers remain intentionally unimplemented until nation mechanics are ported.
* **Behavioural polishing** – Further tuning around nation taxes, nation jail transfers and cross-server parity will follow once nation support lands.
