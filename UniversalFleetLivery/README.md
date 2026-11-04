# Universal Fleet Livery — 0.1.1 alpha

**0.1.1-alpha fixes the shared script-security error in 0.1.0-alpha.** Close Starsector and replace the old mod directory completely. Keep the same mod ID. No extra library, launcher edit or security-setting change is required. The restricted-loader renderer regression passes; live campaign acceptance is still pending.


A standalone, procedural hull-paint mod for **Starsector 0.98a-RC8 / Java 17**. No required mod dependencies.

**This is a compiled, installable alpha, not a gameplay-certified release.** The RC8 texture loader and native sprite renderer have been exercised successfully, including vanilla and two Diable Avionics hull images. A licensed campaign/combat session and the user's exact mod stack were unavailable. Combat/refit integration, the campaign menu and real save/load still require in-game acceptance. See `docs/TEST_REPORT.md` for the exact boundary.

## Install

1. Extract the ZIP into `Starsector/mods/`. The final path must be `mods/UniversalFleetLivery/mod_info.json`.
2. Enable **Universal Fleet Livery** in the launcher. No GraphicsLib, LunaLib, MagicLib or Nexerelin dependency is required.
3. First run **Missions → Fleet Livery: Rendering Test** on a copy of your installation. Deploy the six painted Wolves and the original comparison ship. Zoom in and check alignment, details and transparency.
4. Load a separate save for the initial campaign checks. In the campaign ability picker, assign **Fleet Livery** to an ability slot and activate it.

If a launcher reports a different game version, consult `docs/COMPATIBILITY.md`. Do not assume changing `gameVersion` alone establishes compatibility.

## Use

- **Choose a fleet standard** offers Imperial (black/gold), Naval (blue/silver), Crimson (red/black), Industrial (gunmetal/orange), Arctic (white/blue), Custom, and Original.
- Selecting a scheme opens an original/proposed hull preview. Choose **Set fleet default** to preserve exceptions, or **Apply to entire fleet** to clear exceptions and standardise every active ship.
- **Ships, hull standards and size standards** lets you choose an individual ship, set its override, or define a standard for its base hull or hull size.
- Precedence is **ship override → base hull rule → size rule → fleet default**. Original can be an explicit exemption. Clearing a rule restores inheritance.
- **Custom → Edit custom colours / paint strength** accepts three six-digit RGB values and strength 0–100. Preview does not apply changes. Applying Custom updates the shared Custom template, including ships already assigned to it.
- Newly acquired ships inherit the current policy. Rules apply to the active player fleet. Storage ships and ships transferred away lose the runtime paint attachment; returning with the same fleet-member ID restores their remembered rule. A ship recovered with a new ID is a new acquisition.
- **Disable all liveries** retains preferences and removes runtime paint attachments. **Restore original appearances** clears fleet/size/hull/ship rules. Reopen refit to recreate its displayed ship after changes.
- **Texture status / retry skipped images** retries missing/failed sources and reports the texture budget. Failures are logged with a `UFL:` prefix in `starsector.log`.

The ability may need assigning again if the game's ability-bar handling clears its slot during save cleanup; that behaviour is an in-game acceptance item.

## What is recoloured

The existing base hull image is procedurally remapped with continuous luminance/chroma masks. Alpha and image dimensions remain unchanged. Neutral surfaces use primary/secondary colours; saturated existing paint tends to become accent. Shading and panel detail remain visible, but the algorithm has no semantic understanding of hull geometry. A red manufacturer hull can become mostly gold in Imperial. The preview is the authority for a particular hull.

Separate weapons, engine flames, shields, phase glows, damage overlays and scripted animated parts retain their own rendering. Multi-module variants receive the same rule recursively, but modular ships need live testing. Existing MagicLib paintjobs take precedence: remove their paintjob to use UFL on that ship.

**The ordinary fleet-card thumbnails and campaign fleet silhouette are not recoloured by this alpha.** The livery menu provides its own base-hull preview; weapons and station modules are not assembled in that preview. The hidden cosmetic hullmod implements the base-hull swap for combat/refit-created ships; live integration remains unverified. Avoid conflating the native-renderer evidence with a tested campaign.

## Files and configuration

`data/config/ufl_settings.json` controls maximum image pixels, session texture count, estimated GPU texture budget and optional GraphicsLib integration. Defaults: 4,194,304 pixels per source, 256 generated textures, 256 MiB estimated generated texture memory.

Generated textures are held only in memory for the current game session. Sources are read through the game's resource API. No generated PNGs or temporary/cache directories are written. Saved palettes/designs rebuild their textures as needed after restarting. Old 0.1.0 cache files are unused and can be removed with the old mod directory.

## Save/load and removal

Preferences are stored as ordinary Java maps, strings and booleans in sector persistent data. Runtime hullmods, palette tags and the ability are stripped before saving and reattached after save or save failure. This design avoids requiring mod-defined classes in the saved preferences; **actual Starsector save/load and removal are not yet certified**.

For removal, choose **Restore original appearances**, close the menu, save to a new slot, quit, disable the mod and verify that copy loads. Keep the original save until this passes. Unknown third-party systems that copy modified variants or ability objects into their own persistent data are outside UFL's cleanup boundary.

## Build and contribute

All implementation source is in `src/`; `jars/UniversalFleetLivery.jar` is ready to load. See `docs/BUILD.md` for offline Python/JDK and Gradle build instructions, `docs/ARCHITECTURE.md` for design, and `docs/IN_GAME_CHECKLIST.md` for acceptance.

License: MIT for UFL's implementation and original icon. Game art and evidence screenshots retain their source owners' rights. Diable Avionics credits: **Tartiflette and FlashFrozen**, maintained by Caymon Joestar. No game or third-party dependency jars are redistributed.
