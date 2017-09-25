package fiji.plugin.filamentdetector.exporter;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.scijava.Context;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

import fiji.plugin.filamentdetector.model.Filament;
import fiji.plugin.filamentdetector.model.Filaments;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.plugin.frame.RoiManager;
import ij.process.FloatPolygon;

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
			float[] x = filament.getXCoordinates();
			float[] y = filament.getYCoordinates();

			FloatPolygon positions = new FloatPolygon(x, y, filament.getNumber());
			Roi roi = new PolygonRoi(positions, Roi.FREELINE);

			roi.setPosition(-1, -1, filament.getFrame());
			roi.setName(Integer.toString(filament.getID()));
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
