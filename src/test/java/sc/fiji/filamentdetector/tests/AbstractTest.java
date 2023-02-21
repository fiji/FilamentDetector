/*-
 * #%L
 * A Fiji plugin that allow easy, fast and accurate detection and tracking of biological filaments.
 * %%
 * Copyright (C) 2016 - 2023 Fiji developers.
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
package sc.fiji.filamentdetector.tests;

import org.junit.Before;
import org.scijava.Context;
import org.scijava.display.DisplayService;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

import ij.ImagePlus;
import ij.gui.NewImage;
import ij.process.ImageProcessor;
import net.imagej.DatasetService;
import net.imagej.ops.OpService;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.RealType;
import sc.fiji.filamentdetector.detection.FilamentDetectorService;
import sc.fiji.filamentdetector.tracking.FilamentTrackerService;

public abstract class AbstractTest {

	@Parameter
	protected Context context;

	@Parameter
	protected FilamentDetectorService detectorService;

	@Parameter
	protected FilamentTrackerService trackerService;

	@Parameter
	protected LogService log;

	@Parameter
	protected DatasetService ds;

	@Parameter
	DisplayService displayService;

	/** Subclasses can override to create a context with different services. */
	protected Context createContext() {
		return new Context();
	}

	/** Sets up a SciJava context with {@link OpService}. */
	@Before
	public void setUp() {
		createContext().inject(this);
	}

	protected static <T extends RealType<T>> Img<T> getFakeImage(int width, int maxFrame, int nLines) {

		ImagePlus imp = NewImage.createShortImage("Source", width, width, maxFrame, NewImage.FILL_BLACK);
		ImageProcessor ip;

		for (int i = 0; i < nLines; i++) {
			for (int frame = 0; frame < maxFrame; frame++) {
				int ox1 = (2 * i + 1) * width / nLines / 2;
				int ox2 = (2 * i + 2) * width / nLines / 2;
				int oy1 = (width / nLines) + (frame * 6);
				int oy2 = (2 * width / nLines) + (frame * 6);

				imp.setSlice(frame + 1);
				ip = imp.getProcessor();
				ip.setColor(5000);
				ip.setLineWidth(3);
				ip.drawLine(ox1, oy1, ox2, oy2);
			}
		}
		return (Img<T>) ImageJFunctions.wrap(imp);
	}

}
