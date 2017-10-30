package fiji.plugin.filamentdetector.gui.controller.imagepreprocessor;

import java.net.URL;
import java.util.ResourceBundle;

import org.scijava.Context;

import fiji.plugin.filamentdetector.preprocessing.GaussianFilterPreprocessor;
import fiji.plugin.filamentdetector.preprocessing.ImagePreprocessor;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;

public class GaussianFilterController extends AbstractImagePreprocessorController {

	private static String FXML_PATH = "/fiji/plugin/filamentdetector/gui/view/preprocessor/GaussianFilterView.fxml";
	private static String tooltipImagePath = "/fiji/plugin/filamentdetector/gui/preprocessorexamples/gaussianFilter.png";

	@FXML
	private CheckBox doGaussianFilterCheckbox;

	@FXML
	private TextField gaussianFilterSizeField;

	public GaussianFilterController(Context context, ImagePreprocessor imagePreprocessor) {
		super(context, imagePreprocessor);
		setFXMLPath(FXML_PATH);
		setTooltipImagePath(tooltipImagePath);
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		GaussianFilterPreprocessor imagePreprocessor = (GaussianFilterPreprocessor) getImagePreprocessor();
		doGaussianFilterCheckbox.setSelected(imagePreprocessor.isDoPreprocess());
		gaussianFilterSizeField.setText(Double.toString(imagePreprocessor.getGaussianFilterSize()));
	}

	public void updateParameters() {
		GaussianFilterPreprocessor imagePreprocessor = (GaussianFilterPreprocessor) getImagePreprocessor();
		imagePreprocessor.setDoPreprocess(doGaussianFilterCheckbox.isSelected());
		imagePreprocessor.setGaussianFilterSize(Double.parseDouble(gaussianFilterSizeField.getText()));
	}

}
