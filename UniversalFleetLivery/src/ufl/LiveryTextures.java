package ufl;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.graphics.SpriteAPI;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.*;
import javax.imageio.ImageIO;
import javax.imageio.stream.MemoryCacheImageInputStream;

/** Session-local procedural textures. Every source read goes through the game's SettingsAPI. */
public final class LiveryTextures {
  private static final Map<String, String> loaded = new LinkedHashMap<String, String>();
  private static final Map<String, String> failures = new LinkedHashMap<String, String>();
  private static final LinkedHashMap<String, Source> sources =
      new LinkedHashMap<String, Source>(16, .75f, true) {
        private static final long serialVersionUID = 1L;

        protected boolean removeEldestEntry(Map.Entry<String, Source> entry) {
          return size() > 12;
        }
      };
  private static long allocatedBytes;
  public static String lastError = "";

  private static class Source {
    BufferedImage image;
    String digest;

    Source(BufferedImage image, String digest) {
      this.image = image;
      this.digest = digest;
    }
  }

  public static String get(String original, Palette palette) {
    String request = original + "|" + palette.encode();
    if (loaded.containsKey(request)) return loaded.get(request);
    if (failures.containsKey(request)) return null;
    try {
      Source s = sources.get(original);
      if (s == null) {
        byte[] bytes = read(original);
        BufferedImage image;
        try (MemoryCacheImageInputStream input =
            new MemoryCacheImageInputStream(new ByteArrayInputStream(bytes))) {
          Iterator<javax.imageio.ImageReader> readers = ImageIO.getImageReaders(input);
          if (!readers.hasNext()) throw new IOException("Unsupported image format: " + original);
          javax.imageio.ImageReader reader = readers.next();
          try {
            reader.setInput(input);
            if ((long) reader.getWidth(0) * reader.getHeight(0) > Config.maxPixels)
              throw new IOException("Image exceeds configured pixel limit");
            image = reader.read(0);
          } finally {
            reader.dispose();
          }
        }
        if (image == null) throw new IOException("No image decoder for " + original);
        s = new Source(image, hash(bytes));
        sources.put(original, s);
      }
      long bytes = (long) powerOfTwo(s.image.getWidth()) * powerOfTwo(s.image.getHeight()) * 16 / 3;
      if (loaded.size() >= Config.maxTextures
          || allocatedBytes + bytes > (long) Config.maxTextureMB * 1024 * 1024)
        throw new IOException("Session texture budget reached; raise limits or restart Starsector");
      String id =
          "ufl_memory_"
              + hash(
                  (s.digest + "|" + Recolor.VERSION + "|" + palette.encode())
                      .getBytes(java.nio.charset.StandardCharsets.UTF_8));
      NativeTextureBridge.register(id, Recolor.apply(s.image, palette));
      loaded.put(request, id);
      allocatedBytes += bytes;
      return id;
    } catch (Exception e) {
      lastError = original + ": " + e.getMessage();
      failures.put(request, lastError);
      Global.getLogger(LiveryTextures.class)
          .warn("UFL: leaving original appearance: " + lastError, e);
      return null;
    }
  }

  private static byte[] read(String path) throws IOException {
    List<byte[]> chunks = new ArrayList<byte[]>();
    int total = 0;
    try (InputStream in = Global.getSettings().openStream(path)) {
      byte[] block = new byte[16384];
      int count;
      while ((count = in.read(block)) != -1) {
        total += count;
        if (total > 32 * 1024 * 1024) throw new IOException("Source exceeds 32 MiB");
        if (count > 0) chunks.add(Arrays.copyOf(block, count));
      }
    }
    byte[] result = new byte[total];
    int offset = 0;
    for (byte[] chunk : chunks) {
      System.arraycopy(chunk, 0, result, offset, chunk.length);
      offset += chunk.length;
    }
    return result;
  }

  public static SpriteAPI sprite(String original, Palette palette) {
    String id = get(original, palette);
    return id == null ? null : Global.getSettings().getSprite(id);
  }

  private static int powerOfTwo(int n) {
    int result = 1;
    while (result < n) result <<= 1;
    return result;
  }

  private static String hash(byte[] bytes) throws Exception {
    byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);
    StringBuilder result = new StringBuilder();
    for (byte value : digest) result.append(String.format(Locale.ROOT, "%02x", value & 255));
    return result.toString();
  }

  public static String status() {
    return loaded.size()
        + " textures; approx "
        + allocatedBytes / 1024 / 1024
        + " MiB; "
        + failures.size()
        + " skipped";
  }

  public static void retryFailures() {
    failures.clear();
    lastError = "";
  }

  private LiveryTextures() {}
}
