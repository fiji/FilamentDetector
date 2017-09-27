package fiji.plugin.filamentdetector.kymograph;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import javax.swing.SwingUtilities;

import org.scijava.Context;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.ui.UIService;

import fiji.plugin.filamentdetector.model.TrackedFilament;
import fiji.plugin.filamentdetector.model.TrackedFilaments;
import ij.gui.Line;
import ij.gui.Roi;
import io.scif.services.DatasetIOService;
import javafx.application.Platform;
import net.imagej.Dataset;
import net.imagej.display.ImageDisplay;
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

	public KymographGenerator(Context context) {
		context.inject(this);
		this.kymographParameters = new KymographParameters();
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

		Roi line;
		LineDrawer lineDrawer = new LineDrawer(kymographParameters.getLineThickness(),
				kymographParameters.getStartOffsetLength(), kymographParameters.getEndOffsetLength());

		String baseFolder = new File(getDataset().getSource()).getParent();
		if (baseFolder == null) {
			log.warn("Can't get the parent folder of the image. Kymographs won't be saved.");
		}

		for (TrackedFilament trackedFilament : trackedFilamentsToBuild) {

			// Generate the line
			lineDrawer.setTrackedFilament(trackedFilament);
			line = lineDrawer.draw();

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

}
