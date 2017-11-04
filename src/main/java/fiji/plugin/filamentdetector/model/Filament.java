package fiji.plugin.filamentdetector.model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.ArrayUtils;
import org.scijava.Context;
import org.scijava.convert.ConvertService;

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Floats;

import fiji.plugin.filamentdetector.GeometryUtils;
import ij.ImagePlus;
import ij.gui.PolygonRoi;
import ij.gui.ProfilePlot;
import ij.gui.Roi;
import ij.process.FloatPolygon;
import net.imagej.display.ImageDisplay;
import net.imglib2.RealPoint;

/* A container for Line */
public class Filament implements Comparable<Filament> {

	public static Color DEFAULT_COLOR = Color.orange;
	public static int xIndex = 0;
	public static int yIndex = 1;
	private static int idCounter = 0;

	private final int frame;

	private int id;
	private List<RealPoint> points;

	private double length = Double.NaN;
	private double[] lengths;
	private double tipLength = Double.NaN;

	private double sinuosity = Double.NaN;
	private double[] boundingBox;

	private Color color = DEFAULT_COLOR;

	public Filament(List<RealPoint> points, int frame) {
		assignID();
		this.points = points;
		this.frame = frame;
	}

	public Filament(double[] x, double[] y, int frame) {
		assignID();
		this.frame = frame;
		this.points = IntStream.range(0, x.length).mapToObj(i -> new RealPoint(x[i], y[i]))
				.collect(Collectors.toList());
	}

	public Filament(float[] x, float[] y, int frame) {
		assignID();
		this.frame = frame;
		this.points = IntStream.range(0, x.length).mapToObj(i -> new RealPoint(x[i], y[i]))
				.collect(Collectors.toList());
	}

	public double[] getXCoordinates() {
		return this.points.stream().mapToDouble(p -> p.getDoublePosition(xIndex)).toArray();
	}

	public double[] getYCoordinates() {
		return this.points.stream().mapToDouble(p -> p.getDoublePosition(yIndex)).toArray();
	}

	public float[] getXCoordinatesAsFloat() {
		return Floats.toArray(this.points.stream().mapToDouble(p -> p.getDoublePosition(xIndex)).boxed()
				.collect(Collectors.toList()));
	}

	public float[] getYCoordinatesAsFloat() {
		return Floats.toArray(this.points.stream().mapToDouble(p -> p.getDoublePosition(yIndex)).boxed()
				.collect(Collectors.toList()));
	}

	public int getFrame() {
		return this.frame;
	}

	public int getId() {
		return this.id;
	}

	public int getSize() {
		return this.points.size();
	}

	public RealPoint getFirstPoint() {
		return this.points.get(0);
	}

	public RealPoint getLastPoint() {
		return this.points.get(this.getSize() - 1);
	}

	public double getTipLength() {
		if (Double.isNaN(this.tipLength)) {
			this.tipLength = GeometryUtils.distance(this.getFirstPoint(), this.getLastPoint());
		}
		return this.tipLength;
	}

	public double getLength() {
		if (Double.isNaN(this.length)) {

			this.lengths = new double[this.getSize() - 1];
			this.length = 0;

			RealPoint point1;
			RealPoint point2;

			for (int i = 0; i < this.getSize() - 1; i++) {
				point1 = this.points.get(i);
				point2 = this.points.get(i + 1);
				this.lengths[i] = GeometryUtils.distance(point1, point2);
				this.length += this.lengths[i];
			}

		}
		return this.length;
	}

	public double[] getLengths() {
		this.getLength();
		return this.lengths;
	}

	public double getSinuosity() {
		if (Double.isNaN(this.sinuosity)) {

			double[] x = this.getXCoordinates();
			double[] y = this.getYCoordinates();

			double shortLength = Math.sqrt(Math.pow(x[0] - x[x.length - 1], 2) + Math.pow(y[0] - y[x.length - 1], 2));

			this.sinuosity = this.getLength() / shortLength;

		}
		return this.sinuosity;
	}

	public double[] getBoundingBox() {
		if (boundingBox != null) {
			return boundingBox;
		}

		boundingBox = new double[4];

		double xMin = this.points.stream().mapToDouble(x -> x.getDoublePosition(xIndex)).min().getAsDouble();
		double yMin = this.points.stream().mapToDouble(x -> x.getDoublePosition(yIndex)).min().getAsDouble();
		// RealPoint minPoint = new RealPoint(xMin, yMin);

		double xMax = this.points.stream().mapToDouble(x -> x.getDoublePosition(xIndex)).max().getAsDouble();
		double yMax = this.points.stream().mapToDouble(x -> x.getDoublePosition(yIndex)).max().getAsDouble();
		// RealPoint maxPoint = new RealPoint(xMax, yMax);

		boundingBox[0] = xMin;
		boundingBox[1] = yMin;
		boundingBox[2] = xMax - xMin;
		boundingBox[3] = yMax - yMin;

		return boundingBox;
	}

	@Override
	public String toString() {
		String out = "";
		out += "Frame: " + this.getFrame() + " | ID: " + this.getId();
		return out;
	}

	public String info() {
		String info = "";

		info += this.toString() + "\n";
		info += "N Points: " + this.getSize() + "\n";
		info += "Length: " + String.format("%.2f", this.getLength()) + "\n";
		info += "Sinuosity: " + String.format("%.2f", this.getSinuosity()) + "\n";
		info += "Bounding Box: ";
		info += "x = " + String.format("%.2f", this.getBoundingBox()[0]) + ", ";
		info += "y = " + String.format("%.2f", this.getBoundingBox()[1]) + ", ";
		info += "width = " + String.format("%.2f", this.getBoundingBox()[2]) + ", ";
		info += "height = " + String.format("%.2f", this.getBoundingBox()[3]) + ", " + "\n";

		return info;
	}

