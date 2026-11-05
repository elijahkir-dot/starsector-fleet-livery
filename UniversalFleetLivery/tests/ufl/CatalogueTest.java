package ufl;

import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.*;
import org.json.*;

/** Catalogue migration/validation and output invariants. No game runtime objects needed. */
public final class CatalogueTest {
  private static int checks;

  private static void check(boolean value, String message) {
    if (!value) throw new AssertionError(message);
    checks++;
  }

  public static void main(String[] args) throws Exception {
    Map<String, String> legacy = new LinkedHashMap<String, String>();
    for (Map.Entry<String, Palette> e : Palettes.PRESETS.entrySet())
      legacy.put(e.getKey(), e.getValue().encode());
    JSONObject catalog =
        new JSONObject(Files.readString(Paths.get("data/config/ufl_palettes.json")));
    Palettes.install(catalog.getJSONArray("palettes"));
    check(Palettes.PRESETS.size() == 18, "18 factory palettes loaded");
    for (Map.Entry<String, String> e : legacy.entrySet())
      check(
          e.getValue().equals(Palettes.PRESETS.get(e.getKey()).encode()),
          "legacy colours unchanged: " + e.getKey());
    check(
        Palettes.NAMES.size() == 20
            && !Palettes.PRESETS.containsKey("original")
            && !Palettes.PRESETS.containsKey("custom"),
        "special entries remain separate");
    BufferedImage source = new BufferedImage(3, 1, BufferedImage.TYPE_INT_ARGB);
    source.setRGB(0, 0, 0x7f756c63);
    source.setRGB(1, 0, 0xff8b3330);
    source.setRGB(2, 0, 0x00123456);
    Set<String> encodings = new HashSet<String>();
    for (Map.Entry<String, Palette> e : Palettes.PRESETS.entrySet()) {
      check(encodings.add(e.getValue().encode()), "unique palette: " + e.getKey());
      BufferedImage out = Recolor.apply(source, e.getValue());
      check(
          (out.getRGB(0, 0) >>> 24) == 127
              && (out.getRGB(1, 0) >>> 24) == 255
              && out.getRGB(2, 0) == 0x00123456,
          "alpha preserved: " + e.getKey());
      LiveryState state = new LiveryState(new LinkedHashMap<String, Object>());
      state.defaultId(e.getKey());
      check(
          state.palette(state.resolveId("new", "test", "DESTROYER")) != null,
          "new palette resolves for acquisitions");
    }
    String before = Palettes.PRESETS.get("imperial").encode();
    for (String bad : new String[] {"duplicate", "reserved", "invalidhex", "missinglegacy"}) {
      JSONArray a = new JSONObject(catalog.toString()).getJSONArray("palettes");
      if (bad.equals("duplicate")) a.getJSONObject(1).put("id", "imperial");
      if (bad.equals("reserved")) a.getJSONObject(1).put("id", "custom");
      if (bad.equals("invalidhex")) a.getJSONObject(1).put("primary", "blue");
      if (bad.equals("missinglegacy")) a.getJSONObject(0).put("id", "replacement");
      boolean rejected = false;
      try {
        Palettes.install(a);
      } catch (Exception expected) {
        rejected = true;
      }
      check(
          rejected
              && Palettes.PRESETS.size() == 18
              && Palettes.PRESETS.get("imperial").encode().equals(before),
          "bad catalogue is atomic: " + bad);
    }
    System.out.println(
        "PASS CatalogueTest: "
            + checks
            + " checks; 18 palettes, stable legacy IDs/colours, transparent pixels and atomic"
            + " validation");
  }
}
