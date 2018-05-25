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
package sc.fiji.filamentdetector.detection;

import java.util.stream.Collectors;

import net.imagej.Dataset;
import net.imagej.display.ImageDisplay;

import org.scijava.plugin.AbstractRichPlugin;

import sc.fiji.filamentdetector.model.Filaments;

public abstract class AbstractFilamentDetector extends AbstractRichPlugin implements FilamentDetector {

	private String name;

	private boolean simplifyFilaments = true;
	private double simplifyToleranceDistance = 2;
	private boolean detectOnlyCurrentFrame = false;

	private ImageDisplay imageDisplay;
	private Dataset dataset;

	private Filaments filaments;

	@Override
	public void simplifyFilaments() {
		if (this.isSimplifyFilaments()) {
			Filaments filaments = this.getFilaments();
			filaments = filaments.simplify(this.getSimplifyToleranceDistance());

			// Remove filaments with only one point
			filaments = filaments.stream().filter(filament -> filament.getSize() > 1)
					.collect(Collectors.toCollection(Filaments::new));

			this.setFilaments(filaments);
		}
	}

	@Override
	public void setDetectOnlyCurrentFrame(boolean detectOnlyCurrentFrame) {
		this.detectOnlyCurrentFrame = detectOnlyCurrentFrame;
	}

	@Override
	public boolean isSimplifyFilaments() {
		return simplifyFilaments;
	}

	@Override
	public void setSimplifyFilaments(boolean simplifyFilaments) {
		this.simplifyFilaments = simplifyFilaments;
	}

	@Override
	public double getSimplifyToleranceDistance() {
		return simplifyToleranceDistance;
	}

	@Override
	public void setSimplifyToleranceDistance(double simplifyToleranceDistance) {
		this.simplifyToleranceDistance = simplifyToleranceDistance;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void setDataset(Dataset dataset) {
		this.dataset = dataset;
	}

	@Override
	public void setFilaments(Filaments filaments) {
		this.filaments = filaments;
	}

	@Override
	public Filaments getFilaments() {
		return this.filaments;
	}

	@Override
	public ImageDisplay getImageDisplay() {
		return imageDisplay;
	}

	@Override
	public void setImageDisplay(ImageDisplay imageDisplay) {
		this.imageDisplay = imageDisplay;
	}

	@Override
	public Dataset getDataset() {
		return dataset;
	}

	@Override
	public boolean isDetectOnlyCurrentFrame() {
		return detectOnlyCurrentFrame;
	}

}
