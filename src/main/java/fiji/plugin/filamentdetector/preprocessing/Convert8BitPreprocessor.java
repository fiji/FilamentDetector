package fiji.plugin.filamentdetector.preprocessing;

import org.scijava.Context;

import net.imagej.Dataset;
import net.imagej.axis.CalibratedAxis;
import net.imagej.ops.convert.RealTypeConverter;
import net.imglib2.IterableInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;

public class Convert8BitPreprocessor extends AbstractImagePreprocessor {

	private static boolean DEFAULT_DO_PREPROCESS = true;

	public Convert8BitPreprocessor(Context context) {
		super(context);
		setDoPreprocess(DEFAULT_DO_PREPROCESS);
	}

	@Override
	public <T extends RealType<T>> void preprocess() {
		if (isDoPreprocess() && getInput().getType().getClass() != UnsignedByteType.class) {
			Dataset dataset = getInput().duplicate();

			Img<UnsignedByteType> out = ops.create().img(dataset, new UnsignedByteType());

			RealTypeConverter op = (RealTypeConverter) ops.op("convert.normalizeScale",
					dataset.getImgPlus().firstElement(), out.firstElement());
			ops.convert().imageType(out, (IterableInterval<T>) dataset.getImgPlus(), op);

			CalibratedAxis[] axes = new CalibratedAxis[dataset.numDimensions()];
			for (int i = 0; i != axes.length; i++) {
				axes[i] = dataset.axis(i);
			}
			this.output = matchRAIToDataset(out, dataset);
		} else {
			this.output = getInput();
		}
	}

}
