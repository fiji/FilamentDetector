/*-
 * #%L
 * A Fiji plugin that allow easy, fast and accurate detection and tracking of biological filaments.
 * %%
 * Copyright (C) 2016 - 2021 Fiji developers.
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

package sc.fiji.filamentdetector.gui.controller;

import java.net.URL;
import java.util.ResourceBundle;

import org.scijava.Context;
import org.scijava.app.StatusService;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.web.WebView;
import sc.fiji.filamentdetector.FilamentWorkflow;

public class WelcomeController extends AbstractController implements Initializable {

	private static String FXML_PATH = "/sc/fiji/filamentdetector/gui/view/WelcomeView.fxml";

	@Parameter
	private LogService log;

	@Parameter
	private StatusService status;

	@FXML
	private WebView welcomeTextField;

	@FXML
	private Label activeImageLabel;

	@FXML
	private TextField pixelWidthField;

	@FXML
	private Label pixelWidthUnitLabel;

	@FXML
	private TextField pixelHeightField;

	@FXML
	private Label pixelHeightUnitLabel;

	@FXML
	private TextField voxelDepthField;

	@FXML
	private Label voxelDepthUnitLabel;

	@FXML
	private TextField timeIntervalField;

	@FXML
	private Label timeIntervalUnitLabel;

	@FXML
	private ComboBox<String> channelComboBox;

	private FilamentWorkflow filamentDetector;

	public WelcomeController(Context context, FilamentWorkflow filamentDetector) {
		context.inject(this);
		this.filamentDetector = filamentDetector;
		setFXMLPath(FXML_PATH);
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		welcomeTextField.setContextMenuEnabled(false);
		loadWelcomeText();
	}

	private void loadWelcomeText() {
		String text = "<h1><a id=\"Welcome_to_FilamentDetector_0\"></a>Welcome to FilamentDetector</h1>\n"
				+ "<p><strong>FilamentDetector</strong> allows you to <em>detect</em> and <em>track</em> filaments in an "
				+ "image or a stack of images. <strong>Detection</strong> is done independently on all the images while <strong>tracking</strong> is done "
				+ "on the whole stack in the TIME dimension.</p>\n"
				+ "<p>Once those steps are done, you can <strong>export the data</strong> to CSV files, saves detected filaments as <strong>kymographs</strong>, "
				+ "or run a <strong>specific analysis module</strong> (to analyze microtubule dynamics for example).</p>\n";
		welcomeTextField.getEngine().loadContent(text);
	}

	public void loadImageCalibrations() {
		activeImageLabel.setText(filamentDetector.getImageDisplay().getName());

		pixelWidthField.setText(Double.toString(filamentDetector.getCalibrations().getDx()));
		pixelHeightField.setText(Double.toString(filamentDetector.getCalibrations().getDy()));
		voxelDepthField.setText(Double.toString(filamentDetector.getCalibrations().getDz()));
		timeIntervalField.setText(Double.toString(filamentDetector.getCalibrations().getDt()));

		pixelWidthUnitLabel.setText(filamentDetector.getCalibrations().getUnitX());
		pixelHeightUnitLabel.setText(filamentDetector.getCalibrations().getUnitY());
		voxelDepthUnitLabel.setText(filamentDetector.getCalibrations().getUnitZ());
		timeIntervalUnitLabel.setText(filamentDetector.getCalibrations().getUnitT());

		channelComboBox.getItems().addAll(filamentDetector.getCalibrations().getChannelList());
		channelComboBox.getSelectionModel().selectFirst();

		if (this.filamentDetector.getCalibrations().getSizeZ() <= 1) {
			// Disable the Voxel Depth checkbox
			voxelDepthField.setDisable(true);
		} else {
			voxelDepthField.setDisable(false);
		}
	}

	@FXML
	void writeCalibration(Event event) {

		filamentDetector.getCalibrations().setDx(Double.parseDouble(pixelWidthField.getText()));
		filamentDetector.getCalibrations().setDy(Double.parseDouble(pixelHeightField.getText()));
		filamentDetector.getCalibrations().setDz(Double.parseDouble(voxelDepthField.getText()));
		filamentDetector.getCalibrations().setDt(Double.parseDouble(timeIntervalField.getText()));

		filamentDetector.getCalibrations().channelToUse(channelComboBox.getSelectionModel().getSelectedItem());

		status.showStatus("Image calibrations and channel to use have been updated.");
	}

}
