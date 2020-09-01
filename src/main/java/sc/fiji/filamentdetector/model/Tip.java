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
package sc.fiji.filamentdetector.model;

import java.util.Arrays;

import com.google.common.primitives.Doubles;

/* 
 * Represent a tip of a TrackedFilament over time.
 * 
 */
public class Tip {

	private double[] x;
	private double[] y;
	private int[] frames;

	private double averageX = Double.NaN;
	private double averageY = Double.NaN;

	private double stdX = Double.NaN;
	private double stdY = Double.NaN;

	public Tip(double[] x, double[] y, int[] frames) {
		this.x = x;
		this.y = y;
		this.frames = frames;
	}

	public double[] getX() {
		return x;
	}

	public double[] getY() {
		return y;
	}

	public int[] getFrames() {
		return frames;
	}

	public double getAverageX() {
		if (Double.isNaN(averageX)) {
			averageX = Arrays.stream(x).summaryStatistics().getAverage();
		}
		return averageX;
	}

	public double getAverageY() {
		if (Double.isNaN(averageY)) {
			averageY = Arrays.stream(y).summaryStatistics().getAverage();
		}
		return averageY;
	}

	public double getStdX() {
		if (Double.isNaN(stdX)) {
			double rawSum = Doubles.asList(x).stream()
					.mapToDouble((xx) -> Math.pow(xx.doubleValue() - getAverageX(), 2.0)).sum();
			stdX = Math.sqrt(rawSum / (x.length - 1));
		}
		return stdX;
	}

	public double getStdY() {
		if (Double.isNaN(stdY)) {
			double rawSum = Doubles.asList(y).stream()
					.mapToDouble((yy) -> Math.pow(yy.doubleValue() - getAverageY(), 2.0)).sum();
			stdY = Math.sqrt(rawSum / (y.length - 1));
		}
		return stdY;
	}

	public double getDispX() {
		return x[x.length - 1] - x[0];
	}

	public double getDispY() {
		return y[y.length - 1] - y[0];
	}

	@Override
	public String toString() {
		String out = "";
		out += "Average x : " + getAverageX() + " | ";
		out += "Average y : " + getAverageY() + "\n";
		return out;
	}
}
