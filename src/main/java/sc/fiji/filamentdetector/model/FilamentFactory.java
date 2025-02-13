/*-
 * #%L
 * A Fiji plugin that allow easy, fast and accurate detection and tracking of biological filaments.
 * %%
 * Copyright (C) 2016 - 2025 Fiji developers.
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

import de.biomedical_imaging.ij.steger.Line;
import net.imglib2.roi.geom.real.Polyline;

public class FilamentFactory {

	public static Filament fromLine(Line line, int frame) {
		float[] x = line.getXCoordinates();
		float[] y = line.getYCoordinates();

		return new Filament(x, y, frame);
	}

	public static Filament fromPolyline(Polyline line, int frame) {
		// We only assume two dimensions here.
		double[] x = new double[line.numVertices()];
		double[] y = new double[line.numVertices()];

		for (int i = 0; i < line.numVertices(); i++) {
			x[i] = line.vertex(i).getDoublePosition(0);
			y[i] = line.vertex(i).getDoublePosition(1);
		}

		return new Filament(x, y, frame);
	}

}
