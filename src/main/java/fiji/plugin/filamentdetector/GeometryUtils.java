package fiji.plugin.filamentdetector;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import ij.gui.Plot;
import net.imagej.Dataset;
import net.imagej.axis.Axes;
import net.imglib2.RealPoint;
import net.imglib2.RealRandomAccess;
import net.imglib2.img.Img;
import net.imglib2.interpolation.InterpolatorFactory;
import net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

public class GeometryUtils {

	public static RealPoint add(RealPoint point1, RealPoint point2) {
		RealPoint point = new RealPoint();
		for (int d = 0; d < point1.numDimensions(); d++) {
			point.setPosition(point1.getDoublePosition(d) + point2.getDoublePosition(d), d);
		}
		return point;
	}

	public static RealPoint subtract(RealPoint point1, RealPoint point2) {
		RealPoint point = new RealPoint();
		for (int d = 0; d < point1.numDimensions(); d++) {
			point.setPosition(point1.getDoublePosition(d) - point2.getDoublePosition(d), d);
		}
		return point;
	}

	public static double distance(RealPoint point1, RealPoint point2) {
		double sum = 0;
		for (int d = 0; d < point1.numDimensions(); d++) {
			sum += Math.pow(point1.getDoublePosition(d) - point2.getDoublePosition(d), 2);
		}
		return Math.sqrt(sum);
	}

	public static <T> double[] getIntensities(List<RealPoint> line, Dataset dataset, int frame, int channel, int z) {
		InterpolatorFactory<? extends RealType<?>, Img<? extends RealType<?>>> interpolator = new NLinearInterpolatorFactory();
		return getIntensities(line, dataset, interpolator, frame, channel, z);
	}

	public static <T> double[] getIntensities(List<RealPoint> line, Dataset dataset,
			InterpolatorFactory<? extends RealType<?>, Img<? extends RealType<?>>> interpolator, int frame, int channel,
			int z) {

		Img<? extends RealType<?>> img = dataset.getImgPlus().getImg();
		RealRandomAccess<? extends RealType<?>> interpolated = Views.interpolate(img, interpolator).realRandomAccess();

		double[] intensities = new double[line.size()];
		int xIndex = (int) dataset.dimensionIndex(Axes.X);
		int yIndex = (int) dataset.dimensionIndex(Axes.Y);
		int zIndex = (int) dataset.dimensionIndex(Axes.Z);
		int timeIndex = (int) dataset.dimensionIndex(Axes.TIME);
		int channelIndex = (int) dataset.dimensionIndex(Axes.CHANNEL);
		for (int i = 0; i < line.size(); i++) {
			interpolated.setPosition(line.get(i).getDoublePosition(0), xIndex);
			interpolated.setPosition(line.get(i).getDoublePosition(1), yIndex);
			if (zIndex >= 0) {
				interpolated.setPosition(z, zIndex);
			}
			if (timeIndex >= 0) {
				interpolated.setPosition(frame, timeIndex);
			}
			if (channelIndex >= 0) {
				interpolated.setPosition(channel, channelIndex);
			}
			intensities[i] = interpolated.get().getRealDouble();
		}

		return intensities;
	}

	public static List<RealPoint> getLinePointsFromNumberOfPoints(RealPoint start, RealPoint end, int numPts) {
		List<RealPoint> line = new ArrayList<RealPoint>();
		double x;
		double y;
		for (int i = 0; i < numPts; i++) {
			x = i * (end.getDoublePosition(0) - start.getDoublePosition(0)) / (numPts - 1) + start.getDoublePosition(0);
			y = i * (end.getDoublePosition(1) - start.getDoublePosition(1)) / (numPts - 1) + start.getDoublePosition(1);
			line.add(new RealPoint(x, y));
		}
		return line;
	}

	public static List<RealPoint> getLinePointsFromSpacing(RealPoint start, RealPoint end, double spacing) {
		List<RealPoint> line = new ArrayList<RealPoint>();
		double dist = distance(start, end);
		long nPoints = (long) (dist / spacing);
		line.add(start);
		for (long i = 1; i <= nPoints; i++) {
			line.add(getPointOnVectorFromDistance(start, end, spacing * i));
		}
		// line.add(end);
		return line;
	}

	public static RealPoint getPointOnVectorFromDistance(RealPoint start, RealPoint end, double distance) {
		double distRatio = (double) (distance
				/ Math.sqrt(Math.pow(start.getDoublePosition(0) - end.getDoublePosition(0), 2)
						+ Math.pow(start.getDoublePosition(1) - end.getDoublePosition(1), 2)));
		double x = ((1 - distRatio) * start.getDoublePosition(0) + distRatio * end.getDoublePosition(0));
		double y = ((1 - distRatio) * start.getDoublePosition(1) + distRatio * end.getDoublePosition(1));
		return new RealPoint(x, y);
	}

	public static float[] getPointOnVectorFromDistance(float[] start, float[] end, double distance) {
		float distRatio = (float) (distance
				/ Math.sqrt(Math.pow(start[0] - end[0], 2) + Math.pow(start[1] - end[1], 2)));
		float x = (float) ((1 - distRatio) * start[0] + distRatio * end[0]);
		float y = (float) ((1 - distRatio) * start[1] + distRatio * end[1]);
		return new float[] { x, y };
	}

	public static double[] getPointOnVectorFromDistance(double[] start, double[] end, double distance) {
		double distRatio = (double) (distance
				/ Math.sqrt(Math.pow(start[0] - end[0], 2) + Math.pow(start[1] - end[1], 2)));
		double x = (double) ((1 - distRatio) * start[0] + distRatio * end[0]);
		double y = (double) ((1 - distRatio) * start[1] + distRatio * end[1]);
		return new double[] { x, y };
	}

	public static Plot plotPoints(double[] y) {
		double[] x = IntStream.range(0, y.length).mapToDouble(i -> i).toArray();
		return plotPoints(x, y);
	}

	public static Plot plotPoints(double[] x, double[] y) {
		List<RealPoint> points = IntStream.range(0, x.length).mapToObj(i -> new RealPoint(x[i], y[i]))
				.collect(Collectors.toList());
		return plotPoints(points);
	}

	public static Plot plotPoints(List<RealPoint> points) {

		double[] x = points.stream().mapToDouble(p -> p.getDoublePosition(0)).toArray();
		double[] y = points.stream().mapToDouble(p -> p.getDoublePosition(1)).toArray();

		Plot plot = new Plot("", "x", "y", x, y);
		plot.show();

		return plot;
	}

}
