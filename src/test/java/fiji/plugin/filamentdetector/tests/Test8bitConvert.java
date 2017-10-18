package fiji.plugin.filamentdetector.tests;

import ij.ImagePlus;
import ij.process.ImageConverter;
import net.imagej.Dataset;
import net.imagej.ImageJ;
import net.imagej.axis.CalibratedAxis;
import net.imagej.ops.convert.RealTypeConverter;
import net.imglib2.IterableInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;

public class Test8bitConvert {

	public static void main(final String... args) throws Exception {
		final ImageJ ij = net.imagej.Main.launch(args);

		String fpath = "/home/hadim/.doc/Code/Postdoc/ij/testdata/7,5uM_emccd_lapse1.tif";
		Dataset dataset = ij.dataset().open(fpath);
		dataset.setName("input");
		ij.ui().show(dataset);

		Dataset ij2 = ij2Convert(ij, dataset);
		ij2.setName("ij2");
		ij.ui().show(ij2);
		
		Dataset ij1 = ij1Convert(ij, dataset);
		ij1.setName("ij1");
		ij.ui().show(ij1);

	}

	public static <T extends RealType<T>> Dataset ij2Convert(ImageJ ij, Dataset input) {

		Dataset dataset = input.duplicate();

		Img<UnsignedByteType> out = ij.op().create().img(dataset, new UnsignedByteType());

		RealTypeConverter op = (RealTypeConverter) ij.op().op("convert.normalizeScale",
				dataset.getImgPlus().firstElement(), out.firstElement());
		ij.op().convert().imageType(out, (IterableInterval<T>) dataset.getImgPlus(), op);

		CalibratedAxis[] axes = new CalibratedAxis[dataset.numDimensions()];
		for (int i = 0; i != axes.length; i++) {
			axes[i] = dataset.axis(i);
		}
		Dataset output = ij.dataset().create(out);
		output.setAxes(axes);
		return output;
	}

	public static Dataset ij1Convert(ImageJ ij, Dataset input) {

		Dataset dataset = input.duplicate();

		ImagePlus imp = ij.convert().convert(dataset, ImagePlus.class).duplicate();
		ImageConverter converter = new ImageConverter(imp);
		converter.convertToGray8();
		Dataset output = ij.convert().convert(imp, Dataset.class);

		return output;
	}
}
