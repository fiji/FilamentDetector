package fiji.plugin.filamentdetector.tests;

import java.util.ArrayList;
import java.util.List;

import fiji.plugin.filamentdetector.fxwidgets.ReorderablePaneListView;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class TestJFXReorderList extends Application {

	@Override
	public void start(Stage stage) throws Exception {
		
		List <Pane> panes = new ArrayList<>();
		panes.add(getPane("salut"));
		panes.add(getPane("pane 2"));
		panes.add(getPane("again another"));
						
		ReorderablePaneListView panesList = new ReorderablePaneListView();
		panesList.getItems().setAll(panes);

		VBox layout = new VBox(panesList);
		layout.setPadding(new Insets(10));
		stage.setScene(new Scene(layout));
		stage.show();
	}

	public static Pane getPane(String title) {
		
		GridPane grid = new GridPane();
		grid.setAlignment(Pos.CENTER);
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(25, 25, 25, 25));
		
		Text scenetitle = new Text(title);
		scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
		grid.add(scenetitle, 0, 0, 2, 1);

		Label userName = new Label("User Name:");
		grid.add(userName, 0, 1);

		TextField userTextField = new TextField();
		grid.add(userTextField, 1, 1);

		Label pw = new Label("Password:");
		grid.add(pw, 0, 2);

		PasswordField pwBox = new PasswordField();
		grid.add(pwBox, 1, 2);
		
		grid.setId(title);
		
		return grid;
	}
	
	public static void main(String[] args) {
		launch(TestJFXReorderList.class);
	}



}