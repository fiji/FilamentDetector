/*-
 * #%L
 * A Fiji plugin that allow easy, fast and accurate detection and tracking of biological filaments.
 * %%
 * Copyright (C) 2016 - 2021 Fiji developers.
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

import java.util.stream.Collectors;

import org.scijava.Context;
import org.scijava.Initializable;
import org.scijava.convert.ConvertService;
import org.scijava.event.EventService;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.ui.UIService;

import net.imagej.Dataset;
import net.imagej.display.ImageDisplay;
import sc.fiji.filamentdetector.detection.FilamentDetector;
import sc.fiji.filamentdetector.detection.FilteringParameters;
import sc.fiji.filamentdetector.event.ImageNotFoundEvent;
import sc.fiji.filamentdetector.model.Filaments;
import sc.fiji.filamentdetector.model.TrackedFilaments;
import sc.fiji.filamentdetector.tracking.FilamentsTracker;
import sc.fiji.filamentdetector.tracking.FilteringTrackedFilamentsParameters;

/*
 * This class holds the necessary informations/models necessary during
 *  the use of the GUI. Can be also used for scripting.
 */
public class FilamentWorkflow implements Initializable {

	@Parameter
	private Context context;

	@Parameter
	private ConvertService convert;

	@Parameter
	private LogService log;

	@Parameter
	private UIService ui;

	@Parameter
	private EventService eventService;

	final private ImageDisplay sourceImage;
	private ImageDisplay imageDisplay;
	private Calibrations calibrations;

	private FilamentDetector filamentsDetector;
	private FilamentsTracker filamentsTracker;

	private Filaments filaments;
	private Filaments filteredFilaments;

	private TrackedFilaments trackedFilaments;
	private TrackedFilaments filteredTrackedFilaments;

	public FilamentWorkflow(Context context, ImageDisplay imd) {
		context.inject(this);
		this.imageDisplay = imd;
		this.sourceImage = imd;

		this.filaments = new Filaments();
		this.filteredFilaments = this.filaments;

		this.trackedFilaments = new TrackedFilaments(context);
		this.filteredTrackedFilaments = this.trackedFilaments;
	}

	@Override
	public void initialize() {
		// Get physical pixel sizes (um) and duration between frames (s)
		calibrations = new Calibrations(context, getDataset());
	}

	public void setFilamentDetector(FilamentDetector filamentDetector) {
		this.filamentsDetector = filamentDetector;
	}

	public void setFilamentsTracker(FilamentsTracker filamentsTracker) {
		this.filamentsTracker = filamentsTracker;
	}

	private void initDetection() {
		this.filamentsDetector.setDataset(getDataset());
		this.filamentsDetector.setImageDisplay(imageDisplay);
	}

	public void detectCurrentFrame() {
		this.initDetection();
		this.filamentsDetector.detectCurrentFrame(calibrations.getChannelToUseIndex());
		this.filaments = filamentsDetector.getFilaments();
		this.filteredFilaments = this.filaments;
	}

	public void detect() {
		this.initDetection();
		this.filamentsDetector.detect(calibrations.getChannelToUseIndex());
		this.filaments = filamentsDetector.getFilaments();
		this.filteredFilaments = this.filaments;
	}

	public void track() {
		this.filamentsTracker.setFilaments(getFilaments());
		this.filamentsTracker.track();
		this.trackedFilaments = filamentsTracker.getTrackedFilaments();
		this.filteredTrackedFilaments = this.trackedFilaments;
	}

	public Dataset getDataset() {
		try {
			Dataset dataset = (Dataset) imageDisplay.getActiveView().getData();
			return dataset;
		} catch (NullPointerException e) {
			eventService.publish(new ImageNotFoundEvent());
			return null;
		}
	}

	public void setImageDisplay(ImageDisplay imageDisplay) {
		this.imageDisplay = imageDisplay;
	}

	public ImageDisplay getImageDisplay() {
		return imageDisplay;
	}

	public ImageDisplay getSourceImage() {
		return sourceImage;
	}

	public Calibrations getCalibrations() {
		return calibrations;
	}

	public FilamentDetector getFilamentDetector() {
		return filamentsDetector;
	}

	public FilamentsTracker getFilamentsTracker() {
		return filamentsTracker;
	}

	public Filaments getFilaments() {
		return filteredFilaments;
	}

	public TrackedFilaments getTrackedFilaments() {
		return filteredTrackedFilaments;
	}

	public void filterFilament(FilteringParameters filteringParameters) {
		if (!filteringParameters.isDisableFiltering()) {
			this.filteredFilaments = this.filaments.stream()
					.filter(filament -> filament.getLength() <= filteringParameters.getMaxLength())
					.filter(filament -> filament.getLength() >= filteringParameters.getMinLength())
					.filter(filament -> filament.getSinuosity() <= filteringParameters.getMaxSinuosity())
					.filter(filament -> filament.getSinuosity() >= filteringParameters.getMinSinuosity())
					.collect(Collectors.toCollection(Filaments::new));
		} else {
			this.filteredFilaments = this.filaments;
		}
	}

	public void filterTrackedFilament(FilteringTrackedFilamentsParameters filteringParameters) {
		if (!filteringParameters.isDisableFiltering()) {

			double[] bbox = new double[] { 0, getCalibrations().getSizeX(), 0, getCalibrations().getSizeY() };
			bbox[0] += filteringParameters.getBorderLimit();
			bbox[1] -= filteringParameters.getBorderLimit();
			bbox[2] += filteringParameters.getBorderLimit();
			bbox[3] -= filteringParameters.getBorderLimit();

			this.filteredTrackedFilaments = this.trackedFilaments.stream()
					.filter(trackedFilament -> trackedFilament.size() <= filteringParameters.getMaxSize())
					.filter(trackedFilament -> trackedFilament.size() >= filteringParameters.getMinSize())
					.filter(trackedFilament -> trackedFilament.insideBbox(bbox))
					.collect(Collectors.toCollection(() -> new TrackedFilaments(context)));
		} else {
			this.filteredTrackedFilaments = this.trackedFilaments;
		}
	}

	public Context getContext() {
		return this.context;
	}

}
