package ufl;

import java.awt.image.BufferedImage;

/** Automatic, continuous masks. Does not claim semantic hull segmentation. */
public final class Recolor {
  public static final String VERSION = "ufl-tone-3";

  public static BufferedImage apply(BufferedImage source, Palette p) {
    int w = source.getWidth(), h = source.getHeight();
    BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    int[] pixels = source.getRGB(0, 0, w, h, null, 0, w);
    for (int i = 0; i < pixels.length; i++) pixels[i] = pixel(pixels[i], p);
    out.setRGB(0, 0, w, h, pixels, 0, w);
    return out;
  }

  public static int pixel(int argb, Palette p) {
    int alpha = argb >>> 24;
    if (alpha == 0 || p.strength == 0) return argb;
    double r = (argb >> 16) & 255, g = (argb >> 8) & 255, b = argb & 255;
    double max = Math.max(r, Math.max(g, b)), min = Math.min(r, Math.min(g, b));
    double lum = (.2126 * r + .7152 * g + .0722 * b) / 255.;
    double saturation = max == 0 ? 0 : (max - min) / max;
    // Existing chromatic paint becomes accent. Light neutral panels become secondary.
    double accent = smooth(.18, .55, saturation) * smooth(12, 48, max - min);
    double secondary = (1 - accent) * smooth(.43, .82, lum) * .82;
    double primary = 1 - accent - secondary;
    double shade = .18 + 1.14 * lum;
    // Retain bright specular highlights; no new edges, silhouettes, or markings.
    double highlight = smooth(.78, 1, lum) * .30;
    double blend = p.strength / 100.;
    int rr =
        channel(
            r,
            p.primary >> 16,
            p.secondary >> 16,
            p.accent >> 16,
            primary,
            secondary,
            accent,
            shade,
            highlight,
            blend);
    int gg =
        channel(
            g,
            p.primary >> 8,
            p.secondary >> 8,
            p.accent >> 8,
            primary,
            secondary,
            accent,
            shade,
            highlight,
            blend);
    int bb =
        channel(
            b,
            p.primary,
            p.secondary,
            p.accent,
            primary,
            secondary,
            accent,
            shade,
            highlight,
            blend);
    return (alpha << 24) | (rr << 16) | (gg << 8) | bb;
  }

  private static int channel(
      double original,
      int a,
      int b,
      int c,
      double wa,
      double wb,
      double wc,
      double shade,
      double hi,
      double strength) {
    double target =
        (((a & 255) * wa + (b & 255) * wb + (c & 255) * wc) * shade) * (1 - hi) + 255 * hi;
    return (int)
        Math.max(0, Math.min(255, Math.round(original * (1 - strength) + target * strength)));
  }

  private static double smooth(double a, double b, double x) {
    double t = Math.max(0, Math.min(1, (x - a) / (b - a)));
    return t * t * (3 - 2 * t);
  }

  private Recolor() {}
}
