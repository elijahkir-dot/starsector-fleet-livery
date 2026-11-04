# Third-party notices and research sources

No Starsector, GraphicsLib, LunaLib, MagicLib, Nexerelin, Diable Avionics or Tahlan jars or raw ship sprites are included in this release.

Starsector and its artwork are owned by Fractal Softworks. Evidence images show the game's sprites rendered by its own engine in a test harness; they are not replacement sprites supplied by this mod. A licensed game installation is required for normal play.

Diable Avionics evidence uses its Draft and Storm images. Credit **Tartiflette and FlashFrozen**, with current maintenance by Caymon Joestar. Its published license permits code and graphics reuse for Starsector modding with those credits. Source and license at:

- https://github.com/CaymonJoestar/Diable-Avionics
- https://github.com/CaymonJoestar/Diable-Avionics/blob/b5bc16b305ffce738b1072c28d81c1a5cbcd9ea7/license.txt

Only composite native-renderer evidence, not the original downloaded PNGs, is included. The full Diable mod was not loaded during testing.

MagicLib (MIT, copyright Wispborne and contributors) was studied as a source example. UFL does not vendor its code or artwork. Consulted paintjob manager/hullmod demonstrate native SpriteAPI replacement and variant tags; the refit panel shows private/reflection-based UI work. The independent UFL implementation uses a public campaign dialog instead of reproducing that injection.

Primary technical references:

- Official RC8 API sources, `starfarer.api.zip`, in the game distribution (authoritative compile target).
- Official release: https://fractalsoftworks.com/2025/03/27/starsector-0-98a-release/ (page access unavailable in this session; official release-host distribution was acquired).
- API reference mirror and compiled RC7 comparison: https://github.com/jaghaimo/starsector-api and https://jaghaimo.github.io/starsector-api/
- FleetMemberAPI `setSpriteOverride` is documented as a campaign-view hook: https://jaghaimo.github.io/starsector-api/interfacecom_1_1fs_1_1starfarer_1_1api_1_1fleet_1_1FleetMemberAPI.html
- SpriteAPI: https://jaghaimo.github.io/starsector-api/interfacecom_1_1fs_1_1starfarer_1_1api_1_1graphics_1_1SpriteAPI.html
- MagicLib source: https://github.com/MagicLibStarsector/MagicLib/tree/70900ceb542204b6f9e0f8ee59fd0b7bf5b1c585/src/org/magiclib/paintjobs
- MagicLib `MagicPaintjobManager.kt`: `applyPaintjob`, `getReplacementSprite`, and the comment about campaign spriteOverride sizing.
- MagicLib `MagicPaintjobHullMod.kt`: ship-creation and combat application pattern.
- MagicLib `MagicPaintjobRefitPanel.kt`: refit-panel integration and its use of internal classes/reflection.
- Diable Avionics release notes confirm Magic paintjobs in 2.9.3: https://github.com/CaymonJoestar/Diable-Avionics/releases

Runtime bytecode inspection of the locally acquired RC8 distribution verified texture loader behaviour and the native wrapper requirement of ShipAPI.setSprite. No decompiled game implementation is shipped. The native renderer fixture has an isolated RC8 resource-registry bootstrap; production UFL code does not depend on that obfuscated internal class.
