/*-
 * #%L
 * A Fiji plugin that allow easy, fast and accurate detection and tracking of biological filaments.
 * %%
 * Copyright (C) 2016 - 2021 Fiji developers.
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
package sc.fiji.filamentdetector.gui.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import sc.fiji.filamentdetector.model.TrackedFilament;

public class TrackedFilamentModel {

	final private TrackedFilament trackedFilament;

	private IntegerProperty id;
	private IntegerProperty size;
	private StringProperty color;

	public TrackedFilamentModel(TrackedFilament trackedFilament) {
		this.trackedFilament = trackedFilament;

		this.id = new SimpleIntegerProperty(trackedFilament.getId());
		this.size = new SimpleIntegerProperty(trackedFilament.size());
		this.color = new SimpleStringProperty(trackedFilament.getColorAsHex());
	}

	public IntegerProperty getId() {
		return id;
	}

	public void setId(IntegerProperty id) {
		this.id = id;
	}

	public IntegerProperty getSize() {
		return size;
	}

	public void setSize(IntegerProperty size) {
		this.size = size;
	}

	public TrackedFilament getTrackedFilament() {
		return trackedFilament;
	}

	public StringProperty getColor() {
		return color;
	}

	public void setColor(StringProperty color) {
		this.color = color;
	}
}
