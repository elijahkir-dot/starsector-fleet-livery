package security;

import com.fs.starfarer.api.*;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.jar.*;
import javax.imageio.ImageIO;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

/** Test-only unrestricted bootstrap. The mod JAR is deliberately absent from the app classpath. */
public final class ScriptSecurityTest {
  private static SettingsAPI settings;
  private static ClassLoader loader;
  private static int checks;

  private static void check(boolean ok, String message) {
    if (!ok) throw new AssertionError(message);
    checks++;
  }

  private static Object call(String type, String name, Class<?>[] types, Object... args)
      throws Exception {
    try {
      return Class.forName(type, true, loader).getMethod(name, types).invoke(null, args);
    } catch (InvocationTargetException e) {
      Throwable cause = e.getCause();
      if (cause instanceof Exception) throw (Exception) cause;
      if (cause instanceof Error) throw (Error) cause;
      throw e;
    }
  }

  private static void bootstrap(File game, File mod) throws Exception {
    try (JarFile jar = new JarFile(new File(game, "fs.common_obf.jar"))) {
      for (Enumeration<JarEntry> entries = jar.entries(); entries.hasMoreElements(); ) {
        String n = entries.nextElement().getName();
        if (n.startsWith("com/fs/util/ooOOOO") && n.endsWith(".class") && n.indexOf('$') < 0) {
          Class<?> r = Class.forName(n.substring(0, n.length() - 6).replace('/', '.'));
          Object instance = null;
          for (Method m : r.getDeclaredMethods())
            if (Modifier.isStatic(m.getModifiers())
                && m.getReturnType() == r
                && m.getParameterCount() == 0) {
              instance = m.invoke(null);
              break;
            }
          r.getMethod("\u00d500000").invoke(instance);
          r.getMethod("\u00d500000", String.class).invoke(instance, game.getAbsolutePath());
          r.getMethod("\u00d500000", String.class).invoke(instance, mod.getAbsolutePath());
          break;
        }
      }
    }
    Constructor<?> c =
        Class.forName("com.fs.starfarer.settings.StarfarerSettings$1").getDeclaredConstructor();
    c.setAccessible(true);
    final SettingsAPI real = (SettingsAPI) c.newInstance();
    ModManagerAPI manager =
        (ModManagerAPI)
            java.lang.reflect.Proxy.newProxyInstance(
                ScriptSecurityTest.class.getClassLoader(),
                new Class[] {ModManagerAPI.class},
                (p, m, a) -> "isModEnabled".equals(m.getName()) ? "shaderLib".equals(a[0]) : null);
    settings =
        (SettingsAPI)
            java.lang.reflect.Proxy.newProxyInstance(
                ScriptSecurityTest.class.getClassLoader(),
                new Class[] {SettingsAPI.class},
                (p, m, a) -> {
                  if ("getModManager".equals(m.getName())) return manager;
                  if ("getScriptClassLoader".equals(m.getName())) return loader;
                  if ("getMergedSpreadsheetData".equals(m.getName()))
                    return new org.json.JSONArray();
                  try {
                    return m.invoke(real, a);
                  } catch (InvocationTargetException e) {
                    throw e.getCause();
                  }
                });
    Global.setSettings(settings);
  }

