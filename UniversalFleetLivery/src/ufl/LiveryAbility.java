package ufl;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.abilities.BaseAbilityPlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

public final class LiveryAbility extends BaseAbilityPlugin {
  @Override
  public void pressButton() {
    activate();
  }

  @Override
  public void activate() {
    if (isUsable())
      Global.getSector()
          .getCampaignUI()
          .showInteractionDialog(new LiveryDialog(), Global.getSector().getPlayerFleet());
  }

  @Override
  public boolean isUsable() {
    return Global.getSector() != null
        && Global.getSector().getPlayerFleet() != null
        && !Global.getSector().getCampaignUI().isShowingDialog();
  }

  @Override
  public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {
    tooltip.addTitle("Fleet Livery");
    tooltip.addPara(
        "Preview paint schemes, set fleet standards, and manage individual exceptions. Cosmetic"
            + " changes cost no credits.",
        10f);
  }

  @Override
  public boolean hasTooltip() {
    return true;
  }

  @Override
  public boolean hasCustomButtonPressSounds() {
    return false;
  }

  @Override
  public float getCooldownFraction() {
    return 1f;
  }

  @Override
  public void setCooldownLeft(float days) {}

  @Override
  public float getCooldownLeft() {
    return 0f;
  }

  @Override
  public void advance(float amount) {}

  @Override
  public boolean isDone() {
    return false;
  }

  @Override
  public boolean runWhilePaused() {
    return true;
  }
}
