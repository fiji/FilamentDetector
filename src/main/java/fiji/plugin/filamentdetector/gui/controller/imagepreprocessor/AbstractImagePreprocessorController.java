/*-
 * #%L
 * A Fiji plugin that allow easy, fast and accurate detection and tracking of biological filaments.
 * %%
 * Copyright (C) 2016 - 2017 Hadrien Mary
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */
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
