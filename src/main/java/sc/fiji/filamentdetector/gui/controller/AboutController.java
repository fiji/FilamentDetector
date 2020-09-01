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
import java.util.ResourceBundle;

import org.scijava.Context;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.web.WebView;

public class AboutController extends AbstractController implements Initializable {

	private static String FXML_PATH = "/sc/fiji/filamentdetector/gui/view/AboutView.fxml";

	@FXML
	private WebView aboutTextField;

	public AboutController(Context context) {
		context.inject(this);
		setFXMLPath(FXML_PATH);
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		aboutTextField.setContextMenuEnabled(false);
		loadAboutText();
	}

	private void loadAboutText() {
		String text = "<h1><a></a>About FilamentDetector</h1>\n"
				+ "<p>The <strong>detection</strong> step is based on the ImageJ1 implementation of Steger,"
				+ " 1998 (<a href=\"http://imagej.net/Ridge_Detection\">http://imagej.net/Ridge_Detection</a>)."
				+ " The <strong>tracking</strong> step is based on the Jaqaman tracking algorithm (Jaqaman, 2008)"
				+ " implemented in <a href=\"https://imagej.net/TrackMate\">TrackMate</a>. Instead of using the "
				+ "euclidean distance as a cost function, <strong>FilamentDetector</strong> usez the bounding boxes "
				+ "of the detected filaments to compute the <a href=\"https://en.wikipedia.org/wiki/Jaccard_index\">"
				+ "Intersection over Union</a>.</p>\n" + "<ul>\n"
				+ "<li>Source code: <a href=\"https://github.com/hadim/FilamentDetector\">https://github.com/hadim/FilamentDetector</a></li>\n"
				+ "<li>Report an issue: <a href=\"https://github.com/hadim/FilamentDetector/issues\">https://github.com/hadim/FilamentDetector/issues</a></li>\n"
				+ "<li>Wiki: <a href=\"http://imagej.net/FilamentDetector\">http://imagej.net/FilamentDetector</a></li>\n"
				+ "</ul>";
		aboutTextField.getEngine().loadContent(text);

	}

}
