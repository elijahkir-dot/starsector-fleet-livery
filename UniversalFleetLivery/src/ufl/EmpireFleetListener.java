package ufl;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetInflater;
import com.fs.starfarer.api.campaign.listeners.FleetInflationListener;

/** Transient listener; touches only fleets belonging to the actual player faction. */
public final class EmpireFleetListener implements FleetInflationListener {
  public void reportFleetInflated(CampaignFleetAPI fleet, FleetInflater inflater) {
    LiveryService.syncFleet(fleet);
  }
}
