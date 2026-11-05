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
  private CampaignFleetAPI targetFleet;
  private String scope = "fleet";
  private int fleetPage;
  private String draftId = "imperial";
  private Palette custom;
  private int page, palettePage;
  private FleetMemberAPI pendingSelection;
  private int pickerFrames;
  private static final int PALETTES_PER_PAGE = 4;
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
    List<FleetMemberAPI> members =
        targetFleet == null ? LiveryService.members() : LiveryService.members(targetFleet);
    return selected != null ? selected : members.isEmpty() ? null : members.get(0);
  }

  private void root() {
    selected = null;
    targetFleet = Global.getSector().getPlayerFleet();
    start(
        "Fleet Livery",
        "Choose a livery scope, restore this fleet's original appearance, or open utilities.");
    option("Fleet Livery", "livery");
    option("Restore original fleet colours", "resetFleet");
    dialog
        .getOptionPanel()
        .addOptionConfirmation(
            "resetFleet",
            "Restore original paint on your current fleet and clear its individual, hull and size"
                + " rules? Empire and other-fleet settings are retained.",
            "Restore original colours",
            "Cancel");
    option("Utilities", "utilities");
    option("Close", "close");
    dialog.getOptionPanel().setShortcut("close", Keyboard.KEY_ESCAPE, false, false, false, true);
    showPreview(null);
  }

  private void scopes() {
    selected = null;
    targetFleet = Global.getSector().getPlayerFleet();
    LiveryState settings = LiveryService.state();
    start(
        "Livery scope",
        "Empire standard: "
            + Palettes.name(settings.empireId())
            + ". Active fleet: "
            + (settings.fleetInheritsEmpire()
                ? "inherits empire"
                : Palettes.name(settings.defaultId()))
            + ".\n"
            + "Ship: a single vessel. Fleet: one fleet. Empire: the default for loaded"
            + " player-faction fleets, including normal patrols. Specific choices override"
            + " inherited colours.");
    option("Ship livery", "scope:ship");
    option("Fleet livery", "scope:fleet");
    option("Empire livery", "scope:empire");
    back("root");
  }

  private void shipMenu() {
    scope = "ship";
    selected = null;
    targetFleet = Global.getSector().getPlayerFleet();
    start(
        "Ship livery",
        "Paint one ship in your active fleet. Other ship, fleet and empire choices are retained.");
    option("Choose a ship (visual grid)", "picker");
    option("Choose a ship (list)", "ships");
    back("livery");
  }

  private void fleetMenu() {
    scope = "fleet";
    selected = null;
    start(
        "Fleet livery",
        "Choose your active fleet or an activated player-faction fleet. Fleets without their own"
            + " setting inherit the empire standard.");
    option("Your active fleet", "fleetMain");
    option("Other player-faction fleets", "fleetList");
    back("livery");
  }

  private void fleetList() {
    List<CampaignFleetAPI> fleets = LiveryService.fleets();
    fleets.remove(Global.getSector().getPlayerFleet());
    int pages = Math.max(1, (fleets.size() + 4) / 5);
    fleetPage = Math.max(0, Math.min(fleetPage, pages - 1));
    start(
        "Player-faction fleets",
        "Loaded, non-station fleets only. Hidden fleets acquire the empire standard when the game"
            + " activates them.");
    for (int i = fleetPage * 5; i < Math.min(fleets.size(), fleetPage * 5 + 5); i++)
      option(fleets.get(i).getName(), "fleetId:" + fleets.get(i).getId());
    if (fleetPage > 0) option("Previous fleets", "fleetPrev");
    if (fleetPage + 1 < pages) option("More fleets", "fleetNext");
    back("scope:fleet");
  }

  private void utilities() {
    LiveryState settings = LiveryService.state();
    start(
        "Utilities",
        "Advanced rules and diagnostics. Ship choices override fleet defaults; fleets can inherit"
            + " the empire standard. Advanced hull/size rules apply to your active fleet.");
    option("Advanced ship / hull / size rules", "advanced");
    option(settings.enabled() ? "Disable liveries (keep settings)" : "Enable liveries", "toggle");
    option("Texture status / retry", "status");
    option("What gets painted?", "help");
    back("root");
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
    back("advanced".equals(scope) ? "utilities" : "scope:ship");
  }

  private void palettes() {
    List<String> ids = new ArrayList<String>(Palettes.PRESETS.keySet());
    int pages = Math.max(1, (ids.size() + PALETTES_PER_PAGE - 1) / PALETTES_PER_PAGE);
    palettePage = Math.max(0, Math.min(palettePage, pages - 1));
    start(
        "Palette library  /  " + (palettePage + 1) + " of " + pages,
        "Choose a scheme to preview on "
            + (selected == null
                ? ("empire".equals(scope)
                    ? "an example ship for your empire"
                    : targetFleet.getName())
                : selected.getShipName())
            + ". Each colour card shows Primary / Secondary / Accent. Paint is applied only after"
            + " confirmation.");
    List<Palette> colors = new ArrayList<Palette>();
    List<String> names = new ArrayList<String>();
    for (int i = palettePage * PALETTES_PER_PAGE;
        i < Math.min(ids.size(), (palettePage + 1) * PALETTES_PER_PAGE);
        i++) {
      String id = ids.get(i);
      option(Palettes.name(id), "palette:" + id);
      colors.add(Palettes.PRESETS.get(id));
      names.add(Palettes.name(id));
    }
    if (palettePage > 0) option("Previous palettes", "palettePrev");
    if (palettePage + 1 < pages) option("More palettes", "paletteNext");
    option("Custom - mix your own colours", "palette:custom");
    option("Original appearance", "palette:original");
    back("advanced".equals(scope) ? "utilities" : selected == null ? "livery" : "scope:ship");
    CustomPanelAPI panel =
        dialog.getVisualPanel().showCustomPanel(470, 450, new PaletteSwatches(colors));
    TooltipMakerAPI labels = panel.createUIElement(450, 435, false);
    labels.addTitle("PRIMARY  /  SECONDARY  /  ACCENT");
    for (int i = 0; i < names.size(); i++) {
      LabelAPI label = labels.addPara(names.get(i), 0);
      label.getPosition().inTL(0, 24 + i * 92);
    }
    panel.addUIElement(labels).inTL(12, 10);
  }

  private void picker() {
    List<FleetMemberAPI> pool = LiveryService.members();
    if (pool.isEmpty()) return;
    dialog.showFleetMemberPickerDialog(
        "Choose a ship to paint",
        "Choose ship",
        "Cancel",
        Math.min(4, (pool.size() + 5) / 6),
        6,
        90,
        false,
        false,
        pool,
        new FleetMemberPickerListener() {
          public void pickedFleetMembers(List<FleetMemberAPI> picked) {
            if (picked != null && !picked.isEmpty()) {
              pendingSelection = picked.get(0);
              pickerFrames = 1;
            }
          }

          public void cancelledFleetMemberPicking() {}
        });
  }

  private void rules() {
    if (selected == null) {
      root();
      return;
    }
    start(
        "Existing rules  /  " + selected.getShipName(),
        "Precedence: individual ship, hull, size, fleet. Clear a rule to inherit the next level.");
    option("Clear this ship's exception", "inherit");
    option("Clear this hull's standard", "clear:hull");
    option("Clear this size's standard", "clear:size");
    back("confirm");
    showPreview(draft());
  }

  private void stepPalette(int direction) {
    List<String> ids = new ArrayList<String>(Palettes.PRESETS.keySet());
    if (ids.isEmpty()) return;
    int index = ids.indexOf(draftId);
    if (index < 0) index = direction > 0 ? -1 : 0;
    draftId = ids.get(Math.floorMod(index + direction, ids.size()));
    confirmation();
  }

  private void loadCurrentDraft() {
    try {
      LiveryState settings = LiveryService.state();
      String rule;
      if (selected != null) rule = LiveryService.effectiveId(selected);
      else if ("empire".equals(scope)) rule = settings.empireId();
      else {
        rule =
            settings.fleetRule(
                targetFleet.getId(), targetFleet == Global.getSector().getPlayerFleet());
        if (rule == null) rule = settings.empireId();
      }
      if (rule != null && rule.startsWith(LiveryState.INLINE)) {
        custom = settings.palette(rule);
        draftId = "custom";
      } else if (Palettes.NAMES.containsKey(rule)) draftId = rule;
    } catch (RuntimeException error) {
      draftId = "original";
      Global.getLogger(LiveryDialog.class)
          .warn("UFL: could not read selected ship/fleet appearance", error);
    }
  }

  private Palette draft() {
    return "custom".equals(draftId) ? custom : Palettes.PRESETS.get(draftId);
  }

  private void confirmation() {
    String who =
        selected != null
            ? selected.getShipName()
            : "empire".equals(scope) ? "the player faction" : targetFleet.getName();
    start(
        "Preview: " + Palettes.name(draftId),
        "Scope: "
            + scope
            + " / "
            + who
            + ". Left: original hull. Right: proposed paint. Ship overrides fleet; fleet overrides"
            + " empire. Custom colours are saved as separate snapshots for each assignment.");
    showPreview(draft());
    if (selected != null
        && selected.getVariant() != null
        && LiveryService.hasOtherPaintjob(selected.getVariant()))
      dialog
          .getTextPanel()
          .addPara(
              "A MagicLib paintjob controls this ship. Remove it before UFL can paint the ship.",
              Misc.getNegativeHighlightColor());
    if (!"original".equals(draftId))
      option(
          "custom".equals(draftId)
              ? "Edit custom colours / strength"
              : "Edit a copy of this scheme",
          "editCopy");
    if (selected != null) {
      option("Apply only to " + selected.getShipName(), "apply:ship");
      option("Use inherited colours for this ship", "inherit");
      if ("advanced".equals(scope)) {
        option("Set standard for this hull", "apply:hull");
        option("Set standard for this size", "apply:size");
        option("Manage existing rules", "rules");
      }
    } else if ("empire".equals(scope)) {
      option("Set empire standard (keep overrides)", "apply:empire");
      option("Set empire standard; let your fleet inherit it", "apply:empireAndFleet");
    } else {
      option("Set this fleet's standard (keep ship overrides)", "apply:default");
      option("Standardise this fleet (clear its exceptions)", "apply:all");
      dialog
          .getOptionPanel()
          .addOptionConfirmation(
              "apply:all",
              "Replace individual paint choices in this fleet with the selected scheme? Other"
                  + " fleets and empire settings are retained.",
              "Standardise fleet",
              "Cancel");
      option("Use empire standard for this fleet", "inheritFleet");
    }
    // Advanced rule management has its own route; keep each visible menu compact.
    if (!"advanced".equals(scope)) {
      option("Previous scheme", "schemePrev");
      option("Next scheme", "schemeNext");
    }
    option("Palette library", "palettes");
    back("livery");
  }

  private void editor() {
    start(
        "Custom colours",
        "Enter six-digit RGB hex colours, for example 1F3558. Strength is 0 to 100. These values"
            + " are a draft until you apply the Custom scheme. Applying stores an independent"
            + " colour snapshot for the chosen scope.");
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

  private void apply(String action) {
    if ("fleet".equals(scope) && !LiveryService.owns(targetFleet)) {
      fleetMenu();
      dialog.getTextPanel().addPara("That fleet no longer belongs to your faction.");
      return;
    }
    LiveryState settings = LiveryService.state();
    String rule = draftId;
    if ("custom".equals(draftId)) {
      settings.custom(custom);
      rule = LiveryState.snapshot(custom);
    }
    settings.enabled(true);
    CampaignFleetAPI main = Global.getSector().getPlayerFleet();
    if ("empire".equals(action) || "empireAndFleet".equals(action)) {
      settings.empireId(rule);
      if ("empireAndFleet".equals(action)) settings.inheritFleet(main.getId(), true);
    } else if ("default".equals(action))
      settings.fleetRule(targetFleet.getId(), targetFleet == main, rule);
    else if ("all".equals(action)) {
      List<String> ids = new ArrayList<String>();
      for (FleetMemberAPI member : LiveryService.members(targetFleet)) ids.add(member.getId());
      settings.standardizeFleet(targetFleet.getId(), targetFleet == main, ids, rule);
    } else if ("ship".equals(action)) settings.map("ships").put(selected.getId(), rule);
    else if ("hull".equals(action))
      settings.map("hulls").put(selected.getHullSpec().getBaseHullId(), rule);
    else if ("size".equals(action))
      settings.map("sizes").put(selected.getHullSpec().getHullSize().name(), rule);
    LiveryService.sync();
    root();
    dialog
        .getTextPanel()
        .addPara(
            "Applied "
                + Palettes.name(rule)
                + " to the chosen scope. Reopen an existing refit view if needed.");
  }

  public void optionSelected(String text, Object data) {
    if (!(data instanceof String)) return;
    String id = (String) data;
    if ("close".equals(id)) {
      pendingSelection = null;
      dialog.dismiss();
      return;
    }
    if ("livery".equals(id)) {
      scopes();
      return;
    }
    if ("utilities".equals(id)) {
      utilities();
      return;
    }
    if (id.startsWith("scope:")) {
      scope = id.substring(6);
      selected = null;
      targetFleet = Global.getSector().getPlayerFleet();
      if ("ship".equals(scope)) shipMenu();
      else if ("fleet".equals(scope)) fleetMenu();
      else {
        loadCurrentDraft();
        palettes();
      }
      return;
    }
    if ("fleetMain".equals(id)) {
      scope = "fleet";
      selected = null;
      targetFleet = Global.getSector().getPlayerFleet();
      loadCurrentDraft();
      palettes();
      return;
    }
    if ("fleetList".equals(id)) {
      fleetList();
      return;
    }
    if ("fleetPrev".equals(id)) {
      fleetPage--;
      fleetList();
      return;
    }
    if ("fleetNext".equals(id)) {
      fleetPage++;
      fleetList();
      return;
    }
    if (id.startsWith("fleetId:")) {
      for (CampaignFleetAPI fleet : LiveryService.fleets())
        if (fleet.getId().equals(id.substring(8))) {
          targetFleet = fleet;
          scope = "fleet";
          selected = null;
          loadCurrentDraft();
          palettes();
          return;
        }
      fleetList();
      return;
    }
    if ("advanced".equals(id)) {
      scope = "advanced";
      targetFleet = Global.getSector().getPlayerFleet();
      list();
      return;
    }
    if ("inheritFleet".equals(id)) {
      LiveryService.state()
          .inheritFleet(targetFleet.getId(), targetFleet == Global.getSector().getPlayerFleet());
      LiveryService.sync();
      root();
      return;
    }
    if ("resetFleet".equals(id)) {
      List<String> ids = new ArrayList<String>();
      for (FleetMemberAPI member : LiveryService.members()) ids.add(member.getId());
      LiveryService.state()
          .standardizeFleet(Global.getSector().getPlayerFleet().getId(), true, ids, "original");
      LiveryService.sync();
      root();
      return;
    }
    if ("root".equals(id)) {
      root();
      return;
    }
    if ("picker".equals(id)) {
      picker();
      return;
    }
    if ("palettePrev".equals(id)) {
      palettePage--;
      palettes();
      return;
    }
    if ("paletteNext".equals(id)) {
      palettePage++;
      palettes();
      return;
    }
    if ("schemePrev".equals(id)) {
      stepPalette(-1);
      return;
    }
    if ("schemeNext".equals(id)) {
      stepPalette(1);
      return;
    }
    if ("rules".equals(id)) {
      rules();
      return;
    }
    if ("editCopy".equals(id)) {
      Palette source = draft();
      if (source != null) custom = source;
      draftId = "custom";
      editor();
      return;
    }
    if ("help".equals(id)) {
      start(
          "What gets painted?",
          "Fleet Livery repaints the base hull image. Mounted weapons, barrels, missiles, engine"
              + " flames, lights, damage decals and scripted add-on parts are separate graphics and"
              + " keep their original colours. Red weapon housings can therefore remain red on a"
              + " blue hull. Automatic colour regions also infer tone rather than understanding"
              + " physical armour panels.");
      back("utilities");
      return;
    }
    if ("fleet".equals(id)) {
      scope = "fleet";
      targetFleet = Global.getSector().getPlayerFleet();
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
      selected = null;
      for (FleetMemberAPI m : LiveryService.members())
        if (m.getId().equals(id.substring(5))) {
          selected = m;
          break;
        }
      if (selected == null) {
        list();
        return;
      }
      loadCurrentDraft();
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
      utilities();
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
      utilities();
      dialog
          .getTextPanel()
          .addPara(
              LiveryTextures.status()
                  + ". Skipped textures will be retried. Details are in starsector.log.");
    }
  }

  public void optionMousedOver(String text, Object data) {}

  public void advance(float amount) {
    if (pendingSelection == null) return;
    if (pickerFrames-- > 0) return;
    selected = pendingSelection;
    pendingSelection = null;
    loadCurrentDraft();
    palettes();
  }

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
