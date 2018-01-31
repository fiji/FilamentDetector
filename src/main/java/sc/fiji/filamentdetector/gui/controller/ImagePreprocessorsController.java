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
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.scijava.Context;
import org.scijava.event.EventService;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.ui.UIService;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import net.imagej.Dataset;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import sc.fiji.filamentdetector.FilamentWorkflow;
import sc.fiji.filamentdetector.event.PreventPanelSwitchEvent;
import sc.fiji.filamentdetector.gui.GUIStatusService;
import sc.fiji.filamentdetector.gui.controller.imagepreprocessor.AbstractImagePreprocessorController;
import sc.fiji.filamentdetector.gui.controller.imagepreprocessor.Convert8BitController;
import sc.fiji.filamentdetector.gui.controller.imagepreprocessor.DOGFilterController;
import sc.fiji.filamentdetector.gui.controller.imagepreprocessor.FrangiFilterController;
import sc.fiji.filamentdetector.gui.controller.imagepreprocessor.GaussianFilterController;
import sc.fiji.filamentdetector.gui.controller.imagepreprocessor.NormalizeIntensitiesController;
import sc.fiji.filamentdetector.gui.controller.imagepreprocessor.PseudoFlatFieldCorrectionController;
import sc.fiji.filamentdetector.gui.controller.imagepreprocessor.TubenessFilterController;
import sc.fiji.filamentdetector.gui.fxwidgets.ReorderablePaneListView;
import sc.fiji.filamentdetector.imagepreprocessor.Convert8BitPreprocessor;
import sc.fiji.filamentdetector.imagepreprocessor.DOGFilterPreprocessor;
import sc.fiji.filamentdetector.imagepreprocessor.FrangiFilterPreprocessor;
import sc.fiji.filamentdetector.imagepreprocessor.GaussianFilterPreprocessor;
import sc.fiji.filamentdetector.imagepreprocessor.ImagePreprocessor;
import sc.fiji.filamentdetector.imagepreprocessor.ImagePreprocessors;
import sc.fiji.filamentdetector.imagepreprocessor.NormalizeIntensitiesPreprocessor;
import sc.fiji.filamentdetector.imagepreprocessor.PseudoFlatFieldCorrectionPreprocessor;
import sc.fiji.filamentdetector.imagepreprocessor.TubenessFilterPreprocessor;
import sc.fiji.filamentdetector.overlay.FilamentOverlayService;

public class ImagePreprocessorsController extends AbstractController implements Initializable {

	private static String FXMl_PATH = "/sc/fiji/filamentdetector/gui/view/ImagePreprocessorView.fxml";

	@Parameter
	private Context context;

	@Parameter
	private LogService log;

	@Parameter
	private GUIStatusService status;

	@Parameter
	private EventService eventService;

	@Parameter
	private FilamentOverlayService overlay;

	@Parameter
	private UIService ui;

	@Parameter
	private ImageDisplayService ids;

	@FXML
	private CheckBox saveImageCheckbox;

	@FXML
	private CheckBox showImageCheckbox;

	@FXML
	private CheckBox useForOverlayCheckbox;

	@FXML
	private ProgressIndicator progressIndicator;

	@FXML
	private AnchorPane preprocessorContainer;

	private Thread thread;
	private Task<Integer> task;

	private ImagePreprocessors imagePreprocessors;
	private FilamentWorkflow filamentWorkflow;

	private ReorderablePaneListView processorsControllers;
	private List<AbstractImagePreprocessorController> imagePreprocessorControllers;

