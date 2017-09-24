package fiji.plugin.filamentdetector.gui.controller.helper;

import javafx.event.Event;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;

public class UpperLowerSynchronizer {

	private SliderLabelSynchronizer lowerSync;
	private SliderLabelSynchronizer upperSync;

	public UpperLowerSynchronizer(Slider lowerSlider, TextField lowerTextField, Slider upperSlider,
			TextField upperTextField) {

		lowerSync = new SliderLabelSynchronizer(lowerSlider, lowerTextField);
		upperSync = new SliderLabelSynchronizer(upperSlider, upperTextField);

		lowerSync.setValue(0);
		upperSync.setValue(1);
	}

	public void setUpperValue(double value) {
		if (value <= lowerSync.getValue()) {
			value = lowerSync.getValue() + 0.01;
		}
		upperSync.setValue(value);
	}

	public void setLowerValue(double value) {
		if (value >= upperSync.getValue()) {
			value = upperSync.getValue() - 0.01;
		}
		lowerSync.setValue(value);
	}

	public double getLowerValue() {
		return lowerSync.getValue();
	}

	public double getUpperValue() {
		return upperSync.getValue();
	}

	public boolean isEvent(Event event) {
		return upperSync.isEvent(event) || lowerSync.isEvent(event);
	}

	public void update(Event event) {
		if (upperSync.isEvent(event)) {
			upperSync.update(event);
			if (upperSync.getValue() <= lowerSync.getValue()) {
				upperSync.setValue(lowerSync.getValue() + 0.01);
			}
		} else if (lowerSync.isEvent(event)) {
			lowerSync.update(event);
			if (lowerSync.getValue() >= upperSync.getValue()) {
				lowerSync.setValue(upperSync.getValue() - 0.01);
			}
		}
	}

	public void setUpperTooltip(String tooltipMessage) {
		upperSync.setTooltip(tooltipMessage);
	}

	public void setLowerTooltip(String tooltipMessage) {
		lowerSync.setTooltip(tooltipMessage);
	}
}
