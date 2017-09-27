package fiji.plugin.filamentdetector.kymograph;

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
		Line line = new Line(10, 10, 200, 200);
		line.setStrokeWidth(lineTickness);
		return line;
	}

}
