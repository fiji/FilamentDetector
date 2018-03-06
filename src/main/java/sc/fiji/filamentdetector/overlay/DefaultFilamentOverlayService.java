/*-
 * #%L
 * A Fiji plugin that allow easy, fast and accurate detection and tracking of biological filaments.
 * %%
 * Copyright (C) 2016 - 2017 Hadrien Mary
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
package sc.fiji.filamentdetector.overlay;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.scijava.convert.ConvertService;
import org.scijava.event.EventService;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.OvalRoi;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.plugin.frame.RoiManager;
import net.imagej.Dataset;
import net.imagej.display.ImageDisplay;
import net.imagej.display.OverlayService;
import net.imagej.ops.OpService;
import net.imglib2.type.numeric.real.DoubleType;
import sc.fiji.filamentdetector.event.FilamentSelectedEvent;
import sc.fiji.filamentdetector.event.ImageNotFoundEvent;
import sc.fiji.filamentdetector.model.Filament;
import sc.fiji.filamentdetector.model.Filaments;
import sc.fiji.filamentdetector.model.Tip;
import sc.fiji.filamentdetector.model.TrackedFilament;
import sc.fiji.filamentdetector.model.TrackedFilaments;

@Plugin(type = Service.class)
public class DefaultFilamentOverlayService extends AbstractService implements FilamentOverlayService, MouseListener {

	private static int DEFAULT_COLOR_ALPHA = 190;
	private static int DEFAULT_FILAMENT_WIDTH = 1;

	@Parameter
	private ConvertService convert;

	@Parameter
	private OpService op;

	@Parameter
	private LogService log;

	@Parameter
	private OverlayService overlayService;

	@Parameter
	private ColorService colorService;

	@Parameter
	private EventService eventService;

	private int filamentWidth = DEFAULT_FILAMENT_WIDTH;
	private int colorAlpha = DEFAULT_COLOR_ALPHA;

	private HashMap<Filament, Roi> filamentROIMap = new HashMap<>();

	private List<Filament> filamentDisplayed = new ArrayList<>();
	private List<TrackedFilament> trackedFilamentDisplayed = new ArrayList<>();

	private ImageDisplay imageDisplay;
	private ImagePlus imagePlus;

	private List<Filament> selectedFilaments = new ArrayList<>();
	private List<TrackedFilament> selectedTrackedFilament = new ArrayList<>();

	private boolean drawBoundingBoxes = false;
	private HashMap<Filament, Roi> filamentBoundingBoxesMap = new HashMap<>();

	private HashMap<TrackedFilament, List<Roi>> filamentPlusTipsMap = new HashMap<>();
	private HashMap<TrackedFilament, List<Roi>> filamentMinusTipsMap = new HashMap<>();

	private boolean drawPlusTips = false;
	private boolean drawMinusTips = false;

	private int tipDiameter = 10;

	@Override
	public void add(Filament filament) {
		Dataset data = (Dataset) imageDisplay.getActiveView().getData();
		ImagePlus imp = getImagePlus();

		Roi roi = filament.getRoi();

		if (data.numDimensions() > 3) {
			roi.setPosition(-1, -1, filament.getFrame());
		} else {
			roi.setPosition(filament.getFrame());
		}
		roi.setName(Integer.toString(filament.getId()));
		roi.setStrokeWidth(filamentWidth);

		Color color = filament.getColor();
		Color realColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), colorAlpha);
		roi.setStrokeColor(realColor);

		Overlay overlay = imp.getOverlay();
		if (overlay == null) {
			overlay = new Overlay();
			imp.setOverlay(overlay);
		}

		if (drawBoundingBoxes) {
			Roi boundsRoi = new Roi(roi.getBounds().x, roi.getBounds().y, roi.getBounds().width,
					roi.getBounds().height);
			boundsRoi.setStrokeColor(realColor);
			boundsRoi.setStrokeWidth(1);

			if (data.numDimensions() > 3) {
				boundsRoi.setPosition(-1, -1, filament.getFrame());
			} else {
				boundsRoi.setPosition(filament.getFrame());
			}

			filamentBoundingBoxesMap.put(filament, boundsRoi);
			overlay.add(boundsRoi);
		}

		overlay.add(roi);
		imp.repaintWindow();

		filamentROIMap.put(filament, roi);
		if (!filamentDisplayed.contains(filament)) {
			filamentDisplayed.add(filament);
		}
	}

	@Override
	public void add(Filaments filaments) {
		for (Filament filament : filaments) {
			add(filament);
		}
	}

	@Override
	public void add(TrackedFilament trackedFilament) {

		trackedFilamentDisplayed.add(trackedFilament);
		for (Filament filament : trackedFilament) {
			add(filament);
		}

		if (drawPlusTips) {
			List<Roi> rois = addTip(trackedFilament, trackedFilament.getPlusTip());
			filamentPlusTipsMap.put(trackedFilament, rois);
		}
		if (drawMinusTips) {
			List<Roi> rois = addTip(trackedFilament, trackedFilament.getMinusTip());
			filamentMinusTipsMap.put(trackedFilament, rois);
		}
	}

	private List<Roi> addTip(TrackedFilament trackedFilament, Tip tip) {

		Dataset data = (Dataset) imageDisplay.getActiveView().getData();
		ImagePlus imp = getImagePlus();

		Color color = trackedFilament.getColor();
		Color realColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), colorAlpha);

		Overlay overlay = imp.getOverlay();
		if (overlay == null) {
			overlay = new Overlay();
			imp.setOverlay(overlay);
		}

		double x;
		double y;
		int frame;

		List<Roi> rois = new ArrayList<>();

		for (int i = 0; i < tip.getX().length; i++) {
			x = tip.getX()[i];
			y = tip.getY()[i];
			frame = tip.getFrames()[i];

			Roi tipRoi = new OvalRoi(x - tipDiameter / 2, y - tipDiameter / 2, tipDiameter, tipDiameter);
			tipRoi.setStrokeColor(realColor);
			tipRoi.setStrokeWidth(filamentWidth);

			if (data.numDimensions() > 3) {
				tipRoi.setPosition(-1, -1, frame);
			} else {
				tipRoi.setPosition(frame);
			}
			overlay.add(tipRoi);
			rois.add(tipRoi);
		}
		return rois;
	}

	@Override
	public void add(TrackedFilaments trackedFilaments) {
		for (TrackedFilament trackedFilament : trackedFilaments) {
			add(trackedFilament);
		}
	}

	@Override
	public void clearSelection() {
		for (Filament filament : selectedFilaments) {
			Roi oldRoi = filamentROIMap.get(filament);
			if (oldRoi != null) {
				oldRoi.setStrokeWidth(oldRoi.getStrokeWidth() / 2);
			}
		}
		selectedFilaments.clear();
		selectedTrackedFilament.clear();
	}

	@Override
	public void disableOverlay(boolean disable) {
		ImagePlus imp = getImagePlus();
		imp.setHideOverlay(!disable);
	}

	@Override
	public void exportToROIManager() {

		ImagePlus imp = getImagePlus();

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
	public int getColorAlpha() {
		return colorAlpha;
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
	public void mouseClicked(MouseEvent event) {
		ImagePlus imp = getImagePlus();

		if (imp.getWindow() == null || imp.getWindow().getCanvas() == null) {
			return;
		}

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

	@Override
	public void remove(Filament filament) {
		ImagePlus imp = getImagePlus();

		if (imp != null) {

			Roi roiToRemove = filamentROIMap.get(filament);
			Roi boundingBoxToRemove = filamentBoundingBoxesMap.get(filament);
			Overlay overlay = imp.getOverlay();

			try {
				if (overlay != null) {
					if (roiToRemove != null) {
						overlay.remove(roiToRemove);
					}
					if (boundingBoxToRemove != null) {
						overlay.remove(boundingBoxToRemove);
					}
				}
			} catch (Exception e) {

			}

			filamentROIMap.remove(filament);
			filamentDisplayed.remove(filament);
			filamentBoundingBoxesMap.remove(filament);
			imp.updateAndDraw();
		}
	}

	@Override
	public void remove(Filaments filaments) {
		for (Filament filament : filaments) {
			remove(filament);
		}
		getImagePlus().updateAndDraw();
	}

	@Override
	public void remove(TrackedFilament trackedFilament) {

		trackedFilamentDisplayed.remove(trackedFilament);
		for (Filament filament : trackedFilament) {
			remove(filament);
		}

		ImagePlus imp = getImagePlus();
		List<Roi> plusTipToRemove = filamentPlusTipsMap.get(trackedFilament);
		List<Roi> minusTipToRemove = filamentMinusTipsMap.get(trackedFilament);
		Overlay overlay = imp.getOverlay();

		if (plusTipToRemove != null) {
			for (Roi roi : plusTipToRemove) {
				if (overlay != null && roi != null) {
					overlay.remove(roi);
				}
			}
		}
		if (minusTipToRemove != null) {
			for (Roi roi : minusTipToRemove) {
				if (overlay != null && roi != null) {
					overlay.remove(roi);
				}
			}
		}

		filamentPlusTipsMap.remove(trackedFilament);
		filamentMinusTipsMap.remove(trackedFilament);

		getImagePlus().updateAndDraw();
	}

	@Override
	public void remove(TrackedFilaments trackedFilaments) {
		for (TrackedFilament trackedFilament : trackedFilaments) {
			trackedFilamentDisplayed.remove(trackedFilament);
			remove(trackedFilament);
		}
		getImagePlus().updateAndDraw();
	}

	@Override
	public void reset() {
		for (TrackedFilament trackedFilament : new ArrayList<>(trackedFilamentDisplayed)) {
			remove(trackedFilament);
		}
		for (Filament filament : new ArrayList<>(filamentDisplayed)) {
			remove(filament);
		}

		ImagePlus imp = getImagePlus();
		if (imp != null) {
			imp.updateAndDraw();
		}
	}

	@Override
	public void refresh() {
		for (TrackedFilament trackedFilament : new ArrayList<>(trackedFilamentDisplayed)) {
			remove(trackedFilament);
			add(trackedFilament);
		}
		for (Filament filament : new ArrayList<>(filamentDisplayed)) {
			remove(filament);
			add(filament);
		}

		ImagePlus imp = getImagePlus();
		if (imp != null) {
			imp.updateAndDraw();
		}
	}

	@Override
	public void setColorAlpha(int colorAlpha) {
		this.colorAlpha = colorAlpha;
	}

	@Override
	public void setFilamentWidth(int filamentWidth) {
		this.filamentWidth = filamentWidth;
	}

	@Override
	public void setImageDisplay(ImageDisplay imageDisplay) {
		this.imageDisplay = imageDisplay;
		this.imagePlus = null;

		ImagePlus imp = getImagePlus();
		if (imp != null) {
			imp.getWindow().getCanvas().addMouseListener(this);
		}
		this.refresh();
	}

	@Override
	public void setSelected(Filament filament, boolean moveToFrame, boolean clearSelection) {
		if (clearSelection) {
			this.clearSelection();
		}

		if (!selectedFilaments.contains(filament)) {
			this.selectedFilaments.add(filament);

			Roi roi = filamentROIMap.get(filament);
			if (roi != null) {
				roi.setStrokeWidth(roi.getStrokeWidth() * 2);
				ImagePlus imp = getImagePlus();
				if (moveToFrame) {
					imp.setT(filament.getFrame());
				}
				imp.repaintWindow();
			}
		}
	}

	@Override
	public void setSelected(TrackedFilament trackedFilament, boolean moveToFrame, boolean clearSelection) {
		if (clearSelection) {
			this.clearSelection();
		}
		this.selectedTrackedFilament.add(trackedFilament);
		for (Filament filament : trackedFilament) {
			setSelected(filament, false, false);
		}
		if (moveToFrame) {
			ImagePlus imp = getImagePlus();
			imp.setT(trackedFilament.get(0).getFrame());
		}
	}

	@Override
	public boolean isDrawBoundingBoxes() {
		return drawBoundingBoxes;
	}

	@Override
	public void setDrawBoundingBoxes(boolean drawBoundingBoxes) {
		this.drawBoundingBoxes = drawBoundingBoxes;
	}

	private ImagePlus getImagePlus() {

		if (imageDisplay == null) {
			log.debug("ImageDisplay is null. Can't retrieve ImagePlus.");
			return null;
		}

		if (imagePlus != null) {
			return imagePlus;
		}

		try {
			imagePlus = convert.convert(imageDisplay, ImagePlus.class);
		} catch (NullPointerException e) {
			eventService.publish(new ImageNotFoundEvent());
			return null;
		}

		if (imageDisplay != null && imagePlus == null) {
			log.error("Critical error: ImagePlus not found.");
		}

		if (imagePlus != null) {
			ij.measure.Calibration cal = imagePlus.getCalibration();
			if (cal == null) {
				imagePlus.setCalibration(new ij.measure.Calibration());
			}
		}

		return imagePlus;
	}

	@Override
	public boolean isDrawPlusTips() {
		return drawPlusTips;
	}

	@Override
	public void setDrawPlusTips(boolean drawPlusTips) {
		this.drawPlusTips = drawPlusTips;
	}

	@Override
	public boolean isDrawMinusTips() {
		return drawMinusTips;
	}

	@Override
	public void setDrawMinusTips(boolean drawMinusTips) {
		this.drawMinusTips = drawMinusTips;
	}

	@Override
	public int getTipDiameter() {
		return tipDiameter;
	}

	@Override
	public void setTipDiameter(int tipDiameter) {
		this.tipDiameter = tipDiameter;
	}

	@Override
	public void updateTransparency() {

		Color color;
		Color newColor;

		for (Map.Entry<Filament, Roi> entry : filamentROIMap.entrySet()) {
			color = entry.getValue().getStrokeColor();
			newColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), colorAlpha);
			entry.getValue().setStrokeColor(newColor);
		}
		for (Map.Entry<Filament, Roi> entry : filamentBoundingBoxesMap.entrySet()) {
			color = entry.getValue().getStrokeColor();
			newColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), colorAlpha);
			entry.getValue().setStrokeColor(newColor);
		}
		for (Map.Entry<TrackedFilament, List<Roi>> entry : filamentPlusTipsMap.entrySet()) {
			for (Roi roi : entry.getValue()) {
				color = roi.getStrokeColor();
				newColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), colorAlpha);
				roi.setStrokeColor(newColor);
			}
		}
		for (Map.Entry<TrackedFilament, List<Roi>> entry : filamentMinusTipsMap.entrySet()) {
			for (Roi roi : entry.getValue()) {
				color = roi.getStrokeColor();
				newColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), colorAlpha);
				roi.setStrokeColor(newColor);
			}
		}
		getImagePlus().repaintWindow();
	}

	@Override
	public void updateLineWidth() {

		for (Map.Entry<Filament, Roi> entry : filamentROIMap.entrySet()) {
			entry.getValue().setStrokeWidth(filamentWidth);
		}
		for (Map.Entry<Filament, Roi> entry : filamentBoundingBoxesMap.entrySet()) {
			entry.getValue().setStrokeWidth(filamentWidth);
		}
		for (Map.Entry<TrackedFilament, List<Roi>> entry : filamentPlusTipsMap.entrySet()) {
			for (Roi roi : entry.getValue()) {
				roi.setStrokeWidth(filamentWidth);
			}
		}
		for (Map.Entry<TrackedFilament, List<Roi>> entry : filamentMinusTipsMap.entrySet()) {
			for (Roi roi : entry.getValue()) {
				roi.setStrokeWidth(filamentWidth);
			}
		}
		getImagePlus().repaintWindow();
	}

	@Override
	public void setViewMode(ImageDisplayMode viewMode) {
		ImagePlus imp = getImagePlus();
		if (imp != null) {
			if (viewMode == ImageDisplayMode.COLOR) {
				imp.setDisplayMode(IJ.COLOR);
			} else if (viewMode == ImageDisplayMode.COMPOSITE) {
				imp.setDisplayMode(IJ.COMPOSITE);
			} else if (viewMode == ImageDisplayMode.GRAYSCALE) {
				imp.setDisplayMode(IJ.GRAYSCALE);
			}
			this.autoScaleImage();
		}
	}

	@Override
	public void autoScaleImage() {
		ImagePlus imp = getImagePlus();
		ImageDisplay imd = getImageDisplay();
		Dataset data = (Dataset) imd.getActiveView().getData();

		if (imp != null) {

			int currentChannel = imp.getC();
			DoubleType min = (DoubleType) op.run("stats.min", data);
			DoubleType max = (DoubleType) op.run("stats.max", data);

			for (int c = 0; c <= imp.getChannel(); c++) {
				imp.setC(c);
				imp.setDisplayRange(min.get(), max.get());
				imp.updateChannelAndDraw();
			}
			imp.setC(currentChannel);
		}
	}

}