  private static void pixels(SpriteAPI sprite, BufferedImage expected) throws Exception {
    int w = 1, h = 1;
    while (w < expected.getWidth()) w *= 2;
    while (h < expected.getHeight()) h *= 2;
    ByteBuffer b = BufferUtils.createByteBuffer(w * h * 4);
    GL11.glBindTexture(GL11.GL_TEXTURE_2D, sprite.getTextureId());
    GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, b);
    for (int y = 0; y < expected.getHeight(); y++)
      for (int x = 0; x < expected.getWidth(); x++) {
        int i = (y * w + x) * 4;
        int actual =
            (b.get(i + 3) & 255) << 24
                | (b.get(i) & 255) << 16
                | (b.get(i + 1) & 255) << 8
                | (b.get(i + 2) & 255);
        if (actual != expected.getRGB(x, y))
          throw new AssertionError("Texel mismatch: " + x + "," + y);
      }
    check(true, "exact texels");
  }

  public static void main(String[] args) throws Exception {
    File game = new File(args[0]), mod = new File(args[1]), evidence = new File(args[2]);
    evidence.mkdirs();
    boolean workshop = new File(mod, "jars/UniversalShipPaintWorkshop.jar").isFile(),
        legacy = args.length > 3 && args[3].equals("legacy");
    String pkg = workshop ? "usw" : "ufl";
    File jar =
        new File(
            mod,
            "jars/" + (workshop ? "UniversalShipPaintWorkshop" : "UniversalFleetLivery") + ".jar");
    // The real loader checks devMode before applying its hard-coded blacklist.
    // Supply that launcher setting without changing the blacklist or security state.
    Class<?> engineSettings = Class.forName("com.fs.starfarer.settings.StarfarerSettings");
    for (Field f : engineSettings.getDeclaredFields())
      if (Modifier.isStatic(f.getModifiers()) && f.getType() == org.json.JSONObject.class) {
        f.setAccessible(true);
        if (f.get(null) == null) f.set(null, new org.json.JSONObject().put("devMode", false));
      }
    ClassLoader restricted =
        (ClassLoader)
            Class.forName("com.fs.starfarer.loading.scripts.new")
                .getConstructor(ClassLoader.class)
                .newInstance(ScriptSecurityTest.class.getClassLoader());
    for (String banned :
        new String[] {
          "java.io.File", "java.io.ByteArrayOutputStream", "java.lang.reflect.Method"
        }) {
      try {
        restricted.loadClass(banned);
        throw new AssertionError("Protection missing: " + banned);
      } catch (SecurityException expected) {
        check(true, banned + " blocked");
      }
    }
    loader = new URLClassLoader(new URL[] {jar.toURI().toURL()}, restricted);
    check(
        Class.forName(pkg + ".Palette", true, loader).getClassLoader() == loader,
        "mod bypassed restricted loader");
    bootstrap(game, mod);
    org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.WARN);
    Display.setDisplayMode(new DisplayMode(1120, 800));
    Display.create();
    try {
      System.out.println("SECURITY_LOADER=" + restricted.getClass().getName());
      System.out.println("GL_RENDERER=" + GL11.glGetString(GL11.GL_RENDERER));
      Class<?> paletteClass = Class.forName(pkg + ".Palette", true, loader);
      Object palette =
          paletteClass
              .getConstructor(int.class, int.class, int.class, int.class)
              .newInstance(0x092b55, 0x9da7b1, 0xffb635, 100);
      String sample = "graphics/ships/wolf/wolf_base.png";
      if (legacy) {
        try {
          if (workshop) call("usw.PaintSources", "get", new Class[] {String.class}, sample);
          else {
            Object id =
                call(
                    "ufl.LiveryTextures",
                    "get",
                    new Class[] {String.class, paletteClass},
                    sample,
                    palette);
            if (id == null) {
              String error =
                  (String)
                      Class.forName("ufl.LiveryTextures", true, loader)
                          .getField("lastError")
                          .get(null);
              if (error.contains("File access and reflection")) {
                System.out.println("REPRODUCED LEGACY ERROR: " + error);
                return;
              }
              throw new IllegalStateException(error);
            }
          }
          throw new AssertionError("Expected legacy security failure");
        } catch (SecurityException e) {
          System.out.println("REPRODUCED LEGACY ERROR: " + e.getMessage());
          return;
        }
      }
      int classes = 0;
      try (JarFile archive = new JarFile(jar)) {
        for (Enumeration<JarEntry> entries = archive.entries(); entries.hasMoreElements(); ) {
          String n = entries.nextElement().getName();
          if (!n.startsWith(pkg + "/") || !n.endsWith(".class")) continue;
          Class<?> c =
              Class.forName(n.substring(0, n.length() - 6).replace('/', '.'), false, loader);
          check(c.getClassLoader() == loader, "class escaped security parent");
          c.getDeclaredFields();
          c.getDeclaredMethods();
          c.getDeclaredConstructors();
          classes++;
        }
      }
      System.out.println(
          "PASS: " + classes + " implementation classes resolved through restricted loader");
      GL11.glViewport(0, 0, 1120, 800);
      GL11.glMatrixMode(GL11.GL_PROJECTION);
      GL11.glLoadIdentity();
      GL11.glOrtho(0, 1120, 0, 800, -1, 1);
      GL11.glMatrixMode(GL11.GL_MODELVIEW);
      GL11.glLoadIdentity();
      GL11.glClearColor(.035f, .05f, .07f, 1);
      GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
      String[] images = {
        sample,
        "graphics/ships/eagle/eagle_base.png",
        "graphics/ships/onslaught/onslaught_base.png",
        "graphics/ships/paragon.png"
      };
      int[][] colors = {
        {0x171b24, 0xb29147, 0x8b1326},
        {0x092b55, 0xaeb8c2, 0xffffff},
        {0x444c53, 0xe67f22, 0x82909a},
        {0xa5212b, 0x17191e, 0x8b929a},
        {0xe5e8e9, 0x2875bc, 0xadb8c5},
        {0x8833aa, 0x335566, 0xaaff44}
      };
      Class<?> designClass = workshop ? Class.forName("usw.PaintDesign", true, loader) : null;
      int draws = 0;
      for (int row = 0; row < images.length; row++)
        for (int col = 0; col < 7; col++) {
          SpriteAPI sprite;
          String source = images[row];
          if (col == 0) {
            settings.loadTexture(source);
            sprite = settings.getSprite(source);
          } else {
            int[] rgb = colors[col - 1];
            Object p =
                paletteClass
                    .getConstructor(int.class, int.class, int.class, int.class)
                    .newInstance(rgb[0], rgb[1], rgb[2], 100);
            Object d = workshop ? designClass.getConstructor(paletteClass).newInstance(p) : p;
            String id =
                (String)
                    call(
                        workshop ? "usw.PaintTextures" : "ufl.LiveryTextures",
                        workshop ? "committed" : "get",
                        new Class[] {String.class, workshop ? designClass : paletteClass},
                        source,
                        d);
            check(id != null, "no generated texture");
            check(id.startsWith(pkg + "_memory_"), "not memory texture");
            sprite = settings.getSprite(id);
            String id2 =
                (String)
                    call(
                        workshop ? "usw.PaintTextures" : "ufl.LiveryTextures",
                        workshop ? "committed" : "get",
                        new Class[] {String.class, workshop ? designClass : paletteClass},
                        source,
                        d);
            check(id.equals(id2), "cache miss");
          }
          check(sprite.getTextureId() > 0, "invalid GL id");
          float scale = Math.min(145 / sprite.getWidth(), 170 / sprite.getHeight());
          sprite.setSize(sprite.getWidth() * scale, sprite.getHeight() * scale);
          sprite.setCenter(sprite.getWidth() / 2, sprite.getHeight() / 2);
          sprite.renderAtCenter(80 + col * 160, 690 - row * 190);
          draws++;
        }
      if (workshop) {
        Object source = call("usw.PaintSources", "get", new Class[] {String.class}, sample);
        Class<?> sc = source.getClass();
        BufferedImage original = (BufferedImage) sc.getField("image").get(source);
        Object mask = sc.getField("mask").get(source);
        Class<?> pc = Class.forName("usw.PaintTextures$Preview", true, loader);
        Object preview = pc.getConstructor().newInstance();
        int textureId = 0;
        SpriteAPI sprite = null;
        BufferedImage expected = null;
        for (int i = 0; i < 50; i++) {
          Object p =
              paletteClass
                  .getConstructor(int.class, int.class, int.class, int.class)
                  .newInstance(i * 997, 0x8899aa, 0xd6ad43, 100);
          expected =
              (BufferedImage)
                  call(
                      "usw.PaintEngine",
                      "recolor",
                      new Class[] {BufferedImage.class, mask.getClass(), paletteClass},
                      original,
                      mask,
                      p);
          sprite =
              (SpriteAPI) pc.getMethod("update", BufferedImage.class).invoke(preview, expected);
          if (i == 0) textureId = sprite.getTextureId();
          check(textureId == sprite.getTextureId(), "preview reallocates");
        }
        pixels(sprite, expected);
        pc.getMethod("close").invoke(preview);
        check(!GL11.glIsTexture(textureId), "preview not deleted");
        System.out.println(
            "PASS: 50 preview edits reuse one GPU texture; exact RGBA texels; deletion on close");
      }
      // Signature-only adapter fixture: verifies MethodHandles execute under the security loader.
      Method adapter =
          Class.forName(pkg + (workshop ? ".PaintHullmod" : ".LiveryHullmod"), true, loader)
              .getDeclaredMethod("optionalGraphicsLib", ShipAPI.class, String.class);
      adapter.setAccessible(true);
      adapter.invoke(null, new Object[] {null, "adapter-check"});
      check(
          "adapter-check".equals(System.getProperty("security.fixture.adapter")),
          "optional adapter failed");
      System.out.println(
          "PASS: optional lighting adapter dispatch under restricted loader (test stub, not"
              + " GraphicsLib)");
      check(GL11.glGetError() == 0, "OpenGL error");
      GL11.glFinish();
      ByteBuffer b = BufferUtils.createByteBuffer(1120 * 800 * 4);
      GL11.glReadPixels(0, 0, 1120, 800, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, b);
      BufferedImage shot = new BufferedImage(1120, 800, BufferedImage.TYPE_INT_ARGB);
      int visible = 0;
      for (int y = 0; y < 800; y++)
        for (int x = 0; x < 1120; x++) {
          int i = (y * 1120 + x) * 4,
              r = b.get(i) & 255,
              g = b.get(i + 1) & 255,
              blue = b.get(i + 2) & 255;
          shot.setRGB(x, 799 - y, 0xff000000 | (r << 16) | (g << 8) | blue);
          if (r > 20 || g > 25 || blue > 30) visible++;
        }
      check(visible > 10000, "empty framebuffer");
      ImageIO.write(shot, "png", new File(evidence, "security-renderer.png"));
      System.out.println(
          "PASS: "
              + draws
              + " real RC8 native sprite draws; "
              + visible
              + " visible pixels; no GL errors");
      System.out.println(
          "PASS: " + checks + " assertions; no disk texture cache or security changes");
      System.out.println("NOT TESTED: live campaign UI/combat/refit/save-load, actual GraphicsLib");
    } finally {
      Display.destroy();
    }
  }
}
