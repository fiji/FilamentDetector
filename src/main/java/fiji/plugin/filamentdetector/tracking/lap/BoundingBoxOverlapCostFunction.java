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
package fiji.plugin.filamentdetector.tracking.lap;

import java.util.Arrays;

import fiji.plugin.filamentdetector.model.Filament;
import fiji.plugin.trackmate.tracking.sparselap.costfunction.CostFunction;

public class BoundingBoxOverlapCostFunction implements CostFunction<Filament, Filament> {

	@Override
	public double linkingCost(Filament source, Filament target) {

		// TODO: optimize, optimize, optimize !!!

		// Inspired from
		// http://www.pyimagesearch.com/2016/11/07/intersection-over-union-iou-for-object-detection/

		double[] bboxSource = Arrays.copyOf(source.getBoundingBox(), source.getBoundingBox().length);
		double[] bboxTarget = Arrays.copyOf(target.getBoundingBox(), target.getBoundingBox().length);

		// Change width and height to coordinates
		bboxSource[2] = bboxSource[0] + bboxSource[2];
		bboxSource[3] = bboxSource[1] + bboxSource[3];
		bboxTarget[2] = bboxTarget[0] + bboxTarget[2];
		bboxTarget[3] = bboxTarget[1] + bboxTarget[3];

		double interArea;

		if (checkOverlap(bboxSource, bboxTarget)) {
			// Determine the coordinates of the intersection rectangle
			double xA = Math.max(bboxSource[0], bboxTarget[0]);
			double yA = Math.max(bboxSource[1], bboxTarget[1]);
			double xB = Math.min(bboxSource[2], bboxTarget[2]);
			double yB = Math.min(bboxSource[3], bboxTarget[3]);

			// Compute the area of intersection rectangle
			interArea = (xB - xA + 1) * (yB - yA + 1);
		} else {
			interArea = 0;
		}

		// Compute the area of both the prediction and ground-truth rectangles
		double sourceArea = (bboxSource[2] - bboxSource[0] + 1) * (bboxSource[3] - bboxSource[1] + 1);
		double targetArea = (bboxTarget[2] - bboxTarget[0] + 1) * (bboxTarget[3] - bboxTarget[1] + 1);

		// COmpute union
		double unionArea = sourceArea + targetArea - interArea;

		// Compute the intersection over union by taking the intersection
		// area and dividing it by the sum of prediction + ground-truth
		// areas - the intersection area
		double iou = interArea / unionArea;
		double score = 1 - iou;

		if (score == 0) {
			score = 0.00001;
		}

		if (score == 1) {
			score = source.distanceFromCenter(target);
		}

		// System.out.println("**********");
		// System.out.println(source);
		// System.out.println(target);
		// System.out.println(score);

		return score;
	}

	private boolean checkOverlap(double[] bboxSource, double[] bboxTarget) {
		{
			if (bboxSource[2] < bboxTarget[0])
				return false; // source is left of target
			if (bboxSource[0] > bboxTarget[2])
				return false; // source is right of target
			if (bboxSource[3] < bboxTarget[1])
				return false; // source is above target
			if (bboxSource[1] > bboxTarget[3])
				return false; // source is below target
			return true; // boxes overlap
		}
	}

}