	public ImagePreprocessorsController(Context context, FilamentWorkflow filamentWorkflow) {
		context.inject(this);
		setFXMLPath(FXMl_PATH);
		this.filamentWorkflow = filamentWorkflow;
		this.imagePreprocessors = filamentWorkflow.getImagePreprocessor();
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		this.progressIndicator.setVisible(false);

		VBox vbox = new VBox();
		this.preprocessorContainer.getChildren().add(vbox);

		this.imagePreprocessorControllers = new ArrayList<>();
		AbstractImagePreprocessorController imagePreprocessorController = null;

		for (ImagePreprocessor imagePreprocessor : this.imagePreprocessors.getImagePreprocessors()) {

			if (imagePreprocessor.getClass().equals(Convert8BitPreprocessor.class)) {
				imagePreprocessorController = new Convert8BitController(context, imagePreprocessor);

			} else if (imagePreprocessor.getClass().equals(GaussianFilterPreprocessor.class)) {
				imagePreprocessorController = new GaussianFilterController(context, imagePreprocessor);

			} else if (imagePreprocessor.getClass().equals(PseudoFlatFieldCorrectionPreprocessor.class)) {
				imagePreprocessorController = new PseudoFlatFieldCorrectionController(context, imagePreprocessor);

			} else if (imagePreprocessor.getClass().equals(DOGFilterPreprocessor.class)) {
				imagePreprocessorController = new DOGFilterController(context, imagePreprocessor);

			} else if (imagePreprocessor.getClass().equals(NormalizeIntensitiesPreprocessor.class)) {
				imagePreprocessorController = new NormalizeIntensitiesController(context, imagePreprocessor);

			} else if (imagePreprocessor.getClass().equals(FrangiFilterPreprocessor.class)) {
				imagePreprocessorController = new FrangiFilterController(context, imagePreprocessor);

			} else if (imagePreprocessor.getClass().equals(TubenessFilterPreprocessor.class)) {
				imagePreprocessorController = new TubenessFilterController(context, imagePreprocessor);

			} else {
				log.error(imagePreprocessor + " is can't be loaded.");
			}

			if (imagePreprocessorController != null) {
				this.imagePreprocessorControllers.add(imagePreprocessorController);
			}
		}

		this.processorsControllers = new ReorderablePaneListView(imagePreprocessorControllers);
		this.preprocessorContainer.getChildren().add(this.processorsControllers);

		AnchorPane.setBottomAnchor(this.processorsControllers, 0.0);
		AnchorPane.setTopAnchor(this.processorsControllers, 0.0);
		AnchorPane.setLeftAnchor(this.processorsControllers, 0.0);
		AnchorPane.setRightAnchor(this.processorsControllers, 0.0);

		saveImageCheckbox.setSelected(imagePreprocessors.isSavePreprocessedImage());
		showImageCheckbox.setSelected(imagePreprocessors.isShowPreprocessedImage());

		useForOverlayCheckbox.setSelected(imagePreprocessors.isUseForOverlay());

		useForOverlayCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
				if (old_val != new_val) {
					usePreprocessedImageForOverlay();
				}
			}
		});

		// Enable tooltips
		for (AbstractImagePreprocessorController controller : this.imagePreprocessorControllers) {
			controller.enableTooltip();
		}

		this.updateParameters();
	}

	public void updateParameters() {
		imagePreprocessors.setSavePreprocessedImage(saveImageCheckbox.isSelected());
		imagePreprocessors.setShowPreprocessedImage(showImageCheckbox.isSelected());
		imagePreprocessors.setUseForOverlay(useForOverlayCheckbox.isSelected());

		if (imagePreprocessors.isUseForOverlay()) {
			showImageCheckbox.setSelected(true);
			showImageCheckbox.setDisable(true);
		} else {
			showImageCheckbox.setDisable(false);
		}
	}

	@FXML
	void preprocessImage(ActionEvent event) {
		if (task != null) {
			task.cancel();
		}

		if (thread != null) {
			thread.stop();
		}
		this.progressIndicator.setVisible(true);

		task = new Task<Integer>() {
			@Override
			protected Integer call() throws Exception {
				eventService.publish(new PreventPanelSwitchEvent(true));

				String statusMessage = "Preprocessing steps are : \n"
						+ imagePreprocessorControllers.stream().filter(c -> c.getImagePreprocessor().isDoPreprocess())
								.map(c -> c.getImagePreprocessor().getClass().getSimpleName())
								.collect(Collectors.joining("\n"));
				status.showStatus(statusMessage);

				imagePreprocessors.setImagePreprocessors(imagePreprocessorControllers.stream()
						.map(c -> c.getImagePreprocessor()).collect(Collectors.toList()));

				filamentWorkflow.preProcessImages();
				return 0;
			}

			@Override
			protected void succeeded() {
				super.succeeded();
				eventService.publish(new PreventPanelSwitchEvent(false));
				String statusMessage = "The image has been successfully preprocessed.";
				status.showStatus(statusMessage);
				progressIndicator.setVisible(false);
				overlay.reset();
				overlay.autoScaleImage();
				usePreprocessedImageForOverlay();
			}

			@Override
			protected void cancelled() {
				super.cancelled();
				progressIndicator.setVisible(false);
				eventService.publish(new PreventPanelSwitchEvent(false));
			}

			@Override
			protected void failed() {
				super.failed();
				status.showStatus("Something failed during preprocessing : ");
				StackTraceElement[] stackTrace = this.getException().getStackTrace();
				status.showStatus(
						Arrays.stream(stackTrace).map(StackTraceElement::toString).collect(Collectors.joining("\n")));
				progressIndicator.setVisible(false);
				eventService.publish(new PreventPanelSwitchEvent(false));
			}
		};

		thread = new Thread(task);
		thread.setDaemon(true);
		thread.start();
	}

	private void usePreprocessedImageForOverlay() {
		if (useForOverlayCheckbox.isSelected()) {
			Dataset dataset = imagePreprocessors.getPreprocessedDataset();

			if (dataset != null) {

				ImageDisplay imageDisplay = ids.getImageDisplays().stream()
						.filter(imd -> ((Dataset) imd.getActiveView().getData()).equals(dataset)).findFirst()
						.orElse(null);

				if (imageDisplay == null) {
					ui.show(dataset);
					imageDisplay = ids.getImageDisplays().stream()
							.filter(imd -> ((Dataset) imd.getActiveView().getData()).equals(dataset)).findFirst()
							.orElse(null);
				}

				if (imageDisplay != null) {
					overlay.setImageDisplay(imageDisplay);
					filamentWorkflow.setImageDisplay(imageDisplay);
					status.showStatus("Using preprocessed image for overlay.");
				} else {
					overlay.setImageDisplay(imagePreprocessors.getImageDisplay());
					filamentWorkflow.setImageDisplay(imagePreprocessors.getImageDisplay());
					log.error("Cannot use the preprocessed image for overlay.");
				}
			}

		} else {
			overlay.setImageDisplay(imagePreprocessors.getImageDisplay());
			filamentWorkflow.setImageDisplay(imagePreprocessors.getImageDisplay());
		}
	}
}
