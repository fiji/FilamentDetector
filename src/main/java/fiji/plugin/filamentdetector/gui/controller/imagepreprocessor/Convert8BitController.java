package fiji.plugin.filamentdetector.gui.controller.imagepreprocessor;

import java.net.URL;
import java.util.ResourceBundle;

import org.scijava.Context;

import fiji.plugin.filamentdetector.preprocessing.Convert8BitPreprocessor;
import fiji.plugin.filamentdetector.preprocessing.ImagePreprocessor;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;

public class Convert8BitController extends ImagePreprocessorController {

	public static String FXML_PATH = "/fiji/plugin/filamentdetector/gui/view/preprocessor/Convert8BitView.fxml";

	@FXML
	private CheckBox convert8BitCheckbox;

	public Convert8BitController(Context context, ImagePreprocessor imagePreprocessor) {
		super(context, imagePreprocessor);
		setFXMLPath(FXML_PATH);
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		Convert8BitPreprocessor imagePreprocessor = (Convert8BitPreprocessor) getImagePreprocessor();
		convert8BitCheckbox.setSelected(imagePreprocessor.isDoPreprocess());
	}

	public void updateParameters() {
		Convert8BitPreprocessor imagePreprocessor = (Convert8BitPreprocessor) getImagePreprocessor();
		imagePreprocessor.setDoPreprocess(convert8BitCheckbox.isSelected());
	}

}
