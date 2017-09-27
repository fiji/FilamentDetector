package fiji.plugin.filamentdetector.kymograph;

import java.util.Comparator;

import org.apache.commons.lang3.ArrayUtils;

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
		Filament filament = trackedFilament.stream().max(Comparator.comparing(Filament::getLength)).orElse(null);
		double[] lineCoords = filament.getTips();

		// Extends tips
		lineCoords = extendTips(lineCoords, startOffsetLength, endOffsetLength);

		// Construct the line
		Line line = new Line(lineCoords[0], lineCoords[1], lineCoords[2], lineCoords[3]);
		line.setStrokeWidth(lineTickness);
		line.setPosition(-1);

		return line;
	}

	private double[] extendTips(double[] lineCoords, double startOffsetLength, double endOffsetLength)
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

	private double[] extendTip(double[] lineCoords, double offsetLength) throws Exception {
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

	private double getDistanceRatio(double x0, double y0, double x1, double y1, double offsetLength) {
		double lineLength = Math.sqrt(Math.pow(x0 - x1, 2) + Math.pow(y0 - y1, 2));
		double newLength = offsetLength;
		return newLength / lineLength;
	}

}
