package ufl;

import com.fs.starfarer.api.Global;
import org.json.JSONObject;

public final class Config {
  public static int maxPixels = 4194304, maxTextures = 256, maxTextureMB = 256;
  public static boolean graphicsLib = true;

  public static void load() {
    try {
      JSONObject j = Global.getSettings().loadJSON("data/config/ufl_settings.json");
      maxPixels = Math.max(65536, j.optInt("maxImagePixels", 4194304));
      maxTextures = Math.max(8, j.optInt("maxSessionTextures", 256));
      maxTextureMB = Math.max(16, j.optInt("maxTextureMB", 256));
      graphicsLib = j.optBoolean("graphicsLibIntegration", true);
    } catch (Exception e) {
      Global.getLogger(Config.class).warn("UFL: using default configuration", e);
    }
  }

  private Config() {}
}
