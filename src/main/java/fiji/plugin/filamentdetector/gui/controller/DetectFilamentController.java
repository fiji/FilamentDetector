
package fiji.plugin.filamentdetector.gui.controller;

import java.net.URL;
import java.util.ResourceBundle;

import org.scijava.Context;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;

public class DetectFilamentController extends Controller implements Initializable {

	@FXML
	private ListView<String> listView;

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
	}

	public DetectFilamentController(Context context) {
		context.inject(this);
	}

	public void test() {
		listView.getItems().add("csfsdfsf");
		listView.getItems().add("tete");
	}

}
