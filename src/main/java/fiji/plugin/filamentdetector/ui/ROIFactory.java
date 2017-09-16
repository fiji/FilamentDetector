package fiji.plugin.filamentdetector.ui;

import java.util.ArrayList;
import java.util.List;

import fiji.plugin.filamentdetector.Filament;
import fiji.plugin.filamentdetector.Filaments;
import fiji.plugin.filamentdetector.TrackedFilament;
import fiji.plugin.filamentdetector.TrackedFilaments;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.plugin.frame.RoiManager;
import ij.process.FloatPolygon;

public class ROIFactory {

	public static List<Roi> createROIs(Filaments filaments) {

		List<Roi> rois = new ArrayList<Roi>();

		for (Filament filament : filaments) {
			Roi roi = createROI(filament);
			rois.add(roi);
		}

		return rois;
	}

	public static Roi createROI(Filament filament) {

		float[] x = filament.getXCoordinates();
		float[] y = filament.getYCoordinates();

		FloatPolygon p = new FloatPolygon(x, y, filament.getNumber());
		Roi r = new PolygonRoi(p, Roi.FREELINE);
		r.setPosition(filament.getFrame());
		r.setName(Integer.toString(filament.getID()));

		return r;

	}

	public static List<Roi> createROIs(TrackedFilaments trackedFilaments) {

		List<Roi> rois = new ArrayList<Roi>();
		for (TrackedFilament trackedFilament : trackedFilaments) {
			for (Filament filament : trackedFilament) {
				rois.add(createROI(filament));
			}
		}
		return rois;
	}

	public static void displayInROIManager(Roi roi) {
		displayInROIManager(roi, true);

	}

	public static void displayInROIManager(Roi roi, boolean resetManager) {
		RoiManager rm = RoiManager.getInstance();
		if (rm == null) {
			rm = new RoiManager();

		}

		if (resetManager) {
			rm.reset();
		}
		rm.addRoi(roi);
		rm.setVisible(true);
		rm.runCommand("UseNames", "true");

	}

	public static void displayInROIManager(List<Roi> rois) {
		displayInROIManager(rois, true);

	}

	public static void displayInROIManager(List<Roi> rois, boolean resetManager) {
		RoiManager rm = RoiManager.getInstance();
		if (rm == null) {
			rm = new RoiManager();

		}

		if (resetManager) {
			rm.reset();
		}

		for (Roi roi : rois) {
			rm.addRoi(roi);
		}

		rm.setVisible(true);
		rm.runCommand("UseNames", "true");
	}

}
