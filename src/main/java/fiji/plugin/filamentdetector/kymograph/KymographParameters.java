package fiji.plugin.filamentdetector.kymograph;

public class KymographParameters {

	private boolean buildOneRandomKymograph = false;
	private boolean saveKymographs = false;
	private boolean showKymographs = true;
	private boolean saveKymographLines = false;
	private double lineThickness = 4;
	private double startOffsetLength = 10;
	private double endOffsetLength = 10;

	public boolean isBuildOneRandomKymograph() {
		return buildOneRandomKymograph;
	}

	public void setBuildOneRandomKymograph(boolean buildOneRandomKymograph) {
		this.buildOneRandomKymograph = buildOneRandomKymograph;
	}

	public boolean isSaveKymographs() {
		return saveKymographs;
	}

	public void setSaveKymographs(boolean saveKymographs) {
		this.saveKymographs = saveKymographs;
	}

	public boolean isShowKymographs() {
		return showKymographs;
	}

	public void setShowKymographs(boolean showKymographs) {
		this.showKymographs = showKymographs;
	}

	public double getLineThickness() {
		return lineThickness;
	}

	public void setLineThickness(double lineThickness) {
		this.lineThickness = lineThickness;
	}

	public double getStartOffsetLength() {
		return startOffsetLength;
	}

	public void setStartOffsetLength(double startOffsetLength) {
		this.startOffsetLength = startOffsetLength;
	}

	public double getEndOffsetLength() {
		return endOffsetLength;
	}

	public void setEndOffsetLength(double endOffsetLength) {
		this.endOffsetLength = endOffsetLength;
	}

	public boolean isSaveKymographLines() {
		return saveKymographLines;
	}

	public void setSaveKymographLines(boolean saveKymographLines) {
		this.saveKymographLines = saveKymographLines;
	}

	@Override
	public String toString() {
		String out = "";

		out += "buildOneRandomKymograph = " + buildOneRandomKymograph + "\n";
		out += "showKymographs = " + showKymographs + "\n";
		out += "saveKymographLines = " + saveKymographLines + "\n";
		out += "lineThickness = " + lineThickness + "\n";
		out += "startOffsetLength = " + startOffsetLength + "\n";
		out += "endOffsetLength = " + endOffsetLength + "\n";
		out += "saveKymographs = " + saveKymographs;

		return out;
	}

}
