package fiji.plugin.filamentdetector.preprocessing;

import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.scijava.Context;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.ui.UIService;

import io.scif.services.DatasetIOService;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.ImgPlus;
import net.imagej.display.ImageDisplay;
import net.imagej.ops.OpService;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;

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

	private ImageDisplay image;
	private Dataset preprocessedImage;

	private double gaussianFilterSize = DEFAULT_GAUSSIAN_FITLER_SIZE;
	private boolean doGaussianFilter = DEFAULT_DO_GAUSSIAN_FILTER;
	private boolean savePreprocessedImage = DEFAULT_SAVE_IMAGE;
	private boolean showPreprocessedImage = DEFAULT_SHOW_IMAGE;
	private boolean convertTo8Bit = DEFAULT_CONVERT_TO_8BIT;

	public ImagePreprocessor(Context context, ImageDisplay imd) {
		context.inject(this);
		this.image = imd;
	}

	public void preprocess() {

		if (savePreprocessedImage) {

			// Apply Gaussian filter
			Dataset dataset = (Dataset) image.getActiveView().getData();
			ImgPlus img = dataset.getImgPlus();
			Img<FloatType> floatImg = ops.convert().float32(img);
			RandomAccessibleInterval<FloatType> blurredImg = ops.filter().gauss(floatImg, gaussianFilterSize);

			this.preprocessedImage = ds.create(blurredImg);
			
			if (showPreprocessedImage) {
				ui.show(this.preprocessedImage);
			}
			
			// Save if needed
			if (savePreprocessedImage) {
				if (dataset.getSource() != null) {
					String filePath = FilenameUtils.removeExtension(dataset.getSource());
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
		return convertTo8Bit;
	}

	public void setConvertTo8Bit(boolean convertTo8Bit) {
		this.convertTo8Bit = convertTo8Bit;
	}

}
