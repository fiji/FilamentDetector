package fiji.plugin.filamentdetector.gui.controller.analyzer;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;
import org.scijava.Context;
import org.scijava.plugin.Parameter;

import fiji.plugin.filamentdetector.analyzer.LengthOverTimeAnalyzer;
import fiji.plugin.filamentdetector.gui.GUIStatusService;
import fiji.plugin.filamentdetector.model.TrackedFilament;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.imagej.Dataset;

public class LengthOverTimeAnalyzerController extends AbstractAnalyzerController implements AnalyzerController {

	private static String FXML_VIEW_FILE = "/fiji/plugin/filamentdetector/gui/view/analyzer/LengthOverTimeAnalyzerView.fxml";

	@Parameter
	private GUIStatusService status;

	@FXML
	private CheckBox saveResultsCheckbox;

	@FXML
	private CheckBox showPlotsCheckbox;

	@FXML
	private CheckBox savePlotsCheckbox;

	private LengthOverTimeAnalyzer analyzer;

	private boolean showPlots = true;
	private boolean savePlots = false;

	public LengthOverTimeAnalyzerController(Context context, LengthOverTimeAnalyzer analyzer) {
		super(context);
		this.analyzer = analyzer;
	}

	@Override
	public String getViewFXMlFile() {
		return FXML_VIEW_FILE;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		saveResultsCheckbox.setSelected(analyzer.isSaveResults());
		showPlotsCheckbox.setSelected(showPlots);
		savePlotsCheckbox.setSelected(savePlots);
	}

	@FXML
	void updateParameters(ActionEvent event) {
		analyzer.setSaveResults(saveResultsCheckbox.isSelected());
		showPlots = showPlotsCheckbox.isSelected();
		savePlots = savePlotsCheckbox.isSelected();
	}

	@Override
	public void runPostAnalysisAction() {

		if (showPlots || savePlots) {

			Map<String, List<? extends Number>> results = analyzer.getResults();

			TabPane tabPane = new TabPane();
			Tab tab;
			VBox vbox;
			Button saveButton;

			NumberAxis xAxis;
			NumberAxis yAxis;

			XYChart.Series<Number, Number> series;
			XYChart.Data<Number, Number> data;

			List<LineChart<Number, Number>> lineCharts = new ArrayList<>();

			for (TrackedFilament trackedFilament : analyzer.getFilamentWorkflow().getTrackedFilaments()) {

				xAxis = new NumberAxis();
				yAxis = new NumberAxis();

				xAxis.setLabel("Time (s)");
				yAxis.setLabel("Length (um)");

				LineChart<Number, Number> lineChart = new LineChart<Number, Number>(xAxis, yAxis);
				lineChart.setTitle("Length vs Time Curve for Filament " + trackedFilament.getId());

				series = new XYChart.Series<>();
				for (int i = 0; i < results.get("lengths").size(); i++) {
					if ((Integer) results.get("ids").get(i) == trackedFilament.getId()) {
						data = new XYChart.Data<>();
						data.setXValue(results.get("times").get(i));
						data.setYValue(results.get("lengths").get(i));
						series.getData().add(data);
					}
				}
				lineChart.getData().add(series);
				lineCharts.add(lineChart);

				tab = new Tab("Filament " + trackedFilament.getId());
				tab.setClosable(false);

				vbox = new VBox();
				vbox.getChildren().add(lineChart);

				vbox.setPadding(new Insets(10, 10, 10, 10));
				vbox.setSpacing(10);

				saveButton = new Button("Save this plot");
				vbox.getChildren().add(saveButton);

				saveButton.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent actionEvent) {
						savePlot(trackedFilament, lineChart);
					}
				});

				tab.setContent(vbox);
				tabPane.getTabs().add(tab);
			}

			Stage stage = new Stage();
			stage.initModality(Modality.WINDOW_MODAL);
			stage.setAlwaysOnTop(true);

			stage.setTitle("Length vs Time Curve");
			stage.setScene(new Scene(tabPane));
			stage.show();

			if (savePlots) {
				for (int i = 0; i < analyzer.getFilamentWorkflow().getTrackedFilaments().size(); i++) {
					savePlot(analyzer.getFilamentWorkflow().getTrackedFilaments().get(i), lineCharts.get(i));
				}
			}
		}

	}

	private void savePlot(TrackedFilament trackedFilament, LineChart<Number, Number> lineChart) {
		Dataset dataset = (Dataset) analyzer.getFilamentWorkflow().getImageDisplay().getActiveView().getData();
		if (dataset.getSource() != null) {
			String filePath = FilenameUtils.removeExtension(dataset.getSource());
			filePath += "-LengthOverTime-Filament-" + trackedFilament.getId() + ".png";

			File file = new File(filePath);

			WritableImage image = lineChart.snapshot(new SnapshotParameters(), null);
			try {
				ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
			} catch (IOException e) {
				status.showStatus("Can't save the plot");
				status.showStatus(e.getLocalizedMessage());
			}

		} else {
			status.showStatus("Can't save the plot");
		}

	}
}
