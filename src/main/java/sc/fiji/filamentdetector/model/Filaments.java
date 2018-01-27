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
package sc.fiji.filamentdetector.model;

import java.util.ArrayList;

public class Filaments extends ArrayList<Filament> {

	private static final long serialVersionUID = 1L;

	public int getIndexByID(int id) {
		for (int i = 0; i < this.size(); i++) {
			if (this.get(i).getId() == id) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public String toString() {
		String out = "";
		for (Filament filament : this) {
			out += filament.toString() + "\n";
		}
		return out;
	}

	public String info() {
		String out = "";
		for (Filament filament : this) {
			out += filament.info() + "\n";
		}
		return out;
	}

	public Filaments simplify(double toleranceDistance) {

		Filaments newFilaments = new Filaments();

		for (Filament filament : this) {
			newFilaments.add(filament.simplify(toleranceDistance));
		}

		return newFilaments;

	}

	@Override
	public boolean add(Filament filament) {
		return super.add(filament);
	}

}
