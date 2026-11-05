package ufl;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCustomUIPanelPlugin;
import com.fs.starfarer.api.ui.PositionAPI;
import java.awt.Color;
import java.util.*;
import org.lwjgl.opengl.GL11;

/** Simple colour cards in the existing visual panel; no state queries or image buffers. */
public final class PaletteSwatches extends BaseCustomUIPanelPlugin {
  private final List<Palette> palettes;
  private PositionAPI position;
  private boolean failed;

  public PaletteSwatches(List<Palette> palettes) {
    this.palettes = new ArrayList<Palette>(palettes);
  }

  public void positionChanged(PositionAPI p) {
    position = p;
  }

  public void renderBelow(float alpha) {
    if (position == null || failed) return;
    try {
      GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_CURRENT_BIT);
      try {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        for (int row = 0; row < palettes.size(); row++)
          for (int channel = 0; channel < 3; channel++) {
            Color c = palettes.get(row).color(channel);
            GL11.glColor4f(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, alpha);
            float x = position.getX() + 12 + channel * 146,
                y = position.getY() + position.getHeight() - 82 - row * 92;
            GL11.glRectf(x, y, x + 138, y + 29);
          }
      } finally {
        GL11.glPopAttrib();
      }
    } catch (RuntimeException error) {
      failed = true;
      Global.getLogger(PaletteSwatches.class)
          .warn(
              "UFL: colour cards disabled after render error; palette options remain available",
              error);
    }
  }
}
