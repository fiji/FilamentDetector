/*-
 * #%L
 * A Fiji plugin that allow easy, fast and accurate detection and tracking of biological filaments.
 * %%
 * Copyright (C) 2016 - 2017 Hadrien Mary
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.scijava.Context;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Pane;
import javafx.util.Callback;
import sc.fiji.filamentdetector.FilamentWorkflow;
import sc.fiji.filamentdetector.analyzer.Analyzer;
import sc.fiji.filamentdetector.analyzer.AnalyzerService;
import sc.fiji.filamentdetector.analyzer.LengthOverTimeAnalyzer;
import sc.fiji.filamentdetector.analyzer.TipFitterAnalyzer;
import sc.fiji.filamentdetector.gui.GUIStatusService;
import sc.fiji.filamentdetector.gui.controller.analyzer.AnalyzerController;
import sc.fiji.filamentdetector.gui.controller.analyzer.LengthOverTimeAnalyzerController;
import sc.fiji.filamentdetector.gui.controller.analyzer.TipFitterAnalyzerController;

public class AnalyzeController extends AbstractController implements Initializable {

	private static String FXML_PATH = "/sc/fiji/filamentdetector/gui/view/AnalyzerView.fxml";

	@Parameter
	private Context context;

	@Parameter
	private LogService log;

	@Parameter
	private GUIStatusService status;

	@Parameter
	private AnalyzerService analyzerService;

	@FXML
	private Label nFilamentsField;

	@FXML
	private Label nTrackedFilamentsField;

	@FXML
	private ComboBox<Analyzer> analyzerCombobox;

	@FXML
	private Label analyzerDescription;

	@FXML
	private TitledPane analyzerPane;

	@FXML
	private Button analyzeButton;

	private Thread analyzerThread;
	private Task<Integer> analyzerTask;

	private FilamentWorkflow filamentWorkflow;

	private List<Analyzer> analyzers;
	private Map<Analyzer, AnalyzerController> analyzerControllers;

	public AnalyzeController(Context context, FilamentWorkflow filamentWorkflow) {
		context.inject(this);
		setFXMLPath(FXML_PATH);
		this.filamentWorkflow = filamentWorkflow;
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {

		setNoAnalyzer();

		analyzerCombobox.setCellFactory(new Callback<ListView<Analyzer>, ListCell<Analyzer>>() {
			@Override
			public ListCell<Analyzer> call(ListView<Analyzer> p) {
				return new ListCell<Analyzer>() {
					@Override
					protected void updateItem(Analyzer t, boolean bln) {
						super.updateItem(t, bln);
						if (t != null) {
							setText(t.toString());
						} else {
							setText(null);
						}
					}
				};
			}
		});

		analyzerCombobox.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
			if (newValue != null) {
				setAnalyzer(newValue);
			} else {
				setNoAnalyzer();
			}
		});

		// Add analyzers to Combobox
		analyzers = new ArrayList<>();
		analyzerControllers = new HashMap<>();

		// Add analyzers one by one
		LengthOverTimeAnalyzer lengthOverTimeAnalyzer = analyzerService.getLengthOverTimeAnalyzer();
		lengthOverTimeAnalyzer.setFilamentWorkflow(filamentWorkflow);
		AnalyzerController lengthOverTimeAnalyzerController = new LengthOverTimeAnalyzerController(context,
				lengthOverTimeAnalyzer);
		analyzers.add(lengthOverTimeAnalyzer);
		analyzerControllers.put(lengthOverTimeAnalyzer, lengthOverTimeAnalyzerController);

		TipFitterAnalyzer tipFitterAnalyzer = analyzerService.getTipFitterAnalyzer();
		tipFitterAnalyzer.setFilamentWorkflow(filamentWorkflow);
		AnalyzerController tipFitterAnalyzerController = new TipFitterAnalyzerController(context, tipFitterAnalyzer);
		analyzers.add(tipFitterAnalyzer);
		analyzerControllers.put(tipFitterAnalyzer, tipFitterAnalyzerController);

		/*
		 * NaiveNucleationAnalyzer naiveNucleationAnalyzer =
		 * analyzerService.getNaiveNucleationAnalyzer();
		 * naiveNucleationAnalyzer.setFilamentWorkflow(filamentWorkflow);
		 * AnalyzerController naiveNucleationAnalyzerController = new
		 * NaiveNucleationAnalyzerController(context, naiveNucleationAnalyzer);
		 * analyzers.add(naiveNucleationAnalyzer);
		 * analyzerControllers.put(naiveNucleationAnalyzer,
		 * naiveNucleationAnalyzerController);
		 */

		// Sync the analyzers with the combobox
		analyzerCombobox.setItems(FXCollections.observableList(analyzers));
		analyzerCombobox.getSelectionModel().clearSelection();

	}

	public void initPane() {
		if (this.filamentWorkflow.getTrackedFilaments() == null && this.filamentWorkflow.getFilaments() == null) {
			status.showStatus(
					"No filaments detected. Please use the \\\"Detect Filaments\\\" panel and \"Track Filaments\" panel first.");
			this.getPane().setDisable(true);
			nTrackedFilamentsField.setText("");
			nFilamentsField.setText("");
		} else {
			this.getPane().setDisable(false);
			if (filamentWorkflow.getTrackedFilaments() != null) {
				nTrackedFilamentsField.setText(Integer.toString(filamentWorkflow.getTrackedFilaments().size()));
			}
			if (filamentWorkflow.getFilaments() != null) {
				nFilamentsField.setText(Integer.toString(filamentWorkflow.getFilaments().size()));
			}
		}
	}

	private void setNoAnalyzer() {
		Label noAnalyzerPane = new Label("No analyzer selected.");
		analyzerPane.setContent(noAnalyzerPane);
		analyzeButton.setDisable(true);
	}

	private void setAnalyzer(Analyzer analyzer) {
		analyzerDescription.setText(analyzer.getDescription());
		AnalyzerController controller = analyzerControllers.get(analyzer);
		Pane pane = controller.loadPane();

		analyzerPane.setContent(pane);
		analyzeButton.setDisable(false);
	}

	@FXML
	void analyze(ActionEvent event) {
		if (analyzerTask != null) {
			analyzerTask.cancel();
		}

		if (analyzerThread != null) {
			analyzerThread.stop();
		}

		analyzerTask = new Task<Integer>() {
			@Override
			protected Integer call() throws Exception {
				analyzerCombobox.setDisable(true);
				Analyzer analyzer = analyzerCombobox.getSelectionModel().getSelectedItem();
				status.showStatus("Run analysis : ");
				status.showStatus(analyzer.getAnalyzerInfo());
				analyzer.analyze();
				return 0;
			}

			@Override
			protected void succeeded() {
				super.succeeded();
				Analyzer analyzer = analyzerCombobox.getSelectionModel().getSelectedItem();
				status.showStatus("Analysis Results are : ");
				status.showStatus(analyzer.getResultMessage());

				analyzerCombobox.setDisable(false);
				analyzerControllers.get(analyzer).runPostAnalysisAction();
			}

			@Override
			protected void cancelled() {
				super.cancelled();
				analyzerCombobox.setDisable(false);
			}

			@Override
			protected void failed() {
				super.failed();
				analyzerCombobox.setDisable(false);
				status.showStatus("Something failed during analysis : ");
				StackTraceElement[] stackTrace = this.getException().getStackTrace();
				status.showStatus(
						Arrays.stream(stackTrace).map(StackTraceElement::toString).collect(Collectors.joining("\n")));
			}
		};

		analyzerThread = new Thread(analyzerTask);
		analyzerThread.setContextClassLoader(Thread.currentThread().getContextClassLoader());
		analyzerThread.setDaemon(true);
		analyzerThread.start();
	}

}
