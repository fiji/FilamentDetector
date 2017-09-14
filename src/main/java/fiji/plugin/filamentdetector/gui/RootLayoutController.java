package fiji.plugin.filamentdetector.gui;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import org.scijava.Context;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

/**
 * FXML Controller class
 *
 * @author Hadrien Mary
 */
public class RootLayoutController implements Initializable {

    @Parameter
    private LogService log;

    @FXML
    private Button runButton;
    
    @FXML
    private void handleRunButtonAction(ActionEvent event) {
        log.info("The button is clicked");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void setContext(Context context) {
        context.inject(this);
    }

}