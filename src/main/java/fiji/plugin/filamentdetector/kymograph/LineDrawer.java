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

	public Line draw() {

		//Get the longest filament in all the frames
		double[] lineCoords = new double[4];
		
		Filament filament = trackedFilament.stream().max(Comparator.comparing(Filament::getLength)).orElse(null);
		lineCoords[0] = filament.getXCoordinatesAsDouble()[0];
		lineCoords[1] = filament.getYCoordinatesAsDouble()[0];
		lineCoords[2] = filament.getXCoordinatesAsDouble()[filament.getSize() - 1];
		lineCoords[3] = filament.getYCoordinatesAsDouble()[filament.getSize() - 1];
		
		// Extend the line by the offset
		lineCoords = extendStartTip(lineCoords, startOffsetLength);
		lineCoords = extendEndTip(lineCoords, endOffsetLength);
		
		// Construct the line
		Line line = new Line(lineCoords[0], lineCoords[1], lineCoords[2], lineCoords[3]);
		line.setStrokeWidth(lineTickness);
		return line;
	}

	private double[] extendEndTip(double[] lineCoords, double endOffsetLength) {
		return lineCoords;
	}

	private double[] extendStartTip(double[] lineCoords, double startOffsetLength) {
		double x1 = lineCoords[0]; 
		double y1 = lineCoords[1]; 
		double x2 = lineCoords[2]; 
		double y2 = lineCoords[3]; 
		
		double lineLength = Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
		double newLength = lineLength + startOffsetLength;
		double distanceRatio = newLength / lineLength;
		
		double newX1 = ((1 - distanceRatio) * x1) + (distanceRatio * x2);
		double newY1 = ((1 - distanceRatio) * x1) + (distanceRatio * x2);
		
		return new double[]{newX1, newY1, x2, y2};
	}

}
