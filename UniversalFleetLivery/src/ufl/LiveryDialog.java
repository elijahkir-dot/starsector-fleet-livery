package ufl;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;
import java.util.*;
import org.lwjgl.input.Keyboard;

/** Public campaign dialog APIs; deliberately avoids injecting into private refit UI classes. */
public final class LiveryDialog implements InteractionDialogPlugin {
  private InteractionDialogAPI dialog;
  private FleetMemberAPI selected;
  private String draftId = "imperial";
  private Palette custom;
  private int page;
  private TextFieldAPI[] fields;

  public void init(InteractionDialogAPI dialog) {
    this.dialog = dialog;
    custom = LiveryService.state().custom();
    root();
  }

  private void start(String title, String text) {
    dialog.getTextPanel().clear();
    dialog.getTextPanel().addPara(title, Misc.getHighlightColor());
    dialog.getTextPanel().addPara(text);
    dialog.getOptionPanel().clearOptions();
  }

  private void option(String text, String id) {
    dialog.getOptionPanel().addOption(text, id);
  }

  private void back(String id) {
    option("Back", id);
    dialog.getOptionPanel().setShortcut(id, Keyboard.KEY_ESCAPE, false, false, false, true);
  }

  private FleetMemberAPI previewMember() {
    List<FleetMemberAPI> m = LiveryService.members();
    return selected != null ? selected : m.isEmpty() ? null : m.get(0);
  }

  private void root() {
    selected = null;
    LiveryState s = LiveryService.state();
    start(
        "Fleet Livery",
        "Fleet default: "
            + Palettes.name(s.defaultId())
            + ".\nStatus: "
            + (s.enabled() ? "enabled" : "disabled")
            + ".\n"
            + "Precedence: ship > hull > size > fleet default. New acquisitions inherit these"
            + " rules.");
    option("Choose a fleet standard", "fleet");
    option("Ships, hull standards and size standards", "ships");
    option(s.enabled() ? "Disable all liveries (keep settings)" : "Enable liveries", "toggle");
    option("Restore original appearances and clear all rules", "reset");
    dialog
        .getOptionPanel()
        .addOptionConfirmation(
            "reset",
            "Clear all fleet, hull, size and individual livery rules? Custom colours are retained.",
            "Restore originals",
            "Cancel");
    option("Texture status / retry skipped images", "status");
    option("Close", "close");
    dialog.getOptionPanel().setShortcut("close", Keyboard.KEY_ESCAPE, false, false, false, true);
    showPreview(null);
  }

  private void list() {
    List<FleetMemberAPI> m = LiveryService.members();
    int pages = Math.max(1, (m.size() + 5) / 6);
    page = Math.max(0, Math.min(page, pages - 1));
    start(
        "Choose a ship",
        "Page "
            + (page + 1)
            + " / "
            + pages
            + ". Select a ship to preview paint or set a standard for its hull or size.");
    for (int i = page * 6; i < Math.min(m.size(), page * 6 + 6); i++) {
      FleetMemberAPI v = m.get(i);
      option(
          v.getShipName()
              + " - "
              + v.getHullSpec().getHullName()
              + " ["
              + Palettes.name(LiveryService.effectiveId(v))
              + "]",
          "ship:" + v.getId());
    }
    if (page > 0) option("Previous page", "prev");
    if (page + 1 < pages) option("Next page", "next");
    back("root");
  }

  private void palettes() {
    start(
        "Choose a paint scheme",
        "Select a scheme to preview it. No fleet changes are made until you choose Apply.");
    for (Map.Entry<String, String> e : Palettes.NAMES.entrySet())
      option(e.getValue(), "palette:" + e.getKey());
    if (selected != null) option("Use inherited standard for this ship", "inherit");
    back(selected == null ? "root" : "ships");
  }

  private Palette draft() {
    return "custom".equals(draftId) ? custom : Palettes.PRESETS.get(draftId);
  }

