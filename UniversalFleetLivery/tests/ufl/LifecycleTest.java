package ufl;

import com.fs.starfarer.api.*;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.fleet.*;
import java.lang.reflect.*;
import java.util.*;

/** API doubles exercise lifecycle transitions; these are NOT real campaign save tests. */
public final class LifecycleTest {
  private static int checks;

  private static void check(boolean v, String m) {
    if (!v) throw new AssertionError(m);
    checks++;
  }

  interface Handler {
    Object call(String method, Object[] args);
  }

  @SuppressWarnings("unchecked")
  static <T> T proxy(Class<T> type, Handler h) {
    return (T)
        Proxy.newProxyInstance(
            type.getClassLoader(),
            new Class[] {type},
            (p, m, a) -> {
              if (m.getName().equals("hashCode")) return System.identityHashCode(p);
              if (m.getName().equals("equals")) return p == a[0];
              if (m.getName().equals("toString")) return type.getSimpleName() + " test double";
              Object v = h.call(m.getName(), a);
              if (v == null && m.getReturnType().isPrimitive()) {
                if (m.getReturnType() == boolean.class) return false;
                if (m.getReturnType() == float.class) return 0f;
                if (m.getReturnType() == long.class) return 0L;
                return 0;
              }
              return v;
            });
  }

  static class Variant {
    final Set<String> tags = new LinkedHashSet<String>(), mods = new LinkedHashSet<String>();
    final Map<String, ShipVariantAPI> children = new LinkedHashMap<String, ShipVariantAPI>();
    final ShipHullSpecAPI hull;
    ShipVariantAPI api;

    Variant(ShipHullSpecAPI hull) {
      this.hull = hull;
      api =
          proxy(
              ShipVariantAPI.class,
              (name, a) -> {
                switch (name) {
                  case "clone":
                    Variant clone = new Variant(hull);
                    clone.tags.addAll(tags);
                    clone.mods.addAll(mods);
                    clone.children.putAll(children);
                    return clone.api;
                  case "hasTag":
                    return tags.contains(a[0]);
                  case "getTags":
                    return tags;
                  case "getHullSpec":
                    return hull;
                  case "addTag":
                    tags.add((String) a[0]);
                    break;
                  case "removeTag":
                    tags.remove(a[0]);
                    break;
                  case "hasHullMod":
                    return mods.contains(a[0]);
                  case "addPermaMod":
                  case "addMod":
                    mods.add((String) a[0]);
                    break;
                  case "removeMod":
                  case "removePermaMod":
                    mods.remove(a[0]);
                    break;
                  case "getModuleSlots":
                    return new ArrayList<String>(children.keySet());
                  case "getModuleVariant":
                    return children.get(a[0]);
                  case "setModuleVariant":
                    children.put((String) a[0], (ShipVariantAPI) a[1]);
                    break;
                }
                return null;
              });
    }
  }

  static class Member {
    ShipVariantAPI variant;
    int assignments;
    final FleetMemberAPI api;

    Member(String id, ShipHullSpecAPI hull, ShipVariantAPI initial) {
      variant = initial;
      api =
          proxy(
              FleetMemberAPI.class,
              (name, a) -> {
                switch (name) {
                  case "getId":
                    return id;
                  case "getType":
                    return FleetMemberType.SHIP;
                  case "getHullSpec":
                    return hull;
                  case "getVariant":
                    return variant;
                  case "setVariant":
                    variant = (ShipVariantAPI) a[0];
                    assignments++;
                    break;
                }
                return null;
              });
    }
  }

