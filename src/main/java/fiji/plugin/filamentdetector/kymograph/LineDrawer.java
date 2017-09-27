package fiji.plugin.filamentdetector.kymograph;

import fiji.plugin.filamentdetector.model.TrackedFilament;

public interface LineDrawer {

	double[] draw(TrackedFilament trackedFilament);

	String getName();

	void setName(String name);

	String getDescription();

	void setDescription(String description);
	
	String toString();

}