  private void confirmation() {
    String who = selected == null ? "your fleet" : selected.getShipName();
    start(
        "Preview: " + Palettes.name(draftId),
        "Previewing "
            + who
            + ". Left: original hull. Right: proposed livery. Weapons, separate glows and damage"
            + " overlays retain their own colours. Automatic masks infer regions from the original"
            + " image.");
    showPreview(draft());
    if (selected != null && LiveryService.hasOtherPaintjob(selected.getVariant()))
      dialog
          .getTextPanel()
          .addPara(
              "This ship has an active MagicLib paintjob. Remove that paintjob first; UFL will"
                  + " leave it untouched until then.",
              Misc.getNegativeHighlightColor());
    if ("custom".equals(draftId)) option("Edit custom colours / paint strength", "edit");
    if (selected == null) {
      option("Set fleet default (preserve exceptions)", "apply:default");
      option("Apply to entire fleet (clear all exceptions)", "apply:all");
    } else {
      option("Apply only to " + selected.getShipName(), "apply:ship");
      option("Set standard for hull: " + selected.getHullSpec().getHullName(), "apply:hull");
      option("Set standard for size: " + selected.getHullSpec().getHullSize(), "apply:size");
      option("Clear this ship's exception", "inherit");
      option("Clear this hull's standard", "clear:hull");
      option("Clear this size's standard", "clear:size");
    }
    option("Choose another scheme", "palettes");
    back(selected == null ? "root" : "ships");
  }

  private void editor() {
    start(
        "Custom colours",
        "Enter six-digit RGB hex colours, for example 1F3558. Strength is 0 to 100. These values"
            + " are a draft until you apply the Custom scheme. Applying updates every existing"
            + " assignment to Custom.");
    CustomPanelAPI panel = dialog.getVisualPanel().showCustomPanel(470, 460, null);
    TooltipMakerAPI t = panel.createUIElement(450, 440, false);
    fields = new TextFieldAPI[4];
    String[] labels = {
      "Primary body colour",
      "Secondary light panels",
      "Accent / existing coloured markings",
      "Paint strength (0-100)"
    };
    for (int i = 0; i < 4; i++) {
      t.addPara(labels[i], 12f);
      fields[i] = t.addTextField(250, 6f);
      fields[i].setText(
          i == 3
              ? Integer.toString(custom.strength)
              : String.format(Locale.ROOT, "%06X", custom.color(i).getRGB() & 0xffffff));
    }
    panel.addUIElement(t).inTL(10, 10);
    option("Preview these colours", "customPreview");
    back("confirm");
  }

  private void readCustom() {
    try {
      int[] c = new int[3];
      for (int i = 0; i < 3; i++) {
        String s = fields[i].getText().trim().replace("#", "");
        if (!s.matches("[0-9a-fA-F]{6}")) throw new IllegalArgumentException();
        c[i] = Integer.parseInt(s, 16);
      }
      int strength = Integer.parseInt(fields[3].getText().trim());
      if (strength < 0 || strength > 100) throw new IllegalArgumentException();
      custom = new Palette(c[0], c[1], c[2], strength);
      draftId = "custom";
      confirmation();
    } catch (RuntimeException e) {
      dialog
          .getTextPanel()
          .addPara(
              "Use six hexadecimal digits for each colour and a whole-number strength between 0 and"
                  + " 100.",
              Misc.getNegativeHighlightColor());
    }
  }

  private void showPreview(Palette p) {
    FleetMemberAPI m = previewMember();
    if (m == null) return;
    String source = m.getHullSpec().getSpriteName();
    try {
      Global.getSettings().loadTexture(source);
      SpriteAPI original = Global.getSettings().getSprite(source);
      SpriteAPI proposed =
          p == null ? Global.getSettings().getSprite(source) : LiveryTextures.sprite(source, p);
      if (proposed == null) {
        proposed = Global.getSettings().getSprite(source);
        dialog
            .getTextPanel()
            .addPara(
                "Preview skipped; original appearance retained. " + LiveryTextures.lastError,
                Misc.getNegativeHighlightColor());
      }
      Preview plugin = new Preview(original, proposed);
      CustomPanelAPI panel = dialog.getVisualPanel().showCustomPanel(470, 450, plugin);
      TooltipMakerAPI labels = panel.createUIElement(450, 70, false);
      labels.addPara(
          m.getShipName() + " / " + m.getHullSpec().getHullName(), Misc.getHighlightColor(), 6f);
      labels.addPara("ORIGINAL                          PREVIEW", 10f);
      panel.addUIElement(labels).inTL(12, 10);
    } catch (Exception e) {
      dialog
          .getTextPanel()
          .addPara("Hull preview unavailable: " + e.getMessage(), Misc.getNegativeHighlightColor());
      Global.getLogger(LiveryDialog.class).warn("UFL preview failed", e);
    }
  }

