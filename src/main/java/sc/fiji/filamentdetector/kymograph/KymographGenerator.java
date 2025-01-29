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
package sc.fiji.filamentdetector.kymograph;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.scijava.Context;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.ui.UIService;

import ij.gui.Roi;
import ij.plugin.frame.RoiManager;
import io.scif.services.DatasetIOService;
import javafx.application.Platform;
import net.imagej.Dataset;
import net.imagej.display.ImageDisplay;
import sc.fiji.filamentdetector.model.TrackedFilament;
import sc.fiji.filamentdetector.model.TrackedFilaments;
import sc.fiji.kymographBuilder.KymographFactory;

public class KymographGenerator {

	@Parameter
	private Context context;

	@Parameter
	private UIService ui;

	@Parameter
	private LogService log;

	@Parameter
	private DatasetIOService io;

	private ImageDisplay imageDisplay;

	private TrackedFilaments trackedFilaments;
	private KymographParameters kymographParameters;
	private List<Dataset> kymographs;
	private boolean kymographsHasBeenSaved;

	public KymographGenerator(Context context) {
		context.inject(this);
		this.kymographParameters = new KymographParameters();

		// Disable KymographBuilder logs
		System.setProperty("scijava.log.level:sc.fiji.kymographBuilder.KymographFactory", "none");
		System.setProperty("scijava.log.level:sc.fiji.kymographBuilder.KymographCreator", "none");
	}

	public void build() {
		List<TrackedFilament> trackedFilamentsToBuild;

		if (kymographParameters.isBuildOneRandomKymograph()) {
			trackedFilamentsToBuild = new ArrayList<>();
			trackedFilamentsToBuild.add(getRandomTrackedFilament(trackedFilaments));
		} else {
			trackedFilamentsToBuild = trackedFilaments;
		}

		kymographs = new ArrayList<>();
		KymographFactory kfactory;

		List<Roi> lines = new ArrayList<>();
		Roi line;
		LineGenerator lineDrawer = new LineGenerator(kymographParameters.getLineThickness(),
				kymographParameters.getStartOffsetLength(), kymographParameters.getEndOffsetLength());

		String baseFolder = new File(getDataset().getSource()).getParent();
		if (kymographParameters.isSaveKymographs() && baseFolder == null) {
			kymographsHasBeenSaved = false;
			log.warn("Can't get the parent folder of the image. Kymographs won't be saved.");
		} else {
			kymographsHasBeenSaved = true;
		}

		try {
			for (TrackedFilament trackedFilament : trackedFilamentsToBuild) {

				// Generate the line
				lineDrawer.setTrackedFilament(trackedFilament);

				line = lineDrawer.build(kymographParameters.getLineDrawer());

				if (kymographParameters.isSaveKymographLines() && baseFolder != null) {
					lines.add(line);
				}

				// Build the kymograph
				kfactory = new KymographFactory(context, getDataset(), line);
				kfactory.build();

				final Dataset kymograph;
				kymograph = kfactory.getKymograph();
				kymograph.setName("Kymograph_" + trackedFilament.getId() + ".tif");
				kymographs.add(kymograph);

				// Show it if needed
				if (kymographParameters.isShowKymographs()) {
					Platform.runLater(() -> {
						ui.show(kymograph);
					});
				}

				if (kymographParameters.isSaveKymographs() && baseFolder != null) {
					try {
						io.save(kymograph, Paths.get(baseFolder, kymograph.getName()).toString());
					} catch (IOException e) {
						log.error("Error while saving the following kymograph : " + kymograph);
						log.error(e);
					}
				}
			}
		} catch (Exception e) {
			log.error(e);
		}

		if (kymographParameters.isSaveKymographLines() && baseFolder != null) {
			RoiManager rm = RoiManager.getRoiManager();
			rm.runCommand("Deselect");
			if (rm.getRoisAsArray().length > 0) {
				rm.runCommand("Delete");
			}
			for (Roi lineToSave : lines) {
				lineToSave.setStrokeWidth(1);
				rm.addRoi(lineToSave);
			}
			rm.runCommand("Save", Paths.get(baseFolder, "KymographLines.zip").toString());
		}

	}

	public ImageDisplay getImageDisplay() {
		return imageDisplay;
	}

	public Dataset getDataset() {
		return (Dataset) imageDisplay.getActiveView().getData();
	}

	public TrackedFilaments getTrackedFilaments() {
		return trackedFilaments;
	}

	public KymographParameters getKymographParameters() {
		return kymographParameters;
	}

	public void setImageDisplay(ImageDisplay imageDisplay) {
		this.imageDisplay = imageDisplay;
	}

	public void setTrackedFilaments(TrackedFilaments trackedFilaments) {
		this.trackedFilaments = trackedFilaments;
	}

	public int nKymographs() {
		return kymographs.size();
	}

	private TrackedFilament getRandomTrackedFilament(TrackedFilaments trackedFilaments) {
		Random rand = new Random();
		int listSize = 1;
		List<TrackedFilament> randomTrackedFilaments = rand.ints(listSize, 0, trackedFilaments.size())
				.mapToObj(i -> trackedFilaments.get(i)).collect(Collectors.toList());

		return randomTrackedFilaments.get(0);
	}

	public boolean kymographsHasBeenSaved() {
		return kymographsHasBeenSaved;
	}

	public List<Dataset> getKymographs() {
		return kymographs;
	}

}
