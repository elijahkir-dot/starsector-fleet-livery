# Universal Fleet Livery

**0.1.4-alpha · Starsector 0.98a-RC8 / Java 17 · Author: Ely K**

Give ships from different factions and mods a consistent hull colour scheme, with separate ship, fleet and empire policies.

## Download

**[Download UniversalFleetLivery-0.1.4-alpha.zip](https://github.com/elijahkir-dot/starsector-fleet-livery/releases/download/v0.1.4-alpha/UniversalFleetLivery-0.1.4-alpha.zip)**

[Release notes and all downloads](https://github.com/elijahkir-dot/starsector-fleet-livery/releases).

## Features

- Separate **Ship livery**, **Fleet livery** and **Empire livery** scopes.
- **18 factory palettes**, RGB Custom colours, paged swatches and draft editing.
- Independent Custom colour assignments and a native visual ship picker.
- New acquisitions inherit their applicable colour policy; ship overrides remain independent.
- Empire defaults cover activated, non-station player-faction fleets.
- Paint remains attached during fleet-screen pickup/drop.
- **Restore original fleet colours** and advanced controls under **Utilities**.
- No required GraphicsLib, LunaLib, MagicLib or Nexerelin dependency.

## Installation

1. Close Starsector and back up your save.
2. Replace any old UniversalFleetLivery mod directory completely rather than merging files.
3. Extract the installable ZIP into Starsector/mods/ so mods/UniversalFleetLivery/mod_info.json exists.
4. Enable Universal Fleet Livery in the launcher.
5. Assign the **Fleet Livery** campaign ability. Open **Fleet Livery**, then choose **Ship**, **Fleet** or **Empire**.

The mod ID remains universal_fleet_livery. See the packaged README and compatibility notes for scope inheritance and configuration.

## Alpha status

Automated scope, menu, Custom-colour, empire-lifecycle and drag regression tests and restricted-loader native rendering are recorded in the package. **Live campaign pickup/drop, actual NPC fleet behaviour, combat/refit, save/reload and the new menu layout still require in-game acceptance.** Test on a copied campaign save.

Ordinary fleet-card thumbnails and campaign silhouettes are not recoloured. Weapons, engine flames, shields and separate effects retain their own graphics. Paint Workshop is a separate project and is not included.

## Source and credits

The complete installable mod, Java sources, compiled JAR, build scripts, tests, changelog and evidence are in [UniversalFleetLivery](UniversalFleetLivery/). Read [CHANGELOG.md](UniversalFleetLivery/CHANGELOG.md), [TEST_REPORT.md](UniversalFleetLivery/docs/TEST_REPORT.md) and [IN_GAME_CHECKLIST.md](UniversalFleetLivery/docs/IN_GAME_CHECKLIST.md).

**Author:** Ely K. **License:** MIT for original UFL code and icon. Starsector and third-party artwork remain their respective owners' property. Diable Avionics art credits: Tartiflette and FlashFrozen; maintained by Caymon Joestar.

Not affiliated with or endorsed by Fractal Softworks.
