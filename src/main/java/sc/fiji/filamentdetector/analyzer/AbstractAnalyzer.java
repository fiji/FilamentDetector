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

import java.util.HashMap;
import java.util.Map;

import org.scijava.plugin.AbstractRichPlugin;

import sc.fiji.filamentdetector.FilamentWorkflow;

public abstract class AbstractAnalyzer extends AbstractRichPlugin implements Analyzer {

	protected String name;
	protected String description;
	protected FilamentWorkflow filamentWorkflow;

	protected boolean saveResults = false;
	protected String resultMessage;
	protected Map<String, Object> results;

	public AbstractAnalyzer() {
		super();
		this.results = new HashMap<>();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public FilamentWorkflow getFilamentWorkflow() {
		return filamentWorkflow;
	}

	@Override
	public void setFilamentWorkflow(FilamentWorkflow filamentWorkflow) {
		this.filamentWorkflow = filamentWorkflow;
	}

	@Override
	public String getResultMessage() {
		return resultMessage;
	}

	@Override
	public boolean isSaveResults() {
		return saveResults;
	}

	@Override
	public void setSaveResults(boolean saveResults) {
		this.saveResults = saveResults;
	}

	@Override
	public Map<String, Object> getResults() {
		return this.results;
	}

}
