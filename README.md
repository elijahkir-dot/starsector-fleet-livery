# Universal Fleet Livery

**Procedural fleet-wide colour schemes for Starsector 0.98a-RC8 (Java 17).**

Make ships from different factions and mods look like they belong to the same navy, without replacing their original hull artwork.

> **0.1.1-alpha: early testing release.** The included package has passed automated sprite-rendering and restricted-script-loader regression tests, but **live campaign/combat integration, save/load and compatibility with full mod stacks have not yet been certified**.

## Features

- Fleet-wide colour standards: **Imperial** (black/gold), **Naval** (blue/silver), **Crimson** (red/black), **Industrial** (gunmetal/orange), **Arctic** (white/blue), **Custom**, or **Original**.
- Custom primary, secondary and accent RGB colours with adjustable paint strength.
- Individual ship overrides, base-hull standards and hull-size standards.
- Inheritance priority: **ship → base hull → hull size → fleet default**.
- Newly acquired ships inherit your fleet's current livery policy.
- Recolouring retains original hull shading, armour panels and transparency.
- No required GraphicsLib, LunaLib, MagicLib or Nexerelin dependency.

## Installation

The installable ZIP is named **UniversalFleetLivery-0.1.1-alpha.zip**. Extract it to `Starsector/mods/` so the path is:

```text
Starsector/mods/UniversalFleetLivery/mod_info.json
```

Enable **Universal Fleet Livery** in the Starsector launcher.

**Note:** The ZIP and compiled JAR have not yet been uploaded to this GitHub repository. Do not treat the repository's current contents as an installable release.

## First test

1. Launch **Missions → Fleet Livery: Rendering Test** and inspect the painted ships.
2. Test on a **copy of a campaign save**.
3. Assign the **Fleet Livery** campaign ability and open the livery menu.
4. Check refit, combat, ship transfers and save/reload behaviour before relying on it in a long-running campaign.

## Known limitations

- Ordinary fleet-card thumbnails and campaign fleet silhouettes are **not** recoloured in this alpha.
- Weapons, engine flames, shields, damage overlays and scripted animated parts keep their own visuals.
- The algorithm groups colours by image characteristics, not semantic hull components; results vary by source sprite.
- Existing MagicLib paintjobs take precedence on ships using those paintjobs.
- Actual in-game save/load and mod-removal safety are **not yet certified**.

## Compatibility

Target: **Starsector 0.98a-RC8 / Java 17**. Other game versions have not been verified. No required mod dependencies.

## Source, testing and credits

The supplied 0.1.1-alpha package includes Java sources, build scripts, a changelog, architecture and compatibility notes, test reports and automated test fixtures. These will be published with the complete release package.

**Author:** Ilias Kiritsis  
**License:** MIT for original UFL code and icon. Starsector and third-party art remain their respective owners' property.

**Diable Avionics art used in test evidence:** Tartiflette and FlashFrozen; maintained by Caymon Joestar.

*Not affiliated with or endorsed by Fractal Softworks.*
