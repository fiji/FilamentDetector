package fiji.plugin.filamentdetector.gui.model;

import fiji.plugin.filamentdetector.model.TrackedFilament;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class TrackedFilamentModel {

	final private TrackedFilament trackedFilament;

	private IntegerProperty id;
	private IntegerProperty size;
	private StringProperty color;

	public TrackedFilamentModel(TrackedFilament trackedFilament) {
		this.trackedFilament = trackedFilament;

		this.id = new SimpleIntegerProperty(trackedFilament.getId());
		this.size = new SimpleIntegerProperty(trackedFilament.size());
		this.color = new SimpleStringProperty(trackedFilament.getColorAsHex());
	}

	public IntegerProperty getId() {
		return id;
	}

	public void setId(IntegerProperty id) {
		this.id = id;
	}

	public IntegerProperty getSize() {
		return size;
	}

	public void setSize(IntegerProperty size) {
		this.size = size;
	}

	public TrackedFilament getTrackedFilament() {
		return trackedFilament;
	}

	public StringProperty getColor() {
		return color;
	}

	public void setColor(StringProperty color) {
		this.color = color;
	}
}
