package fiji.plugin.filamentdetector.gui.controller.imagepreprocessor;

import java.net.URL;
import java.util.ResourceBundle;

import org.scijava.Context;

import fiji.plugin.filamentdetector.imagepreprocessor.ImagePreprocessor;
import fiji.plugin.filamentdetector.imagepreprocessor.TubenessFilterPreprocessor;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;

public class TubenessFilterController extends AbstractImagePreprocessorController {

	public static String FXML_PATH = "/fiji/plugin/filamentdetector/gui/view/preprocessor/TubenessFilterView.fxml";
	private static String tooltipImagePath = "/fiji/plugin/filamentdetector/gui/preprocessorexamples/tubenessFilter.png";

	@FXML
	private TextField sigma;

	@FXML
	private CheckBox doProcessCheckbox;

	public TubenessFilterController(Context context, ImagePreprocessor imagePreprocessor) {
		super(context, imagePreprocessor);
		setFXMLPath(FXML_PATH);
		setTooltipImagePath(tooltipImagePath);
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		TubenessFilterPreprocessor imagePreprocessor = (TubenessFilterPreprocessor) getImagePreprocessor();
		doProcessCheckbox.setSelected(imagePreprocessor.isDoPreprocess());
		sigma.setText(Double.toString(imagePreprocessor.getSigma()));
	}

	public void updateParameters() {
		TubenessFilterPreprocessor imagePreprocessor = (TubenessFilterPreprocessor) getImagePreprocessor();
		imagePreprocessor.setDoPreprocess(doProcessCheckbox.isSelected());
		imagePreprocessor.setSigma(Double.parseDouble(sigma.getText()));
	}

}
