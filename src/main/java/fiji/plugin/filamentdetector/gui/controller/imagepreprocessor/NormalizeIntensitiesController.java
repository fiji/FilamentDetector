package fiji.plugin.filamentdetector.gui.controller.imagepreprocessor;

import java.net.URL;
import java.util.ResourceBundle;

import org.scijava.Context;

import fiji.plugin.filamentdetector.imagepreprocessor.ImagePreprocessor;
import fiji.plugin.filamentdetector.imagepreprocessor.NormalizeIntensitiesPreprocessor;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;

public class NormalizeIntensitiesController extends AbstractImagePreprocessorController {

	public static String FXML_PATH = "/fiji/plugin/filamentdetector/gui/view/preprocessor/NormalizeIntensitiesView.fxml";
	private static String tooltipImagePath = "/fiji/plugin/filamentdetector/gui/preprocessorexamples/normalizeIntensities.png";

	@FXML
	private CheckBox doProcessCheckbox;

	public NormalizeIntensitiesController(Context context, ImagePreprocessor imagePreprocessor) {
		super(context, imagePreprocessor);
		setFXMLPath(FXML_PATH);
		setTooltipImagePath(tooltipImagePath);
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		NormalizeIntensitiesPreprocessor imagePreprocessor = (NormalizeIntensitiesPreprocessor) getImagePreprocessor();
		doProcessCheckbox.setSelected(imagePreprocessor.isDoPreprocess());
	}

	public void updateParameters() {
		NormalizeIntensitiesPreprocessor imagePreprocessor = (NormalizeIntensitiesPreprocessor) getImagePreprocessor();
		imagePreprocessor.setDoPreprocess(doProcessCheckbox.isSelected());
	}

}
