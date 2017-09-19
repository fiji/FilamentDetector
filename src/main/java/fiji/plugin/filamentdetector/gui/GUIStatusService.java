package fiji.plugin.filamentdetector.gui;

import org.scijava.app.DefaultStatusService;
import org.scijava.app.StatusService;
import org.scijava.app.event.StatusEvent;
import org.scijava.event.EventService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.Service;

import javafx.scene.control.TextArea;

@Plugin(type = Service.class)
public class GUIStatusService extends DefaultStatusService implements StatusService {

	@Parameter
	private EventService eventService;

	private TextArea logField;

	public void setTextField(TextArea logField) {
		this.logField = logField;
	}

	protected void publish(final StatusEvent statusEvent) {
		eventService.publishLater(statusEvent);
		if (logField != null) {
			logField.appendText("\n· " + statusEvent.getStatusMessage());
		}
	}

}