package org.dark.shaders.util;

import com.fs.starfarer.api.combat.ShipAPI;

/** Test-only signature fixture; this is not GraphicsLib. */
public final class ShaderLib {
  public static void overrideShipTexture(ShipAPI ship, String path) {
    System.setProperty("security.fixture.adapter", path);
  }
}
