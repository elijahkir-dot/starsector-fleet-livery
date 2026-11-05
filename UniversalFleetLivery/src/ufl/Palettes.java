package ufl;

import com.fs.starfarer.api.Global;
import java.util.*;
import org.json.*;

/** Stable legacy IDs; the optional JSON catalogue extends the five safe built-in fallbacks. */
public final class Palettes {
  public static final Map<String, Palette> PRESETS = new LinkedHashMap<String, Palette>();
  public static final Map<String, String> NAMES = new LinkedHashMap<String, String>();
  public static final Map<String, String> GROUPS = new LinkedHashMap<String, String>();

  static {
    add("imperial", "Imperial - black and gold", 0x292b32, 0x8e7950, 0xe7b94f);
    add("naval", "Naval - dark blue and silver", 0x263c62, 0xb6c5d3, 0x7599ce);
    add("crimson", "Crimson - red and black", 0x9e2937, 0x32323a, 0xe04a45);
    add("industrial", "Industrial - gunmetal and orange", 0x48515a, 0x8c9396, 0xed812d);
    add("arctic", "Arctic - white and blue", 0xd5e3ec, 0x6f94b5, 0x347cc2);
    specialNames();
  }

  public static void load() {
    try {
      install(
          Global.getSettings().loadJSON("data/config/ufl_palettes.json").getJSONArray("palettes"));
    } catch (Exception error) {
      Global.getLogger(Palettes.class)
          .warn("UFL: palette catalogue unavailable/invalid; existing palettes retained", error);
    }
  }

  static void install(JSONArray entries) throws JSONException {
    Map<String, Palette> colors = new LinkedHashMap<String, Palette>();
    Map<String, String> names = new LinkedHashMap<String, String>(),
        groups = new LinkedHashMap<String, String>();
    if (entries.length() > 100) throw new IllegalArgumentException("Maximum 100 factory palettes");
    for (int i = 0; i < entries.length(); i++) {
      JSONObject entry = entries.getJSONObject(i);
      String id = entry.getString("id"), name = entry.getString("name").trim();
      if (!id.matches("[a-z][a-z0-9_]{0,31}")
          || id.equals("custom")
          || id.equals("original")
          || colors.containsKey(id)
          || name.isEmpty()
          || name.length() > 80)
        throw new IllegalArgumentException("Invalid/duplicate palette id or name: " + id);
      int[] rgb = new int[3];
      int channel = 0;
      for (String key : new String[] {"primary", "secondary", "accent"}) {
        String hex = entry.getString(key);
        if (!hex.matches("[0-9a-fA-F]{6}"))
          throw new IllegalArgumentException("Invalid RGB colour for " + id);
        rgb[channel++] = Integer.parseInt(hex, 16);
      }
      int strength = entry.optInt("strength", 100);
      if (strength < 0 || strength > 100)
        throw new IllegalArgumentException("Invalid strength for " + id);
      colors.put(id, new Palette(rgb[0], rgb[1], rgb[2], strength));
      names.put(id, name);
      groups.put(id, entry.optString("group", "Factory"));
    }
    for (String legacy : new String[] {"imperial", "naval", "crimson", "industrial", "arctic"})
      if (!colors.containsKey(legacy))
        throw new IllegalArgumentException("Missing legacy palette: " + legacy);
    PRESETS.clear();
    PRESETS.putAll(colors);
    NAMES.clear();
    NAMES.putAll(names);
    GROUPS.clear();
    GROUPS.putAll(groups);
    specialNames();
  }

  private static void add(String id, String name, int a, int b, int c) {
    PRESETS.put(id, new Palette(a, b, c, 100));
    NAMES.put(id, name);
    GROUPS.put(id, "Factory");
  }

  private static void specialNames() {
    NAMES.put("custom", "Custom - your colours");
    NAMES.put("original", "Original appearance");
  }

  public static String name(String id) {
    if (id != null && id.startsWith(LiveryState.INLINE)) return "Custom colours";
    String name = NAMES.get(id);
    return name == null ? "Original appearance" : name;
  }

  private Palettes() {}
}
