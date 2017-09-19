package fiji.plugin.filamentdetector;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.scijava.Context;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

import net.imagej.Dataset;
import net.imagej.axis.Axes;

/* An easy way to get Dataset calibrations */
public class Calibrations {

	@Parameter
	LogService log;

	private Dataset data;

	private double dx;
	private double dy;
	private double dz;
	private double dt;

	private String unitX;
	private String unitY;
	private String unitZ;
	private String unitT;

	private List<String> channelList;
	private int channelToUseIndex = 0;

	public Calibrations(Context context, Dataset data) {
		context.inject(this);

		this.data = data;

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
		this.dx = dx;
	}

	public void setDy(double dy) {
		this.dy = dy;
	}

	public void setDz(double dz) {
		this.dz = dz;
	}

	public void setDt(double dt) {
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
			channelToUseIndex = newChannelIndex;
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

}
