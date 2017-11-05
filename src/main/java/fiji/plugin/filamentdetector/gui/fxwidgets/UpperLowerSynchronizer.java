/*-
 * #%L
 * A Fiji plugin that allow easy, fast and accurate detection and tracking of biological filaments.
 * %%
 * Copyright (C) 2016 - 2017 Hadrien Mary
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
package fiji.plugin.filamentdetector.gui.fxwidgets;

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
