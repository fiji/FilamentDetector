/*-
 * #%L
 * A Fiji plugin that allow easy, fast and accurate detection and tracking of biological filaments.
 * %%
 * Copyright (C) 2016 - 2023 Fiji developers.
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
package sc.fiji.filamentdetector.tracking;

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
