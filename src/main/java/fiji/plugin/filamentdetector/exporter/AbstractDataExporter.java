package fiji.plugin.filamentdetector.exporter;

import org.scijava.Context;

import com.google.gson.JsonArray;

public abstract class AbstractDataExporter<T> implements DataExporter<T> {

	public AbstractDataExporter(Context context) {
		context.inject(this);
	}

	protected JsonArray serializeCoordinates(double[] coords) {
		return serializeCoordinates(coords, 1);
	}

	protected JsonArray serializeCoordinates(double[] coords, double calibration) {
		JsonArray coordsArray = new JsonArray();
		for (int i = 0; i < coords.length; i++) {
			coordsArray.add(coords[i] * calibration);
		}
		return coordsArray;
	}
}
