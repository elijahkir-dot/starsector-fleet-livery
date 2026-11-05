package ufl;

import com.fs.starfarer.api.*;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.fleet.*;
import java.util.*;

/** Models the exact native pick-up/remove and drop/reinsert lifecycle, not a mouse screenshot. */
public final class DragRegressionTest {
  public static void main(String[] args) {
    ShipHullSpecAPI hull =
        LifecycleTest.proxy(
            ShipHullSpecAPI.class,
            (m, a) ->
                m.equals("getBaseHullId")
                    ? "wolf"
                    : m.equals("getHullSize") ? ShipAPI.HullSize.FRIGATE : null);
    LifecycleTest.Variant original = new LifecycleTest.Variant(hull);
    LifecycleTest.Member member = new LifecycleTest.Member("drag-ship", hull, original.api);
    List<FleetMemberAPI> roster = new ArrayList<FleetMemberAPI>();
    roster.add(member.api);
    FleetDataAPI data =
        LifecycleTest.proxy(
            FleetDataAPI.class,
            (m, a) ->
                m.equals("getMembersListCopy") ? new ArrayList<FleetMemberAPI>(roster) : null);
    CampaignFleetAPI player =
        LifecycleTest.proxy(
            CampaignFleetAPI.class,
            (m, a) -> m.equals("getFleetData") ? data : m.equals("getId") ? "main" : null);
    CoreUITabId[] tab = {CoreUITabId.FLEET};
    CampaignUIAPI ui =
        LifecycleTest.proxy(
            CampaignUIAPI.class, (m, a) -> m.equals("getCurrentCoreTab") ? tab[0] : null);
    CharacterDataAPI character =
        LifecycleTest.proxy(
            CharacterDataAPI.class,
            (m, a) -> m.equals("getAbilities") ? new HashSet<String>() : null);
    Map<String, Object> persistent = new LinkedHashMap<String, Object>();
    Global.setSector(
        LifecycleTest.proxy(
            SectorAPI.class,
            (m, a) -> {
              switch (m) {
                case "getPlayerFleet":
                  return player;
                case "getCampaignUI":
                  return ui;
                case "getCharacterData":
                  return character;
                case "getPersistentData":
                  return persistent;
              }
              return null;
            }));
    LiveryService.install();
    LiveryService.state().defaultId("naval");
    LiveryService.sync();
    if (!member.variant.hasHullMod(LiveryService.HULLMOD))
      throw new AssertionError("Initial paint missing");
    ShipVariantAPI painted = member.variant;
    roster.clear(); // Native CoreUI removes a picked-up ship from the roster until placement.
    LiveryService.sync();
    if (args.length > 0 && args[0].equals("old")) {
      if (member.variant.hasHullMod(LiveryService.HULLMOD))
        throw new AssertionError("Old bug did not reproduce");
      System.out.println(
          "REPRODUCED 0.1.3: picked-up ship temporarily leaves roster, cleanup strips livery before"
              + " drop");
      return;
    }
    if (!member.variant.hasHullMod(LiveryService.HULLMOD) || member.variant != painted)
      throw new AssertionError("Paint changed while ship picked up");
    LiveryService.beforeSave();
    if (member.variant.hasHullMod(LiveryService.HULLMOD))
      throw new AssertionError("Held member not stripped before save");
    LiveryService.afterSave();
    if (!member.variant.hasHullMod(LiveryService.HULLMOD))
      throw new AssertionError("Held member not restored after save");
    roster.add(member.api);
    LiveryService.sync();
    if (!member.variant.hasHullMod(LiveryService.HULLMOD))
      throw new AssertionError("Drop lost paint");
    roster.clear();
    tab[0] = null;
    LiveryService.sync();
    if (member.variant.hasHullMod(LiveryService.HULLMOD))
      throw new AssertionError("Real removal was never cleaned");
    System.out.println(
        "PASS DragRegressionTest: paint retained across pickup/drop and save callbacks while held;"
            + " cleanup resumes after fleet UI closes; native UI interaction still needs retest");
  }
}
