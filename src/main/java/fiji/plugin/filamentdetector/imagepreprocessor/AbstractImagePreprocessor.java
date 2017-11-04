package fiji.plugin.filamentdetector.imagepreprocessor;

import org.scijava.Context;
import org.scijava.plugin.AbstractRichPlugin;
import org.scijava.plugin.Parameter;

import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.axis.CalibratedAxis;
import net.imagej.ops.OpService;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;

public abstract class AbstractImagePreprocessor extends AbstractRichPlugin implements ImagePreprocessor {

	private String name;
	
	private boolean doPreprocess = false;
	private Dataset input;
	protected Dataset output;

	@Parameter
	protected Context context;

	@Parameter
	protected OpService ops;

	@Parameter
	protected DatasetService ds;

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public Dataset getOutput() {
		return output;
	}

	@Override
	public void setInput(Dataset input) {
		this.input = input;
	}

	@Override
	public Dataset getInput() {
		return input;
	}

	@Override
	public boolean isDoPreprocess() {
		return doPreprocess;
	}

	@Override
	public void setDoPreprocess(boolean doPreprocess) {
		this.doPreprocess = doPreprocess;
	}

	@Override
	public <T extends RealType<T>> Dataset matchRAIToDataset(RandomAccessibleInterval<T> rai, Dataset dataset) {
		CalibratedAxis[] axes = new CalibratedAxis[dataset.numDimensions()];
		for (int i = 0; i != axes.length; i++) {
			axes[i] = dataset.axis(i);
		}
		Dataset output = ds.create(rai);
		output.setAxes(axes);
		return output;
	}

	public <T extends RealType<T>> Dataset matchRAIToDataset(Img<T> rai, Dataset dataset) {
		CalibratedAxis[] axes = new CalibratedAxis[dataset.numDimensions()];
		for (int i = 0; i != axes.length; i++) {
			axes[i] = dataset.axis(i);
		}
		Dataset output = ds.create(rai);
		output.setAxes(axes);
		return output;
	}

}
