package ufl;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;

/** Transient campaign script: no Java mod object is serialized into the save. */
public final class LiveryScript implements EveryFrameScript {
  private long nextScan;
  private boolean warned;

  public void advance(float amount) {
    try {
      long now = System.nanoTime();
      if (now >= nextScan) {
        LiveryService.sync();
        nextScan = now + 350000000L;
      }
      LiveryService.warmOne();
    } catch (Exception e) {
      if (!warned) {
        warned = true;
        Global.getLogger(LiveryScript.class).error("UFL: campaign synchronization failed", e);
      }
    }
  }

  public boolean isDone() {
    return false;
  }

  public boolean runWhilePaused() {
    return true;
  }
}
