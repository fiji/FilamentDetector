package fiji.plugin.filamentdetector;

import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import fiji.plugin.filamentdetector.gui.MainAppFrame;
import net.imagej.ImageJ;
import net.imagej.display.ImageDisplay;

@Plugin(type = Command.class, menuPath = "Plugins>Tracking>FilamentDetector")
public class FilamentDetectorPlugin implements Command {

	@Parameter
	private ImageJ ij;

	@Parameter
	private LogService log;

	@Parameter
	private FilamentDetectorService mainService;

	@Parameter(type = ItemIO.INPUT)
	private ImageDisplay imd;

	public static final String PLUGIN_NAME = "FilamentDetector";
	public static final String VERSION = version();

	private static String version() {
		String version = null;
		final Package pack = FilamentDetectorPlugin.class.getPackage();
		if (pack != null) {
			version = pack.getImplementationVersion();
		}
		return version == null ? "DEVELOPMENT" : version;
	}

	@Override
	public void run() {
		log.info("Running " + PLUGIN_NAME + " version " + VERSION);

		// Launch JavaFX interface
		MainAppFrame app = new MainAppFrame(ij.context(), imd);
		app.setTitle(PLUGIN_NAME + " version " + VERSION);
		app.init();
	}

}
