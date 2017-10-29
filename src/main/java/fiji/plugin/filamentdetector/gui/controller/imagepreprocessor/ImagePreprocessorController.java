package fiji.plugin.filamentdetector.gui.controller.imagepreprocessor;

import org.scijava.Context;

import fiji.plugin.filamentdetector.gui.controller.Controller;
import fiji.plugin.filamentdetector.preprocessing.ImagePreprocessor;
import javafx.fxml.Initializable;

public abstract class ImagePreprocessorController extends Controller implements Initializable {

	private ImagePreprocessor imagePreprocessor;

	public ImagePreprocessorController(Context context, ImagePreprocessor imagePreprocessor) {
		context.inject(this);
		this.imagePreprocessor = imagePreprocessor;
	}

	public ImagePreprocessor getImagePreprocessor() {
		return imagePreprocessor;
	}

}
