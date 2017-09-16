package fiji.plugin.filamentdetector.overlay;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.scijava.convert.ConvertService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

import fiji.plugin.filamentdetector.model.Filament;
import fiji.plugin.filamentdetector.model.TrackedFilament;
import fiji.plugin.filamentdetector.model.TrackedFilaments;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.process.FloatPolygon;
import net.imagej.display.ImageDisplay;
import net.imagej.display.OverlayService;

@Plugin(type = Service.class)
public class DefaultFilamentOverlayService extends AbstractService implements FilamentOverlayService {

	private static int DEFAULT_COLOR_ALPHA = 255;
	
	@Parameter
	private ConvertService convert;

	@Parameter
	private OverlayService overlayService;

	private int filamentWidth = 2;
	private Color filamentColor = Color.orange;
	private int colorALpha = DEFAULT_COLOR_ALPHA;

	private Map<Filament, Roi> filamentROIMap = new HashMap<>();
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

		roi.setPosition(filament.getFrame());
		roi.setName(Integer.toString(filament.getID()));
		roi.setStrokeWidth(filamentWidth);
		
		Color realColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), colorALpha);
		roi.setStrokeColor(realColor);

		Overlay overlay = imp.getOverlay();
		if (overlay == null) {
			overlay = new Overlay();
			imp.setOverlay(overlay);
		}
		overlay.add(roi);

		filamentROIMap.put(filament, roi);

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
	public void remove(Filament filament) {
		ImagePlus imp = convert.convert(imageDisplay, ImagePlus.class);

		Roi roiToRemove = filamentROIMap.get(filament);
		Overlay overlay = imp.getOverlay();

		if (overlay != null && roiToRemove != null) {
			overlay.remove(roiToRemove);
		}

	}

	@Override
	public void remove(TrackedFilament trackedFilament) {
		for (Filament filament : trackedFilament) {
			remove(filament);
		}
	}

	@Override
	public void remove(TrackedFilaments trackedFilaments) {
		for (TrackedFilament trackedFilament : trackedFilaments) {
			remove(trackedFilament);
		}
	}

	@Override
	public Color getFilamentColor() {
		return filamentColor;
	}

	@Override
	public void setFilamentColor(Color filamentColor) {
		this.filamentColor = filamentColor;
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
	public int getColorALpha() {
		return colorALpha;
	}

	@Override
	public void setColorAlpha(int colorALpha) {
		this.colorALpha = colorALpha;
	}

}
