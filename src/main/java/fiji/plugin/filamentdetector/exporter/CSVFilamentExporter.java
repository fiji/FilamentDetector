package fiji.plugin.filamentdetector.exporter;

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

import fiji.plugin.filamentdetector.Calibrations;
import fiji.plugin.filamentdetector.model.Filament;
import fiji.plugin.filamentdetector.model.Filaments;

public class CSVFilamentExporter extends FilamentsExporter<Filaments> {

	@Parameter
	private LogService log;

	public static String NAME = "CSV Exporter";
	public static String DESCRIPTION = "Export filaments as a CSV file. Note that the CSV format does not "
			+ "export the x and y positions of the filaments. Only features such as size, length and "
			+ "sinuosity will be exported. Calibrations of the image will be applied to all spatial values.";
	public static String EXTENSION = "*.csv";
	public static String EXTENSION_DESCRIPTION = "CSV File (*.csv)";
	public static List<String> EXTENSION_FILTERS = Arrays.asList("*.csv");

	private Calibrations calibrations;

	public CSVFilamentExporter(Context context, Calibrations calibrations) {
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
	public void export(Filaments filaments, File file) {

		try {
			CSVWriter writer = new CSVWriter(new FileWriter(file), ';');

			String[] columns = Arrays.asList("id", "length", "frame", "sinuosity", "size", "color").stream()
					.toArray(String[]::new);
			writer.writeNext(columns);

			List<String[]> data = new ArrayList<>();
			List<String> row;

			for (Filament filament : filaments) {
				row = new ArrayList<>();

				row.add(Integer.toString(filament.getId()));
				row.add(Double.toString(filament.getLength() * calibrations.getDx()));
				row.add(Integer.toString(filament.getFrame()));
				row.add(Double.toString(filament.getSinuosity()));
				row.add(Integer.toString(filament.getSize()));
				row.add(filament.getColorAsHex());

				data.add(row.toArray(new String[0]));
			}

			writer.writeAll(data);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
