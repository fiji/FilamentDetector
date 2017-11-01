package fiji.plugin.filamentdetector.gui.controller.analyzer;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.scijava.Context;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

import fiji.plugin.filamentdetector.analyzer.tipfitter.TipFitterAnalyzer;
import fiji.plugin.filamentdetector.gui.GUIStatusService;
import fiji.plugin.filamentdetector.gui.fxwidgets.SliderLabelSynchronizer;
import fiji.plugin.filamentdetector.model.TrackedFilament;
import fiji.plugin.filamentdetector.model.TrackedFilaments;
import fiji.plugin.filamentdetector.overlay.FilamentOverlayService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;

public class TipFitterAnalyzerController extends AbstractAnalyzerController implements AnalyzerController {

	private static String FXML_VIEW_FILE = "/fiji/plugin/filamentdetector/gui/view/analyzer/TipFitterAnalyzerView.fxml";

	@Parameter
	private GUIStatusService status;

	@Parameter
	private LogService log;

	@Parameter
	private FilamentOverlayService overlay;

	@FXML
	private TextField channelIndexField;

	@FXML
	private Slider lineWidthSlider;

	@FXML
	private TextField lineWidthField;

	@FXML
	private Slider lineFitLengthSlider;

	@FXML
	private TextField lineFitLengthField;

	@FXML
	private Slider polynomDegreeSlider;

	@FXML
	private TextField polynomDegreeField;

	@FXML
	private Slider relativeDistSlider;

	@FXML
	private TextField relativeDistField;

	@FXML
	private CheckBox overlayFilamentsCheckbox;

	private SliderLabelSynchronizer lineWidthSync;
	private SliderLabelSynchronizer lineFitLengthSync;
	private SliderLabelSynchronizer polynomDegreeSync;
	private SliderLabelSynchronizer relativeDistSync;

	private TipFitterAnalyzer analyzer;

	public TipFitterAnalyzerController(Context context, TipFitterAnalyzer analyzer) {
		super(context);
		this.analyzer = analyzer;
	}

	@Override
	public String getViewFXMlFile() {
		return FXML_VIEW_FILE;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		lineWidthSync = new SliderLabelSynchronizer(lineWidthSlider, lineWidthField);
		lineWidthSync.setValue(this.analyzer.getFitter().getLineWidth());

		lineFitLengthSync = new SliderLabelSynchronizer(lineFitLengthSlider, lineFitLengthField);
		lineFitLengthSync.setValue(this.analyzer.getFitter().getLineFitLength());

		polynomDegreeSync = new SliderLabelSynchronizer(polynomDegreeSlider, polynomDegreeField);
		polynomDegreeSync.setValue(this.analyzer.getFitter().getPolynomDegree());

		relativeDistSync = new SliderLabelSynchronizer(relativeDistSlider, relativeDistField);
		relativeDistSync.setValue(this.analyzer.getFitter().getRelativePositionFromEnd());

		channelIndexField.setText(Integer.toString(this.analyzer.getFitter().getChannelIndex()));
	}

	@FXML
	void updateParameters(ActionEvent event) {

		if (lineWidthSync.isEvent(event)) {
			lineWidthSync.update(event);
			this.analyzer.getFitter().setLineWidth(lineWidthSync.getValue());

		} else if (lineFitLengthSync.isEvent(event)) {
			lineFitLengthSync.update(event);
			this.analyzer.getFitter().setLineFitLength(lineFitLengthSync.getValue());

		} else if (polynomDegreeSync.isEvent(event)) {
			polynomDegreeSync.update(event);
			this.analyzer.getFitter().setPolynomDegree((int) polynomDegreeSync.getValue());

		} else if (relativeDistSync.isEvent(event)) {
			relativeDistSync.update(event);
			this.analyzer.getFitter().setRelativePositionFromEnd(relativeDistSync.getValue());

		}
	}

	@Override
	public void runPostAnalysisAction() {
		Map<TrackedFilament, TrackedFilament> side1 = (Map<TrackedFilament, TrackedFilament>) this.analyzer.getResults()
				.get("side1Filaments");
		Map<TrackedFilament, TrackedFilament> side2 = (Map<TrackedFilament, TrackedFilament>) this.analyzer.getResults()
				.get("side2Filaments");

		TrackedFilaments side1AsList = side1.values().stream().collect(Collectors.toCollection(TrackedFilaments::new));
		TrackedFilaments side2AsList = side2.values().stream().collect(Collectors.toCollection(TrackedFilaments::new));

		overlay.reset();
		overlay.add(side1AsList);
		overlay.add(side2AsList);
	}
}
