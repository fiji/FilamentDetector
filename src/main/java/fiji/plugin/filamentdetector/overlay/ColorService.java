package fiji.plugin.filamentdetector.overlay;

import java.awt.Color;

import net.imagej.ImageJService;

public interface ColorService extends ImageJService {

	public void initialize();

	public int getLength();

	public Color getColor(int colorCounter);

	void initialize(String lut);

	String getLut();

}