  private void apply(String scope) {
    LiveryState s = LiveryService.state();
    if ("custom".equals(draftId)) s.custom(custom);
    s.enabled(true);
    if ("default".equals(scope)) s.defaultId(draftId);
    else if ("all".equals(scope)) s.standardize(draftId);
    else if ("ship".equals(scope)) s.map("ships").put(selected.getId(), draftId);
    else if ("hull".equals(scope))
      s.map("hulls").put(selected.getHullSpec().getBaseHullId(), draftId);
    else if ("size".equals(scope))
      s.map("sizes").put(selected.getHullSpec().getHullSize().name(), draftId);
    LiveryService.sync();
    root();
    dialog
        .getTextPanel()
        .addPara("Applied " + Palettes.name(draftId) + ". Reopen refit if it was already open.");
  }

  public void optionSelected(String text, Object data) {
    if (!(data instanceof String)) return;
    String id = (String) data;
    if ("close".equals(id)) {
      dialog.dismiss();
      return;
    }
    if ("root".equals(id)) {
      root();
      return;
    }
    if ("fleet".equals(id)) {
      selected = null;
      palettes();
      return;
    }
    if ("ships".equals(id)) {
      list();
      return;
    }
    if ("prev".equals(id)) {
      page--;
      list();
      return;
    }
    if ("next".equals(id)) {
      page++;
      list();
      return;
    }
    if (id.startsWith("ship:")) {
      for (FleetMemberAPI m : LiveryService.members())
        if (m.getId().equals(id.substring(5))) {
          selected = m;
          break;
        }
      palettes();
      return;
    }
    if (id.startsWith("palette:")) {
      draftId = id.substring(8);
      confirmation();
      return;
    }
    if ("palettes".equals(id)) {
      palettes();
      return;
    }
    if ("confirm".equals(id)) {
      confirmation();
      return;
    }
    if ("edit".equals(id)) {
      editor();
      return;
    }
    if ("customPreview".equals(id)) {
      readCustom();
      return;
    }
    if (id.startsWith("apply:")) {
      apply(id.substring(6));
      return;
    }
    if ("inherit".equals(id) && selected != null) {
      LiveryService.state().map("ships").remove(selected.getId());
      LiveryService.sync();
      list();
      return;
    }
    if (id.startsWith("clear:") && selected != null) {
      String k = id.endsWith("hull") ? "hulls" : "sizes";
      LiveryService.state()
          .map(k)
          .remove(
              id.endsWith("hull")
                  ? selected.getHullSpec().getBaseHullId()
                  : selected.getHullSpec().getHullSize().name());
      LiveryService.sync();
      confirmation();
      return;
    }
    if ("toggle".equals(id)) {
      LiveryState s = LiveryService.state();
      s.enabled(!s.enabled());
      LiveryService.sync();
      root();
      return;
    }
    if ("reset".equals(id)) {
      LiveryService.state().reset();
      LiveryService.sync();
      root();
      return;
    }
    if ("status".equals(id)) {
      LiveryService.retryTextures();
      root();
      dialog
          .getTextPanel()
          .addPara(
              LiveryTextures.status()
                  + ". Skipped textures will be retried. Details are in starsector.log.");
    }
  }

  public void optionMousedOver(String text, Object data) {}

  public void advance(float amount) {}

  public void backFromEngagement(EngagementResultAPI result) {}

  public Object getContext() {
    return null;
  }

  public Map<String, MemoryAPI> getMemoryMap() {
    return null;
  }

  private static final class Preview extends BaseCustomUIPanelPlugin {
    private final SpriteAPI original, proposed;
    private PositionAPI position;

    Preview(SpriteAPI a, SpriteAPI b) {
      original = a;
      proposed = b;
      fit(a);
      fit(b);
    }

    private void fit(SpriteAPI s) {
      float scale = Math.min(190 / s.getWidth(), 305 / s.getHeight());
      s.setSize(s.getWidth() * scale, s.getHeight() * scale);
      s.setCenter(s.getWidth() / 2, s.getHeight() / 2);
      s.setAngle(0);
    }

    @Override
    public void positionChanged(PositionAPI p) {
      position = p;
    }

    @Override
    public void render(float alpha) {
      if (position == null) return;
      original.setAlphaMult(alpha);
      proposed.setAlphaMult(alpha);
      float y = position.getY() + position.getHeight() / 2 - 20;
      original.renderAtCenter(position.getX() + 120, y);
      proposed.renderAtCenter(position.getX() + 350, y);
    }
  }
}
