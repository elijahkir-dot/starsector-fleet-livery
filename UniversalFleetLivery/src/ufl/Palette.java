package ufl;

import java.awt.Color;
import java.util.Locale;

/** Immutable, dependency-free palette. Encoded values are safe variant tag data. */
public final class Palette {
  public final int primary, secondary, accent, strength;

  public Palette(int primary, int secondary, int accent, int strength) {
    this.primary = primary & 0xffffff;
    this.secondary = secondary & 0xffffff;
    this.accent = accent & 0xffffff;
    this.strength = Math.max(0, Math.min(100, strength));
  }

  public String encode() {
    return String.format(Locale.ROOT, "%06x_%06x_%06x_%d", primary, secondary, accent, strength);
  }

  public static Palette decode(String value) {
    String[] p = value.split("_");
    if (p.length != 4
        || !p[0].matches("[0-9a-fA-F]{6}")
        || !p[1].matches("[0-9a-fA-F]{6}")
        || !p[2].matches("[0-9a-fA-F]{6}")) {
      throw new IllegalArgumentException("Invalid livery palette: " + value);
    }
    int s = Integer.parseInt(p[3]);
    if (s < 0 || s > 100) throw new IllegalArgumentException("Strength must be 0..100");
    return new Palette(
        Integer.parseInt(p[0], 16), Integer.parseInt(p[1], 16), Integer.parseInt(p[2], 16), s);
  }

  public Palette withColor(int channel, int rgb) {
    return new Palette(
        channel == 0 ? rgb : primary,
        channel == 1 ? rgb : secondary,
        channel == 2 ? rgb : accent,
        strength);
  }

  public Color color(int channel) {
    return new Color(channel == 0 ? primary : channel == 1 ? secondary : accent);
  }
}
