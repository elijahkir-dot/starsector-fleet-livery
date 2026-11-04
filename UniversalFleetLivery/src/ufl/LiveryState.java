package ufl;

import java.util.LinkedHashMap;
import java.util.Map;

/** Only JDK maps, strings and booleans enter the save. No mod classes in persistent data. */
public final class LiveryState {
  private final Map<String, Object> data;

  public LiveryState(Map<String, Object> data) {
    this.data = data;
    if (!data.containsKey("schema")) data.put("schema", "1");
    if (!data.containsKey("enabled")) data.put("enabled", Boolean.TRUE);
    if (!data.containsKey("default")) data.put("default", "original");
    if (!data.containsKey("custom"))
      data.put("custom", new Palette(0x344c60, 0xc2cbd3, 0xd8ac4b, 100).encode());
    map("ships");
    map("hulls");
    map("sizes");
  }

  @SuppressWarnings("unchecked")
  public Map<String, String> map(String name) {
    Object m = data.get(name);
    if (!(m instanceof Map)) {
      m = new LinkedHashMap<String, String>();
      data.put(name, m);
    }
    return (Map<String, String>) m;
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
  }

  public Palette custom() {
    try {
      return Palette.decode(String.valueOf(data.get("custom")));
    } catch (RuntimeException e) {
      return Palettes.PRESETS.get("naval");
    }
  }

  public void custom(Palette p) {
    data.put("custom", p.encode());
  }

  public String resolveId(String shipId, String baseHullId, String size) {
    if (!enabled()) return "original";
    String id = map("ships").get(shipId);
    if (id == null) id = map("hulls").get(baseHullId);
    if (id == null) id = map("sizes").get(size);
    if (id == null) id = defaultId();
    return Palettes.NAMES.containsKey(id) ? id : "original";
  }

  public Palette palette(String id) {
    return "custom".equals(id) ? custom() : Palettes.PRESETS.get(id);
  }

  public void standardize(String id) {
    map("ships").clear();
    map("hulls").clear();
    map("sizes").clear();
    defaultId(id);
  }

  public void reset() {
    standardize("original");
    enabled(true);
  }
}
