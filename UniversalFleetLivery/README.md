# Universal Fleet Livery — 0.1.4 alpha

Standalone procedural hull paint for **Starsector 0.98a-RC8 / Java 17**. No required GraphicsLib, LunaLib, MagicLib or Nexerelin dependency. This ZIP contains compiled code, full source, resources and build/test tools. Paint Workshop is a separate project and is not included.

This update separates **Ship / Fleet / Empire** settings, simplifies the main menu and corrects cleanup during fleet-screen pickup/drop. The player confirmed visible fleet-screen recolouring in 0.1.2. The new scope and drag behaviour has automated regression evidence; a live campaign retest is still required. See `docs/TEST_REPORT.md`.

## Install and open

1. Close Starsector. Completely replace the previous `UniversalFleetLivery` folder with the folder from this ZIP. Keep only one installed version. The final path is `Starsector/mods/UniversalFleetLivery/mod_info.json`.
2. Enable **Universal Fleet Livery** in the launcher.
3. In the campaign ability picker, assign **Fleet Livery** to an ability slot and activate it. This mod uses its ability button, not the separate Workshop's G shortcut.
4. The main menu has **Fleet Livery**, **Restore original fleet colours**, **Utilities**, and Close/Escape.

For a fresh renderer check, **Missions → Fleet Livery: Rendering Test** provides painted Wolves and an original comparison. Use a separate campaign save for initial acceptance of this alpha.

## Choose a scope

Open **Fleet Livery**, then choose:

| Scope | What it changes |
|---|---|
| Ship livery | One ship in your active fleet, selected through the visual grid or text list. |
| Fleet livery | Your active fleet, or one activated non-station fleet belonging to the player faction. |
| Empire livery | The inherited standard for eligible player-faction fleets. Existing fleet and ship exceptions remain. |

A ship override wins over its fleet; a fleet override wins over the empire. Advanced hull and hull-size rules remain under **Utilities** and apply only to the active fleet: ship → hull → size → fleet → empire. **Original** is an explicit exemption; **Use inherited colours** removes that override.

For fleets, **Set this fleet's standard** preserves exceptions. **Standardise this fleet** clears that fleet's ship exceptions and, for the active fleet, its hull/size rules; it asks for confirmation. **Use empire standard** removes only the fleet override, leaving more specific rules intact.

Empire's second Apply option sets the empire standard and lets the active fleet inherit it. It retains individual/hull/size exceptions. To make an already customised fleet completely uniform, standardise that fleet explicitly.

**Restore original fleet colours** restores your current fleet, clears its individual/hull/size rules and gives it an Original fleet exemption. It keeps the empire standard, other fleet settings and remembered exceptions on ships elsewhere. To resume inheritance, choose Fleet livery → Your active fleet → a palette → Use empire standard.

## Preview and custom colours

Four colour cards per page show Primary / Secondary / Accent. Choose a numbered card to see original and proposed base-hull previews. Previous/Next scheme compares presets. Browsing and editing do not apply paint.

**Edit a copy of this scheme** opens three six-digit RGB hex fields and strength 0–100. Choose Preview, then Apply to the selected scope. Each Custom assignment saves its own colour snapshot: editing one ship no longer changes an earlier fleet or empire assignment. The last-used Custom draft is retained for reuse; named custom-palette management is not included.

Factory palettes: Imperial, Naval, Crimson, Industrial, Arctic, Verdant, Obsidian, Solar, Royal, Cerulean, Copperhead, Vanguard, Ghost, Dusk, Hazard, Terran, Ceremonial and Rustbelt, plus Custom and Original. Contributors can add entries to `data/config/ufl_palettes.json`; no file editing is needed to use the shipped catalogue.

## Empire coverage and ownership

Eligible fleets are the active player fleet and activated non-station fleets whose faction ID equals the sector's player-faction ID. Normal player-faction patrols can inherit. Dormant fleets are not forced into existence; painting runs after activation. Commission employers, allies and hired fleets with a different faction ID do not qualify.

Station fleets, storage inventories, colony production lists and scripted renderers are not covered by empire automation. A manufactured ship inherits after joining an eligible fleet. Remembered ship overrides follow its fleet-member ID between eligible fleets; a newly recovered ship with a new ID is a new acquisition. Runtime paint is stripped after departure from eligible ownership, with cleanup deferred for temporarily held ships while the fleet screen is open.

Existing campaigns keep their old active-fleet standard on upgrade; they do not silently adopt empire colours. Legacy shared-Custom rules migrate to independent snapshots with the same colour values. New campaigns initially inherit Original from the empire.

## What gets painted

The base hull image is remapped from its luminance and chroma. Dimensions and transparency are preserved, along with source shading and panel detail. Automatic masks infer colour families, not physical armour panels. Saturated original paint tends toward Accent; monochrome hulls may offer limited channel separation.

Weapons, barrels, missiles, flames, glows, damage overlays and scripted add-on graphics use separate layers and keep their own colours. This can leave red weapon housings on a blue destroyer. Preview shows the base hull only. Multi-module variants receive rules recursively but need live testing. Known MagicLib paintjobs take priority; remove those paintjobs before using UFL on the same ship.

The player has confirmed painted ordinary fleet cards. Native rendering tests also pass; combat/refit, all thumbnail paths and the full mod stack remain live acceptance items. Reopen an existing refit view after changing paint. No shared hull specification, original asset or other mod file is overwritten.

## Utilities, limits and removal

Utilities contains advanced hull/size rules, enable/disable, texture status/retry and the paint-layer explanation. Disabling retains preferences and strips runtime paint across eligible scopes.

Generated textures are session-local, with no disk texture cache. `data/config/ufl_settings.json` caps source pixels (4,194,304), generated textures (256) and estimated generated GPU memory (256 MiB). Unsupported sources or exhausted budgets retain original appearance and log the cause. Errors appear in `starsector.log` with UFL context.

Preferences use ordinary Java maps, strings and booleans. Runtime hullmods/tags and the ability are stripped before save and reapplied afterward; the script and inflation listener are transient. Real save/load and uninstall acceptance are still pending. Before removing the mod, disable liveries in Utilities, close the menu, save to a new slot, quit, disable UFL in the launcher and verify that copy loads. Keep the original save until verified. Copies hidden inside third-party private persistence are outside the cleanup boundary.

## Build and documentation

Source: `src/`. Ready-to-load JAR: `jars/UniversalFleetLivery.jar`. See `docs/BUILD.md`, `docs/ARCHITECTURE.md`, `docs/COMPATIBILITY.md`, `docs/TEST_REPORT.md` and `docs/IN_GAME_CHECKLIST.md`.

MIT license covers UFL implementation and original icon. Evidence images retain the game/mod artists' rights. No game or dependency JARs are redistributed. See `docs/THIRD_PARTY_NOTICES.md`.