	/* Simplify the filament by reducing the number of points */
	public Filament simplify(double toleranceDistance) {
		double[] x = this.getXCoordinates();
		double[] y = this.getYCoordinates();

		double[] newX = new double[x.length];
		double[] newY = new double[y.length];

		newX[0] = x[0];
		newY[0] = y[0];

		double totDist = 0;
		double dist;
		int newSize = 1;

		for (int i = 1; i < x.length; i++) {
			dist = Math.sqrt(Math.pow(x[i] - x[i - 1], 2) + Math.pow(y[i] - y[i - 1], 2));

			if (totDist > toleranceDistance) {
				newX[newSize] = x[i];
				newY[newSize] = y[i];
				totDist = 0;
				newSize++;
			} else {
				totDist += dist;
			}
		}

		newX = Arrays.copyOf(newX, newSize);
		newY = Arrays.copyOf(newY, newSize);

		Filament newFilament = new Filament(newX, newY, this.getFrame());
		newFilament.setColor(this.getColor());
		return newFilament;
	}

	@Override
	public int compareTo(Filament filament) {
		return this.getId() - filament.getId();
	}

	public Color getColor() {
		return color;
	}

	public String getColorAsHex() {
		return String.format("#%02x%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
	}

	public void setColor(Color color) {
		this.color = color;
	}

	/*
	 * Return both tips: [x1, y1, x2, y2]
	 */
	public double[] getTips() {
		double[] coords = new double[4];
		coords[0] = this.getFirstPoint().getDoublePosition(xIndex);
		coords[1] = this.getFirstPoint().getDoublePosition(yIndex);
		coords[2] = this.getLastPoint().getDoublePosition(xIndex);
		coords[3] = this.getLastPoint().getDoublePosition(yIndex);
		return coords;
	}

	public void reverseCoordinates() {
		Collections.reverse(this.points);
		this.boundingBox = null;
		this.length = Double.NaN;
		this.lengths = null;
		this.tipLength = Double.NaN;
	}

	public final static Comparator<Filament> frameComparator = Comparator.comparing(s -> s.getFrame());

	public Filament copy() {
		return new Filament(this.getXCoordinates(), this.getYCoordinates(), this.getFrame());
	}

	public double distanceFromCenter(Filament filament) {
		double[] bbox1 = this.getBoundingBox();
		double[] bbox2 = filament.getBoundingBox();

		double center1_x = (2 * bbox1[0] + bbox1[2]) / 2;
		double center1_y = (2 * bbox1[1] + bbox1[3]) / 2;
		double center2_x = (2 * bbox2[0] + bbox2[2]) / 2;
		double center2_y = (2 * bbox2[1] + bbox2[3]) / 2;

		double dist = GeometryUtils.distance(new RealPoint(center1_x, center1_y), new RealPoint(center2_x, center2_y));
		return dist;
	}

	public Roi getRoi() {
		float[] x = this.getXCoordinatesAsFloat();
		float[] y = this.getYCoordinatesAsFloat();
		FloatPolygon positions = new FloatPolygon(x, y, this.getSize());
		return new PolygonRoi(positions, Roi.FREELINE);
	}

	public List<Double> getStartTip() {
		List<Double> position = new ArrayList<>();
		position.add(this.getTips()[0]);
		position.add(this.getTips()[1]);
		return position;
	}

	public List<Double> getEndTip() {
		List<Double> position = new ArrayList<>();
		position.add(this.getTips()[2]);
		position.add(this.getTips()[3]);
		return position;
	}

	public double[] getStartTipAsArray() {
		return Doubles.toArray(this.getStartTip());
	}

	public double[] getEndTipAsArray() {
		return Doubles.toArray(this.getEndTip());
	}

	public List<Double> getIntensities(Context context, ImageDisplay imd, double channel, double width) {
		// TODO: Make an IJ2 version
		// See https://gitter.im/imglib/imglib2?at=59ee2d4e8808bed73d1b5198
		ConvertService convert = context.getService(ConvertService.class);
		ImagePlus imp = convert.convert(imd, ImagePlus.class).duplicate();

		Roi roi = this.getRoi();
		roi.setStrokeWidth(width);
		imp.setC((int) channel);
		imp.setRoi(roi);
		imp.setT(this.getFrame());
		ProfilePlot profiler = new ProfilePlot(imp);
		List<Double> profile = Doubles.asList(profiler.getProfile());
		imp.deleteRoi();

		return profile;
	}

	public double[] getIntensitiesAsArray(Context context, ImageDisplay imd, double channel, double width) {
		List<Double> profile = getIntensities(context, imd, channel, width);
		return ArrayUtils.toPrimitive(profile.toArray(new Double[profile.size()]));

	}

	private synchronized void assignID() {
		this.id = idCounter;
		idCounter++;
	}

	public boolean insideBbox(double[] bbox) {

		for (RealPoint point : points) {
			if (point.getDoublePosition(xIndex) < bbox[0]) {
				return false;
			} else if (point.getDoublePosition(xIndex) > bbox[1]) {
				return false;
			} else if (point.getDoublePosition(yIndex) < bbox[2]) {
				return false;
			} else if (point.getDoublePosition(yIndex) > bbox[3]) {
				return false;
			}
		}
		return true;
	}
}
