package fiji.plugin.filamentdetector.gui.controller.imagepreprocessor;

import java.net.URL;
import java.util.ResourceBundle;

import org.scijava.Context;

import fiji.plugin.filamentdetector.preprocessing.DOGFilterPreprocessor;
import fiji.plugin.filamentdetector.preprocessing.ImagePreprocessor;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;

public class DOGFilterController extends ImagePreprocessorController {

	public static String FXML_PATH = "/fiji/plugin/filamentdetector/gui/view/preprocessor/DOGFilterView.fxml";
	private static String tooltipImagePath = "/fiji/plugin/filamentdetector/gui/preprocessorexamples/dogFiltering.png";

	@FXML
	private TextField sigma1DOGField;

	@FXML
	private TextField sigma2DOGField;

	@FXML
	private CheckBox doDifferenceOfGaussianFilterCheckbox;

	public DOGFilterController(Context context, ImagePreprocessor imagePreprocessor) {
		super(context, imagePreprocessor);
		setFXMLPath(FXML_PATH);
		setTooltipImagePath(tooltipImagePath);
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		DOGFilterPreprocessor imagePreprocessor = (DOGFilterPreprocessor) getImagePreprocessor();
		doDifferenceOfGaussianFilterCheckbox.setSelected(imagePreprocessor.isDoPreprocess());
		sigma1DOGField.setText(Double.toString(imagePreprocessor.getSigma1()));
		sigma2DOGField.setText(Double.toString(imagePreprocessor.getSigma2()));
	}

	public void updateParameters() {
		DOGFilterPreprocessor imagePreprocessor = (DOGFilterPreprocessor) getImagePreprocessor();
		imagePreprocessor.setDoPreprocess(doDifferenceOfGaussianFilterCheckbox.isSelected());
		imagePreprocessor.setSigma1(Double.parseDouble(sigma1DOGField.getText()));
		imagePreprocessor.setSigma2(Double.parseDouble(sigma2DOGField.getText()));
	}

}
