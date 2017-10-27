package fiji.plugin.filamentdetector.model;

import de.biomedical_imaging.ij.steger.Line;

public class FilamentFactory {

	public static Filament fromLine(Line line, int frame) {
		float[] x = line.getXCoordinates();
		float[] y = line.getYCoordinates();

		return new Filament(x, y, frame);
	}

}
