package fiji.plugin.filamentdetector.exporter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.List;

import org.scijava.Context;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import fiji.plugin.filamentdetector.Calibrations;
import fiji.plugin.filamentdetector.model.Filament;
import fiji.plugin.filamentdetector.model.TrackedFilament;
import fiji.plugin.filamentdetector.model.TrackedFilaments;

public class JSONTrackedFilamentExporter extends TrackedFilamentsExporter<TrackedFilaments> {

	@Parameter
	private LogService log;

	public static String NAME = "JSON Exporter";
	public static String DESCRIPTION = "A JSON Exporter. Calibrations of the image will be"
			+ " applied to all spatial values.";
	public static String EXTENSION = "*.json";
	public static String EXTENSION_DESCRIPTION = "JSON File (*.json)";
	public static List<String> EXTENSION_FILTERS = Arrays.asList("*.json");

	private Calibrations calibrations;

	public JSONTrackedFilamentExporter(Context context, Calibrations calibrations) {
		super(context);
		this.calibrations = calibrations;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getExtension() {
		return EXTENSION;
	}

	@Override
	public List<String> getExtensionFilters() {
		return EXTENSION_FILTERS;
	}

	@Override
	public String getExtensionDescription() {
		return EXTENSION_DESCRIPTION;
	}

	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

	@Override
	public String toString() {
		return NAME;
	}

	@Override
	public void export(TrackedFilaments trackedFilaments, File file) {
		JsonObject root = new JsonObject();

		JsonArray trackedFilamentsArray = new JsonArray();
		JsonObject trackedFilamentElement;
		JsonObject filamentElement;
		JsonArray filamentsArray;

		double[] x;
		double[] y;

		for (TrackedFilament trackedFilament : trackedFilaments) {
			trackedFilamentElement = new JsonObject();
			trackedFilamentElement.add("id", new JsonPrimitive(trackedFilament.getId()));

			filamentsArray = new JsonArray();

			for (Filament filament : trackedFilament) {
				filamentElement = new JsonObject();

				filamentElement.add("id", new JsonPrimitive(filament.getId()));
				filamentElement.add("length", new JsonPrimitive(filament.getLength() * calibrations.getDx()));
				filamentElement.add("frame", new JsonPrimitive(filament.getFrame()));
				filamentElement.add("sinuosity", new JsonPrimitive(filament.getSinuosity()));
				filamentElement.add("size", new JsonPrimitive(filament.getSize()));
				filamentElement.add("color", new JsonPrimitive(filament.getColorAsHex()));

				x = filament.getXCoordinates();
				filamentElement.add("x", serializeCoordinates(x, calibrations.getDx()));

				y = filament.getYCoordinates();
				filamentElement.add("y", serializeCoordinates(y, calibrations.getDy()));

				filamentsArray.add(filamentElement);
			}

			trackedFilamentElement.add("filaments", filamentsArray);
			trackedFilamentsArray.add(trackedFilamentElement);

		}

		root.add("tracks", trackedFilamentsArray);

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String json = gson.toJson(root);

		try {
			file.createNewFile();
			FileOutputStream fOut = new FileOutputStream(file);
			OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
			myOutWriter.append(json);
			myOutWriter.close();
			fOut.close();
		} catch (IOException e) {
			log.error(e);
		}
	}

}
