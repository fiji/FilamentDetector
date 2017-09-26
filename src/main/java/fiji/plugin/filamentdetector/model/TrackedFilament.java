package fiji.plugin.filamentdetector.model;

import java.awt.Color;

public class TrackedFilament extends Filaments {

	private static final long serialVersionUID = 1L;

	private static int idCounter = 0;
	private int id;
	private Color color;

	public boolean lastFilamentIs(Filament filament) {
		return filament == this.get(this.size() - 1);
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

}
