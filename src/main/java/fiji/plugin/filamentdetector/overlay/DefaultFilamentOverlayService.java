package fiji.plugin.filamentdetector.overlay;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.jfree.util.Log;
import org.scijava.convert.ConvertService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

import fiji.plugin.filamentdetector.model.Filament;
import fiji.plugin.filamentdetector.model.Filaments;
import fiji.plugin.filamentdetector.model.TrackedFilament;
import fiji.plugin.filamentdetector.model.TrackedFilaments;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.plugin.frame.RoiManager;
import ij.process.FloatPolygon;
import net.imagej.display.ImageDisplay;
import net.imagej.display.OverlayService;

@Plugin(type = Service.class)
public class DefaultFilamentOverlayService extends AbstractService implements FilamentOverlayService {

	private static int DEFAULT_COLOR_ALPHA = 190;

	@Parameter
	private ConvertService convert;

	@Parameter
	private OverlayService overlayService;

	@Parameter
	private ColorService colorService;

	private int filamentWidth = 2;
	private Color filamentColor = Color.orange;
	private int colorAlpha = DEFAULT_COLOR_ALPHA;

	private Map<Filament, Roi> filamentROIMap = new HashMap<>();
	private Map<Filament, Color> filamentColorMap = new HashMap<>();
	private ImageDisplay imageDisplay;

	@Override
	public void add(Filament filament) {
		add(filament, this.filamentColor);
	}

