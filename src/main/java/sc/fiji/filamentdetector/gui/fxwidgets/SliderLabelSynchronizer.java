/*-
 * #%L
 * A Fiji plugin that allow easy, fast and accurate detection and tracking of biological filaments.
 * %%
 * Copyright (C) 2016 - 2020 Fiji developers.
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
package sc.fiji.filamentdetector.gui.fxwidgets;

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
