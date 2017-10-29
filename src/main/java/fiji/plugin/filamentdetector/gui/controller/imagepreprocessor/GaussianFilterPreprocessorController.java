package fiji.plugin.filamentdetector.gui.controller.imagepreprocessor;

import java.net.URL;
import java.util.ResourceBundle;

import org.scijava.Context;

import fiji.plugin.filamentdetector.preprocessing.GaussianFilterPreprocessor;
import fiji.plugin.filamentdetector.preprocessing.ImagePreprocessor;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;

public class GaussianFilterPreprocessorController extends ImagePreprocessorController {

	public static String FXML_PATH = "/fiji/plugin/filamentdetector/gui/view/preprocessor/GaussianFilterPreprocessorView.fxml";

	@FXML
	private CheckBox doGaussianFilterCheckbox;

	@FXML
	private TextField gaussianFilterSizeField;

	public GaussianFilterPreprocessorController(Context context, ImagePreprocessor imagePreprocessor) {
		super(context, imagePreprocessor);
		setFXMLPath(FXML_PATH);
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
