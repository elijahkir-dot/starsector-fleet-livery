# Universal Fleet Livery 0.1.1-alpha

**Early alpha for Starsector 0.98a-RC8 / Java 17.** No required mod dependencies.

## What it does

Apply a unified naval colour scheme to the player's fleet, including ships from different mods. Choose Imperial, Naval, Crimson, Industrial, Arctic, Custom, or Original. Override individual ships or define rules for a hull class or size; new acquisitions inherit the fleet policy. Preview proposed liveries before applying them.

## What's changed since 0.1.0-alpha

Fixes the shared script-security error in 0.1.0-alpha. The restricted-loader renderer regression now passes. **Close the game and replace the old mod folder completely**, rather than merging directories.

## Installation

1. Download `UniversalFleetLivery-0.1.1-alpha.zip`.
2. Extract into `Starsector/mods/` so `mods/UniversalFleetLivery/mod_info.json` exists.
3. Enable **Universal Fleet Livery** in the launcher.
4. First try **Missions → Fleet Livery: Rendering Test**, then test on a copied campaign save.

## Testing status

Compiled and packaged; automated renderer and script-security tests passed. **Live campaign/combat integration, refit behaviour, save/load, mod removal and large mod-stack compatibility remain unverified.** Use a backup save.

Ordinary fleet-card thumbnails and campaign silhouettes are not recoloured in this alpha. Weapons, engine flames, shields and separate visual effects keep their original appearance.

See the packaged `docs/TEST_REPORT.md`, `docs/IN_GAME_CHECKLIST.md` and `docs/COMPATIBILITY.md` for details.

Not affiliated with Fractal Softworks. Original implementation and icon licensed MIT; third-party art retains its owners' rights.
