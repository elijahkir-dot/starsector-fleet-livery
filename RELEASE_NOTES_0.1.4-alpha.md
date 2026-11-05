# Universal Fleet Livery 0.1.4-alpha

**Early alpha for Starsector 0.98a-RC8 / Java 17.** Author: **Ely K**. No required mod dependencies.

## Download

Use **UniversalFleetLivery-0.1.4-alpha.zip** under Assets. This is the compiled, installable mod package.

## Changes since 0.1.1-alpha

- Separate **Ship livery**, **Fleet livery** and **Empire livery** scopes with explicit inheritance.
- **18 factory palettes**, paged colour swatches, draft editing and a native visual ship picker.
- Independent Custom colour snapshots so editing one assignment does not recolour other Custom assignments.
- Retain paint while picking up and dropping a ship in the fleet screen.
- Root menu: **Fleet Livery**, **Restore original fleet colours**, **Utilities**, and Close.
- Apply empire defaults to activated, non-station player-faction fleets, with ownership cleanup.
- Includes the Windows RC8 texture-registrar fix introduced in 0.1.2-alpha.

## Installation and upgrade

1. Close Starsector and back up your campaign save.
2. Replace the old UniversalFleetLivery directory completely; do not merge folders.
3. Extract the ZIP into Starsector/mods/ so mods/UniversalFleetLivery/mod_info.json exists.
4. Enable Universal Fleet Livery in the launcher. The mod ID is unchanged.
5. Assign and activate the Fleet Livery campaign ability. Open Fleet Livery, then choose Ship, Fleet or Empire.

## Testing and limitations

The packaged reports record automated scope, menu, Custom-colour, empire-lifecycle and drag regression tests, plus restricted-loader native rendering. **Live campaign pickup/drop, actual NPC fleet behaviour, combat/refit, save/reload and the new menu layout still require in-game acceptance.** Use a backup save.

Ordinary fleet-card thumbnails and campaign silhouettes are not recoloured. Weapons, flames, shields and separate effects retain their own graphics. This package does not include Paint Workshop.

See the packaged CHANGELOG.md, docs/TEST_REPORT.md and docs/IN_GAME_CHECKLIST.md.

Original UFL code and icon: MIT. Starsector and third-party art remain their owners' property. Not affiliated with Fractal Softworks.