	@Override
	public void add(Filament filament, Color color) {
		ImagePlus imp = convert.convert(imageDisplay, ImagePlus.class);

		float[] x = filament.getXCoordinates();
		float[] y = filament.getYCoordinates();

		FloatPolygon positions = new FloatPolygon(x, y, filament.getNumber());
		Roi roi = new PolygonRoi(positions, Roi.FREELINE);

		roi.setPosition(-1, -1, filament.getFrame());
		roi.setName(Integer.toString(filament.getID()));
		roi.setStrokeWidth(filamentWidth);

		Color realColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), colorAlpha);
		roi.setStrokeColor(realColor);

		Overlay overlay = imp.getOverlay();
		if (overlay == null) {
			overlay = new Overlay();
			imp.setOverlay(overlay);
		}
		overlay.add(roi);
		imp.repaintWindow();

		filamentROIMap.put(filament, roi);
		filamentColorMap.put(filament, color);

	}

	@Override
	public void add(TrackedFilament trackedFilament) {
		add(trackedFilament, this.filamentColor);
	}

	@Override
	public void add(TrackedFilament trackedFilament, Color color) {
		for (Filament filament : trackedFilament) {
			add(filament, color);
		}
	}

	@Override
	public void add(TrackedFilaments trackedFilaments) {
		for (TrackedFilament trackedFilament : trackedFilaments) {
			add(trackedFilament, trackedFilament.getColor());
		}
	}

	@Override
	public void add(TrackedFilaments trackedFilaments, Color color) {
		for (TrackedFilament trackedFilament : trackedFilaments) {
			add(trackedFilament, color);
		}
	}

	@Override
	public void add(Filaments filaments, Color color) {
		for (Filament filament : filaments) {
			add(filament, color);
		}
	}

	@Override
	public void add(Filaments filaments) {
		colorService.initialize();
		for (int i = 0; i < filaments.size(); i++) {
			Color color = colorService.getColor(i);
			add(filaments.get(i), color);
		}
	}

	@Override
	public void remove(Filaments filaments) {
		for (Filament filament : filaments) {
			remove(filament);
		}
		convert.convert(imageDisplay, ImagePlus.class).updateAndDraw();
	}

	@Override
	public void remove(Filament filament) {
		ImagePlus imp = convert.convert(imageDisplay, ImagePlus.class);

		Roi roiToRemove = filamentROIMap.get(filament);
		Overlay overlay = imp.getOverlay();

		if (overlay != null && roiToRemove != null) {
			overlay.remove(roiToRemove);
		}

		filamentROIMap.remove(filament);
		filamentColorMap.remove(filament);
		imp.updateAndDraw();
	}

	@Override
	public void remove(TrackedFilament trackedFilament) {
		for (Filament filament : trackedFilament) {
			remove(filament);
		}
		convert.convert(imageDisplay, ImagePlus.class).updateAndDraw();
	}

	@Override
	public void remove(TrackedFilaments trackedFilaments) {
		for (TrackedFilament trackedFilament : trackedFilaments) {
			remove(trackedFilament);
		}
		convert.convert(imageDisplay, ImagePlus.class).updateAndDraw();
	}

	@Override
	public Color getFilamentColor() {
		return filamentColor;
	}

	@Override
	public javafx.scene.paint.Color getFilamentColorAsJavaFX() {
		return javafx.scene.paint.Color.rgb(filamentColor.getRed(), filamentColor.getGreen(), filamentColor.getBlue(),
				filamentColor.getAlpha() / 255);
	}

	@Override
	public void setFilamentColor(Color filamentColor) {
		this.filamentColor = filamentColor;
	}

	public void setFilamentColor(javafx.scene.paint.Color filamentColorJavaFX) {
		filamentColor = new Color((int) filamentColorJavaFX.getRed(), (int) filamentColorJavaFX.getGreen(),
				(int) filamentColorJavaFX.getBlue(), (int) (filamentColorJavaFX.getOpacity() * 255));
	}

	@Override
	public void setFilamentWidth(int filamentWidth) {
		this.filamentWidth = filamentWidth;
	}

	@Override
	public int getFilamentWidth() {
		return filamentWidth;
	}

	@Override
	public ImageDisplay getImageDisplay() {
		return imageDisplay;
	}

	@Override
	public void setImageDisplay(ImageDisplay imageDisplay) {
		this.imageDisplay = imageDisplay;
	}

	@Override
	public int getColorAlpha() {
		return colorAlpha;
	}

	@Override
	public void setColorAlpha(int colorAlpha) {
		this.colorAlpha = colorAlpha;
	}

	@Override
	public void reset() {

		ImagePlus imp = convert.convert(imageDisplay, ImagePlus.class);

		for (Map.Entry<Filament, Roi> entry : filamentROIMap.entrySet()) {
			Overlay overlay = imp.getOverlay();

			if (overlay != null && entry.getValue() != null) {
				overlay.remove(entry.getValue());
			}
		}

		filamentROIMap = new HashMap<>();
		filamentColorMap = new HashMap<>();
		imp.updateAndDraw();
	}

	@Override
	public void refresh() {

		ImagePlus imp = convert.convert(imageDisplay, ImagePlus.class);

		for (Map.Entry<Filament, Roi> entry : filamentROIMap.entrySet()) {
			Overlay overlay = imp.getOverlay();

			if (overlay != null && entry.getValue() != null) {
				overlay.remove(entry.getValue());
			}
		}

		for (Map.Entry<Filament, Roi> entry : filamentROIMap.entrySet()) {
			add(entry.getKey(), filamentColorMap.get(entry.getKey()));
		}

		imp.updateAndDraw();
	}

	@Override
	public void exportToROIManager() {

		ImagePlus imp = convert.convert(imageDisplay, ImagePlus.class);

		Overlay overlay = imp.getOverlay();
		if (overlay != null) {

			RoiManager rm = RoiManager.getInstance();
			if (rm == null) {
				rm = RoiManager.getRoiManager();
			}
			for (int i = 0; i < overlay.size(); i++) {
				rm.addRoi(overlay.get(i));
			}
		}

	}

	@Override
	public void disableOverlay(boolean disable) {
		ImagePlus imp = convert.convert(imageDisplay, ImagePlus.class);
		imp.setHideOverlay(!disable);
	}
}
