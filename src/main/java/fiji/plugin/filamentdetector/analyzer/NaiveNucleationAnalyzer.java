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
package fiji.plugin.filamentdetector.analyzer;

import java.util.List;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.conditions.Conditions;
import org.scijava.Priority;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import fiji.plugin.filamentdetector.GeometryUtils;
import fiji.plugin.filamentdetector.model.Filament;
import fiji.plugin.filamentdetector.model.TrackedFilament;
import net.imagej.Dataset;
import net.imglib2.RealPoint;

@Plugin(type = Analyzer.class, priority = Priority.HIGH)
public class NaiveNucleationAnalyzer extends AbstractAnalyzer {

	public static String NAME = "Naive Nucleation Analyzer";
	public static String DESCRIPTION = "This module uses the tracked filaments as seeds and look over "
			+ "each frame and both tips whether the intensity is above a specified threshold. If it is then a "
			+ "nucleation event is declared.";

	@Parameter
	private LogService log;

	private double pixelSpacing = 1;
	private double lineThickness = 1;
	private double lineLength = 10;
	private double intensityThreshold = 100;
	private int maxFrame = 15;
	private int channelIndex = 0;

	public NaiveNucleationAnalyzer() {
		super();
		setName(NAME);
		setDescription(DESCRIPTION);
		
		// TODO: allow line to have thickness to reduce noise
		// TODO: set intensity threshold according to image threshold (Otsu or similar)
		// TODO: add results to GUI
	}

	@Override
	public String getAnalyzerInfo() {
		String out = "";
		out += "Name : " + this.name + "\n";
		out += "Save results : " + this.saveResults + "\n";
		out += "lineLength : " + this.lineLength + "\n";
		out += "pixelSpacing : " + this.pixelSpacing + "\n";
		out += "lineThickness : " + this.lineThickness + "\n";
		out += "intensityThreshold : " + this.intensityThreshold + "\n";
		out += "maxFrame : " + this.maxFrame + "\n";
		out += "channelIndex : " + this.channelIndex + "\n";
		out += "\n";
		return out;
	}

	@Override
	public void analyze() {

		int nFilaments = filamentWorkflow.getTrackedFilaments().size();
		INDArray framesNucleationEvents = Nd4j.create(1, nFilaments);
		int frameNucleation;

		for (int i = 0; i < nFilaments; i++) {
			TrackedFilament trackedFilament = filamentWorkflow.getTrackedFilaments().get(i);
			frameNucleation = frameFirstNucleation(trackedFilament);
			framesNucleationEvents.putScalar(i, frameNucleation);
		}

		int nucleationEvents = framesNucleationEvents.cond(Conditions.greaterThan(-1)).sumNumber().intValue();
		double nucleationRate = (double) nucleationEvents / (double) nFilaments;

		this.resultMessage = "Analysis is done.";
		this.results.put("nucleation_events", nucleationEvents);
		this.results.put("number_of_seeds", nFilaments);
		this.results.put("nucleation_rate", nucleationRate);
	}

	private int frameFirstNucleation(TrackedFilament trackedFilament) {
		Dataset dataset = this.filamentWorkflow.getDataset();

		INDArray frames = Nd4j.create(trackedFilament.getFrames().stream().mapToDouble(d -> d).toArray());
		Filament filament;

		RealPoint start;
		RealPoint end;
		double seedLength;
		RealPoint p1;
		RealPoint p2;
		List<RealPoint> line1;
		List<RealPoint> line2;

		double intensities1;
		double intensities2;

		for (int frame = 0; frame < frames.max(1).getDouble(0); frame++) {
			filament = trackedFilament.getFilamentByFrame(frame);
			if (filament != null) {

				start = filament.getFirstPoint();
				end = filament.getLastPoint();
				seedLength = GeometryUtils.distance(start, end);

				p1 = GeometryUtils.getPointOnVectorFromDistance(start, end, seedLength + this.lineLength);
				p2 = GeometryUtils.getPointOnVectorFromDistance(end, start, seedLength + this.lineLength);

				line1 = GeometryUtils.getLinePointsFromSpacing(end, p1, this.pixelSpacing);
				line2 = GeometryUtils.getLinePointsFromSpacing(start, p2, this.pixelSpacing);

				intensities1 = GeometryUtils.getIntensities(line1, dataset, frame, this.channelIndex, 0).mean(1)
						.getDouble(0);
				intensities2 = GeometryUtils.getIntensities(line2, dataset, frame, this.channelIndex, 0).mean(1)
						.getDouble(0);

				if (intensities1 > this.intensityThreshold || intensities2 > this.intensityThreshold) {
					return frame;
				}

			}
		}

		return -1;
	}

	public double getPixelSpacing() {
		return pixelSpacing;
	}

	public void setPixelSpacing(double pixelSpacing) {
		this.pixelSpacing = pixelSpacing;
	}

	public double getLineThickness() {
		return lineThickness;
	}

	public void setLineThickness(double lineThickness) {
		this.lineThickness = lineThickness;
	}

	public double getIntensityThreshold() {
		return intensityThreshold;
	}

	public void setIntensityThreshold(double intensityThreshold) {
		this.intensityThreshold = intensityThreshold;
	}

	public int getMaxFrame() {
		return maxFrame;
	}

	public void setMaxFrame(int maxFrame) {
		this.maxFrame = maxFrame;
	}

	public int getChannelIndex() {
		return channelIndex;
	}

	public void setChannelIndex(int channelIndex) {
		this.channelIndex = channelIndex;
	}

	public double getLineLength() {
		return lineLength;
	}

	public void setLineLength(double lineLength) {
		this.lineLength = lineLength;
	}

}
