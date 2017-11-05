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
package fiji.plugin.filamentdetector.tests.manual;

import org.scijava.Context;
import org.scijava.Priority;
import org.scijava.log.LogService;
import org.scijava.plugin.AbstractRichPlugin;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;
import org.scijava.plugin.RichPlugin;

import net.imagej.ImageJ;

public class TestScijavaPlugin {

	public static void main(final String... args) throws Exception {
		final ImageJ ij = net.imagej.Main.launch(args);
		Context context = ij.getContext();

		LogService log = ij.log();
		PluginService plugin = ij.plugin();

		// plugin.reloadPlugins();
		for (PluginInfo<TestScijavaPlugin.Detector> detectorInfo : plugin
				.getPluginsOfType(TestScijavaPlugin.Detector.class)) {

			Detector detector = detectorInfo.createInstance();
			log.info(detector);
			log.info(detector.getName());
			log.info(detector.getInfo());
			log.info(detector.getIdentifier());
			log.info(detector.getVersion());
			log.info(detector.getPriority());
			log.info("******************************");
		}

		TestScijavaPlugin.RidgeDetector detector = (RidgeDetector) plugin
				.getPlugin(TestScijavaPlugin.RidgeDetector.class).createInstance();
		log.info(detector.getName());

	}

	public static interface Detector extends RichPlugin {
		String getName();
	}

	public static abstract class AbstractDetector extends AbstractRichPlugin implements Detector {
		public String getName() {
			return "abstract";
		}
	}

	@Plugin(type = Detector.class, priority = Priority.HIGH)
	public static class RidgeDetector extends AbstractDetector {
		public String getName() {
			return "ridge detector";
		}
	}
}
