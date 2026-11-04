package ufl;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * Per-instance sprite swap also used when the engine creates a refit ship. No shared sprite tint.
 */
public final class LiveryHullmod extends BaseHullMod {
  private static boolean shaderWarned;

  @Override
  public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
    if (ship == null) return;
    try {
      if (ship.getVariant() == null) return;
      Palette p = LiveryService.palette(ship.getVariant());
      if (p == null) return;
      String path = LiveryTextures.get(ship.getHullSpec().getSpriteName(), p);
      if (path == null) return;
      SpriteAPI old = ship.getSpriteAPI(), replacement = Global.getSettings().getSprite(path);
      replacement.setSize(old.getWidth(), old.getHeight());
      replacement.setCenter(old.getCenterX(), old.getCenterY());
      replacement.setAngle(old.getAngle());
      replacement.setAlphaMult(old.getAlphaMult());
      replacement.setColor(old.getColor());
      replacement.setTexX(old.getTexX());
      replacement.setTexY(old.getTexY());
      replacement.setTexWidth(old.getTexWidth());
      replacement.setTexHeight(old.getTexHeight());
      replacement.setBlendFunc(old.getBlendSrc(), old.getBlendDest());
      ship.setSprite(replacement);
      optionalGraphicsLib(ship, path);
    } catch (Exception e) {
      Global.getLogger(LiveryHullmod.class).warn("UFL: sprite swap skipped", e);
    }
  }

  private static void optionalGraphicsLib(ShipAPI ship, String path) {
    if (!Config.graphicsLib || !Global.getSettings().getModManager().isModEnabled("shaderLib"))
      return;
    try {
      Class<?> lib =
          Global.getSettings().getScriptClassLoader().loadClass("org.dark.shaders.util.ShaderLib");
      MethodHandle method =
          MethodHandles.publicLookup()
              .findStatic(
                  lib,
                  "overrideShipTexture",
                  MethodType.methodType(void.class, ShipAPI.class, String.class));
      method.invokeExact(ship, path);
    } catch (Throwable e) {
      if (e instanceof VirtualMachineError) throw (VirtualMachineError) e;
      if (e instanceof ThreadDeath) throw (ThreadDeath) e;
      if (!shaderWarned) {
        shaderWarned = true;
        Global.getLogger(LiveryHullmod.class)
            .warn(
                "UFL: optional GraphicsLib override unavailable; lighting may use original colours",
                e);
      }
    }
  }

  @Override
  public boolean isApplicableToShip(ShipAPI ship) {
    return false;
  }
}
