package fiji.plugin.filamentdetector.analyzer;

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
public class AnalyzerService extends AbstractService implements ImageJService {

	@Parameter
	private Context context;

	@Parameter
	private LogService log;

	@Parameter
	private PluginService pluginService;

	public List<Analyzer> getAnalyzers() {
		List<Analyzer> detectors = new ArrayList<>();
		for (PluginInfo<Analyzer> pluginInfo : pluginService.getPluginsOfType(Analyzer.class)) {
			try {
				Analyzer plugin = pluginInfo.createInstance();
				plugin.setContext(context);
				detectors.add(plugin);
			} catch (InstantiableException e) {
				log.error("Can't load the following Analyzer : " + pluginInfo.getName());
				log.error(e.getMessage());
			}
		}
		return detectors;
	}

	public LengthOverTimeAnalyzer getLengthOverTimeAnalyzer() {
		PluginInfo<SciJavaPlugin> pluginInfo = pluginService.getPlugin(LengthOverTimeAnalyzer.class);
		try {
			LengthOverTimeAnalyzer plugin = (LengthOverTimeAnalyzer) pluginInfo.createInstance();
			plugin.setContext(context);
			return plugin;
		} catch (InstantiableException e) {
			log.error("Can't load the following Analyzer : " + pluginInfo.getName());
			log.error(e.getMessage());
			return null;
		}
	}

	public TipFitterAnalyzer getTipFitterAnalyzer() {
		PluginInfo<SciJavaPlugin> pluginInfo = pluginService.getPlugin(TipFitterAnalyzer.class);
		try {
			TipFitterAnalyzer plugin = (TipFitterAnalyzer) pluginInfo.createInstance();
			plugin.setContext(context);
			return plugin;
		} catch (InstantiableException e) {
			log.error("Can't load the following Analyzer : " + pluginInfo.getName());
			log.error(e.getMessage());
			return null;
		}
	}

	public NaiveNucleationAnalyzer getNaiveNucleationAnalyzer() {
		PluginInfo<SciJavaPlugin> pluginInfo = pluginService.getPlugin(NaiveNucleationAnalyzer.class);
		try {
			NaiveNucleationAnalyzer plugin = (NaiveNucleationAnalyzer) pluginInfo.createInstance();
			plugin.setContext(context);
			return plugin;
		} catch (InstantiableException e) {
			log.error("Can't load the following Analyzer : " + pluginInfo.getName());
			log.error(e.getMessage());
			return null;
		}
	}

}
