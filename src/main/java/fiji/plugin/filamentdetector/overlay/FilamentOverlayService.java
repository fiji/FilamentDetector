package fiji.plugin.filamentdetector.overlay;

import fiji.plugin.filamentdetector.model.Filament;
import fiji.plugin.filamentdetector.model.Filaments;
import fiji.plugin.filamentdetector.model.TrackedFilament;
import fiji.plugin.filamentdetector.model.TrackedFilaments;
import net.imagej.ImageJService;
import net.imagej.display.ImageDisplay;

public interface FilamentOverlayService extends ImageJService {

	void add(Filament filament);

	void add(Filaments filaments);

	void add(TrackedFilament trackedFilament);

	void add(TrackedFilaments trackedFilaments);

	void clearSelection();

	void disableOverlay(boolean disable);

	void exportToROIManager();

	int getColorAlpha();

	int getFilamentWidth();

	ImageDisplay getImageDisplay();

	void refresh();

	void remove(Filament filament);

	void remove(Filaments filaments);

	void remove(TrackedFilament trackedFilament);

	void remove(TrackedFilaments trackedFilaments);

	void reset();

	void setColorAlpha(int colorAlpha);

	void setFilamentWidth(int filamentWidth);

	void setImageDisplay(ImageDisplay imageDisplay);

	void setSelected(Filament filament, boolean moveToFrame, boolean clearSelection);

	void setSelected(TrackedFilament trackedFilament, boolean moveToFrame, boolean clearSelection);

	boolean isDrawBoundingBoxes();

	void setDrawBoundingBoxes(boolean drawBoundingBoxes);

	boolean isDrawPlusTips();

	boolean isDrawMinusTips();

	void setDrawPlusTips(boolean drawPlusTips);

	void setDrawMinusTips(boolean drawMinusTips);

	void setTipDiameter(int tipDiameter);

	int getTipDiameter();

	void updateTransparency();

	void updateLineWidth();
	
	void setViewMode(ImageDisplayMode viewMode);
}
