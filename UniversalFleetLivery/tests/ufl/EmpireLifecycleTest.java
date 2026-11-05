package ufl;

import com.fs.starfarer.api.*;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.listeners.*;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.fleet.*;
import java.util.*;

public final class EmpireLifecycleTest {
  private static int checks;

  private static void check(boolean ok, String label) {
    if (!ok) throw new AssertionError(label);
    checks++;
  }

  static final class Fleet {
    final CampaignFleetAPI api;
    final List<FleetMemberAPI> members = new ArrayList<FleetMemberAPI>();
    String faction = "player";
    boolean inflated = true, station;
    int reads;

    Fleet(String id) {
      FactionAPI fac =
          LifecycleTest.proxy(FactionAPI.class, (m, a) -> m.equals("getId") ? faction : null);
      FleetDataAPI data =
          LifecycleTest.proxy(
              FleetDataAPI.class,
              (m, a) -> {
                if (m.equals("getMembersListCopy")) {
                  reads++;
                  if (!inflated) throw new AssertionError("Dormant fleet was forced to inflate");
                  return new ArrayList<FleetMemberAPI>(members);
                }
                return null;
              });
      api =
          LifecycleTest.proxy(
              CampaignFleetAPI.class,
              (m, a) -> {
                switch (m) {
                  case "getId":
                    return id;
                  case "getFaction":
                    return fac;
                  case "isInflated":
                    return inflated;
                  case "isStationMode":
                    return station;
                  case "getFleetData":
                    return data;
                }
                return null;
              });
    }
  }

