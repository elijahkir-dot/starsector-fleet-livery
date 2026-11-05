package ufl;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ModSpecAPI;

public final class LiveryModPlugin extends BaseModPlugin {
  @Override
  public void onApplicationLoad() {
    Config.load();
    Palettes.load();
    Global.getLogger(LiveryModPlugin.class)
        .info(
            "Universal Fleet Livery 0.1.4-alpha; runtime "
                + Global.getSettings().getVersionString());
    for (ModSpecAPI mod : Global.getSettings().getModManager().getEnabledModsCopy())
      Global.getLogger(LiveryModPlugin.class)
          .info("UFL environment: " + mod.getId() + " " + mod.getVersion());
  }

  @Override
  public void onGameLoad(boolean newGame) {
    LiveryService.install();
  }

  @Override
  public void beforeGameSave() {
    LiveryService.beforeSave();
  }

  @Override
  public void afterGameSave() {
    LiveryService.afterSave();
  }

  @Override
  public void onGameSaveFailed() {
    LiveryService.afterSave();
  }
}
