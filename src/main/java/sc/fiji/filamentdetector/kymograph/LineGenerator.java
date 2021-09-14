/*-
 * #%L
 * A Fiji plugin that allow easy, fast and accurate detection and tracking of biological filaments.
 * %%
 * Copyright (C) 2016 - 2021 Fiji developers.
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
package sc.fiji.filamentdetector.kymograph;

import org.apache.commons.lang3.ArrayUtils;

import ij.gui.Line;
import sc.fiji.filamentdetector.kymograph.linedrawer.LineDrawer;
import sc.fiji.filamentdetector.model.TrackedFilament;

public class LineGenerator {

	private TrackedFilament trackedFilament;
	private double lineTickness;
	private double startOffsetLength;
	private double endOffsetLength;

	public LineGenerator(double lineTickness, double startOffsetLength, double endOffsetLength) {
		this.lineTickness = lineTickness;
		this.startOffsetLength = startOffsetLength;
		this.endOffsetLength = endOffsetLength;
	}

	public TrackedFilament getTrackedFilament() {
		return trackedFilament;
	}

	public void setTrackedFilament(TrackedFilament trackedFilament) {
		this.trackedFilament = trackedFilament;
	}

	public Line build(LineDrawer lineDrawer) throws Exception {

		// Get the line from the line drawer
		double[] lineCoords = lineDrawer.draw(trackedFilament);

		// Extends tips
		lineCoords = extendTips(lineCoords, startOffsetLength, endOffsetLength);

		// Construct the line
		Line line = new Line(lineCoords[0], lineCoords[1], lineCoords[2], lineCoords[3]);
		line.setStrokeWidth(lineTickness);
		line.setPosition(-1);

		return line;
	}

	protected double[] extendTips(double[] lineCoords, double startOffsetLength, double endOffsetLength)
			throws Exception {

		double[] newStart = new double[2];
		double[] newEnd = new double[2];

		// Extend the line by an offset at the start tip
		newStart = extendTip(lineCoords, startOffsetLength);

		// Extend the line by an offset at the end tip
		double tmp = lineCoords[0];
		lineCoords[0] = lineCoords[2];
		lineCoords[2] = tmp;
		tmp = lineCoords[1];
		lineCoords[1] = lineCoords[3];
		lineCoords[3] = tmp;
		newEnd = extendTip(lineCoords, endOffsetLength);

		return ArrayUtils.addAll(newStart, newEnd);
	}

	protected double[] extendTip(double[] lineCoords, double offsetLength) throws Exception {
		double x0 = lineCoords[0];
		double y0 = lineCoords[1];
		double x1 = lineCoords[2];
		double y1 = lineCoords[3];

		double distanceRatio = getDistanceRatio(x0, y0, x1, y1, offsetLength);
		distanceRatio *= -1;

		double newX;
		double newY;

		newX = ((1 - distanceRatio) * x0) + (distanceRatio * x1);
		newY = ((1 - distanceRatio) * y0) + (distanceRatio * y1);

		return new double[] { newX, newY };

	}

	protected double getDistanceRatio(double x0, double y0, double x1, double y1, double offsetLength) {
		double lineLength = Math.sqrt(Math.pow(x0 - x1, 2) + Math.pow(y0 - y1, 2));
		double newLength = offsetLength;
		return newLength / lineLength;
	}

}
