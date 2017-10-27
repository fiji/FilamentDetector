package fiji.plugin.filamentdetector.gui.model;

import fiji.plugin.filamentdetector.model.Filament;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class FilamentModel {

	final private Filament filament;

	private IntegerProperty id;
	private DoubleProperty length;
	private IntegerProperty frame;
	private StringProperty color;

	public FilamentModel(Filament filament) {
		this.filament = filament;

		this.id = new SimpleIntegerProperty(filament.getId());
		this.length = new SimpleDoubleProperty(filament.getLength());
		this.frame = new SimpleIntegerProperty(filament.getFrame());
		this.color = new SimpleStringProperty(filament.getColorAsHex());
	}

	public IntegerProperty getId() {
		return id;
	}

	public void setId(IntegerProperty id) {
		id = id;
	}

	public DoubleProperty getLength() {
		return length;
	}

	public void setLength(DoubleProperty length) {
		this.length = length;
	}

	public Filament getFilament() {
		return filament;
	}

	public IntegerProperty getFrame() {
		return frame;
	}

	public void setFrame(IntegerProperty frame) {
		this.frame = frame;
	}

	public StringProperty getColor() {
		return color;
	}

	public void setColor(StringProperty color) {
		this.color = color;
	}

}