  public static void main(String[] args) {
    ShipHullSpecAPI hull =
        proxy(
            ShipHullSpecAPI.class,
            (name, a) -> {
              switch (name) {
                case "getBaseHullId":
                  return "wolf";
                case "getHullSize":
                  return ShipAPI.HullSize.FRIGATE;
                case "getSpriteName":
                  return "graphics/ships/wolf/wolf_base.png";
              }
              return null;
            });
    Variant stock = new Variant(hull), moduleStock = new Variant(hull);
    stock.children.put("module", moduleStock.api);
    stock.mods.add("unrelated_mod");
    stock.tags.add("unrelated_tag");
    Member a = new Member("owned-a", hull, stock.api), b = new Member("owned-b", hull, stock.api);
    List<FleetMemberAPI> fleet = new ArrayList<FleetMemberAPI>();
    fleet.add(a.api);
    fleet.add(b.api);
    FleetDataAPI fleetData =
        proxy(
            FleetDataAPI.class,
            (name, x) ->
                name.equals("getMembersListCopy") ? new ArrayList<FleetMemberAPI>(fleet) : null);
    Set<String> abilities = new LinkedHashSet<String>(), known = new LinkedHashSet<String>();
    CampaignFleetAPI player =
        proxy(
            CampaignFleetAPI.class,
            (name, x) -> {
              switch (name) {
                case "getFleetData":
                  return fleetData;
                case "hasAbility":
                  return abilities.contains(x[0]);
                case "addAbility":
                  abilities.add((String) x[0]);
                  break;
                case "removeAbility":
                  abilities.remove(x[0]);
                  break;
              }
              return null;
            });
    CharacterDataAPI character =
        proxy(
            CharacterDataAPI.class,
            (name, x) -> {
              switch (name) {
                case "getAbilities":
                  return known;
                case "addAbility":
                  known.add((String) x[0]);
                  break;
                case "removeAbility":
                  known.remove(x[0]);
                  break;
              }
              return null;
            });
    Map<String, Object> persisted = new LinkedHashMap<String, Object>();
    SectorAPI sector =
        proxy(
            SectorAPI.class,
            (name, x) -> {
              switch (name) {
                case "getPlayerFleet":
                  return player;
                case "getPersistentData":
                  return persisted;
                case "getCharacterData":
                  return character;
              }
              return null;
            });
    Global.setSector(sector);
    LiveryService.install();
    check(a.variant == stock.api, "no clone or gameplay change for original appearance");
    LiveryService.state().defaultId("naval");
    LiveryService.sync();
    check(
        a.variant != stock.api && b.variant != stock.api && a.variant != b.variant,
        "per-ship variant isolation");
    check(
        LiveryService.tag(stock.api) == null && !stock.api.hasHullMod(LiveryService.HULLMOD),
        "stock variant left untouched");
    check(LiveryService.tag(moduleStock.api) == null, "stock module untouched");
    check(
        LiveryService.palette(a.variant.getModuleVariant("module")) != null,
        "module receives inherited palette");
    check(
        a.variant.hasHullMod("unrelated_mod") && a.variant.getTags().contains("unrelated_tag"),
        "unrelated modifications retained");
    a.variant.addTag("ML_paintjob-test");
    LiveryService.sync();
    check(
        !a.variant.hasHullMod(LiveryService.HULLMOD) && a.variant.hasTag("ML_paintjob-test"),
        "known paintjob takes precedence without deleting its tag");
    a.variant.removeTag("ML_paintjob-test");
    LiveryService.sync();
    check(
        a.variant.hasHullMod(LiveryService.HULLMOD),
        "own palette resumes when competing paintjob removed");
    int assigned = a.assignments;
    LiveryService.sync();
    check(a.assignments == assigned, "stable policy does not recreate variants");
    Member c = new Member("new-c", hull, stock.api);
    fleet.add(c.api);
    LiveryService.sync();
    check(LiveryService.palette(c.variant) != null, "newly acquired ship inherits");
    LiveryService.state().map("ships").put("owned-a", "original");
    LiveryService.sync();
    check(
        LiveryService.palette(a.variant) == null && LiveryService.palette(b.variant) != null,
        "individual original exemption");
    fleet.remove(b.api);
    LiveryService.sync();
    check(
        LiveryService.palette(b.variant) == null && !b.variant.hasHullMod(LiveryService.HULLMOD),
        "sold/transferred ship runtime cosmetic removed");
    LiveryService.beforeSave();
    check(
        !c.variant.hasHullMod(LiveryService.HULLMOD) && LiveryService.tag(c.variant) == null,
        "before-save strips runtime hullmod and tags");
    check(
        !abilities.contains(LiveryService.ABILITY) && !known.contains(LiveryService.ABILITY),
        "before-save removes mod ability objects");
    LiveryService.sync();
    check(!c.variant.hasHullMod(LiveryService.HULLMOD), "save suspension prevents reattachment");
    LiveryService.afterSave();
    check(
        c.variant.hasHullMod(LiveryService.HULLMOD) && abilities.contains(LiveryService.ABILITY),
        "after-save restoration");
    LiveryService.state().enabled(false);
    LiveryService.sync();
    check(!c.variant.hasHullMod(LiveryService.HULLMOD), "disable restores runtime variants");
    LiveryService.state().enabled(true);
    LiveryService.sync();
    check(c.variant.hasHullMod(LiveryService.HULLMOD), "reenable restores configured appearance");
    Member malformed =
        new Member(
            "bad-variant",
            hull,
            proxy(
                ShipVariantAPI.class,
                (method, values) -> {
                  if (method.equals("getTags"))
                    throw new IllegalStateException("intentional invalid variant");
                  return null;
                }));
    fleet.add(0, malformed.api);
    LiveryService.sync();
    fleet.remove(c.api); // The remaining valid ship is an Original exemption; no GL stub is needed.
    LiveryService.warmOne();
    fleet.add(c.api);
    check(
        c.variant.hasHullMod(LiveryService.HULLMOD),
        "malformed member does not block other members");
    LiveryService.beforeSave();
    check(
        !c.variant.hasHullMod(LiveryService.HULLMOD) && !abilities.contains(LiveryService.ABILITY),
        "malformed cleanup does not block save stripping");
    fleet.remove(malformed.api);
    LiveryService.afterSave();
    System.out.println(
        "PASS LifecycleTest: "
            + checks
            + " checks using API doubles; live engine save/load NOT tested");
  }
}
