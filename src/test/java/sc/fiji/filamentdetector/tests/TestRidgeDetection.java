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
package sc.fiji.filamentdetector.tests;

import static org.junit.Assert.assertEquals;

import net.imagej.Dataset;
import net.imagej.axis.Axes;
import net.imagej.axis.CalibratedAxis;
import net.imagej.axis.DefaultLinearAxis;
import net.imagej.display.ImageDisplay;
import net.imglib2.img.Img;

import org.junit.Test;

import sc.fiji.filamentdetector.FilamentWorkflow;
import sc.fiji.filamentdetector.model.Filaments;
import sc.fiji.filamentdetector.model.TrackedFilaments;

public class TestRidgeDetection extends AbstractTest {

	@Test
	public void testFilamentWorkflow() {

		int width = 100;
		int maxFrame = 10;
		int nLines = 5;

		Img img = getFakeImage(width, maxFrame, nLines);
		Dataset dataset = ds.create(img);
		CalibratedAxis[] caxis = { new DefaultLinearAxis(Axes.X), new DefaultLinearAxis(Axes.Y),
				new DefaultLinearAxis(Axes.TIME) };
		dataset.setAxes(caxis);

		ImageDisplay imd = (ImageDisplay) displayService.createDisplayQuietly(dataset);

		FilamentWorkflow wf = new FilamentWorkflow(context, imd);
		wf.initialize();
		wf.getCalibrations().setChannelToUseIndex(1);

		wf.setFilamentDetector(detectorService.getRidgeFilamentDetector());
		wf.getFilamentDetector().setImageDisplay(imd);
		wf.getFilamentDetector().setDataset(dataset);
		wf.detect();
		Filaments filaments = wf.getFilaments();

		wf.setFilamentsTracker(trackerService.getBBoxTracker());
		wf.track();
		TrackedFilaments trackedFilaments = wf.getTrackedFilaments();

		log.info(filaments.size());
		log.info(trackedFilaments.size());
		assertEquals(filaments.size(), nLines * maxFrame);
		assertEquals(trackedFilaments.size(), nLines);

	}
}