package fiji.plugin.filamentdetector.tracking;

import java.util.ArrayList;
import java.util.List;

import org.scijava.Context;
import org.scijava.InstantiableException;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;
import org.scijava.plugin.SciJavaPlugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

import net.imagej.ImageJService;

@Plugin(type = Service.class)
public class FilamentTrackerService extends AbstractService implements ImageJService {

	@Parameter
	private Context context;

	@Parameter
	private LogService log;

	@Parameter
	private PluginService pluginService;

	public List<FilamentsTracker> getDetectors() {
		List<FilamentsTracker> plugins = new ArrayList<>();
		for (PluginInfo<FilamentsTracker> pluginInfo : pluginService.getPluginsOfType(FilamentsTracker.class)) {
			try {
				FilamentsTracker plugin = pluginInfo.createInstance();
				plugin.setContext(context);
				plugins.add(plugin);
			} catch (InstantiableException e) {
				log.error("Can't load the following Filament Tracker : " + pluginInfo.getName());
				log.error(e.getMessage());
			}
		}
		return plugins;
	}

	public BBoxLAPFilamentsTracker getBBoxTracker() {
		PluginInfo<SciJavaPlugin> pluginInfo = pluginService.getPlugin(BBoxLAPFilamentsTracker.class);
		try {
			BBoxLAPFilamentsTracker plugin = (BBoxLAPFilamentsTracker) pluginInfo.createInstance();
			plugin.setContext(context);
			return plugin;
		} catch (InstantiableException e) {
			log.error("Can't load the following Filament Tracker : " + pluginInfo.getName());
			log.error(e.getMessage());
			return null;
		}
	}

}
