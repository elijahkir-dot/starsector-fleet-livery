package ufl;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.zip.CRC32;
import javax.imageio.ImageIO;

public final class CoreTest {
  private static int count;

  static void check(boolean v, String message) {
    if (!v) throw new AssertionError(message);
    count++;
  }

  public static void main(String[] args) throws Exception {
    Palette navy = Palettes.PRESETS.get("naval");
    check(navy.encode().equals(Palette.decode(navy.encode()).encode()), "palette round trip");
    for (String invalid :
        new String[] {
          "x", "ffffff_000000_000000_101", "zzzzzz_000000_ffffff_50", "ffffff_000000_ffffff_-1"
        }) {
      boolean rejected = false;
      try {
        Palette.decode(invalid);
      } catch (RuntimeException e) {
        rejected = true;
      }
      check(rejected, "reject invalid palette " + invalid);
    }
    Random r = new Random(7421);
    BufferedImage image = new BufferedImage(137, 91, BufferedImage.TYPE_INT_ARGB);
    for (int y = 0; y < 91; y++) for (int x = 0; x < 137; x++) image.setRGB(x, y, r.nextInt());
    int[] before = image.getRGB(0, 0, 137, 91, null, 0, 137);
    BufferedImage recolored = Recolor.apply(image, navy),
        again = Recolor.apply(image, navy),
        zero = Recolor.apply(image, new Palette(0, 0, 0, 0));
    check(
        recolored.getWidth() == 137 && recolored.getHeight() == 91,
        "odd image dimensions unchanged");
    int alphaErrors = 0, zeroErrors = 0, determinismErrors = 0, sourceErrors = 0;
    for (int y = 0; y < 91; y++)
      for (int x = 0; x < 137; x++) {
        int a = image.getRGB(x, y), b = recolored.getRGB(x, y);
        if ((a >>> 24) != (b >>> 24)) alphaErrors++;
        if (a != zero.getRGB(x, y)) zeroErrors++;
        if (b != again.getRGB(x, y)) determinismErrors++;
        if (a != before[y * 137 + x]) sourceErrors++;
      }
    check(alphaErrors == 0, "every alpha byte preserved");
    check(zeroErrors == 0, "strength zero exact identity");
    check(determinismErrors == 0, "deterministic output");
    check(sourceErrors == 0, "source not mutated");
    check(
        Recolor.pixel(0x0012a4ff, navy) == 0x0012a4ff,
        "hidden RGB of fully transparent pixels retained");
    Set<Long> hashes = new HashSet<Long>();
    for (Palette p : Palettes.PRESETS.values()) {
      CRC32 hash = new CRC32();
      for (int pixel : Recolor.apply(image, p).getRGB(0, 0, 137, 91, null, 0, 137)) {
        hash.update(pixel >> 24);
        hash.update(pixel >> 16);
        hash.update(pixel >> 8);
        hash.update(pixel);
      }
      hashes.add(hash.getValue());
    }
    check(hashes.size() == 5, "all five fixed palettes produce distinct images");
    Map<String, Object> raw = new LinkedHashMap<String, Object>();
    LiveryState s = new LiveryState(raw);
    check(s.resolveId("a", "wolf", "FRIGATE").equals("original"), "initial original");
    s.defaultId("imperial");
    check(
        s.resolveId("new", "wolf", "FRIGATE").equals("imperial"),
        "new acquisition inherits fleet default");
    s.map("sizes").put("FRIGATE", "naval");
    check(s.resolveId("a", "wolf", "FRIGATE").equals("naval"), "size precedence");
    s.map("hulls").put("wolf", "crimson");
    check(s.resolveId("a", "wolf", "FRIGATE").equals("crimson"), "hull precedence");
    s.map("ships").put("a", "arctic");
    check(s.resolveId("a", "wolf", "FRIGATE").equals("arctic"), "individual precedence");
    s.map("ships").put("a", "original");
    check(
        s.resolveId("a", "wolf", "FRIGATE").equals("original"),
        "original exemption does not inherit");
    s.map("ships").remove("a");
    check(
        s.resolveId("a", "wolf", "FRIGATE").equals("crimson"),
        "clear exception returns inheritance");
    s.enabled(false);
    check(s.resolveId("a", "wolf", "FRIGATE").equals("original"), "disable");
    s.enabled(true);
    check(s.resolveId("a", "wolf", "FRIGATE").equals("crimson"), "reenable retains rules");
    s.custom(new Palette(0x123456, 0xabcdef, 0xfedcba, 76));
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
      out.writeObject(raw);
    }
    @SuppressWarnings("unchecked")
    Map<String, Object> restored =
        (Map<String, Object>)
            new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())).readObject();
    LiveryState loaded = new LiveryState(restored);
    check(loaded.resolveId("a", "wolf", "FRIGATE").equals("crimson"), "settings serialization");
    check(loaded.custom().encode().equals(s.custom().encode()), "custom colors serialization");
    loaded.standardize("industrial");
    check(
        loaded.resolveId("a", "wolf", "FRIGATE").equals("industrial")
            && loaded.map("hulls").isEmpty()
            && loaded.map("ships").isEmpty(),
        "whole-fleet standard clears exceptions");
    loaded.reset();
    check(loaded.resolveId("new", "wolf", "FRIGATE").equals("original"), "reset originals");
    if (args.length > 0) {
      File dir = new File(args[0]);
      int files = 0, totalPixels = 0;
      long start = System.nanoTime();
      for (String file :
          new String[] {
            "wolf/wolf_base.png",
            "eagle/eagle_base.png",
            "onslaught/onslaught_base.png",
            "paragon.png",
            "lasher/lasher_base.png",
            "hammerhead/hammerhead_base.png"
          }) {
        BufferedImage hull = ImageIO.read(new File(dir, "graphics/ships/" + file));
        for (Palette p : Palettes.PRESETS.values()) {
          BufferedImage out = Recolor.apply(hull, p);
          int bad = 0;
          for (int y = 0; y < hull.getHeight(); y++)
            for (int x = 0; x < hull.getWidth(); x++)
              if ((hull.getRGB(x, y) >>> 24) != (out.getRGB(x, y) >>> 24)) bad++;
          check(bad == 0, "vanilla alpha " + file + " " + p.encode());
          files++;
          totalPixels += hull.getWidth() * hull.getHeight();
        }
      }
      System.out.printf(
          Locale.ROOT,
          "Vanilla corpus: %d bakes, %d pixels, %.1f ms total (includes image reads and alpha"
              + " checks)%n",
          files,
          totalPixels,
          (System.nanoTime() - start) / 1e6);
    }
    System.out.println("PASS CoreTest: " + count + " checks");
  }
}
