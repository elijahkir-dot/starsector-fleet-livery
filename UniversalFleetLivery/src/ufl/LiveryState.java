package ufl;

import java.util.*;

/**
 * Only JDK maps/strings/booleans are persisted. Scope settings contain immutable colour snapshots.
 */
public final class LiveryState {
  public static final String INLINE = "paint:";
  private final Map<String, Object> data;

  public LiveryState(Map<String, Object> data) {
    this.data = data;
    boolean previousFleet = data.containsKey("default");
    if (!data.containsKey("enabled")) data.put("enabled", Boolean.TRUE);
    if (!data.containsKey("default")) data.put("default", "original");
    if (!data.containsKey("custom"))
      data.put("custom", new Palette(0x344c60, 0xc2cbd3, 0xd8ac4b, 100).encode());
    if (!data.containsKey("empire")) data.put("empire", "original");
    if (!data.containsKey("fleetInheritsEmpire")) data.put("fleetInheritsEmpire", !previousFleet);
    for (String name : new String[] {"ships", "hulls", "sizes", "fleets"}) map(name);
    if (!"2".equals(data.get("schema"))) {
      // Preserve the appearance of old shared-Custom assignments, then isolate future edits.
      String snapshot = snapshot(custom());
      for (String key : new String[] {"default", "empire"})
        if ("custom".equals(data.get(key))) data.put(key, snapshot);
      for (String key : new String[] {"ships", "hulls", "sizes", "fleets"})
        for (Map.Entry<String, String> e : map(key).entrySet())
          if ("custom".equals(e.getValue())) e.setValue(snapshot);
      data.put("schema", "2");
    }
  }

  @SuppressWarnings("unchecked")
  public Map<String, String> map(String name) {
    Object old = data.get(name);
    if (!(old instanceof Map)) {
      old = new LinkedHashMap<String, String>();
      data.put(name, old);
    }
    return (Map<String, String>) old;
  }

  public boolean enabled() {
    return Boolean.TRUE.equals(data.get("enabled"));
  }

  public void enabled(boolean value) {
    data.put("enabled", value);
  }

  public String defaultId() {
    return String.valueOf(data.get("default"));
  }

  public void defaultId(String id) {
    data.put("default", id);
    data.put("fleetInheritsEmpire", Boolean.FALSE);
  }

  public boolean fleetInheritsEmpire() {
    return Boolean.TRUE.equals(data.get("fleetInheritsEmpire"));
  }

  public String empireId() {
    return String.valueOf(data.get("empire"));
  }

  public void empireId(String id) {
    data.put("empire", id);
  }

  public String fleetRule(String fleetId, boolean main) {
    return main ? (fleetInheritsEmpire() ? null : defaultId()) : map("fleets").get(fleetId);
  }

  public void fleetRule(String fleetId, boolean main, String id) {
    if (main) defaultId(id);
    else map("fleets").put(fleetId, id);
  }

  public void inheritFleet(String fleetId, boolean main) {
    if (main) data.put("fleetInheritsEmpire", Boolean.TRUE);
    else map("fleets").remove(fleetId);
  }

  public Palette custom() {
    try {
      return Palette.decode(String.valueOf(data.get("custom")));
    } catch (RuntimeException e) {
      return Palettes.PRESETS.get("naval");
    }
  }

  public void custom(Palette palette) {
    data.put("custom", palette.encode());
  }

  public static String snapshot(Palette palette) {
    return INLINE + palette.encode();
  }

  public String resolveId(String shipId, String hullId, String size) {
    return resolveId(shipId, hullId, size, "", true);
  }

  public String resolveId(String shipId, String hullId, String size, String fleetId, boolean main) {
    if (!enabled()) return "original";
    String id = map("ships").get(shipId);
    if (id == null && main) id = map("hulls").get(hullId);
    if (id == null && main) id = map("sizes").get(size);
    if (id == null) id = fleetRule(fleetId, main);
    if (id == null) id = empireId();
    return valid(id) ? id : "original";
  }

  public Palette palette(String id) {
    if (id != null && id.startsWith(INLINE))
      try {
        return Palette.decode(id.substring(INLINE.length()));
      } catch (RuntimeException ignored) {
        return null;
      }
    return "custom".equals(id) ? custom() : Palettes.PRESETS.get(id);
  }

  private boolean valid(String id) {
    return Palettes.NAMES.containsKey(id)
        || (id != null && id.startsWith(INLINE) && palette(id) != null);
  }

  public void standardize(String id) {
    map("ships").clear();
    map("hulls").clear();
    map("sizes").clear();
    defaultId(id);
  }

  public void standardizeFleet(String fleetId, boolean main, Collection<String> ships, String id) {
    for (String ship : ships) map("ships").remove(ship);
    if (main) {
      map("hulls").clear();
      map("sizes").clear();
    }
    fleetRule(fleetId, main, id);
    enabled(true);
  }

  public void reset() {
    standardize("original");
    enabled(true);
  }
}