  public static void main(String[] args) {
    ShipHullSpecAPI hull =
        LifecycleTest.proxy(
            ShipHullSpecAPI.class,
            (m, a) ->
                m.equals("getBaseHullId")
                    ? "wolf"
                    : m.equals("getHullSize") ? ShipAPI.HullSize.FRIGATE : null);
    LifecycleTest.Variant stock = new LifecycleTest.Variant(hull);
    LifecycleTest.Member captain = new LifecycleTest.Member("captain", hull, stock.api),
        patrol = new LifecycleTest.Member("patrol", hull, stock.api),
        merchant = new LifecycleTest.Member("merchant", hull, stock.api),
        foreign = new LifecycleTest.Member("foreign", hull, stock.api),
        station = new LifecycleTest.Member("station", hull, stock.api);
    Fleet main = new Fleet("main"),
        a = new Fleet("patrol-fleet"),
        b = new Fleet("merchant-fleet"),
        enemy = new Fleet("enemy"),
        hidden = new Fleet("hidden"),
        base = new Fleet("base");
    main.members.add(captain.api);
    a.members.add(patrol.api);
    b.members.add(merchant.api);
    enemy.members.add(foreign.api);
    enemy.faction = "hegemony";
    hidden.inflated = false;
    base.station = true;
    base.members.add(station.api);
    List<CampaignFleetAPI> world =
        new ArrayList<CampaignFleetAPI>(
            Arrays.asList(a.api, b.api, enemy.api, hidden.api, base.api));
    LocationAPI location =
        LifecycleTest.proxy(
            LocationAPI.class,
            (m, x) -> m.equals("getFleets") ? new ArrayList<CampaignFleetAPI>(world) : null);
    List<Object> listeners = new ArrayList<Object>();
    ListenerManagerAPI manager =
        LifecycleTest.proxy(
            ListenerManagerAPI.class,
            (m, x) -> {
              if (m.equals("addListener")) {
                check(Boolean.TRUE.equals(x[1]), "empire listener is transient");
                listeners.add(x[0]);
              }
              if (m.equals("removeListenerOfClass")) listeners.removeIf(v -> v.getClass() == x[0]);
              return null;
            });
    CharacterDataAPI character =
        LifecycleTest.proxy(
            CharacterDataAPI.class,
            (m, x) -> m.equals("getAbilities") ? new HashSet<String>() : null);
    FactionAPI playerFaction =
        LifecycleTest.proxy(FactionAPI.class, (m, x) -> m.equals("getId") ? "player" : null);
    Map<String, Object> persisted = new LinkedHashMap<String, Object>();
    Global.setSector(
        LifecycleTest.proxy(
            SectorAPI.class,
            (m, x) -> {
              switch (m) {
                case "getPlayerFleet":
                  return main.api;
                case "getPlayerFaction":
                  return playerFaction;
                case "getAllLocations":
                  return Arrays.asList(location);
                case "getListenerManager":
                  return manager;
                case "getCharacterData":
                  return character;
                case "getPersistentData":
                  return persisted;
              }
              return null;
            }));
    LiveryService.install();
    LiveryService.install();
    check(listeners.size() == 1, "reload does not duplicate empire listener");
    LiveryState state = LiveryService.state();
    state.empireId("naval");
    state.inheritFleet("main", true);
    LiveryService.sync();
    check(
        LiveryService.palette(captain.variant)
            .encode()
            .equals(Palettes.PRESETS.get("naval").encode()),
        "main fleet inherits empire");
    check(
        LiveryService.palette(patrol.variant)
            .encode()
            .equals(Palettes.PRESETS.get("naval").encode()),
        "patrol inherits empire");
    check(
        !stock.api.hasHullMod(LiveryService.HULLMOD)
            && !foreign.variant.hasHullMod(LiveryService.HULLMOD),
        "shared stock and foreign fleet unchanged");
    check(
        hidden.reads == 0 && !station.variant.hasHullMod(LiveryService.HULLMOD),
        "no forced inflation or station painting");
    state.defaultId("imperial");
    state.fleetRule("patrol-fleet", false, "crimson");
    state.map("ships").put("merchant", "arctic");
    LiveryService.sync();
    check(
        LiveryService.palette(captain.variant)
            .encode()
            .equals(Palettes.PRESETS.get("imperial").encode()),
        "main fleet scope independent");
    check(
        LiveryService.palette(patrol.variant)
            .encode()
            .equals(Palettes.PRESETS.get("crimson").encode()),
        "NPC fleet scope independent");
    check(
        LiveryService.palette(merchant.variant)
            .encode()
            .equals(Palettes.PRESETS.get("arctic").encode()),
        "ship override independent");
    a.faction = "hegemony";
    LiveryService.sync();
    check(
        !patrol.variant.hasHullMod(LiveryService.HULLMOD),
        "ownership loss strips empire runtime paint");
    a.faction = "player";
    ((EmpireFleetListener) listeners.get(0)).reportFleetInflated(a.api, null);
    check(
        patrol.variant.hasHullMod(LiveryService.HULLMOD),
        "inflation callback restores a newly active owned fleet");
    ((EmpireFleetListener) listeners.get(0)).reportFleetInflated(hidden.api, null);
    check(hidden.reads == 0, "an early inflation notification does not force a dormant fleet");
    state.enabled(false);
    LiveryService.sync();
    check(
        !captain.variant.hasHullMod(LiveryService.HULLMOD)
            && !patrol.variant.hasHullMod(LiveryService.HULLMOD),
        "global disable restores all scoped fleets");
    state.enabled(true);
    LiveryService.sync();
    LiveryService.beforeSave();
    check(
        !captain.variant.hasHullMod(LiveryService.HULLMOD)
            && !patrol.variant.hasHullMod(LiveryService.HULLMOD)
            && !merchant.variant.hasHullMod(LiveryService.HULLMOD),
        "all touched fleets stripped before save");
    ((EmpireFleetListener) listeners.get(0)).reportFleetInflated(a.api, null);
    check(
        !patrol.variant.hasHullMod(LiveryService.HULLMOD),
        "inflation listener respects save suspension");
    LiveryService.afterSave();
    check(
        captain.variant.hasHullMod(LiveryService.HULLMOD)
            && patrol.variant.hasHullMod(LiveryService.HULLMOD),
        "all scoped paint restored after save");
    state.standardizeFleet("main", true, Arrays.asList("captain"), "original");
    LiveryService.sync();
    check(
        !captain.variant.hasHullMod(LiveryService.HULLMOD)
            && patrol.variant.hasHullMod(LiveryService.HULLMOD),
        "current fleet reset does not reset empire fleets");
    System.out.println(
        "PASS EmpireLifecycleTest: "
            + checks
            + " API-double checks; ownership, lazy inflation, listener, independent scopes and"
            + " multi-fleet save cleanup; actual NPC gameplay NOT tested");
  }
}
