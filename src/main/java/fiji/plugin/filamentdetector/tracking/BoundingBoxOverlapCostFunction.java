package fiji.plugin.filamentdetector.tracking;

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

		return 1 - iou;
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
