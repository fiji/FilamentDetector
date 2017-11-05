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
package fiji.plugin.filamentdetector.overlay;

import fiji.plugin.filamentdetector.model.Filament;
import fiji.plugin.filamentdetector.model.Filaments;
import fiji.plugin.filamentdetector.model.TrackedFilament;
import fiji.plugin.filamentdetector.model.TrackedFilaments;
import net.imagej.ImageJService;
import net.imagej.display.ImageDisplay;

public interface FilamentOverlayService extends ImageJService {

	void add(Filament filament);

	void add(Filaments filaments);

	void add(TrackedFilament trackedFilament);

	void add(TrackedFilaments trackedFilaments);

	void clearSelection();

	void disableOverlay(boolean disable);

	void exportToROIManager();

	int getColorAlpha();

	int getFilamentWidth();

	ImageDisplay getImageDisplay();

	void refresh();

	void remove(Filament filament);

	void remove(Filaments filaments);

	void remove(TrackedFilament trackedFilament);

	void remove(TrackedFilaments trackedFilaments);

	void reset();

	void setColorAlpha(int colorAlpha);

	void setFilamentWidth(int filamentWidth);

	void setImageDisplay(ImageDisplay imageDisplay);

	void setSelected(Filament filament, boolean moveToFrame, boolean clearSelection);

	void setSelected(TrackedFilament trackedFilament, boolean moveToFrame, boolean clearSelection);

	boolean isDrawBoundingBoxes();

	void setDrawBoundingBoxes(boolean drawBoundingBoxes);

	boolean isDrawPlusTips();

	boolean isDrawMinusTips();

	void setDrawPlusTips(boolean drawPlusTips);

	void setDrawMinusTips(boolean drawMinusTips);

	void setTipDiameter(int tipDiameter);

	int getTipDiameter();

	void updateTransparency();

	void updateLineWidth();

	void setViewMode(ImageDisplayMode viewMode);
}
