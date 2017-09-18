package fiji.plugin.filamentdetector;

import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.ContextCommand;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.DialogPrompt;

import fiji.plugin.filamentdetector.gui.MainAppFrame;
import net.imagej.ImageJ;
import net.imagej.display.ImageDisplay;

@Plugin(type = ContextCommand.class, menuPath = "Plugins>Tracking>FilamentDetector")
public class FilamentDetectorPlugin implements Command {

	@Parameter
	private ImageJ ij;

	@Parameter
	private LogService log;

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

		FilamentDetector filamentDetector = new FilamentDetector(ij.context(), imd);

		try {
			// Launch JavaFX interface
			MainAppFrame app = new MainAppFrame(ij, filamentDetector);
			app.setTitle(PLUGIN_NAME + " version " + VERSION);
			app.init();
		} catch (Exception e) {
			ij.ui().showDialog(e.getMessage(), "Error during initialization", DialogPrompt.MessageType.ERROR_MESSAGE);
			
			// TODO: Find a better way to exit
			System.exit(-1);
		}

	}

}
