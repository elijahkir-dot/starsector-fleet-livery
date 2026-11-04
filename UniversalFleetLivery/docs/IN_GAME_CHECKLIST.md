# 0.1.1 upgrade check

- Close the game and completely replace the old mod folder with this release. Avoid keeping two versions of the same mod in mods/.
- Enable the intended mod and open the campaign ability, then select a vanilla Wolf. Opening the ship must no longer show the file/reflection security message.
- Change a preset and, for Workshop, edit each channel and compare Original. Apply, reopen, cancel and restore Original.
- Verify a paint in refit and combat. Save, restart and load; verify the saved palette/design returns.
- Test one third-party hull and the normal GraphicsLib configuration, if installed. If any error remains, retain starsector.log from that session.

These steps require the real game and remain pending; the native-loader regression does not certify the campaign UI.

# In-game acceptance checklist

These checks are **pending** unless a later test record explicitly marks them complete. Record exact game version, operating system and mod versions with each run. The native renderer fixture is not a substitute for this checklist.

1. **Launcher:** enable only UFL on RC8. Confirm no dependency request or CSV/script loading error. Locate the UFL runtime/environment lines in `starsector.log`.
2. **Showcase mission:** open Fleet Livery: Rendering Test. Deploy all seven player Wolves. Compare original, Imperial, Naval, Crimson, Industrial, Arctic and Custom. Inspect alpha edges, weapon alignment, facing/rotation, hull damage and phase skimmer. Confirm enemy ships retain original colours.
3. **Campaign menu:** load a separate test save. Assign Fleet Livery from the ability picker. Verify all options fit at the user's resolution/UI scale. Test six-ship pagination, Escape/back, preview and close.
4. **Preview/apply separation:** preview each scheme and return without applying. Fleet state should not change. Confirm original/proposed hull aspect ratio and clarity.
5. **Custom:** enter three RGB hex colours, preview, then apply. Reject invalid digits and strength outside 0–100. Test strength zero. Change Custom later and check existing Custom assignments update.
6. **Refit:** open a painted ship in refit. Close/reopen after changing its livery. Confirm weapons, vents, capacitors, OP, S-mods and hull stats are unchanged. Refit weapons and verify livery persists.
7. **Combat/simulator:** deploy painted and exempt ships. Confirm only intended ships are recoloured, including NPC ships of identical hull. Test damage, overload, shields, phasing and recovery. Compare frame time after caches are warm.
8. **Inheritance:** set a fleet default; add a size rule; add a hull rule; add one individual override and one Original exemption. Verify precedence. Change the default with exceptions retained, then use whole-fleet standardisation and confirm exceptions clear.
9. **Acquisitions:** buy, salvage and manufacture a ship. Check default/class rule after joining the active player fleet. Keep one ship unpainted explicitly.
10. **Transfers:** move an overridden ship to storage and back. Sell a painted ship; inspect it if obtainable. Recover it again and record whether the member ID is retained. No unrelated NPC should inherit a global default.
11. **Save/load:** save, reload, refit, save again and reload. Check custom palette, hull/size rules, per-ship exemptions and new acquisitions. Check whether the ability remains assigned after save cleanup. Inspect XML only if diagnosing a problem; no `ufl.Livery*` object should be required by the serialized campaign.
12. **Disable/revert:** disable with settings retained; re-enable; restore all originals. Close/reopen refit after each. Save a new slot, quit, disable the mod in the launcher, and load that copy. Confirm removal before using it on the main save.
13. **Full mod stack:** repeat representative checks with the user's exact Nexerelin, Diable Avionics, Tahlan Shipworks and other versions. Test modular ships and at least one custom-rendered ship separately.
14. **Competing paintjobs:** apply an existing Diable/MagicLib paintjob. UFL should warn and yield. Remove it and confirm the UFL rule resumes. Unrecognised alternative-paint systems require separate testing.
15. **GraphicsLib:** test enabled and disabled, with lighting/normal maps. Confirm the generated diffuse texture is used without reverting during rendering. Inspect any optional-adapter warning.
16. **Stress/errors:** large fleet with shared and unique hulls, rapid Custom preview changes, missing/unsupported image, read-only mod directory and texture-budget exhaustion. Originals should remain available, errors should be clear and no file outside UFL's cache/temp directory should be changed.

Useful report bundle: version/startup section of `starsector.log`, `mods/enabled_mods.json`, relevant `mod_info.json` files, reproduction steps and a screenshot/video of the failure. Never send a license key.
