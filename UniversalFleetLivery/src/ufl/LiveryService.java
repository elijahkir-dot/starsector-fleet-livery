package ufl;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.loading.VariantSource;
import java.util.*;

/**
 * Ownership and lifecycle boundary. Never mutate a hull specification or a shared stock variant.
 */
public final class LiveryService {
  public static final String MOD = "universal_fleet_livery",
      HULLMOD = "ufl_livery",
      ABILITY = "ufl_manage_livery",
      KEY = "ufl_settings_v1",
      TAG = "ufl_palette_";
  private static final Map<String, FleetMemberAPI> tracked =
      new LinkedHashMap<String, FleetMemberAPI>();
  private static boolean suspended;
  private static final Set<String> warmed = new HashSet<String>();
  private static final Set<String> warnings = new HashSet<String>();

  public static LiveryState state() {
    Object old = Global.getSector().getPersistentData().get(KEY);
    if (!(old instanceof Map)) {
      old = new LinkedHashMap<String, Object>();
      Global.getSector().getPersistentData().put(KEY, old);
    }
    @SuppressWarnings("unchecked")
    Map<String, Object> data = (Map<String, Object>) old;
    return new LiveryState(data);
  }

  public static List<FleetMemberAPI> members() {
    List<FleetMemberAPI> out = new ArrayList<FleetMemberAPI>();
    if (Global.getSector() != null && Global.getSector().getPlayerFleet() != null) {
      for (FleetMemberAPI m :
          Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy())
        if (m.getType() == FleetMemberType.SHIP) out.add(m);
    }
    return out;
  }

  public static String effectiveId(FleetMemberAPI m) {
    return state()
        .resolveId(
            m.getId(), m.getHullSpec().getBaseHullId(), m.getHullSpec().getHullSize().name());
  }

  public static void install() {
    suspended = false;
    tracked.clear();
    warmed.clear();
    state();
    Global.getSector().removeTransientScriptsOfClass(LiveryScript.class);
    Global.getSector().addTransientScript(new LiveryScript());
    if (!Global.getSector().getCharacterData().getAbilities().contains(ABILITY))
      Global.getSector().getCharacterData().addAbility(ABILITY);
    if (!Global.getSector().getPlayerFleet().hasAbility(ABILITY))
      Global.getSector().getPlayerFleet().addAbility(ABILITY);
    sync();
  }

  public static void sync() {
    if (suspended || Global.getSector() == null || Global.getSector().getPlayerFleet() == null)
      return;
    List<FleetMemberAPI> current = members();
    Set<String> ids = new HashSet<String>();
    LiveryState state = state();
    for (FleetMemberAPI m : current) {
      ids.add(m.getId());
      tracked.put(m.getId(), m);
      try {
        Palette p =
            state.palette(
                state.resolveId(
                    m.getId(),
                    m.getHullSpec().getBaseHullId(),
                    m.getHullSpec().getHullSize().name()));
        String desired = p == null || hasOtherPaintjob(m.getVariant()) ? null : TAG + p.encode();
        if (!matches(m.getVariant(), desired, 0)) {
          ShipVariantAPI v = m.getVariant().clone();
          v.setSource(VariantSource.REFIT);
          decorate(v, desired, 0);
          m.setVariant(v, false, false);
        }
      } catch (Exception e) {
        if (warnings.add(m.getId()))
          Global.getLogger(LiveryService.class)
              .warn("UFL: skipped incompatible member " + m.getId(), e);
      }
    }
    for (Iterator<Map.Entry<String, FleetMemberAPI>> it = tracked.entrySet().iterator();
        it.hasNext(); ) {
      Map.Entry<String, FleetMemberAPI> e = it.next();
      if (!ids.contains(e.getKey())) {
        safeStrip(e.getValue());
        it.remove();
      }
    }
  }

  private static boolean matches(ShipVariantAPI v, String desired, int depth) {
    if (depth > 12) return true;
    String found = tag(v);
    if (desired == null
        ? (found != null || v.hasHullMod(HULLMOD))
        : (!desired.equals(found) || !v.hasHullMod(HULLMOD))) return false;
    for (String slot : v.getModuleSlots()) {
      ShipVariantAPI child = v.getModuleVariant(slot);
      if (child != null && !matches(child, desired, depth + 1)) return false;
    }
    return true;
  }

