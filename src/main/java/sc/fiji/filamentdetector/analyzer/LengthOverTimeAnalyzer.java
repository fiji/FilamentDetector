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
package sc.fiji.filamentdetector.analyzer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import com.opencsv.CSVWriter;

import net.imagej.Dataset;
import sc.fiji.filamentdetector.Calibrations;
import sc.fiji.filamentdetector.model.TrackedFilament;
import sc.fiji.filamentdetector.model.TrackedFilaments;

@Plugin(type = Analyzer.class, priority = Priority.HIGH)
public class LengthOverTimeAnalyzer extends AbstractAnalyzer {

	public static String NAME = "Filament Growth Curve";
	public static String DESCRIPTION = "Generate the classic filament length versus time curve."
			+ " (this analyzer is in development)";

	public LengthOverTimeAnalyzer() {
		setName(NAME);
		setDescription(DESCRIPTION);
	}

	@Override
	public String getAnalyzerInfo() {
		String out = "";
		out += "Name : " + this.name + "\n";
		out += "Save results : " + this.saveResults + "\n";
		out += "\n";
		return out;
	}

	@Override
	public void analyze() {

		this.resultMessage = "";

		Calibrations calibrations = filamentWorkflow.getCalibrations();
		TrackedFilaments trackedFilaments = filamentWorkflow.getTrackedFilaments();

		List<Integer> filamentIDs = new ArrayList<>();
		List<Double> lengths = new ArrayList<>();
		List<Double> times = new ArrayList<>();

		List<Double> lengthsTrackedFilament;
		List<Double> timesTrackedFilament;

		for (TrackedFilament trackedFilament : trackedFilaments) {

			lengthsTrackedFilament = trackedFilament.getLengths();
			timesTrackedFilament = trackedFilament.getFrames();

			// Convert using calibrations
			lengthsTrackedFilament = lengthsTrackedFilament.stream().map(x -> x * calibrations.getDx())
					.collect(Collectors.toList());
			timesTrackedFilament = timesTrackedFilament.stream().map(x -> x * calibrations.getDt())
					.collect(Collectors.toList());

			filamentIDs.addAll(Collections.nCopies(lengthsTrackedFilament.size(), trackedFilament.getId()));
			lengths.addAll(lengthsTrackedFilament);
			times.addAll(timesTrackedFilament);
		}

		this.results = new HashMap<>();
		this.results.put("ids", filamentIDs);
		this.results.put("lengths", lengths);
		this.results.put("times", times);

		if (saveResults) {

			// Set the name of the file
			Dataset dataset = (Dataset) filamentWorkflow.getSourceImage().getActiveView().getData();
			if (dataset.getSource() != null && dataset.getSource() != "") {
				String filePath = FilenameUtils.removeExtension(dataset.getSource());
				filePath += "-LengthOverTime.csv";

				File file = new File(filePath);
				try (CSVWriter writer = new CSVWriter(new FileWriter(file), ';', CSVWriter.DEFAULT_QUOTE_CHARACTER,
						CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END)) {

					writer.writeNext(Arrays.asList("filament id", "time", "length").stream().toArray(String[]::new));
					List<String[]> data = new ArrayList<>();
					List<String> row;
					for (int i = 0; i < lengths.size(); i++) {
						row = new ArrayList<>();
						row.add(Integer.toString(filamentIDs.get(i)));
						row.add(Double.toString(times.get(i)));
						row.add(Double.toString(lengths.get(i)));
						data.add(row.toArray(new String[0]));
					}
					writer.writeAll(data);

				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			else {
				this.resultMessage += "Can't save the result file !";
			}
		}
	}

}
