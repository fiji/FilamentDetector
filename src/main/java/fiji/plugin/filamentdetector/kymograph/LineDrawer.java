package fiji.plugin.filamentdetector.kymograph;

import java.util.Comparator;

import fiji.plugin.filamentdetector.model.Filament;
import fiji.plugin.filamentdetector.model.TrackedFilament;
import ij.gui.Line;

public class LineDrawer {

	private TrackedFilament trackedFilament;
	private double lineTickness;
	private double startOffsetLength;
	private double endOffsetLength;

	public LineDrawer(double lineTickness, double startOffsetLength, double endOffsetLength) {
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

	public Line draw() throws Exception {

		// Get the longest filament in all the frames
		double[] lineCoords = new double[4];

		Filament filament = trackedFilament.stream().max(Comparator.comparing(Filament::getLength)).orElse(null);
		lineCoords[0] = filament.getXCoordinatesAsDouble()[0];
		lineCoords[1] = filament.getYCoordinatesAsDouble()[0];
		lineCoords[2] = filament.getXCoordinatesAsDouble()[filament.getSize() - 1];
		lineCoords[3] = filament.getYCoordinatesAsDouble()[filament.getSize() - 1];

		// Extend the line by the offset at the start tip
		lineCoords = extendTip(lineCoords, startOffsetLength);
		// Extend the line by the offset at the end tip
		/*
		 * double tmp = lineCoords[0]; lineCoords[0] = lineCoords[2]; lineCoords[2] =
		 * tmp; tmp = lineCoords[1]; lineCoords[1] = lineCoords[3]; lineCoords[3] = tmp;
		 * lineCoords = extendTip(lineCoords, endOffsetLength);
		 */

		// Construct the line
		Line line = new Line(lineCoords[0], lineCoords[1], lineCoords[2], lineCoords[3]);
		line.setStrokeWidth(lineTickness);
		return line;
	}

	private double[] extendTip(double[] lineCoords, double offsetLength) throws Exception {
		double x0 = lineCoords[0];
		double y0 = lineCoords[1];
		double x1 = lineCoords[2];
		double y1 = lineCoords[3];

		double distanceRatio = getDistanceRatio(x0, y0, x1, y1, offsetLength);

		double newX;
		double newY;

		if (y0 >= y1 && x1 <= x0) {
			newX = ((1 - distanceRatio) * x0) + (distanceRatio * x1);
			newY = ((1 - distanceRatio) * y0) + (distanceRatio * y1);
			return new double[] { newX, newY, x0, y0 };

		} else if (y1 >= y0 && x1 >= x0) {
			newX = ((1 - distanceRatio) * x0) + (distanceRatio * x1);
			newY = ((1 - distanceRatio) * y0) + (distanceRatio * y1);
			return new double[] { newX, newY, x1, y1 };

		} else if (y1 <= y0 && x1 >= x0) {
			distanceRatio *= -1;
			newX = ((1 - distanceRatio) * x0) + (distanceRatio * x1);
			newY = ((1 - distanceRatio) * y0) + (distanceRatio * y1);
			return new double[] { x1, y1, newX, newY };

		} else if (y1 >= y0 && x1 <= x0) {
			distanceRatio *= -1;
			newX = ((1 - distanceRatio) * x0) + (distanceRatio * x1);
			newY = ((1 - distanceRatio) * y0) + (distanceRatio * y1);
			return new double[] { x0, y0, newX, newY };

		} else {
			throw new Exception("Error during line drawing. This error should not happen.");
		}

	}

	private double getDistanceRatio(double x0, double y0, double x1, double y1, double offsetLength) {
		double lineLength = Math.sqrt(Math.pow(x0 - x1, 2) + Math.pow(y0 - y1, 2));
		double newLength = lineLength + offsetLength;
		return newLength / lineLength;
	}

}
