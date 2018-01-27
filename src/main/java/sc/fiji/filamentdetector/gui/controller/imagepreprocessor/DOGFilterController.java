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
package sc.fiji.filamentdetector.gui.controller.imagepreprocessor;

import java.net.URL;
import java.util.ResourceBundle;

import org.scijava.Context;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import sc.fiji.filamentdetector.imagepreprocessor.DOGFilterPreprocessor;
import sc.fiji.filamentdetector.imagepreprocessor.ImagePreprocessor;

public class DOGFilterController extends AbstractImagePreprocessorController {

	public static String FXML_PATH = "/sc/fiji/filamentdetector/gui/view/preprocessor/DOGFilterView.fxml";
	private static String tooltipImagePath = "/sc/fiji/filamentdetector/gui/preprocessorexamples/dogFiltering.png";

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
