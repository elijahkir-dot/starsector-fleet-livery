package ufl;

import java.io.*;
import java.util.*;

public final class ScopeTest {
  private static int checks;

  private static void check(boolean ok, String message) {
    if (!ok) throw new AssertionError(message);
    checks++;
  }

  public static void main(String[] args) throws Exception {
    LiveryState state = new LiveryState(new LinkedHashMap<String, Object>());
    state.empireId("naval");
    check(
        state.resolveId("a", "wolf", "FRIGATE", "player", true).equals("naval"),
        "new player fleet inherits empire");
    check(
        state.resolveId("n", "wolf", "FRIGATE", "patrol", false).equals("naval"),
        "NPC fleet inherits empire");
    state.defaultId("imperial");
    check(
        state.resolveId("a", "wolf", "FRIGATE", "player", true).equals("imperial"),
        "main fleet override");
    check(
        state.resolveId("n", "wolf", "FRIGATE", "patrol", false).equals("naval"),
        "main fleet override does not change NPC");
    state.fleetRule("patrol", false, "crimson");
    check(
        state.resolveId("n", "wolf", "FRIGATE", "patrol", false).equals("crimson"),
        "independent NPC fleet override");
    state.map("ships").put("n", "arctic");
    check(
        state.resolveId("n", "wolf", "FRIGATE", "patrol", false).equals("arctic"),
        "ship overrides its fleet");
    state.map("hulls").put("wolf", "industrial");
    check(
        state.resolveId("a", "wolf", "FRIGATE", "player", true).equals("industrial"),
        "legacy advanced hull rule retained for main");
    check(
        state.resolveId("other", "wolf", "FRIGATE", "other-patrol", false).equals("naval"),
        "main advanced rules do not recolour other fleets");
    state.inheritFleet("patrol", false);
    state.map("ships").remove("n");
    check(
        state.resolveId("n", "wolf", "FRIGATE", "patrol", false).equals("naval"),
        "cleared fleet returns to empire");
    Palette red = new Palette(0x991122, 0x334455, 0xaabbcc, 77),
        blue = new Palette(0x123499, 0xeeeeee, 0x3377aa, 100);
    String redRule = LiveryState.snapshot(red), blueRule = LiveryState.snapshot(blue);
    state.map("ships").put("a", redRule);
    state.fleetRule("patrol", false, blueRule);
    state.custom(new Palette(0x112233, 0x445566, 0x778899, 50));
    check(
        state
            .palette(state.resolveId("a", "wolf", "FRIGATE", "player", true))
            .encode()
            .equals(red.encode()),
        "ship Custom is an independent snapshot");
    check(
        state
            .palette(state.resolveId("n", "wolf", "FRIGATE", "patrol", false))
            .encode()
            .equals(blue.encode()),
        "fleet Custom is independent of ship and draft");
    state.standardizeFleet("player", true, Arrays.asList("a"), "original");
    check(
        state.resolveId("a", "wolf", "FRIGATE", "player", true).equals("original"),
        "fleet restore overrides empire");
    check(
        state.fleetRule("patrol", false).equals(blueRule) && state.empireId().equals("naval"),
        "fleet reset retains other fleets and empire");
    Map<String, Object> legacy = new LinkedHashMap<String, Object>();
    legacy.put("schema", "1");
    legacy.put("default", "custom");
    legacy.put("custom", red.encode());
    Map<String, String> oldShips = new LinkedHashMap<String, String>();
    oldShips.put("old", "custom");
    legacy.put("ships", oldShips);
    LiveryState migrated = new LiveryState(legacy);
    migrated.empireId("naval");
    migrated.custom(blue);
    check(
        migrated
            .palette(migrated.resolveId("old", "wolf", "FRIGATE"))
            .encode()
            .equals(red.encode()),
        "legacy ship Custom appearance preserved on migration");
    check(
        migrated
            .palette(migrated.resolveId("new", "wolf", "FRIGATE"))
            .encode()
            .equals(red.encode()),
        "legacy fleet Custom preserved rather than overwritten");
    check(!migrated.fleetInheritsEmpire(), "upgrade preserves an existing fleet setting");
    migrated.inheritFleet("player", true);
    check(
        migrated.resolveId("new", "wolf", "FRIGATE").equals("naval"),
        "upgrade can explicitly adopt empire colours");
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    new ObjectOutputStream(bytes).writeObject(legacy);
    @SuppressWarnings("unchecked")
    Map<String, Object> loaded =
        (Map<String, Object>)
            new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())).readObject();
    check(
        new LiveryState(loaded).resolveId("old", "wolf", "FRIGATE").equals(redRule),
        "scope data survives ordinary JDK-map serialization");
    state.map("ships").put("bad", "paint:broken");
    check(
        state.resolveId("bad", "x", "x").equals("original"), "invalid snapshot falls back safely");
    System.out.println(
        "PASS ScopeTest: "
            + checks
            + " checks; independent ship/fleet/empire settings, migration, snapshots and scoped"
            + " reset");
  }
}
