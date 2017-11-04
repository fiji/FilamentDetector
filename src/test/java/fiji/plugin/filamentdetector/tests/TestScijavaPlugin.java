package fiji.plugin.filamentdetector.tests;

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
		
		TestScijavaPlugin.RidgeDetector detector = (RidgeDetector) plugin.getPlugin(TestScijavaPlugin.RidgeDetector.class).createInstance();
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
