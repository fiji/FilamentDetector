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
package sc.fiji.filamentdetector.gui.controller.analyzer;

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
import sc.fiji.filamentdetector.analyzer.LengthOverTimeAnalyzer;
import sc.fiji.filamentdetector.gui.GUIStatusService;
import sc.fiji.filamentdetector.model.TrackedFilament;

public class LengthOverTimeAnalyzerController extends AbstractAnalyzerController implements AnalyzerController {

	private static String FXML_VIEW_FILE = "/sc/fiji/filamentdetector/gui/view/analyzer/LengthOverTimeAnalyzerView.fxml";

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
		setFXMLPath(FXML_VIEW_FILE);
		this.analyzer = analyzer;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		saveResultsCheckbox.setSelected(analyzer.isSaveResults());
		showPlotsCheckbox.setSelected(showPlots);
		savePlotsCheckbox.setSelected(savePlots);

		this.updateParameters(null);
	}

	@FXML
	void updateParameters(ActionEvent event) {
		analyzer.setSaveResults(saveResultsCheckbox.isSelected());
		showPlots = showPlotsCheckbox.isSelected();
		savePlots = savePlotsCheckbox.isSelected();

		if (savePlotsCheckbox.isSelected()) {
			showPlotsCheckbox.setSelected(true);
			showPlotsCheckbox.setDisable(true);
		} else {
			showPlotsCheckbox.setDisable(false);
		}
	}

	@Override
	public void runPostAnalysisAction() {

		if (showPlots || savePlots) {

			Map<String, Object> results = analyzer.getResults();

			// I know this is an horrible practice....
			List<Integer> ids = (List<Integer>) results.get("ids");
			List<Double> lengths = (List<Double>) results.get("lengths");
			List<Double> times = (List<Double>) results.get("times");

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
				for (int i = 0; i < lengths.size(); i++) {
					if (ids.get(i) == trackedFilament.getId()) {
						data = new XYChart.Data<>();
						data.setXValue((double) times.get(i));
						data.setYValue((double) lengths.get(i));
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
