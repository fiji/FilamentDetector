/*-
 * #%L
 * A Fiji plugin that allow easy, fast and accurate detection and tracking of biological filaments.
 * %%
 * Copyright (C) 2016 - 2023 Fiji developers.
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */
package sc.fiji.filamentdetector.exporter;

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

import sc.fiji.filamentdetector.Calibrations;
import sc.fiji.filamentdetector.model.Filament;
import sc.fiji.filamentdetector.model.TrackedFilament;
import sc.fiji.filamentdetector.model.TrackedFilaments;

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
