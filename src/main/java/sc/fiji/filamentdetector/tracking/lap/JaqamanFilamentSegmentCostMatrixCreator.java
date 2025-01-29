/*-
 * #%L
 * A Fiji plugin that allow easy, fast and accurate detection and tracking of biological filaments.
 * %%
 * Copyright (C) 2016 - 2025 Fiji developers.
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
package sc.fiji.filamentdetector.tracking.lap;

import static fiji.plugin.trackmate.tracking.jaqaman.LAPUtils.checkFeatureMap;
import static fiji.plugin.trackmate.tracking.TrackerKeys.KEY_ALLOW_GAP_CLOSING;
import static fiji.plugin.trackmate.tracking.TrackerKeys.KEY_ALLOW_TRACK_MERGING;
import static fiji.plugin.trackmate.tracking.TrackerKeys.KEY_ALLOW_TRACK_SPLITTING;
import static fiji.plugin.trackmate.tracking.TrackerKeys.KEY_ALTERNATIVE_LINKING_COST_FACTOR;
import static fiji.plugin.trackmate.tracking.TrackerKeys.KEY_CUTOFF_PERCENTILE;
import static fiji.plugin.trackmate.tracking.TrackerKeys.KEY_GAP_CLOSING_FEATURE_PENALTIES;
import static fiji.plugin.trackmate.tracking.TrackerKeys.KEY_GAP_CLOSING_MAX_DISTANCE;
import static fiji.plugin.trackmate.tracking.TrackerKeys.KEY_GAP_CLOSING_MAX_FRAME_GAP;
import static fiji.plugin.trackmate.tracking.TrackerKeys.KEY_MERGING_FEATURE_PENALTIES;
import static fiji.plugin.trackmate.tracking.TrackerKeys.KEY_MERGING_MAX_DISTANCE;
import static fiji.plugin.trackmate.tracking.TrackerKeys.KEY_SPLITTING_FEATURE_PENALTIES;
import static fiji.plugin.trackmate.tracking.TrackerKeys.KEY_SPLITTING_MAX_DISTANCE;
import static fiji.plugin.trackmate.util.TMUtils.checkMapKeys;
import static fiji.plugin.trackmate.util.TMUtils.checkParameter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import net.imglib2.algorithm.MultiThreaded;
import net.imglib2.util.Util;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;

import fiji.plugin.trackmate.tracking.jaqaman.costfunction.CostFunction;
import fiji.plugin.trackmate.tracking.jaqaman.costmatrix.CostMatrixCreator;
import fiji.plugin.trackmate.tracking.jaqaman.costmatrix.DefaultCostMatrixCreator;
import fiji.plugin.trackmate.tracking.jaqaman.costmatrix.ResizableDoubleArray;
import fiji.plugin.trackmate.tracking.jaqaman.costmatrix.SparseCostMatrix;
import sc.fiji.filamentdetector.model.Filament;

public class JaqamanFilamentSegmentCostMatrixCreator implements CostMatrixCreator<Filament, Filament>, MultiThreaded {

	private static final String BASE_ERROR_MESSAGE = "[JaqamanFilamentSegmentCostMatrixCreator] ";

	private final Map<String, Object> settings;

	private String errorMessage;

	private SparseCostMatrix scm;

	private long processingTime;

	private List<Filament> uniqueSources;

	private List<Filament> uniqueTargets;

	private final Graph<Filament, DefaultWeightedEdge> graph;

	private double alternativeCost = -1;

	private int numThreads;

	/**
	 * Instantiates a cost matrix creator for the top-left quadrant of the segment
	 * linking cost matrix.
	 * 
	 */
	public JaqamanFilamentSegmentCostMatrixCreator(
		final Graph<Filament, DefaultWeightedEdge> graph,
		final Map<String, Object> settings)
	{
		this.graph = graph;
		this.settings = settings;
		setNumThreads();
	}

	@Override
	public boolean checkInput() {
		final StringBuilder str = new StringBuilder();
		if (!checkSettingsValidity(settings, str)) {
			errorMessage = BASE_ERROR_MESSAGE + "Incorrect settings map:\n" + str.toString();
			return false;
		}
		return true;
	}

	@Override
	public String getErrorMessage() {
		return errorMessage;
	}

	@Override
	public boolean process() {
		final long start = System.currentTimeMillis();

		/*
		 * Extract parameters
		 */

		// Gap closing.
		@SuppressWarnings("unchecked")
		final Map<String, Double> gcFeaturePenalties = (Map<String, Double>) settings
				.get(KEY_GAP_CLOSING_FEATURE_PENALTIES);
		final CostFunction<Filament, Filament> gcCostFunction = getCostFunctionFor(gcFeaturePenalties);
		final int maxFrameInterval = (Integer) settings.get(KEY_GAP_CLOSING_MAX_FRAME_GAP);
		final double gcMaxDistance = (Double) settings.get(KEY_GAP_CLOSING_MAX_DISTANCE);
		final double gcCostThreshold = gcMaxDistance * gcMaxDistance;
		final boolean allowGapClosing = (Boolean) settings.get(KEY_ALLOW_GAP_CLOSING);

		// Merging
		@SuppressWarnings("unchecked")
		final Map<String, Double> mFeaturePenalties = (Map<String, Double>) settings.get(KEY_MERGING_FEATURE_PENALTIES);
		final CostFunction<Filament, Filament> mCostFunction = getCostFunctionFor(mFeaturePenalties);
		final double mMaxDistance = (Double) settings.get(KEY_MERGING_MAX_DISTANCE);
		final double mCostThreshold = mMaxDistance * mMaxDistance;
		final boolean allowMerging = (Boolean) settings.get(KEY_ALLOW_TRACK_MERGING);

		// Splitting
		@SuppressWarnings("unchecked")
		final Map<String, Double> sFeaturePenalties = (Map<String, Double>) settings
				.get(KEY_SPLITTING_FEATURE_PENALTIES);
		final CostFunction<Filament, Filament> sCostFunction = getCostFunctionFor(sFeaturePenalties);
		final boolean allowSplitting = (Boolean) settings.get(KEY_ALLOW_TRACK_SPLITTING);
		final double sMaxDistance = (Double) settings.get(KEY_SPLITTING_MAX_DISTANCE);
		final double sCostThreshold = sMaxDistance * sMaxDistance;

		// Alternative cost
		final double alternativeCostFactor = (Double) settings.get(KEY_ALTERNATIVE_LINKING_COST_FACTOR);
		final double percentile = (Double) settings.get(KEY_CUTOFF_PERCENTILE);

		// Do we have to work?
		if (!allowGapClosing && !allowSplitting && !allowMerging) {
			uniqueSources = Collections.emptyList();
			uniqueTargets = Collections.emptyList();
			scm = new SparseCostMatrix(new double[0], new int[0], new int[0], 0);
			return true;
		}

		/*
		 * Find segment ends, starts and middle points.
		 */

		final boolean mergingOrSplitting = allowMerging || allowSplitting;

		final FilamentGraphSegmentSplitter segmentSplitter = new FilamentGraphSegmentSplitter(graph,
				mergingOrSplitting);
		final List<Filament> segmentEnds = segmentSplitter.getSegmentEnds();
		final List<Filament> segmentStarts = segmentSplitter.getSegmentStarts();

		/*
		 * Generate all middle points list. We have to sort it by the same order we will
		 * sort the unique list of targets, otherwise the SCM will complains it does not
		 * receive columns in the right order.
		 */
		final List<Filament> allMiddles;
		if (mergingOrSplitting) {
			final List<List<Filament>> segmentMiddles = segmentSplitter.getSegmentMiddles();
			allMiddles = new ArrayList<>();
			for (final List<Filament> segment : segmentMiddles) {
				allMiddles.addAll(segment);
			}
		} else {
			allMiddles = Collections.emptyList();
		}

		final Object lock = new Object();

		/*
		 * Sources and targets.
		 */
		final ArrayList<Filament> sources = new ArrayList<>();
		final ArrayList<Filament> targets = new ArrayList<>();
		// Corresponding costs.
		final ResizableDoubleArray linkCosts = new ResizableDoubleArray();

		/*
		 * A. We iterate over all segment ends, targeting 1st the segment starts
		 * (gap-closing) then the segment middles (merging).
		 */

		final ExecutorService executorGCM = Executors.newFixedThreadPool(numThreads);
		for (final Filament source : segmentEnds) {
			executorGCM.submit(new Runnable() {
				@Override
				public void run() {
					final int sourceFrame = source.getFrame();

					/*
					 * Iterate over segment starts - GAP-CLOSING.
					 */

					if (allowGapClosing) {
						for (final Filament target : segmentStarts) {
							// Check frame interval, must be within user
							// specification.
							final int targetFrame = target.getFrame();
							final int tdiff = targetFrame - sourceFrame;
							if (tdiff < 1 || tdiff > maxFrameInterval) {
								continue;
							}

							// Check max distance
							final double cost = gcCostFunction.linkingCost(source, target);
							if (cost > gcCostThreshold) {
								continue;
							}

							synchronized (lock) {
								sources.add(source);
								targets.add(target);
								linkCosts.add(cost);
							}
						}
					}

					/*
					 * Iterate over middle points - MERGING.
					 */

					if (allowMerging) {
						for (final Filament target : allMiddles) {
							// Check frame interval, must be 1.
							final int targetFrame = target.getFrame();
							final int tdiff = targetFrame - sourceFrame;
							if (tdiff != 1) {
								continue;
							}

							// Check max distance
							final double cost = mCostFunction.linkingCost(source, target);
							if (cost > mCostThreshold) {
								continue;
							}

							synchronized (lock) {
								sources.add(source);
								targets.add(target);
								linkCosts.add(cost);
							}
						}
					}
				}
			});
		}
		executorGCM.shutdown();
		try {
			executorGCM.awaitTermination(1, TimeUnit.DAYS);
		} catch (final InterruptedException e) {
			errorMessage = BASE_ERROR_MESSAGE + e.getMessage();
			return false;
		}

		/*
		 * Iterate over middle points targeting segment starts - SPLITTING
		 */
		if (allowSplitting) {
			final ExecutorService executorS = Executors.newFixedThreadPool(numThreads);
			for (final Filament source : allMiddles) {
				executorS.submit(new Runnable() {
					@Override
					public void run() {
						final int sourceFrame = source.getFrame();
						for (final Filament target : segmentStarts) {
							// Check frame interval, must be 1.
							final int targetFrame = target.getFrame();
							final int tdiff = targetFrame - sourceFrame;

							if (tdiff != 1) {
								continue;
							}

							// Check max distance
							final double cost = sCostFunction.linkingCost(source, target);
							if (cost > sCostThreshold) {
								continue;
							}
							synchronized (lock) {
								sources.add(source);
								targets.add(target);
								linkCosts.add(cost);
							}
						}
					}
				});
			}
			executorS.shutdown();
			try {
				executorS.awaitTermination(1, TimeUnit.DAYS);
			} catch (final InterruptedException e) {
				errorMessage = BASE_ERROR_MESSAGE + e.getMessage();
			}
		}
		linkCosts.trimToSize();

		/*
		 * Build a sparse cost matrix from this. If the accepted costs are not empty.
		 */

		if (sources.isEmpty() || targets.isEmpty()) {
			uniqueSources = Collections.emptyList();
			uniqueTargets = Collections.emptyList();
			alternativeCost = Double.NaN;
			scm = null;
			/*
			 * CAREFUL! We return null if no acceptable links are found.
			 */
		} else {

			final DefaultCostMatrixCreator<Filament, Filament> creator =
				new DefaultCostMatrixCreator<>(sources, targets, linkCosts.data,
					alternativeCostFactor, percentile);

			if (!creator.checkInput() || !creator.process()) {
				errorMessage = "Linking track segments: " + creator.getErrorMessage();
				return false;
			}
			/*
			 * Compute the alternative cost from the cost array
			 */

			if (percentile == 1) {
				alternativeCost = alternativeCostFactor * Util.max(linkCosts.data);
			} else {
				alternativeCost = alternativeCostFactor * Util.percentile(linkCosts.data, percentile);
			}

			scm = creator.getResult();
			uniqueSources = creator.getSourceList();
			uniqueTargets = creator.getTargetList();
		}

		final long end = System.currentTimeMillis();
		processingTime = end - start;
		return true;
	}

	protected CostFunction<Filament, Filament> getCostFunctionFor(final Map<String, Double> featurePenalties) {
		final CostFunction<Filament, Filament> costFunction = new BoundingBoxOverlapCostFunction();
		return costFunction;
	}

	@Override
	public SparseCostMatrix getResult() {
		return scm;
	}

	@Override
	public List<Filament> getSourceList() {
		return uniqueSources;
	}

	@Override
	public List<Filament> getTargetList() {
		return uniqueTargets;
	}

	@Override
	public double getAlternativeCostForSource(final Filament source) {
		return alternativeCost;
	}

	@Override
	public double getAlternativeCostForTarget(final Filament target) {
		return alternativeCost;
	}

	@Override
	public long getProcessingTime() {
		return processingTime;
	}

	private static final boolean checkSettingsValidity(final Map<String, Object> settings, final StringBuilder str) {
		if (null == settings) {
			str.append("Settings map is null.\n");
			return false;
		}

		boolean ok = true;
		// Gap-closing
		ok = ok & checkParameter(settings, KEY_ALLOW_GAP_CLOSING, Boolean.class, str);
		ok = ok & checkParameter(settings, KEY_GAP_CLOSING_MAX_DISTANCE, Double.class, str);
		ok = ok & checkParameter(settings, KEY_GAP_CLOSING_MAX_FRAME_GAP, Integer.class, str);
		ok = ok & checkFeatureMap(settings, KEY_GAP_CLOSING_FEATURE_PENALTIES, str);
		// Splitting
		ok = ok & checkParameter(settings, KEY_ALLOW_TRACK_SPLITTING, Boolean.class, str);
		ok = ok & checkParameter(settings, KEY_SPLITTING_MAX_DISTANCE, Double.class, str);
		ok = ok & checkFeatureMap(settings, KEY_SPLITTING_FEATURE_PENALTIES, str);
		// Merging
		ok = ok & checkParameter(settings, KEY_ALLOW_TRACK_MERGING, Boolean.class, str);
		ok = ok & checkParameter(settings, KEY_MERGING_MAX_DISTANCE, Double.class, str);
		ok = ok & checkFeatureMap(settings, KEY_MERGING_FEATURE_PENALTIES, str);
		// Others
		ok = ok & checkParameter(settings, KEY_ALTERNATIVE_LINKING_COST_FACTOR, Double.class, str);
		ok = ok & checkParameter(settings, KEY_CUTOFF_PERCENTILE, Double.class, str);

		// Check keys
		final List<String> mandatoryKeys = new ArrayList<>();
		mandatoryKeys.add(KEY_ALLOW_GAP_CLOSING);
		mandatoryKeys.add(KEY_GAP_CLOSING_MAX_DISTANCE);
		mandatoryKeys.add(KEY_GAP_CLOSING_MAX_FRAME_GAP);
		mandatoryKeys.add(KEY_ALLOW_TRACK_SPLITTING);
		mandatoryKeys.add(KEY_SPLITTING_MAX_DISTANCE);
		mandatoryKeys.add(KEY_ALLOW_TRACK_MERGING);
		mandatoryKeys.add(KEY_MERGING_MAX_DISTANCE);
		mandatoryKeys.add(KEY_ALTERNATIVE_LINKING_COST_FACTOR);
		mandatoryKeys.add(KEY_CUTOFF_PERCENTILE);
		final List<String> optionalKeys = new ArrayList<>();
		optionalKeys.add(KEY_GAP_CLOSING_FEATURE_PENALTIES);
		optionalKeys.add(KEY_SPLITTING_FEATURE_PENALTIES);
		optionalKeys.add(KEY_MERGING_FEATURE_PENALTIES);
		ok = ok & checkMapKeys(settings, mandatoryKeys, optionalKeys, str);

		return ok;
	}

	@Override
	public void setNumThreads() {
		this.numThreads = Runtime.getRuntime().availableProcessors();
	}

	@Override
	public void setNumThreads(final int numThreads) {
		this.numThreads = numThreads;
	}

	@Override
	public int getNumThreads() {
		return numThreads;
	}

}
