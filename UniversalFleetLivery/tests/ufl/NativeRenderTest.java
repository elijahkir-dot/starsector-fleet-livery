package ufl;

import com.fs.starfarer.api.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.*;
import java.nio.ByteBuffer;
import java.util.*;
import javax.imageio.ImageIO;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

/** Real RC8 SettingsAPI and SpriteAPI + LWJGL, without a running campaign or combat engine. */
public final class NativeRenderTest {
  public static void main(String[] args) throws Exception {
    final File game = new File(args[0]), mod = new File(args[1]);
    File evidence = new File(args[2]);
    evidence.mkdirs();
    int height = args.length > 3 ? 420 : 800;
    Display.setDisplayMode(new DisplayMode(1260, height));
    Display.setTitle("UFL native sprite renderer test");
    Display.create();
    try {
      System.out.println("GL_VENDOR=" + GL11.glGetString(GL11.GL_VENDOR));
      System.out.println("GL_RENDERER=" + GL11.glGetString(GL11.GL_RENDERER));
      // Isolated RC8 fixture bootstrap: the normal launcher installs filesystem resource roots.
      // This is not mod runtime code and changes no game files.
      try (java.util.jar.JarFile jar =
          new java.util.jar.JarFile(new File(game, "fs.common_obf.jar"))) {
        java.util.Enumeration<java.util.jar.JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
          String name = entries.nextElement().getName();
          if (name.startsWith("com/fs/util/ooOOOO")
              && name.endsWith(".class")
              && name.indexOf('$') < 0) {
            Class<?> resources =
                Class.forName(name.substring(0, name.length() - 6).replace('/', '.'));
            Object instance = null;
            for (Method method : resources.getDeclaredMethods())
              if (java.lang.reflect.Modifier.isStatic(method.getModifiers())
                  && method.getReturnType() == resources
                  && method.getParameterCount() == 0) {
                instance = method.invoke(null);
                break;
              }
            resources.getMethod("\u00d500000").invoke(instance);
            resources
                .getMethod("\u00d500000", String.class)
                .invoke(instance, game.getAbsolutePath());
            if (args.length > 3)
              resources
                  .getMethod("\u00d500000", String.class)
                  .invoke(instance, new File(args[3]).getAbsolutePath());
            break;
          }
        }
      }
      Constructor<?> c =
          Class.forName("com.fs.starfarer.settings.StarfarerSettings$1").getDeclaredConstructor();
      c.setAccessible(true);
      final SettingsAPI real = (SettingsAPI) c.newInstance();
      final ModSpecAPI spec =
          (ModSpecAPI)
              Proxy.newProxyInstance(
                  NativeRenderTest.class.getClassLoader(),
                  new Class[] {ModSpecAPI.class},
                  (proxy, method, a) ->
                      "getPath".equals(method.getName()) ? mod.getAbsolutePath() : null);
      final ModManagerAPI manager =
          (ModManagerAPI)
              Proxy.newProxyInstance(
                  NativeRenderTest.class.getClassLoader(),
                  new Class[] {ModManagerAPI.class},
                  (proxy, method, a) ->
                      "getModSpec".equals(method.getName())
                          ? spec
                          : "isModEnabled".equals(method.getName()) ? false : null);
      SettingsAPI settings =
          (SettingsAPI)
              Proxy.newProxyInstance(
                  NativeRenderTest.class.getClassLoader(),
                  new Class[] {SettingsAPI.class},
                  (proxy, method, a) -> {
                    if ("getModManager".equals(method.getName())) return manager;

                    try {
                      return method.invoke(real, a);
                    } catch (InvocationTargetException ex) {
                      throw ex.getCause();
                    }
                  });
      Global.setSettings(settings);
      org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.WARN);
      GL11.glViewport(0, 0, 1260, height);
      GL11.glMatrixMode(GL11.GL_PROJECTION);
      GL11.glLoadIdentity();
      GL11.glOrtho(0, 1260, 0, height, -1, 1);
      GL11.glMatrixMode(GL11.GL_MODELVIEW);
      GL11.glLoadIdentity();
      GL11.glClearColor(.035f, .05f, .07f, 1);
      GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
      String[] images = {
        "graphics/ships/wolf/wolf_base.png",
        "graphics/ships/eagle/eagle_base.png",
        "graphics/ships/onslaught/onslaught_base.png",
        "graphics/ships/paragon.png"
      };
      List<Palette> palettes = new ArrayList<Palette>();
      palettes.add(null);
      palettes.addAll(Palettes.PRESETS.values());
      palettes.add(new Palette(0x285442, 0x99ccb0, 0xeac86a, 100));
      int rowCount = args.length > 3 ? 2 : images.length;
      for (int row = 0; row < rowCount; row++) {
        String source =
            args.length > 3
                ? (row % 2 == 0
                    ? "graphics/da/ships/diableavionics_draft.png"
                    : "graphics/da/ships/diableavionics_storm.png")
                : images[row];
        settings.loadTexture(source);
        BufferedImage image =
            ImageIO.read(new File(args.length > 3 ? new File(args[3]) : game, source));
        for (int col = 0; col < palettes.size(); col++) {
          SpriteAPI sprite =
              palettes.get(col) == null
                  ? settings.getSprite(source)
                  : LiveryTextures.sprite(source, palettes.get(col));
          if (sprite == null)
            throw new AssertionError("Failed texture: " + LiveryTextures.lastError);
          if (sprite.getTextureId() <= 0) throw new AssertionError("Invalid native texture ID");
          float scale = Math.min(158f / image.getWidth(), 170f / image.getHeight());
          sprite.setSize(image.getWidth() * scale, image.getHeight() * scale);
          sprite.setCenter(sprite.getWidth() / 2, sprite.getHeight() / 2);
          sprite.renderAtCenter(90 + col * 180, height - 110 - row * 190);
        }
      }
      GL11.glFinish();
      int err = GL11.glGetError();
      if (err != GL11.GL_NO_ERROR) throw new AssertionError("OpenGL error " + err);
      ByteBuffer pixels = BufferUtils.createByteBuffer(1260 * height * 4);
      GL11.glReadPixels(0, 0, 1260, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixels);
      BufferedImage shot = new BufferedImage(1260, height, BufferedImage.TYPE_INT_ARGB);
      int changed = 0;
      for (int y = 0; y < height; y++)
        for (int x = 0; x < 1260; x++) {
          int i = (y * 1260 + x) * 4,
              r = pixels.get(i) & 255,
              g = pixels.get(i + 1) & 255,
              b = pixels.get(i + 2) & 255;
          shot.setRGB(x, height - 1 - y, 0xff000000 | (r << 16) | (g << 8) | b);
          if (r > 20 || g > 25 || b > 30) changed++;
        }
      if (changed < 10000) throw new AssertionError("Empty framebuffer");
      ImageIO.write(
          shot,
          "png",
          new File(
              evidence, args.length > 3 ? "diable-native-renderer.png" : "native-renderer.png"));
      System.out.println(
          "PASS: "
              + (rowCount * palettes.size())
              + " real engine sprites rendered with valid texture IDs, no GL errors; "
              + changed
              + " non-background pixels.");
      System.out.println(
          "PASS: generated absolute-path PNGs accepted by the actual RC8 SettingsAPI texture"
              + " loader.");
      // Negative-path tests and bounded allocation, using the same real texture path.
      if (LiveryTextures.get("graphics/ships/missing-ufl-test.png", Palettes.PRESETS.get("naval"))
          != null) throw new AssertionError("missing source should fail gracefully");
      int cap = Config.maxTextures;
      Config.maxTextures = 1;
      String sample = args.length > 3 ? "graphics/da/ships/diableavionics_draft.png" : images[0];
      if (LiveryTextures.get(sample, new Palette(0x102030, 0x405060, 0x708090, 99)) != null)
        throw new AssertionError("texture cap not enforced");
      Config.maxTextures = cap;
      System.out.println("PASS: missing-image fallback and session texture budget guard.");
      System.out.println(
          "NOT TESTED: launcher, live combat, live refit, campaign UI, campaign save/load.");
    } finally {
      Display.destroy();
    }
  }
}
