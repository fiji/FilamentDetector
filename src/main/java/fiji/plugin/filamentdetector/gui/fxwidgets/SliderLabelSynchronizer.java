package fiji.plugin.filamentdetector.gui.fxwidgets;

import java.text.DecimalFormat;
import java.text.ParseException;

import javafx.event.Event;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;

public class SliderLabelSynchronizer {

	private Slider slider;
	private TextField textField;
	private double value;

	public SliderLabelSynchronizer(Slider slider, TextField textField) {
		this.slider = slider;
		this.textField = textField;
		setValue(0);
	}

	public void setValue(double value) {
		textField.setText(Double.toString(value));
		slider.setValue(value);
		this.value = value;
	}

	public double getValue() {
		return this.value;
	}

	public boolean isEvent(Event event) {
		return event.getSource().equals(slider) || event.getSource().equals(textField);
	}

	public void update(Event event) {

		DecimalFormat f = new DecimalFormat("##.00");
		double newValue;

		if (event.getSource().equals(slider)) {
			try {
				newValue = f.parse(f.format(slider.getValue())).doubleValue();
				textField.setText(Double.toString(newValue));
				this.value = newValue;
			} catch (ParseException e) {
				slider.setValue(this.value);
			}

		} else if (event.getSource().equals(textField)) {
			try {
				newValue = f.parse(f.format(Double.parseDouble(textField.getText()))).doubleValue();
				slider.setValue(newValue);
				this.value = newValue;
			} catch (NumberFormatException | ParseException | ClassCastException e) {
				textField.setText(Double.toString(this.value));
			}
		}
	}

	public void setTooltip(String tooltipMessage) {
		Tooltip tooltip = new Tooltip(tooltipMessage);
		Tooltip.install(slider, tooltip);
		Tooltip.install(textField, tooltip);
	}

}
