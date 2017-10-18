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
import net.imglib2.type.numeric.real.FloatType;

public class ImagePreprocessor {

	private static double DEFAULT_GAUSSIAN_FITLER_SIZE = 1;

	private static boolean DEFAULT_DO_GAUSSIAN_FILTER = false;
	private static boolean DEFAULT_SAVE_IMAGE = false;
	private static boolean DEFAULT_SHOW_IMAGE = false;
	private static boolean DEFAULT_CONVERT_TO_8BIT = true;

	private static boolean DEFAULT_DIFFERENCE_OF_GAUSSIAN = false;
	private static double DEFAULT_DOG_SIGMA1 = 6;
	private static double DEFAULT_DOG_SIGMA2 = 2;

	private static boolean DEFAULT_USE_FOR_OVERLAY = false;

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

	private double gaussianFilterSize = DEFAULT_GAUSSIAN_FITLER_SIZE;
	private boolean doGaussianFilter = DEFAULT_DO_GAUSSIAN_FILTER;

	private boolean savePreprocessedImage = DEFAULT_SAVE_IMAGE;
	private boolean showPreprocessedImage = DEFAULT_SHOW_IMAGE;
	private boolean doConvertTo8Bit = DEFAULT_CONVERT_TO_8BIT;

	private boolean doDifferenceOfGaussianFilter = DEFAULT_DIFFERENCE_OF_GAUSSIAN;
	private double sigma1 = DEFAULT_DOG_SIGMA1;
	private double sigma2 = DEFAULT_DOG_SIGMA2;

	private boolean useForOverlay = DEFAULT_USE_FOR_OVERLAY;

	private boolean hasBeenPreprocessed = false;

	public ImagePreprocessor(Context context, ImageDisplay imd) {
		context.inject(this);
		this.imageDisplay = imd;
	}

	public void preprocess() {

		Dataset originalDataset = (Dataset) this.imageDisplay.getActiveView().getData();
		Dataset temp = originalDataset;

		if (doConvertTo8Bit) {
			temp = converTo8Bit(temp);
			hasBeenPreprocessed = true;
		}

		if (doGaussianFilter) {
			temp = applyGaussianFilter(temp);
			hasBeenPreprocessed = true;
		}

		if (doDifferenceOfGaussianFilter) {
			temp = applyDifferenceOfGaussianFilter(temp);
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

	public ImageDisplay getImageDisplay() {
		return imageDisplay;
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

	public double getSigma1() {
		return sigma1;
	}

	public void setSigma1(double sigma1) {
		this.sigma1 = sigma1;
	}

	public double getSigma2() {
		return sigma2;
	}

	public void setSigma2(double sigma2) {
		this.sigma2 = sigma2;
	}

	public boolean isDoDifferenceOfGaussianFilter() {
		return doDifferenceOfGaussianFilter;
	}

	public void setDoDifferenceOfGaussianFilter(boolean doDifferenceOfGaussianFilter) {
		this.doDifferenceOfGaussianFilter = doDifferenceOfGaussianFilter;
	}

	public boolean isUseForOverlay() {
		return useForOverlay;
	}

	public void setUseForOverlay(boolean useForOverlay) {
		this.useForOverlay = useForOverlay;
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
		Dataset dataset = input.duplicate();

		int[] fixedAxisIndices = new int[] { dataset.dimensionIndex(Axes.X), dataset.dimensionIndex(Axes.Y) };

		RandomAccessibleInterval<T> out = (RandomAccessibleInterval<T>) ops.create().img(dataset.getImgPlus());

		double[] sigmas = new double[] { gaussianFilterSize, gaussianFilterSize };
		UnaryComputerOp op = (UnaryComputerOp) ops.op("filter.gauss", dataset.getImgPlus(), sigmas);
		ops.slice(out, (RandomAccessibleInterval<T>) dataset.getImgPlus(), op, fixedAxisIndices);

		CalibratedAxis[] axes = new CalibratedAxis[dataset.numDimensions()];
		for (int i = 0; i != axes.length; i++) {
			axes[i] = dataset.axis(i);
		}
		Dataset output = ds.create(out);
		output.setAxes(axes);
		return output;
	}

	private <T extends RealType<T>> Dataset applyDifferenceOfGaussianFilter(Dataset input) {
		Dataset dataset = input.duplicate();

		int[] fixedAxisIndices = new int[] { dataset.dimensionIndex(Axes.X), dataset.dimensionIndex(Axes.Y) };

		// Convert to 32 bits
		Img<FloatType> out = (Img<FloatType>) ops.run("convert.float32", dataset.getImgPlus());

		// Apply filter
		Img<FloatType> out2 = (Img<FloatType>) ops.create().img(out);
		UnaryComputerOp op = (UnaryComputerOp) ops.op("filter.dog", out, sigma1, sigma2);
		ops.slice(out2, out, op, fixedAxisIndices);

		// Clip intensities
		Img<T> out3 = (Img<T>) ops.create().img(dataset.getImgPlus());
		RealTypeConverter op2 = (RealTypeConverter) ops.op("convert.clip", dataset.getImgPlus().firstElement(),
				out2.firstElement());
		ops.convert().imageType(out3, out2, op2);

		CalibratedAxis[] axes = new CalibratedAxis[dataset.numDimensions()];
		for (int i = 0; i != axes.length; i++) {
			axes[i] = dataset.axis(i);
		}
		Dataset output = ds.create(out3);
		output.setAxes(axes);
		return output;
	}

}
