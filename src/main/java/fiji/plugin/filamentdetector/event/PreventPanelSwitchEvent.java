package fiji.plugin.filamentdetector.event;

import org.scijava.event.SciJavaEvent;

public class PreventPanelSwitchEvent extends SciJavaEvent {

	private boolean preventPaneSwitch;

	public PreventPanelSwitchEvent(boolean preventPaneSwitch) {
		this.preventPaneSwitch = preventPaneSwitch;
	}

	public boolean getPreventPaneSwitch() {
		return preventPaneSwitch;
	}

}
