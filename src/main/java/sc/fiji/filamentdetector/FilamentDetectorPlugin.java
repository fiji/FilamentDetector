/*-
 * #%L
 * A Fiji plugin that allow easy, fast and accurate detection and tracking of biological filaments.
 * %%
 * Copyright (C) 2016 - 2025 Fiji developers.
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
package sc.fiji.filamentdetector;

import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.DynamicCommand;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.DialogPrompt;

import net.imagej.ImageJ;
import net.imagej.display.ImageDisplay;
import sc.fiji.filamentdetector.gui.MainAppFrame;

@Plugin(type = DynamicCommand.class, menuPath = "Plugins>Tracking>FilamentDetector")
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

		try {
			FilamentWorkflow filamentDetector = new FilamentWorkflow(ij.context(), imd);

			// Launch JavaFX interface
			MainAppFrame app = new MainAppFrame(ij, filamentDetector);
			app.setTitle(PLUGIN_NAME + " version " + VERSION);
			app.initialize();

		} catch (Exception e) {
			ij.ui().showDialog("Error during initialization", e.getMessage(), DialogPrompt.MessageType.ERROR_MESSAGE);
			log.error("Error during initialization");
			e.printStackTrace();
		}

	}

}
