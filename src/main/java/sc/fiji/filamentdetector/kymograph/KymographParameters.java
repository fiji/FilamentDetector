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
package sc.fiji.filamentdetector.kymograph;

import sc.fiji.filamentdetector.kymograph.linedrawer.LineDrawer;
import sc.fiji.filamentdetector.kymograph.linedrawer.LongestFilamentLineDrawer;

public class KymographParameters {

	private boolean buildOneRandomKymograph = false;
	private boolean saveKymographs = false;
	private boolean showKymographs = true;
	private boolean saveKymographLines = false;
	private double lineThickness = 8;
	private double startOffsetLength = 20;
	private double endOffsetLength = 20;
	private LineDrawer lineDrawer = new LongestFilamentLineDrawer();

	public boolean isBuildOneRandomKymograph() {
		return buildOneRandomKymograph;
	}

	public void setBuildOneRandomKymograph(boolean buildOneRandomKymograph) {
		this.buildOneRandomKymograph = buildOneRandomKymograph;
	}

	public boolean isSaveKymographs() {
		return saveKymographs;
	}

	public void setSaveKymographs(boolean saveKymographs) {
		this.saveKymographs = saveKymographs;
	}

	public boolean isShowKymographs() {
		return showKymographs;
	}

	public void setShowKymographs(boolean showKymographs) {
		this.showKymographs = showKymographs;
	}

	public double getLineThickness() {
		return lineThickness;
	}

	public void setLineThickness(double lineThickness) {
		this.lineThickness = lineThickness;
	}

	public double getStartOffsetLength() {
		return startOffsetLength;
	}

	public void setStartOffsetLength(double startOffsetLength) {
		this.startOffsetLength = startOffsetLength;
	}

	public double getEndOffsetLength() {
		return endOffsetLength;
	}

	public void setEndOffsetLength(double endOffsetLength) {
		this.endOffsetLength = endOffsetLength;
	}

	public boolean isSaveKymographLines() {
		return saveKymographLines;
	}

	public void setSaveKymographLines(boolean saveKymographLines) {
		this.saveKymographLines = saveKymographLines;
	}

	public LineDrawer getLineDrawer() {
		return lineDrawer;
	}

	public void setLineDrawer(LineDrawer lineDrawer) {
		this.lineDrawer = lineDrawer;
	}

	@Override
	public String toString() {
		String out = "";

		out += "buildOneRandomKymograph = " + buildOneRandomKymograph + "\n";
		out += "showKymographs = " + showKymographs + "\n";
		out += "saveKymographLines = " + saveKymographLines + "\n";
		out += "lineThickness = " + lineThickness + "\n";
		out += "startOffsetLength = " + startOffsetLength + "\n";
		out += "endOffsetLength = " + endOffsetLength + "\n";
		out += "saveKymographs = " + saveKymographs + "\n";
		out += "lineDrawer = " + lineDrawer.getName();

		return out;
	}

}
