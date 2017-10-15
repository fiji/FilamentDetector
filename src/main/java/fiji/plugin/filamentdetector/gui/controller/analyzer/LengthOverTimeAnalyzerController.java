package fiji.plugin.filamentdetector.gui.controller.analyzer;

import java.net.URL;
import java.util.ResourceBundle;

import fiji.plugin.filamentdetector.analyzer.LengthOverTimeAnalyzer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;

public class LengthOverTimeAnalyzerController extends AbstractAnalyzerController implements AnalyzerController {

	private static String FXML_VIEW_FILE = "/fiji/plugin/filamentdetector/gui/view/analyzer/LengthOverTimeAnalyzerView.fxml";

	@FXML
	private CheckBox saveResultsCheckbox;

	@FXML
	private CheckBox savePlotsCheckbox;

	private LengthOverTimeAnalyzer analyzer;

	public LengthOverTimeAnalyzerController(LengthOverTimeAnalyzer analyzer) {
		this.analyzer = analyzer;
	}

	@Override
	public String getViewFXMlFile() {
		return FXML_VIEW_FILE;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		saveResultsCheckbox.setSelected(analyzer.isSaveResults());
		savePlotsCheckbox.setSelected(analyzer.isSavePlots());
	}

	@FXML
	void updateParameters(ActionEvent event) {
		analyzer.setSaveResults(saveResultsCheckbox.isSelected());
		analyzer.setSavePlots(savePlotsCheckbox.isSelected());
	}

}
