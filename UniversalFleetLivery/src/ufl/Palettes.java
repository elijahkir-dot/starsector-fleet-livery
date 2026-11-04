package ufl;

import java.util.LinkedHashMap;
import java.util.Map;

public final class Palettes {
  public static final Map<String, Palette> PRESETS = new LinkedHashMap<String, Palette>();
  public static final Map<String, String> NAMES = new LinkedHashMap<String, String>();

  static {
    add("imperial", "Imperial - black and gold", 0x292b32, 0x8e7950, 0xe7b94f);
    add("naval", "Naval - dark blue and silver", 0x263c62, 0xb6c5d3, 0x7599ce);
    add("crimson", "Crimson - red and black", 0x9e2937, 0x32323a, 0xe04a45);
    add("industrial", "Industrial - gunmetal and orange", 0x48515a, 0x8c9396, 0xed812d);
    add("arctic", "Arctic - white and blue", 0xd5e3ec, 0x6f94b5, 0x347cc2);
    NAMES.put("custom", "Custom - your colours");
    NAMES.put("original", "Original appearance");
  }

  private static void add(String id, String name, int a, int b, int c) {
    PRESETS.put(id, new Palette(a, b, c, 100));
    NAMES.put(id, name);
  }

  public static String name(String id) {
    String n = NAMES.get(id);
    return n == null ? "Original appearance" : n;
  }

  private Palettes() {}
}
