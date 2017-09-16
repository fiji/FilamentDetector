package fiji.plugin.filamentdetector.tests;

import org.scijava.Context;

import fiji.plugin.filamentdetector.gui.MainAppFrame;
import net.imagej.ImageJ;

public class TestGUI {

	public static void main(final String... args) throws Exception {
		final ImageJ ij = net.imagej.Main.launch(args);
		Context context = ij.getContext();

		MainAppFrame app = new MainAppFrame(ij);
		app.setTitle("test GUI");
		app.init();
	}
}