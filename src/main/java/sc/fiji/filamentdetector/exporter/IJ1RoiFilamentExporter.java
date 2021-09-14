/*-
 * #%L
 * A Fiji plugin that allow easy, fast and accurate detection and tracking of biological filaments.
 * %%
 * Copyright (C) 2016 - 2021 Fiji developers.
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
import java.util.Arrays;
import java.util.List;

import org.scijava.Context;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.plugin.frame.RoiManager;
import ij.process.FloatPolygon;
import sc.fiji.filamentdetector.model.Filament;
import sc.fiji.filamentdetector.model.Filaments;

public class IJ1RoiFilamentExporter extends FilamentsExporter<Filaments> {

	@Parameter
	private LogService log;

	public static String NAME = "IJ1 Roi Exporter";
	public static String DESCRIPTION = "A ImageJ1 Roi Exporter. This operation will clear the Roi Manager.";
	public static String EXTENSION = ".zip";
	public static String EXTENSION_DESCRIPTION = "ZIP File (*.zip)";
	public static List<String> EXTENSION_FILTERS = Arrays.asList("*.zip");

	public IJ1RoiFilamentExporter(Context context) {
		super(context);
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
	public void export(Filaments filaments, File file) {

		RoiManager rm = RoiManager.getInstance();
		if (rm == null) {
			rm = RoiManager.getRoiManager();
		}

		rm.runCommand("Deselect");
		if (rm.getRoisAsArray().length > 0) {
			rm.runCommand("Delete");
		}

		for (Filament filament : filaments) {
			float[] x = filament.getXCoordinatesAsFloat();
			float[] y = filament.getYCoordinatesAsFloat();

			FloatPolygon positions = new FloatPolygon(x, y, filament.getSize());
			Roi roi = new PolygonRoi(positions, Roi.FREELINE);

			roi.setPosition(-1, -1, filament.getFrame());
			roi.setName(Integer.toString(filament.getId()));
			roi.setStrokeColor(filament.getColor());

			rm.addRoi(roi);
		}

		rm.runCommand("Save", file.getAbsolutePath());
		rm.runCommand("Deselect");
		if (rm.getRoisAsArray().length > 0) {
			rm.runCommand("Delete");
		}

	}

}
