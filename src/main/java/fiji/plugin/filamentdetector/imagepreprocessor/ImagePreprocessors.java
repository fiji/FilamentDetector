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
package fiji.plugin.filamentdetector.imagepreprocessor;

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
	private static boolean DEFAULT_USE_FOR_OVERLAY = true;

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
	private ImagePreprocessorService imageProcessorPlugin;

	@Parameter
	ConvertService convertService;

	private ImageDisplay imageDisplay;
	private Dataset preprocessedDataset;

	private List<ImagePreprocessor> imagePreprocessorList;

	private boolean savePreprocessedImage = DEFAULT_SAVE_IMAGE;
	private boolean showPreprocessedImage = DEFAULT_SHOW_IMAGE;
	private boolean useForOverlay = DEFAULT_USE_FOR_OVERLAY;

	private boolean hasBeenPreprocessed = false;

	public ImagePreprocessors(Context context, ImageDisplay imd) {
		context.inject(this);
		this.imageDisplay = imd;

		this.imagePreprocessorList = new ArrayList<>();
		this.imagePreprocessorList.add(imageProcessorPlugin.get8BitConverter());
		this.imagePreprocessorList.add(imageProcessorPlugin.getGaussianFilter());
		this.imagePreprocessorList.add(imageProcessorPlugin.getPseudoFlatFieldCorrector());
		this.imagePreprocessorList.add(imageProcessorPlugin.getDOGFilter());
		this.imagePreprocessorList.add(imageProcessorPlugin.getTubenessFilter());
		this.imagePreprocessorList.add(imageProcessorPlugin.getFrangiFilter());
		this.imagePreprocessorList.add(imageProcessorPlugin.getIntensitiesNormalizer());
	}

	public void preprocess() {

		Dataset originalDataset = (Dataset) this.imageDisplay.getActiveView().getData();
		Dataset temp = originalDataset;

		for (ImagePreprocessor processor : imagePreprocessorList) {
			processor.setInput(temp);
			processor.preprocess();
			temp = processor.getOutput();

			if (processor.isDoPreprocess()) {
				hasBeenPreprocessed = true;
			}
		}

		if (hasBeenPreprocessed) {

			this.preprocessedDataset = temp;
			this.preprocessedDataset
					.setName(FilenameUtils.removeExtension(originalDataset.getName()) + "-Preprocessed.tif");

			// Show if needed
			if (showPreprocessedImage) {
				Platform.runLater(() -> {
					ui.show(this.preprocessedDataset);
				});
			}

			// Save if needed
			if (savePreprocessedImage) {
				if (originalDataset.getSource() != null) {
					String filePath = FilenameUtils.removeExtension(originalDataset.getSource());
					filePath += "-Preprocessed.tif";
					try {
						dsio.save(this.preprocessedDataset, filePath);
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
		return imagePreprocessorList;
	}

	public void setImagePreprocessors(List<ImagePreprocessor> imagePreprocessors) {
		this.imagePreprocessorList = imagePreprocessors;
	}

	public Dataset getPreprocessedDataset() {
		return preprocessedDataset;
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

	public ImagePreprocessor getPreProcessorByName(String name) {
		return this.getImagePreprocessors().stream().filter(x -> x.getClass().getSimpleName().equals(name)).findFirst()
				.orElse(null);
	}
}
