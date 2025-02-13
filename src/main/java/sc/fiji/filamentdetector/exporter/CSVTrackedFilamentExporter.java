/*-
 * #%L
 * A Fiji plugin that allow easy, fast and accurate detection and tracking of biological filaments.
 * %%
 * Copyright (C) 2016 - 2025 Fiji developers.
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
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.scijava.Context;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

import com.opencsv.CSVWriter;

import sc.fiji.filamentdetector.Calibrations;
import sc.fiji.filamentdetector.model.Filament;
import sc.fiji.filamentdetector.model.TrackedFilament;
import sc.fiji.filamentdetector.model.TrackedFilaments;

public class CSVTrackedFilamentExporter extends TrackedFilamentsExporter<TrackedFilaments> {

	@Parameter
	private LogService log;

	public static String NAME = "CSV Exporter";
	public static String DESCRIPTION = "Export tracked filaments as a CSV file. Note that the CSV format does not "
			+ "export the x and y positions of the filaments. Only features such as size, length and "
			+ "sinuosity will be exported. Calibrations of the image will be applied to all spatial values.";
	public static String EXTENSION = "*.csv";
	public static String EXTENSION_DESCRIPTION = "CSV File (*.csv)";
	public static List<String> EXTENSION_FILTERS = Arrays.asList("*.csv");

	private Calibrations calibrations;

	public CSVTrackedFilamentExporter(Context context, Calibrations calibrations) {
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

		try (CSVWriter writer = new CSVWriter(new FileWriter(file), ';', CSVWriter.DEFAULT_QUOTE_CHARACTER,
				CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END)) {

			String[] columns = Arrays.asList("track id", "filament id", "length", "frame", "sinuosity", "size", "color")
					.stream().toArray(String[]::new);
			writer.writeNext(columns);

			List<String[]> data = new ArrayList<>();
			List<String> row;

			for (TrackedFilament trackedFilament : trackedFilaments) {
				for (Filament filament : trackedFilament) {
					row = new ArrayList<>();

					row.add(Integer.toString(trackedFilament.getId()));
					row.add(Integer.toString(filament.getId()));
					row.add(Double.toString(filament.getLength() * calibrations.getDx()));
					row.add(Integer.toString(filament.getFrame()));
					row.add(Double.toString(filament.getSinuosity()));
					row.add(Integer.toString(filament.getSize()));
					row.add(filament.getColorAsHex());

					data.add(row.toArray(new String[0]));
				}
			}

			writer.writeAll(data);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
