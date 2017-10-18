package fiji.plugin.filamentdetector.preprocessing;

import java.io.IOException;

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
import net.imagej.axis.Axes;
import net.imagej.axis.CalibratedAxis;
import net.imagej.display.ImageDisplay;
import net.imagej.ops.OpService;
import net.imagej.ops.convert.RealTypeConverter;
import net.imagej.ops.special.computer.UnaryComputerOp;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;

public class ImagePreprocessor {

	private static int DEFAULT_GAUSSIAN_FITLER_SIZE = 1;
	private static boolean DEFAULT_DO_GAUSSIAN_FILTER = true;
	private static boolean DEFAULT_SAVE_IMAGE = false;
	private static boolean DEFAULT_SHOW_IMAGE = false;
	private static boolean DEFAULT_CONVERT_TO_8BIT = true;

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

	private ImageDisplay image;
	private Dataset preprocessedImage;

	private double gaussianFilterSize = DEFAULT_GAUSSIAN_FITLER_SIZE;
	private boolean doGaussianFilter = DEFAULT_DO_GAUSSIAN_FILTER;
	private boolean savePreprocessedImage = DEFAULT_SAVE_IMAGE;
	private boolean showPreprocessedImage = DEFAULT_SHOW_IMAGE;
	private boolean doConvertTo8Bit = DEFAULT_CONVERT_TO_8BIT;

	private boolean hasBeenPreprocessed = false;

	public ImagePreprocessor(Context context, ImageDisplay imd) {
		context.inject(this);
		this.image = imd;
	}

	public void preprocess() {

		Dataset originalDataset = (Dataset) this.image.getActiveView().getData();
		Dataset temp = originalDataset;

		if (doConvertTo8Bit) {
			temp = converTo8Bit(temp);
			hasBeenPreprocessed = true;
		}

		if (doGaussianFilter) {
			temp = applyGaussianFilter(temp);
			hasBeenPreprocessed = true;
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

	public ImageDisplay getImage() {
		return image;
	}

	public Dataset getPreprocessedImage() {
		return preprocessedImage;
	}

	public double getGaussianFilterSize() {
		return gaussianFilterSize;
	}

	public void setGaussianFilterSize(double gaussianFilterSize) {
		this.gaussianFilterSize = gaussianFilterSize;
	}

	public boolean isDoGaussianFilter() {
		return doGaussianFilter;
	}

	public void setDoGaussianFilter(boolean doGaussianFilter) {
		this.doGaussianFilter = doGaussianFilter;
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

	public boolean isConvertTo8Bit() {
		return doConvertTo8Bit;
	}

	public void setConvertTo8Bit(boolean convertTo8Bit) {
		this.doConvertTo8Bit = convertTo8Bit;
	}

	public boolean isHasBeenPreprocessed() {
		return hasBeenPreprocessed;
	}

	private <T extends RealType<T>> Dataset converTo8Bit(Dataset input) {
		if (input.getType().getClass() != UnsignedByteType.class) {
			Dataset dataset = input.duplicate();

			Img<UnsignedByteType> out = ops.create().img(dataset, new UnsignedByteType());

			RealTypeConverter op = (RealTypeConverter) ops.op("convert.normalizeScale",
					dataset.getImgPlus().firstElement(), out.firstElement());
			ops.convert().imageType(out, (IterableInterval<T>) dataset.getImgPlus(), op);

			CalibratedAxis[] axes = new CalibratedAxis[dataset.numDimensions()];
			for (int i = 0; i != axes.length; i++) {
				axes[i] = dataset.axis(i);
			}
			Dataset output = ds.create(out);
			output.setAxes(axes);

			return output;
		} else {
			return input;
		}
	}

	private <T extends RealType<T>> Dataset applyGaussianFilter(Dataset input) {
		// Apply Gaussian filter
		Dataset dataset = input.duplicate();

		int[] fixedAxisIndices = new int[] { dataset.dimensionIndex(Axes.X), dataset.dimensionIndex(Axes.Y) };

		RandomAccessibleInterval<T> blurredImg = (RandomAccessibleInterval<T>) ops.create().img(dataset.getImgPlus());

		double[] sigmas = new double[] { gaussianFilterSize, gaussianFilterSize };
		UnaryComputerOp op = (UnaryComputerOp) ops.op("filter.gauss", dataset.getImgPlus(), sigmas);
		ops.slice(blurredImg, (RandomAccessibleInterval<T>) dataset.getImgPlus(), op, fixedAxisIndices);

		CalibratedAxis[] axes = new CalibratedAxis[dataset.numDimensions()];
		for (int i = 0; i != axes.length; i++) {
			axes[i] = dataset.axis(i);
		}
		Dataset output = ds.create(blurredImg);
		output.setAxes(axes);
		return output;
	}

}
