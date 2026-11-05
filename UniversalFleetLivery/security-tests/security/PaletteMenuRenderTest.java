package security;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import java.lang.reflect.*;
import java.util.*;
import org.lwjgl.opengl.GL11;

/** Actual new swatch callbacks and existing UFL ship-preview callbacks, under the mod loader. */
public final class PaletteMenuRenderTest {
  private static PositionAPI position() {
    return (PositionAPI)
        Proxy.newProxyInstance(
            PaletteMenuRenderTest.class.getClassLoader(),
            new Class[] {PositionAPI.class},
            (p, m, a) -> {
              switch (m.getName()) {
                case "getX":
                  return 10f;
                case "getY":
                  return 20f;
                case "getWidth":
                  return 470f;
                case "getHeight":
                  return 450f;
              }
              return null;
            });
  }

  public static void run(ClassLoader loader) throws Exception {
    Class<?> catalog = Class.forName("ufl.Palettes", true, loader);
    catalog.getMethod("load").invoke(null);
    Map<?, ?> presets = (Map<?, ?>) catalog.getField("PRESETS").get(null);
    if (presets.size() != 18)
      throw new AssertionError("Factory JSON not loaded by actual SettingsAPI");
    List<Object> colors = new ArrayList<Object>(presets.values());
    Class<?> swatches = Class.forName("ufl.PaletteSwatches", true, loader);
    for (int page = 0; page * 4 < colors.size(); page++) {
      CustomUIPanelPlugin cards =
          (CustomUIPanelPlugin)
              swatches
                  .getConstructor(List.class)
                  .newInstance(colors.subList(page * 4, Math.min(colors.size(), page * 4 + 4)));
      cards.positionChanged(position());
      for (int i = 0; i < 100; i++) cards.renderBelow(1f);
      Field failed = swatches.getDeclaredField("failed");
      failed.setAccessible(true);
      if (failed.getBoolean(cards)) throw new AssertionError("Healthy swatch callback disabled");
      java.nio.ByteBuffer sample = org.lwjgl.BufferUtils.createByteBuffer(4);
      GL11.glReadPixels(91, 402, 1, 1, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, sample);
      int actual =
          ((sample.get(0) & 255) << 16) | ((sample.get(1) & 255) << 8) | (sample.get(2) & 255);
      int expected =
          colors.get(page * 4).getClass().getField("primary").getInt(colors.get(page * 4));
      if (actual != expected) throw new AssertionError("Swatch pixel differs from palette colour");
      if (GL11.glGetError() != 0) throw new AssertionError("Palette cards GL error");
    }
    Class<?> palette = Class.forName("ufl.Palette", true, loader),
        textures = Class.forName("ufl.LiveryTextures", true, loader);
    Class<?> preview = Class.forName("ufl.LiveryDialog$Preview", true, loader);
    Constructor<?> ctor = preview.getDeclaredConstructor(SpriteAPI.class, SpriteAPI.class);
    ctor.setAccessible(true);
    int draws = 0;
    for (String source :
        new String[] {
          "graphics/ships/wolf/wolf_base.png",
          "graphics/ships/eagle/eagle_base.png",
          "graphics/ships/phase/zigguraut.png"
        }) {
      Global.getSettings().loadTexture(source);
      for (Object color : colors) {
        String id =
            (String) textures.getMethod("get", String.class, palette).invoke(null, source, color);
        if (id == null) throw new AssertionError("Palette texture failed");
        CustomUIPanelPlugin plugin =
            (CustomUIPanelPlugin)
                ctor.newInstance(
                    Global.getSettings().getSprite(source), Global.getSettings().getSprite(id));
        plugin.positionChanged(position());
        for (int frame = 0; frame < 3; frame++) {
          plugin.render(1f);
          draws += 2;
        }
        if (GL11.glGetError() != 0)
          throw new AssertionError("Actual ship preview callback GL error");
      }
    }
    System.out.println(
        "PASS: actual PaletteSwatches callbacks for all five pages / 500 frames; no GL errors");
    System.out.println(
        "PASS: actual LiveryDialog ship-preview callbacks: all 18 factory palettes on Wolf, Eagle,"
            + " Ziggurat; "
            + draws
            + " native sprite draws; no GL errors");
  }
}
