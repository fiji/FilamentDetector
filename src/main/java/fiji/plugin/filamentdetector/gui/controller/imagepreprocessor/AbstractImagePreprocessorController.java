package fiji.plugin.filamentdetector.gui.controller.imagepreprocessor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;

import org.scijava.Context;

import fiji.plugin.filamentdetector.gui.controller.AbstractController;
import fiji.plugin.filamentdetector.imagepreprocessor.ImagePreprocessor;
import javafx.fxml.Initializable;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public abstract class AbstractImagePreprocessorController extends AbstractController implements Initializable {

	private ImagePreprocessor imagePreprocessor;
	private String tooltipImagePath = null;

	public AbstractImagePreprocessorController(Context context, ImagePreprocessor imagePreprocessor) {
		context.inject(this);
		this.imagePreprocessor = imagePreprocessor;
	}

	public ImagePreprocessor getImagePreprocessor() {
		return imagePreprocessor;
	}

	public void setTooltipImagePath(String tooltipImagePath) {
		this.tooltipImagePath = tooltipImagePath;
	}

	public void enableTooltip() {
		if (this.tooltipImagePath != null) {
			URL imgPath = getClass().getResource(this.tooltipImagePath);

			File initialFile = new File(imgPath.getFile());
			InputStream targetStream;
			try {
				targetStream = new FileInputStream(initialFile);
				Image image = new Image(targetStream);
				ImageView imageView = new ImageView(image);
				imageView.setPreserveRatio(true);
				imageView.setFitHeight(300);

				Tooltip tooltip = new Tooltip();
				tooltip.setStyle("-fx-background-color: transparent;");
				tooltip.setGraphic(imageView);
				Tooltip.install(getPane(), tooltip);
			} catch (FileNotFoundException e) {
			}
		}
	}

}
