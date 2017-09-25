package fiji.plugin.filamentdetector.overlay;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Map;

import org.scijava.convert.ConvertService;
import org.scijava.event.EventService;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

import fiji.plugin.filamentdetector.event.FilamentSelectedEvent;
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
public class DefaultFilamentOverlayService extends AbstractService implements FilamentOverlayService, MouseListener {

	private static int DEFAULT_COLOR_ALPHA = 190;

	@Parameter
	private ConvertService convert;

	@Parameter
	private LogService log;

	@Parameter
	private OverlayService overlayService;

	@Parameter
	private ColorService colorService;

	@Parameter
	private EventService eventService;

	private int filamentWidth = 2;
	private Color filamentColor = Filament.DEFAULT_COLOR;
	private int colorAlpha = DEFAULT_COLOR_ALPHA;

	private Map<Filament, Roi> filamentROIMap = new HashMap<>();
	private Map<Filament, Color> filamentColorMap = new HashMap<>();
	private ImageDisplay imageDisplay;

	private Filament selectedFilament;

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
			add(filaments.get(i), filaments.get(i).getColor());
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

		// Set listeners
		ImagePlus imp = convert.convert(imageDisplay, ImagePlus.class);
		imp.getWindow().getCanvas().addMouseListener(this);

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
		
		if (imp != null) {
			imp.updateAndDraw();
		}
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

	@Override
	public void setSelected(Filament filament, boolean moveToFrame) {

		if (selectedFilament != null) {
			Roi oldRoi = filamentROIMap.get(selectedFilament);
			if (oldRoi != null) {
				oldRoi.setStrokeWidth(oldRoi.getStrokeWidth() / 2);
			}
		}
		selectedFilament = filament;

		Roi roi = filamentROIMap.get(filament);
		roi.setStrokeWidth(roi.getStrokeWidth() * 2);

		ImagePlus imp = convert.convert(imageDisplay, ImagePlus.class);

		if (moveToFrame) {
			imp.setT(roi.getTPosition());
		}

		imp.repaintWindow();
	}

	@Override
	public void mouseClicked(MouseEvent event) {
		ImagePlus imp = convert.convert(imageDisplay, ImagePlus.class);

		// Get the real x and y
		double x = imp.getWindow().getCanvas().offScreenXD(event.getX());
		double y = imp.getWindow().getCanvas().offScreenYD(event.getY());

		int frame = imp.getFrame();
		// int slice = imp.getSlice();
		// int channel = imp.getChannel();

		Filament filament;
		Roi roi;
		for (Map.Entry<Filament, Roi> entry : filamentROIMap.entrySet()) {
			filament = entry.getKey();
			roi = entry.getValue();

			if (roi.getTPosition() == frame) {
				if (roi.getBounds().contains((int) Math.floor(x), (int) Math.floor(y))) {
					eventService.publish(new FilamentSelectedEvent(filament));
					break;
				}
			}
		}

	}

	@Override
	public void mouseEntered(MouseEvent event) {
	}

	@Override
	public void mouseExited(MouseEvent event) {
	}

	@Override
	public void mousePressed(MouseEvent event) {
	}

	@Override
	public void mouseReleased(MouseEvent event) {
	}
}
