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
package sc.fiji.filamentdetector.tracking;

public class FilteringTrackedFilamentsParameters {

	private double maxSize = Double.POSITIVE_INFINITY;
	private double minSize = 1;
	private double borderLimit = 0;

	private boolean disableFiltering = false;

	public double getMaxSize() {
		return maxSize;
	}

	public void setMaxSize(double maxSize) {
		this.maxSize = maxSize;
	}

	public double getMinSize() {
		return minSize;
	}

	public void setMinSize(double minSize) {
		this.minSize = minSize;
	}

	public boolean isDisableFiltering() {
		return disableFiltering;
	}

	public void setDisableFiltering(boolean disableFiltering) {
		this.disableFiltering = disableFiltering;
	}

	public double getBorderLimit() {
		return borderLimit;
	}

	public void setBorderLimit(double borderLimit) {
		this.borderLimit = borderLimit;
	}

	@Override
	public String toString() {
		String out = "";

		out += "maxSize = " + maxSize + "\n";
		out += "minSize = " + minSize + "\n";
		out += "borderLimit = " + borderLimit;

		return out;
	}

}
