# 0.1.4-alpha — 4 November 2026

- Simplify the root to Fleet Livery, Restore original fleet colours and Utilities, plus Close.
- Separate Ship / Fleet / Empire branches and policy. Scope inheritance is explicit; old active-fleet rules are preserved on upgrade.
- Apply empire defaults only to activated, non-station player-faction fleets. Add a public transient fleet-inflation listener and periodic ownership cleanup without inflating dormant fleets.
- Keep Custom assignments as independent snapshots. Migrate old shared-Custom assignments without changing their colour values.
- Retain runtime paint while the fleet UI temporarily removes a picked-up member from its roster. Do not mutate the actual roster or shared sprites. Resume cleanup after real departure.
- Extend pre-save stripping to all tracked eligible fleets, retaining plain-map preferences and transient runtime integration.
- Add scope, empire lifecycle, menu and drag regression tests. Reproduce the old drag failure against the released 0.1.3 JAR. Repeat real restricted-loader preview rendering.

Live campaign pickup/drop, NPC fleet behaviour, save/reload and the new menu layout remain acceptance items. Rendering classes are unchanged from 0.1.3; service and persistence policy are updated. No Workshop installer is included.

# 0.1.3-alpha — 4 November 2026

- Expand the data-driven factory catalogue to 18 palettes, retaining the five original IDs and exact colours.
- Add four-card palette pages with Primary/Secondary/Accent swatches, compact menu options and previous/next comparison.
- Add a native visual ship picker with deferred handoff, retain the text list, and group clearing rules in a separate menu.
- Allow editing a draft copy of any factory scheme in the existing Custom fields; no apply occurs while browsing/editing.
- Explain which separate weapon/glow/add-on graphics retain their own appearance.
- Add catalogue/controller tests and native tests of the actual swatch and ship-preview callbacks.

No change to the hull recolouring, texture transport, per-ship application or save-data classes. No Paint Workshop package is included.

# 0.1.2-alpha — 3 November 2026

- Fix Windows RC8 texture generation. Official Windows and Linux RC8 have different internal names for the same public image registrar. Bind the two verified signatures once using permitted public MethodHandles; no direct platform-specific native call remains in the JAR.
- Include the underlying exception class/message in future registration failures.
- Reproduce the exact 0.1.1 failure with official Windows binaries; verify corrected draws using both distributions' actual restricted loaders, including Ziggurat.

Replace the old folder completely while Starsector is closed. Palette/design encoding and persistence keys are unchanged. Test the two appearance mods separately; do not ask both to control the same ship.

# 0.1.1-alpha — 3 November 2026

Fix the shared “File access and reflection are not allowed to scripts” failure in 0.1.0-alpha.

- Read source resources only through SettingsAPI.openStream, assemble byte arrays without blocked ByteArrayOutputStream, and decode with an explicit in-memory ImageIO stream.
- Register generated images directly in RC8's native texture registry. No generated PNG files, disk caches, temporary directories, filesystem permissions or changes to script security.
- Keep the optional GraphicsLib adapter behind permitted public MethodHandles instead of blocked reflection. Its dispatch is tested against a signature fixture, not the real library.
- Add a regression fixture using Starsector's actual restricted script class loader. The original packages reproduce the reported error; corrected packages render successfully through the same loader.

Upgrade by closing the game and replacing the old mod directory completely. The mod ID, palette encoding, source hashes and save-data format are unchanged. Existing primitive palette/design records need no migration; full campaign upgrade acceptance remains pending. Old generated cache files are no longer used and can be deleted with the old directory.
