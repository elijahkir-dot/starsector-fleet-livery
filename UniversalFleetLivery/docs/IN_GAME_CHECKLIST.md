# Live acceptance — 0.1.4-alpha

These checks remain pending in a real campaign. Automated/native-renderer results are documented separately. Record exact RC build, OS and mod versions. Keep Workshop disabled while testing Fleet Livery.

1. Close the game, replace only UniversalFleetLivery, enable it, and confirm no launcher/script errors. Open the assigned Fleet Livery campaign ability.
2. Check the three main choices plus Close at your actual UI scale. Open Fleet Livery and confirm Ship, Fleet and Empire are distinct. Check Back/Escape and Utilities.
3. Browse all five palette pages and Previous/Next scheme. Preview must not apply. Test a visual-grid selection and text-list selection, cancelling each picker/editor.
4. Set empire colours. On an upgraded save, the old active-fleet override should remain until explicitly switched to inheritance. Set a different fleet rule, then a different individual ship rule. Verify ship > fleet > empire; test Original exemptions.
5. In Utilities, verify legacy hull/size rules still work only in the active fleet and remain below ship overrides. Standardise the active fleet and confirm its exceptions clear after confirmation.
6. Set different Custom colours on a ship, fleet and empire. Change one; the others must retain their earlier snapshots. Reopen a ship through both pickers and check its saved values.
7. Pick up a painted ship in the ordinary fleet screen, hold it for over 5 seconds, move it, drop it and repeat. Its paint should remain while held. Verify reorder has not changed any saved rule.
8. Reset original fleet colours, first cancelling confirmation and then accepting. Only the current fleet should reset; the empire/other-fleet rules remain. Re-enable fleet inheritance explicitly afterward.
9. With a colony, inspect a normal activated player-faction patrol and set a separate fleet override. Let another patrol activate later and check empire inheritance. Foreign/commission/allied fleets and stations must remain unchanged. Fleet deletion/recreation with a new ID inherits anew.
10. Buy, salvage and manufacture ships; check inheritance after joining an eligible fleet. Transfer to storage/sell/return, checking cleanup and whether the engine retains member ID. Test a confirmed transfer during the fleet screen as well as after closing it.
11. Reopen refit after palette changes, change weapons and confirm stats/loadout are unaffected. In combat/simulator, inspect rotation, alpha edges, damage, overload/phasing, weapons and independent glows. Test modular and scripted hulls separately.
12. Save, quit and reload. Check all three scopes, Custom snapshots, exemptions, old-save migration and newly acquired ships. Check the ability remains available/assigned. Repeat with a failed-save path only if encountered naturally; retain its log.
13. Disable liveries from Utilities, then re-enable. Check eligible empire fleets as well as your own. Save a separate slot with liveries disabled, quit, disable UFL in the launcher and verify that copy loads before trusting uninstall.
14. Repeat with the actual Nexerelin/Diable/Tahlan stack, existing MagicLib paintjobs and GraphicsLib lighting. UFL should yield to known MagicLib paintjobs. Test the other paint mod separately.
15. Stress many unique/shared hulls and repeated previews. Exercise a missing source or exhausted texture budget in a test installation. Originals must remain visible, errors must identify the cause, and no source assets should change. Record frame-time behaviour after caches warm.

Useful report: starsector.log from the failing session, enabled_mods.json, exact mod versions, reproduction steps and screenshot/video. Do not include a license key.
