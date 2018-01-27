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
package sc.fiji.filamentdetector;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.scijava.Context;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

import ij.ImagePlus;
import net.imagej.Dataset;
import net.imagej.axis.Axes;

/* An easy way to get Dataset calibrations */
public class Calibrations {

	@Parameter
	LogService log;

	private Dataset data;
	private ImagePlus imp;

	private double dx;
	private double dy;
	private double dz;
	private double dt;

	private double sizeX;
	private double sizeY;
	private double sizeZ;
	private double sizeT;

	private String unitX;
	private String unitY;
	private String unitZ;
	private String unitT;

	private List<String> channelList;
	private int channelToUseIndex = 1;

	public Calibrations(Context context, Dataset data) {
		new Calibrations(context, data, null);
	}

	public Calibrations(Context context, Dataset data, ImagePlus imp) {
		context.inject(this);

		this.data = data;
		this.imp = imp;

		// Get dimensions indexes
		int xIndex = this.data.dimensionIndex(Axes.X);
		int yIndex = this.data.dimensionIndex(Axes.Y);
		int zIndex = this.data.dimensionIndex(Axes.Z);
		int timeIndex = this.data.dimensionIndex(Axes.TIME);
		int channelIndex = this.data.dimensionIndex(Axes.CHANNEL);

		// Set image calibrations of dimensions
		this.dx = xIndex != -1 ? this.data.axis(xIndex).calibratedValue(1) : 1;
		this.dy = yIndex != -1 ? this.data.axis(yIndex).calibratedValue(1) : 1;
		this.dz = zIndex != -1 ? this.data.axis(zIndex).calibratedValue(1) : 1;
		this.dt = timeIndex != -1 ? this.data.axis(timeIndex).calibratedValue(1) : 1;

		this.unitX = xIndex != -1 ? this.data.axis(xIndex).unit() : "";
		this.unitY = yIndex != -1 ? this.data.axis(yIndex).unit() : "";
		this.unitZ = zIndex != -1 ? this.data.axis(zIndex).unit() : "";
		this.unitT = timeIndex != -1 ? this.data.axis(timeIndex).unit() : "";

		this.sizeX = xIndex != -1 ? this.data.dimension(xIndex) : -1;
		this.sizeY = yIndex != -1 ? this.data.dimension(yIndex) : -1;
		this.sizeZ = zIndex != -1 ? this.data.dimension(zIndex) : -1;
		this.sizeT = timeIndex != -1 ? this.data.dimension(timeIndex) : -1;

		channelList = new ArrayList<>();
		for (int i = 1; i <= this.data.dimension(Axes.CHANNEL); i++) {
			channelList.add("Channel " + i);
		}
	}

	public Dataset getData() {
		return data;
	}

	public double getDx() {
		return dx;
	}

	public void setDx(double dx) {
		setImagePlusCalibration();
		this.dx = dx;
	}

	public void setDy(double dy) {
		setImagePlusCalibration();
		this.dy = dy;
	}

	public void setDz(double dz) {
		setImagePlusCalibration();
		this.dz = dz;
	}

	public void setDt(double dt) {
		setImagePlusCalibration();
		this.dt = dt;
	}

	public double getDy() {
		return dy;
	}

	public double getDz() {
		return dz;
	}

	public double getDt() {
		return dt;
	}

	public String getUnitX() {
		return unitX;
	}

	public String getUnitY() {
		return unitY;
	}

	public String getUnitZ() {
		return unitZ;
	}

	public String getUnitT() {
		return unitT;
	}

	public double getSizeX() {
		return sizeX;
	}

	public double getSizeY() {
		return sizeY;
	}

	public double getSizeZ() {
		return sizeZ;
	}

	public double getSizeT() {
		return sizeT;
	}

	public List<String> getChannelList() {
		return channelList;
	}

	public void setChannelList(List<String> channelList) {
		this.channelList = channelList;
	}

	public void channelToUse(String channelToUse) {
		int newChannelIndex = IntStream.range(0, channelList.size())
				.filter(i -> channelList.get(i).equals(channelToUse)).findFirst().orElse(-1);

		if (newChannelIndex != -1) {
			channelToUseIndex = newChannelIndex + 1;
		} else {
			log.error(channelToUse + " is not a valid channel.");
		}
	}

	public String getChannelToUse() {
		return channelList.get(channelToUseIndex);
	}

	public int getChannelToUseIndex() {
		return channelToUseIndex;
	}

	public void setChannelToUseIndex(int channelToUseIndex) {
		this.channelToUseIndex = channelToUseIndex;
	}

	private void setImagePlusCalibration() {
		if (imp != null) {
			ij.measure.Calibration cal = imp.getCalibration();
			if (cal == null) {
				cal = new ij.measure.Calibration();
				imp.setCalibration(cal);
			}
			cal.pixelWidth = getDx();
			cal.pixelHeight = getDy();
			cal.pixelDepth = getDz();
			cal.frameInterval = getDt();
			cal.fps = getDt();
		}
	}

}
