package fiji.plugin.filamentdetector.model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class TrackedFilament extends Filaments {

	private static final long serialVersionUID = 1L;

	private static int idCounter = 0;
	private int id;
	private Color color;

	// The most dynamic tip
	private Tip plusTip;

	// The less dynamic tip
	private Tip minusTip;

	public boolean lastFilamentIs(Filament filament) {
		return filament.equals(this.get(this.size() - 1));
	}

	public int getId() {
		return id;
	}

	public TrackedFilament() {
		assignID();
	}

	private synchronized void assignID() {
		this.id = idCounter;
		idCounter++;
	}

	public Color getColor() {
		return color;
	}

	public String getColorAsHex() {
		return String.format("#%02x%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
	}

	public void setColor(Color color) {
		this.color = color;
		for (Filament filament : this) {
			filament.setColor(color);
		}
	}

	public List<Double> getLengths() {
		List<Double> lengths = new ArrayList<>();
		for (Filament filament : this) {
			lengths.add(filament.getLength());
		}
		return lengths;
	}

	public List<Double> getFrames() {
		List<Double> frames = new ArrayList<>();
		for (Filament filament : this) {
			frames.add((double) filament.getFrame());
		}
		return frames;
	}

	public Tip getPlusTip() {
		if (plusTip == null) {
			this.detectTips();
		}
		return plusTip;
	}

	public Tip getMinusTip() {
		if (minusTip == null) {
			this.detectTips();
		}
		return minusTip;
	}

	protected void detectTips() {

		List<Double> tip1X = new ArrayList<>();
		List<Double> tip1Y = new ArrayList<>();
		List<Double> tip2X = new ArrayList<>();
		List<Double> tip2Y = new ArrayList<>();

		List<Integer> tip1Frame = new ArrayList<>();
		List<Integer> tip2Frame = new ArrayList<>();

		double[] x;
		double[] y;

		for (Filament filament : this) {
			x = filament.getXCoordinatesAsDouble();
			y = filament.getYCoordinatesAsDouble();

			tip1X.add(x[0]);
			tip1Y.add(y[0]);

			tip2X.add(x[x.length - 1]);
			tip2Y.add(y[x.length - 1]);

			tip1Frame.add(filament.getFrame());
			tip2Frame.add(filament.getFrame());
		}

		Tip tip1 = new Tip(tip1X.stream().mapToDouble(d -> d).toArray(), tip1Y.stream().mapToDouble(d -> d).toArray(),
				tip1Frame.stream().mapToInt(d -> d).toArray());

		Tip tip2 = new Tip(tip2X.stream().mapToDouble(d -> d).toArray(), tip2Y.stream().mapToDouble(d -> d).toArray(),
				tip2Frame.stream().mapToInt(d -> d).toArray());

		boolean reverseCoordinates = false;
		double scoreTip1 = (tip1.getStdX() * tip1.getDispX() + tip1.getStdY() * tip1.getDispY());
		double scoreTip2 = (tip2.getStdX() * tip2.getDispX() + tip2.getStdY() * tip2.getDispY());

		if (scoreTip1 > scoreTip2) {
			plusTip = tip1;
			minusTip = tip2;
			reverseCoordinates = true;
		} else {
			plusTip = tip2;
			minusTip = tip1;
		}

		// By convention we say the index 0 of the coordinates is the minusTip
		// and the last index is the plusTip.
		if (reverseCoordinates) {
			for (Filament filament : this) {
				filament.reverseCoordinates();
			}
		}
	}

	public Filament getFilamentByFrame(double frame) {
		return this.stream().filter(x -> x.getFrame() == frame).findFirst().orElse(null);
	}

}
