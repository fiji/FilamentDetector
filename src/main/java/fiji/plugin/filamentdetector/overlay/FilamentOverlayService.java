package fiji.plugin.filamentdetector.overlay;

import java.awt.Color;

import fiji.plugin.filamentdetector.Filament;
import fiji.plugin.filamentdetector.TrackedFilament;
import fiji.plugin.filamentdetector.TrackedFilaments;
import net.imagej.ImageJService;
import net.imagej.display.ImageDisplay;

public interface FilamentOverlayService extends ImageJService {

	void add(Filament filament);

	int getFilamentWidth();

	void setFilamentWidth(int filamentWidth);

	void setFilamentColor(Color filamentColor);

	Color getFilamentColor();

	void remove(Filament filament);

	void add(Filament filament, Color color);

	void setImageDisplay(ImageDisplay imageDisplay);

	ImageDisplay getImageDisplay();

	void add(TrackedFilament trackedFilament, Color color);

	void add(TrackedFilament trackedFilament);

	void remove(TrackedFilament trackedFilament);

	void add(TrackedFilaments trackedFilaments, Color color);

	void add(TrackedFilaments trackedFilaments);

	void remove(TrackedFilaments trackedFilaments);

	void setColorAlpha(int colorALpha);

	int getColorALpha();
}
