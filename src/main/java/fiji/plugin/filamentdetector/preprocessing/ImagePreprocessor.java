package fiji.plugin.filamentdetector.preprocessing;

import org.scijava.Named;
import org.scijava.plugin.RichPlugin;

import net.imagej.Dataset;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;

public interface ImagePreprocessor extends Named, RichPlugin {
	public <T extends RealType<T>> void preprocess();

	public Dataset getOutput();

	public void setInput(Dataset input);

	public Dataset getInput();

	public boolean isDoPreprocess();

	public void setDoPreprocess(boolean doPreprocess);

	public <T extends RealType<T>> Dataset matchRAIToDataset(RandomAccessibleInterval<T> rai, Dataset dataset);
}
