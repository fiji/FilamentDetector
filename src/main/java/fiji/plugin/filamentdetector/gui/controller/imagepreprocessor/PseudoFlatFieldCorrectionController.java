package fiji.plugin.filamentdetector.gui.controller.imagepreprocessor;

import java.net.URL;
import java.util.ResourceBundle;

import org.scijava.Context;

import fiji.plugin.filamentdetector.imagepreprocessor.ImagePreprocessor;
import fiji.plugin.filamentdetector.imagepreprocessor.PseudoFlatFieldCorrectionPreprocessor;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;

public class PseudoFlatFieldCorrectionController extends AbstractImagePreprocessorController {

	public static String FXML_PATH = "/fiji/plugin/filamentdetector/gui/view/preprocessor/PseudoFlatFieldCorrectionView.fxml";
	private static String tooltipImagePath = "/fiji/plugin/filamentdetector/gui/preprocessorexamples/pseudoFlatFieldCorrection.png";

	@FXML
	private CheckBox doflatFieldCorrectionCheckbox;

	@FXML
	private TextField flatFieldCorrectionGaussianFilterSizeField;

	public PseudoFlatFieldCorrectionController(Context context, ImagePreprocessor imagePreprocessor) {
		super(context, imagePreprocessor);
		setFXMLPath(FXML_PATH);
		setTooltipImagePath(tooltipImagePath);
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
