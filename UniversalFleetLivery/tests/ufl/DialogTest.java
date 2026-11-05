package ufl;

import com.fs.starfarer.api.*;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.fleet.*;
import com.fs.starfarer.api.ui.*;
import java.lang.reflect.Proxy;
import java.nio.file.*;
import java.util.*;
import org.json.*;

/** Actual dialog controller with API doubles. Does not claim a live native UI session. */
public final class DialogTest {
  interface Handler {
    Object run(String name, Object[] args) throws Throwable;
  }

  private static int checks;

  private static void check(boolean ok, String label) {
    if (!ok) throw new AssertionError(label);
    checks++;
  }

  @SuppressWarnings("unchecked")
  static <T> T proxy(Class<T> type, Handler h) {
    return (T)
        Proxy.newProxyInstance(
            DialogTest.class.getClassLoader(),
            new Class[] {type},
            (p, m, a) -> {
              Object value = h.run(m.getName(), a == null ? new Object[0] : a);
              if (value != null || !m.getReturnType().isPrimitive()) return value;
              if (m.getReturnType() == boolean.class) return false;
              if (m.getReturnType() == float.class) return 0f;
              if (m.getReturnType() == int.class) return 0;
              return null;
            });
  }

  public static void main(String[] args) throws Exception {
    Palettes.install(
        new JSONObject(Files.readString(Paths.get("data/config/ufl_palettes.json")))
            .getJSONArray("palettes"));
    Map<String, Object> data = new LinkedHashMap<String, Object>();
    ShipHullSpecAPI hull =
        proxy(
            ShipHullSpecAPI.class,
            (m, a) ->
                m.equals("getHullSize")
                    ? com.fs.starfarer.api.combat.ShipAPI.HullSize.FRIGATE
                    : m.equals("getHullName")
                        ? "Test hull"
                        : m.equals("getBaseHullId") ? "test" : null);
    FleetMemberAPI member =
        proxy(
            FleetMemberAPI.class,
            (m, a) ->
                m.equals("getType")
                    ? FleetMemberType.SHIP
                    : m.equals("getId")
                        ? "test-ship"
                        : m.equals("getShipName")
                            ? "Test ship"
                            : m.equals("getHullSpec") ? hull : null);
    FleetDataAPI fleetData =
        proxy(
            FleetDataAPI.class,
            (m, a) -> m.equals("getMembersListCopy") ? Arrays.asList(member) : null);
    CampaignFleetAPI player =
        proxy(CampaignFleetAPI.class, (m, a) -> m.equals("getFleetData") ? fleetData : null);
    Global.setSector(
        proxy(
            SectorAPI.class,
            (m, a) ->
                m.equals("getPersistentData") ? data : m.equals("getPlayerFleet") ? player : null));
    PositionAPI[] pos = new PositionAPI[1];
    pos[0] = proxy(PositionAPI.class, (m, a) -> pos[0]);
    LabelAPI label = proxy(LabelAPI.class, (m, a) -> m.equals("getPosition") ? pos[0] : null);
    java.util.List<String[]> draftFields = new java.util.ArrayList<String[]>();
    TooltipMakerAPI tooltip =
        proxy(
            TooltipMakerAPI.class,
            (m, a) -> {
              if (m.equals("addPara")) return label;
              if (m.equals("addTextField")) {
                String[] value = {""};
                draftFields.add(value);
                return proxy(
                    TextFieldAPI.class,
                    (method, args2) -> {
                      if (method.equals("getText")) return value[0];
                      if (method.equals("setText")) value[0] = (String) args2[0];
                      return null;
                    });
              }
              return null;
            });
    CustomPanelAPI panel =
        proxy(
            CustomPanelAPI.class,
            (m, a) ->
                m.equals("createUIElement") ? tooltip : m.equals("addUIElement") ? pos[0] : null);
    VisualPanelAPI visual =
        proxy(VisualPanelAPI.class, (m, a) -> m.equals("showCustomPanel") ? panel : null);
    Map<String, String> options = new LinkedHashMap<String, String>();
    OptionPanelAPI optionPanel =
        proxy(
            OptionPanelAPI.class,
            (m, a) -> {
              if (m.equals("clearOptions")) options.clear();
              if (m.equals("addOption")) options.put((String) a[1], (String) a[0]);
              return null;
            });
    TextPanelAPI text = proxy(TextPanelAPI.class, (m, a) -> null);
    FleetMemberPickerListener[] picker = new FleetMemberPickerListener[1];
    InteractionDialogAPI dialog =
        proxy(
            InteractionDialogAPI.class,
            (m, a) -> {
              switch (m) {
                case "getTextPanel":
                  return text;
                case "getOptionPanel":
                  return optionPanel;
                case "getVisualPanel":
                  return visual;
                case "showFleetMemberPickerDialog":
                  picker[0] = (FleetMemberPickerListener) a[a.length - 1];
                  break;
              }
              return null;
            });
    // Suppress texture I/O only: this test is controller/state validation, not a renderer test.
    Global.setSettings(
        proxy(
            SettingsAPI.class,
            (m, a) -> {
              if (m.equals("loadTexture"))
                throw new IllegalArgumentException("rendering not part of controller fixture");
              return null;
            }));
    LiveryDialog plugin = new LiveryDialog();
    plugin.init(dialog);
    String original = LiveryService.state().defaultId();
    check(
        options
            .keySet()
            .equals(
                new LinkedHashSet<String>(
                    Arrays.asList("livery", "resetFleet", "utilities", "close"))),
        "root has exactly three primary choices plus Close");
    plugin.optionSelected("", "livery");
    check(
        options.containsKey("scope:ship")
            && options.containsKey("scope:fleet")
            && options.containsKey("scope:empire"),
        "three distinct livery scopes exposed");
    plugin.optionSelected("", "utilities");
    check(
        options.containsKey("advanced")
            && options.containsKey("status")
            && options.containsKey("help"),
        "technical controls grouped in Utilities");
    plugin.optionSelected("", "fleet");
    Set<String> found = new LinkedHashSet<String>();
    int pages = 0;
    do {
      int cards = 0;
      for (String id : options.keySet())
        if (id.startsWith("palette:")
            && !id.equals("palette:custom")
            && !id.equals("palette:original")) {
          found.add(id);
          cards++;
        }
      check(cards > 0 && cards <= 4, "page fits four cards");
      check(options.size() <= 9, "palette menu stays within nine options");
      pages++;
      if (!options.containsKey("paletteNext")) break;
      plugin.optionSelected("", "paletteNext");
    } while (pages < 10);
    check(pages == 5 && found.size() == 18, "all 18 factory palettes reachable across five pages");
    plugin.optionSelected("", "palette:royal");
    check(
        options.containsKey("schemeNext")
            && options.containsKey("schemePrev")
            && options.containsKey("editCopy"),
        "quick comparison and editable copy exposed");
    plugin.optionSelected("", "schemeNext");
    plugin.optionSelected("", "schemePrev");
    check(
        LiveryService.state().defaultId().equals(original),
        "browsing/comparing does not apply paint");
    String oldCustom = LiveryService.state().custom().encode();
    plugin.optionSelected("", "editCopy");
    check(draftFields.size() == 4, "editable copy has three colour fields and strength");
    check(
        LiveryService.state().custom().encode().equals(oldCustom),
        "editing a copy does not overwrite saved Custom before Apply");
    plugin.optionSelected("", "scope:ship");
    plugin.optionSelected("", "picker");
    check(picker[0] != null, "native fleet picker opens");
    picker[0].pickedFleetMembers(Arrays.asList(member));
    check(!options.containsKey("paletteNext"), "picker handoff deferred");
    plugin.advance(.016f);
    plugin.advance(.016f);
    check(
        options.containsKey("palettePrev"),
        "chosen ship enters palette browser after picker teardown");
    check(LiveryService.state().map("ships").isEmpty(), "ship selection alone creates no override");
    Palette assigned = new Palette(0x123456, 0x234567, 0x345678, 79);
    LiveryService.state().map("ships").put("test-ship", LiveryState.snapshot(assigned));
    plugin.optionSelected("", "scope:ship");
    plugin.optionSelected("", "ships");
    plugin.optionSelected("", "ship:test-ship");
    plugin.optionSelected("", "palette:custom");
    plugin.optionSelected("", "editCopy");
    check(
        draftFields.get(4)[0].equals("123456") && draftFields.get(7)[0].equals("79"),
        "text-list selection loads this ship's saved custom snapshot");
    check(
        LiveryService.state().map("ships").get("test-ship").equals(LiveryState.snapshot(assigned)),
        "opening saved custom colours does not change the assignment");
    System.out.println(
        "PASS DialogTest: "
            + checks
            + " checks using API doubles; palette reachability, staging and visual-picker handoff;"
            + " real UI NOT tested");
  }
}
