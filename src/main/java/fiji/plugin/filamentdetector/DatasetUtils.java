package fiji.plugin.filamentdetector;

import org.scijava.Context;
import org.scijava.log.LogService;

import net.imagej.Dataset;
import net.imagej.ImageJ;
import net.imagej.display.ImageDisplay;

public class DatasetUtils {

	// TODO: use IJ Ops to do that
	static public ImageDisplay convertTo8Bit(ImageDisplay imageDisplay, ImageJ ij) {
		/*
		 * ImageDisplay out = null; Dataset dataset = (Dataset)
		 * imageDisplay.getActiveView().getData();
		 * 
		 * Img<DoubleType> clipped = ij.op().create().img(dataset); Op clip_op =
		 * ij.op().op("convert.clip", dataset.getImgPlus().firstElement(),
		 * dataset.firstElement()); ij.op().op("convert.imageType", clipped, dataset,
		 * clip_op);
		 * 
		 * Dataset converted = ij.dataset().create(ij.op().create().imgPlus(clipped));
		 * ij.ui().show(converted);
		 * 
		 * return ij.imageDisplay().getActiveImageDisplay();
		 */
		return null;
	}

	public static void main(final String... args) throws Exception {
		final ImageJ ij = net.imagej.Main.launch(args);
		Context context = ij.getContext();

		LogService log = ij.log();

		String fpath = "/home/hadim/.doc/Code/Postdoc/ij/testdata/test-16bit.tif";
		Dataset dataset = ij.dataset().open(fpath);
		ij.ui().show(dataset);

		ImageDisplay imd = ij.imageDisplay().getActiveImageDisplay();

		ImageDisplay converted = DatasetUtils.convertTo8Bit(imd, ij);
	}

}
