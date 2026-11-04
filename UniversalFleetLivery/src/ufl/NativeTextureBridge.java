package ufl;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.graphics.SpriteAPI;
import java.awt.image.BufferedImage;
import java.io.IOException;

/** Isolates the version-specific RC8 native image registrar. No filesystem or reflection. */
public final class NativeTextureBridge {
  public static SpriteAPI register(String id, BufferedImage image) throws IOException {
    try {
      // Verified in official RC8 jars and through the actual restricted script class loader.
      com.fs.graphics.oOoO.o00000(id, image);
      return Global.getSettings().getSprite(id);
    } catch (LinkageError e) {
      throw new IOException(
          "In-memory texture registration unavailable; this mod targets Starsector 0.98a-RC8", e);
    }
  }

  private NativeTextureBridge() {}
}
