package fiji.plugin.filamentdetector.gui.controller.imagepreprocessor;

import java.net.URL;
import java.util.ResourceBundle;

import org.scijava.Context;

import fiji.plugin.filamentdetector.preprocessing.FrangiFilterPreprocessor;
import fiji.plugin.filamentdetector.preprocessing.ImagePreprocessor;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;

public class FrangiFilterController extends ImagePreprocessorController {

	public static String FXML_PATH = "/fiji/plugin/filamentdetector/gui/view/preprocessor/FrangiFilterView.fxml";
	private static String tooltipImagePath = "/fiji/plugin/filamentdetector/gui/preprocessorexamples/frangiFiltering.png";

	@FXML
	private TextField spacing;

	@FXML
	private TextField scale;

	@FXML
	private CheckBox doProcessCheckbox;

	public FrangiFilterController(Context context, ImagePreprocessor imagePreprocessor) {
		super(context, imagePreprocessor);
		setFXMLPath(FXML_PATH);
		setTooltipImagePath(tooltipImagePath);
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		FrangiFilterPreprocessor imagePreprocessor = (FrangiFilterPreprocessor) getImagePreprocessor();
		doProcessCheckbox.setSelected(imagePreprocessor.isDoPreprocess());
		spacing.setText(Double.toString(imagePreprocessor.getSpacing()));
		scale.setText(Integer.toString(imagePreprocessor.getScale()));
	}

	public void updateParameters() {
		FrangiFilterPreprocessor imagePreprocessor = (FrangiFilterPreprocessor) getImagePreprocessor();
		imagePreprocessor.setDoPreprocess(doProcessCheckbox.isSelected());
		imagePreprocessor.setSpacing(Double.parseDouble(spacing.getText()));
		imagePreprocessor.setScale((int) Double.parseDouble(scale.getText()));
	}

}
