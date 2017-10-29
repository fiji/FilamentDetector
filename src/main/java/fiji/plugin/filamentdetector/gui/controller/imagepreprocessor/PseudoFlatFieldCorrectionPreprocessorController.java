package fiji.plugin.filamentdetector.gui.controller.imagepreprocessor;

import java.net.URL;
import java.util.ResourceBundle;

import org.scijava.Context;

import fiji.plugin.filamentdetector.preprocessing.ImagePreprocessor;
import fiji.plugin.filamentdetector.preprocessing.PseudoFlatFieldCorrectionPreprocessor;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;

public class PseudoFlatFieldCorrectionPreprocessorController extends ImagePreprocessorController {

	public static String FXML_PATH = "/fiji/plugin/filamentdetector/gui/view/preprocessor/PseudoFlatFieldCorrectionView.fxml";

	@FXML
	private CheckBox doflatFieldCorrectionCheckbox;

	@FXML
	private TextField flatFieldCorrectionGaussianFilterSizeField;

	public PseudoFlatFieldCorrectionPreprocessorController(Context context, ImagePreprocessor imagePreprocessor) {
		super(context, imagePreprocessor);
		setFXMLPath(FXML_PATH);
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		PseudoFlatFieldCorrectionPreprocessor imagePreprocessor = (PseudoFlatFieldCorrectionPreprocessor) getImagePreprocessor();
		doflatFieldCorrectionCheckbox.setSelected(imagePreprocessor.isDoPreprocess());
		flatFieldCorrectionGaussianFilterSizeField
				.setText(Double.toString(imagePreprocessor.getFlatFieldCorrectionGaussianFilterSize()));
	}

	public void updateParameters() {
		PseudoFlatFieldCorrectionPreprocessor imagePreprocessor = (PseudoFlatFieldCorrectionPreprocessor) getImagePreprocessor();
		imagePreprocessor.setDoPreprocess(doflatFieldCorrectionCheckbox.isSelected());
		imagePreprocessor.setFlatFieldCorrectionGaussianFilterSize(
				Double.parseDouble(flatFieldCorrectionGaussianFilterSizeField.getText()));
	}

}
