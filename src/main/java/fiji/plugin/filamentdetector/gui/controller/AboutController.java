
package fiji.plugin.filamentdetector.gui.controller;

import java.net.URL;
import java.util.ResourceBundle;

import org.scijava.Context;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.web.WebView;

public class AboutController extends Controller implements Initializable {

	@FXML
	private WebView aboutTextField;

	public AboutController(Context context) {
		context.inject(this);
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
				+ "euclidean distance as a cost function, <strong>FilamentDetector</strong> usez the bounding boxe "
				+ "of the detected filaments to compute the <a href=\"https://en.wikipedia.org/wiki/Jaccard_index\">"
				+ "Intersection over Union</a>.</p>\n" + "<ul>\n"
				+ "<li>Source code: <a href=\"https://github.com/hadim/FilamentDetector\">https://github.com/hadim/FilamentDetector</a></li>\n"
				+ "<li>Report an issue: <a href=\"https://github.com/hadim/FilamentDetector/issues\">https://github.com/hadim/FilamentDetector/issues</a></li>\n"
				+ "<li>Wiki: <a href=\"http://imagej.net/FilamentDetector\">http://imagej.net/FilamentDetector</a></li>\n"
				+ "</ul>";
		aboutTextField.getEngine().loadContent(text);

	}

}
