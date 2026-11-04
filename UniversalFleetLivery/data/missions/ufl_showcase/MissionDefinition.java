package data.missions.ufl_showcase;

import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.*;
import com.fs.starfarer.api.mission.*;
import java.util.Map;
import ufl.*;

/** Actual-engine acceptance fixture. Run this from the Missions menu. */
public final class MissionDefinition implements MissionDefinitionPlugin {
  public void defineMission(MissionDefinitionAPI api) {
    api.initFleet(FleetSide.PLAYER, "UFL", FleetGoal.ATTACK, false);
    api.initFleet(FleetSide.ENEMY, "Original", FleetGoal.ATTACK, true);
    api.setFleetTagline(FleetSide.PLAYER, "One hull, six livery palettes");
    api.setFleetTagline(FleetSide.ENEMY, "Unmodified comparison hulls");
    int i = 0;
    for (Map.Entry<String, Palette> e : Palettes.PRESETS.entrySet()) {
      FleetMemberAPI m =
          api.addToFleet(
              FleetSide.PLAYER,
              "wolf_Assault",
              FleetMemberType.SHIP,
              Palettes.name(e.getKey()),
              i++ == 0);
      ShipVariantAPI v = m.getVariant().clone();
      LiveryService.decorate(v, LiveryService.TAG + e.getValue().encode(), 0);
      m.setVariant(v, false, false);
    }
    FleetMemberAPI custom =
        api.addToFleet(
            FleetSide.PLAYER, "wolf_Assault", FleetMemberType.SHIP, "Custom demonstration", false);
    ShipVariantAPI v = custom.getVariant().clone();
    LiveryService.decorate(
        v, LiveryService.TAG + new Palette(0x285442, 0x99ccb0, 0xeac86a, 100).encode(), 0);
    custom.setVariant(v, false, false);
    api.addToFleet(
        FleetSide.PLAYER, "wolf_Assault", FleetMemberType.SHIP, "Original comparison", false);
    for (int n = 0; n < 4; n++)
      api.addToFleet(FleetSide.ENEMY, "wolf_Assault", FleetMemberType.SHIP, false);
    api.addBriefingItem(
        "Deploy all ships; pause and compare hull details, alpha edges, facing, and the original"
            + " ship.");
    api.addBriefingItem(
        "Weapons and glow overlays intentionally retain their original colours. No shaders or"
            + " libraries required.");
    api.initMap(-5000, 5000, -5000, 5000);
  }
}