  public static void decorate(ShipVariantAPI v, String desired, int depth) {
    if (depth > 12) return;
    v.setSource(VariantSource.REFIT);
    removeTags(v);
    v.removePermaMod(HULLMOD);
    v.removeMod(HULLMOD);
    if (desired != null) {
      v.addTag(desired);
      v.addPermaMod(HULLMOD);
    }
    for (String slot : new ArrayList<String>(v.getModuleSlots())) {
      ShipVariantAPI child = v.getModuleVariant(slot);
      if (child != null) {
        child = child.clone();
        decorate(child, desired, depth + 1);
        v.setModuleVariant(slot, child);
      }
    }
  }

  public static String tag(ShipVariantAPI v) {
    for (String s : v.getTags()) if (s.startsWith(TAG)) return s;
    return null;
  }

  public static boolean hasOtherPaintjob(ShipVariantAPI v) {
    for (String t : v.getTags()) if (t.startsWith("ML_paintjob-")) return true;
    return false;
  }

  public static Palette palette(ShipVariantAPI v) {
    if (hasOtherPaintjob(v)) return null;
    String t = tag(v);
    if (t == null) return null;
    try {
      return Palette.decode(t.substring(TAG.length()));
    } catch (RuntimeException e) {
      return null;
    }
  }

  private static void removeTags(ShipVariantAPI v) {
    for (String s : new ArrayList<String>(v.getTags())) if (s.startsWith(TAG)) v.removeTag(s);
  }

  private static void strip(ShipVariantAPI v, int depth) {
    if (v == null || depth > 12) return;
    v.removePermaMod(HULLMOD);
    v.removeMod(HULLMOD);
    removeTags(v);
    for (String slot : v.getModuleSlots()) strip(v.getModuleVariant(slot), depth + 1);
  }

  private static void warn(FleetMemberAPI member, String action, Exception error) {
    if (warnings.add(member.getId() + ":" + action))
      Global.getLogger(LiveryService.class)
          .warn("UFL: skipped " + action + " for " + member.getId(), error);
  }

  private static void safeStrip(FleetMemberAPI member) {
    try {
      strip(member.getVariant(), 0);
    } catch (Exception error) {
      warn(member, "cosmetic cleanup", error);
    }
  }

  /** Do one not-yet-warmed hull per frame; OpenGL must remain on this thread. */
  public static void warmOne() {
    if (suspended) return;
    for (FleetMemberAPI m : members()) {
      try {
        if (warm(m.getVariant(), 0)) return;
      } catch (Exception e) {
        warn(m, "prewarming", e);
      }
    }
  }

  private static boolean warm(ShipVariantAPI v, int depth) {
    if (depth > 12) return false;
    Palette p = palette(v);
    if (p != null) {
      String source = v.getHullSpec().getSpriteName(), key = source + "|" + p.encode();
      if (warmed.add(key)) {
        LiveryTextures.get(source, p);
        return true;
      }
    }
    for (String slot : v.getModuleSlots()) {
      ShipVariantAPI child = v.getModuleVariant(slot);
      if (child != null && warm(child, depth + 1)) return true;
    }
    return false;
  }

  /** Remove runtime attachments from the serialized campaign. Plain settings remain. */
  public static void beforeSave() {
    suspended = true;
    for (FleetMemberAPI m : tracked.values()) safeStrip(m);
    for (FleetMemberAPI m : members()) safeStrip(m);
    if (Global.getSector() != null && Global.getSector().getPlayerFleet() != null) {
      Global.getSector().getPlayerFleet().removeAbility(ABILITY);
      Global.getSector().getCharacterData().removeAbility(ABILITY);
    }
  }

  public static void afterSave() {
    suspended = false;
    if (Global.getSector() == null || Global.getSector().getPlayerFleet() == null) return;
    Global.getSector().getCharacterData().addAbility(ABILITY);
    if (!Global.getSector().getPlayerFleet().hasAbility(ABILITY))
      Global.getSector().getPlayerFleet().addAbility(ABILITY);
    sync();
  }

  public static void retryTextures() {
    warmed.clear();
    LiveryTextures.retryFailures();
  }

  private LiveryService() {}
}
