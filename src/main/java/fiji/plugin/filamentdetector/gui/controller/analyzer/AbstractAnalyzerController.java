package fiji.plugin.filamentdetector.gui.controller.analyzer;

import org.scijava.Context;

import fiji.plugin.filamentdetector.gui.controller.Controller;
import javafx.fxml.Initializable;

public abstract class AbstractAnalyzerController extends Controller implements Initializable, AnalyzerController {

	public AbstractAnalyzerController(Context context) {
		context.inject(this);
	}
}
