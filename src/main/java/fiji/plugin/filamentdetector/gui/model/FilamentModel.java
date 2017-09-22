package fiji.plugin.filamentdetector.gui.model;

import fiji.plugin.filamentdetector.model.Filament;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class FilamentModel {

	final private Filament filament;

	private IntegerProperty ID;
	private DoubleProperty length;

	public FilamentModel(Filament filament) {
		this.filament = filament;

		this.ID = new SimpleIntegerProperty(filament.getID());
		this.length = new SimpleDoubleProperty(filament.getLength());
	}

	public IntegerProperty getID() {
		return ID;
	}

	public void setID(IntegerProperty iD) {
		ID = iD;
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

}
