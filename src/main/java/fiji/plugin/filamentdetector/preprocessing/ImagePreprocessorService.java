package fiji.plugin.filamentdetector.preprocessing;

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
public class ImagePreprocessorService extends AbstractService implements ImageJService {

	@Parameter
	private Context context;

	@Parameter
	private LogService log;

	@Parameter
	private PluginService pluginService;

	public List<ImagePreprocessor> getImagePreprocessors() {
		List<ImagePreprocessor> detectors = new ArrayList<>();
		for (PluginInfo<ImagePreprocessor> pluginInfo : pluginService.getPluginsOfType(ImagePreprocessor.class)) {
			try {
				ImagePreprocessor plugin = pluginInfo.createInstance();
				plugin.setContext(context);
				detectors.add(plugin);
			} catch (InstantiableException e) {
				log.error("Can't load the following Image Preprocessor : " + pluginInfo.getName());
				log.error(e.getMessage());
			}
		}
		return detectors;
	}

	public Convert8BitPreprocessor get8BitConverter() {
		PluginInfo<SciJavaPlugin> pluginInfo = pluginService.getPlugin(Convert8BitPreprocessor.class);
		try {
			Convert8BitPreprocessor plugin = (Convert8BitPreprocessor) pluginInfo.createInstance();
			plugin.setContext(context);
			return plugin;
		} catch (InstantiableException e) {
			log.error("Can't load the following Image Preprocessor : " + pluginInfo.getName());
			log.error(e.getMessage());
			return null;
		}
	}

	public DOGFilterPreprocessor getDOGFilter() {
		PluginInfo<SciJavaPlugin> pluginInfo = pluginService.getPlugin(DOGFilterPreprocessor.class);
		try {
			DOGFilterPreprocessor plugin = (DOGFilterPreprocessor) pluginInfo.createInstance();
			plugin.setContext(context);
			return plugin;
		} catch (InstantiableException e) {
			log.error("Can't load the following Image Preprocessor : " + pluginInfo.getName());
			log.error(e.getMessage());
			return null;
		}
	}

	public FrangiFilterPreprocessor getFrangiFilter() {
		PluginInfo<SciJavaPlugin> pluginInfo = pluginService.getPlugin(FrangiFilterPreprocessor.class);
		try {
			FrangiFilterPreprocessor plugin = (FrangiFilterPreprocessor) pluginInfo.createInstance();
			plugin.setContext(context);
			return plugin;
		} catch (InstantiableException e) {
			log.error("Can't load the following Image Preprocessor : " + pluginInfo.getName());
			log.error(e.getMessage());
			return null;
		}
	}

	public GaussianFilterPreprocessor getGaussianFilter() {
		PluginInfo<SciJavaPlugin> pluginInfo = pluginService.getPlugin(GaussianFilterPreprocessor.class);
		try {
			GaussianFilterPreprocessor plugin = (GaussianFilterPreprocessor) pluginInfo.createInstance();
			plugin.setContext(context);
			return plugin;
		} catch (InstantiableException e) {
			log.error("Can't load the following Image Preprocessor : " + pluginInfo.getName());
			log.error(e.getMessage());
			return null;
		}
	}

	public NormalizeIntensitiesPreprocessor getIntensitiesNormalizer() {
		PluginInfo<SciJavaPlugin> pluginInfo = pluginService.getPlugin(NormalizeIntensitiesPreprocessor.class);
		try {
			NormalizeIntensitiesPreprocessor plugin = (NormalizeIntensitiesPreprocessor) pluginInfo.createInstance();
			plugin.setContext(context);
			return plugin;
		} catch (InstantiableException e) {
			log.error("Can't load the following Image Preprocessor : " + pluginInfo.getName());
			log.error(e.getMessage());
			return null;
		}
	}

	public PseudoFlatFieldCorrectionPreprocessor getPseudoFlatFieldCorrector() {
		PluginInfo<SciJavaPlugin> pluginInfo = pluginService.getPlugin(PseudoFlatFieldCorrectionPreprocessor.class);
		try {
			PseudoFlatFieldCorrectionPreprocessor plugin = (PseudoFlatFieldCorrectionPreprocessor) pluginInfo
					.createInstance();
			plugin.setContext(context);
			return plugin;
		} catch (InstantiableException e) {
			log.error("Can't load the following Image Preprocessor : " + pluginInfo.getName());
			log.error(e.getMessage());
			return null;
		}
	}

	public TubenessFilterPreprocessor getTubenessFilter() {
		PluginInfo<SciJavaPlugin> pluginInfo = pluginService.getPlugin(TubenessFilterPreprocessor.class);
		try {
			TubenessFilterPreprocessor plugin = (TubenessFilterPreprocessor) pluginInfo.createInstance();
			plugin.setContext(context);
			return plugin;
		} catch (InstantiableException e) {
			log.error("Can't load the following Image Preprocessor : " + pluginInfo.getName());
			log.error(e.getMessage());
			return null;
		}
	}

}
