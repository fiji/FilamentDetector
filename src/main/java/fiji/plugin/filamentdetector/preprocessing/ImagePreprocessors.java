package fiji.plugin.filamentdetector.preprocessing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.scijava.Context;
import org.scijava.convert.ConvertService;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.ui.UIService;

import io.scif.services.DatasetIOService;
import javafx.application.Platform;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.display.ImageDisplay;
import net.imagej.ops.OpService;

public class ImagePreprocessors {

	private static boolean DEFAULT_SAVE_IMAGE = false;
	private static boolean DEFAULT_SHOW_IMAGE = false;
	private static boolean DEFAULT_USE_FOR_OVERLAY = false;

	@Parameter
	private Context context;

	@Parameter
	private LogService log;

	@Parameter
	private OpService ops;

	@Parameter
	private DatasetIOService dsio;

	@Parameter
	private DatasetService ds;

	@Parameter
	private UIService ui;

	@Parameter
	private ConvertService convert;

	@Parameter
	ConvertService convertService;

	private ImageDisplay imageDisplay;
	private Dataset preprocessedImage;

	private List<ImagePreprocessor> imagePreprocessors;

	private boolean savePreprocessedImage = DEFAULT_SAVE_IMAGE;
	private boolean showPreprocessedImage = DEFAULT_SHOW_IMAGE;
	private boolean useForOverlay = DEFAULT_USE_FOR_OVERLAY;

	private boolean hasBeenPreprocessed = false;

	public ImagePreprocessors(Context context, ImageDisplay imd) {
		context.inject(this);
		this.imageDisplay = imd;

		this.imagePreprocessors = new ArrayList<>();

		this.imagePreprocessors.add(new Convert8BitPreprocessor(context));
		this.imagePreprocessors.add(new GaussianFilterPreprocessor(context));
		this.imagePreprocessors.add(new PseudoFlatFieldCorrectionPreprocessor(context));
		this.imagePreprocessors.add(new DOGFilterPreprocessor(context));
	}

	public void preprocess() {

		Dataset originalDataset = (Dataset) this.imageDisplay.getActiveView().getData();
		Dataset temp = originalDataset;

		for (ImagePreprocessor processor : imagePreprocessors) {		
			processor.setInput(temp);
			processor.preprocess();
			temp = processor.getOutput();
			
			if (processor.isDoPreprocess()) {
				hasBeenPreprocessed = true;
			}
		}

		if (hasBeenPreprocessed) {

			this.preprocessedImage = temp;
			this.preprocessedImage
					.setName(FilenameUtils.removeExtension(originalDataset.getName()) + "-Preprocessed.tif");

			// Show if needed
			if (showPreprocessedImage) {
				Platform.runLater(() -> {
					ui.show(this.preprocessedImage);
				});
			}

			// Save if needed
			if (savePreprocessedImage) {
				if (originalDataset.getSource() != null) {
					String filePath = FilenameUtils.removeExtension(originalDataset.getSource());
					filePath += "-Preprocessed.tif";
					try {
						dsio.save(this.preprocessedImage, filePath);
					} catch (IOException e) {
						log.error("Can't save the result file.");
					}
				} else {
					log.error("Can't save the result file because the source is not set.");
				}
			}
		}
	}

	public ImageDisplay getImageDisplay() {
		return imageDisplay;
	}

	public List<ImagePreprocessor> getImagePreprocessors() {
		return imagePreprocessors;
	}

	public void setImagePreprocessors(List<ImagePreprocessor> imagePreprocessors) {
		this.imagePreprocessors = imagePreprocessors;
	}

	public Dataset getPreprocessedImage() {
		return preprocessedImage;
	}

	public boolean isSavePreprocessedImage() {
		return savePreprocessedImage;
	}

	public void setSavePreprocessedImage(boolean savePreprocessedImage) {
		this.savePreprocessedImage = savePreprocessedImage;
	}

	public boolean isShowPreprocessedImage() {
		return showPreprocessedImage;
	}

	public void setShowPreprocessedImage(boolean showPreprocessedImage) {
		this.showPreprocessedImage = showPreprocessedImage;
	}

	public boolean isHasBeenPreprocessed() {
		return hasBeenPreprocessed;
	}

	public boolean isUseForOverlay() {
		return useForOverlay;
	}

	public void setUseForOverlay(boolean useForOverlay) {
		this.useForOverlay = useForOverlay;
	}
}
