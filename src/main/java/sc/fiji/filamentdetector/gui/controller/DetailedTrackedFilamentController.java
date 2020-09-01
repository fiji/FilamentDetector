/*-
 * #%L
 * A Fiji plugin that allow easy, fast and accurate detection and tracking of biological filaments.
 * %%
 * Copyright (C) 2016 - 2020 Fiji developers.
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
import java.text.DecimalFormat;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import sc.fiji.filamentdetector.model.TrackedFilament;

public class DetailedTrackedFilamentController extends AbstractController implements Initializable {

	@FXML
	private Label idLabel;

	@FXML
	private Label sizeLabel;

	@FXML
	private Label colorLabel;

	@FXML
	public Button removeFilamentLabel;

	private TrackedFilament trackedFilament;

	public DetailedTrackedFilamentController(TrackedFilament trackedFilament) {
		this.trackedFilament = trackedFilament;
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		DecimalFormat f = new DecimalFormat("##.00");

		idLabel.setText(Integer.toString(trackedFilament.getId()));
		sizeLabel.setText(Integer.toString(trackedFilament.size()));
		colorLabel.setStyle("-fx-background-color:" + trackedFilament.getColorAsHex());
	}

	public Button getRemoveFilamentLabel() {
		return removeFilamentLabel;
	}

	public TrackedFilament getTrackedFilament() {
		return trackedFilament;
	}
}
