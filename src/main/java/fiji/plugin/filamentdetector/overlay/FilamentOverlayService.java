package fiji.plugin.filamentdetector.overlay;

import java.awt.Color;

import fiji.plugin.filamentdetector.model.Filament;
import fiji.plugin.filamentdetector.model.Filaments;
import fiji.plugin.filamentdetector.model.TrackedFilament;
import fiji.plugin.filamentdetector.model.TrackedFilaments;
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
	
	void add(Filaments filaments, Color color);
	
	void add(Filaments filaments);
	
	void remove(Filaments filaments);

	void setColorAlpha(int colorALpha);

	int getColorALpha();
	
	void reset();
}
