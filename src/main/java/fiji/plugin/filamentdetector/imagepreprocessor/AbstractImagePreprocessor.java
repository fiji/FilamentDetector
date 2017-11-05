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
