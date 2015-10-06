/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 tools4j.org (Marco Terzer)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.tools4j.meanvar;

import java.util.Arrays;

/**
 * Utility to calculate mean, variance and standard deviation of a sample based on a sliding window. The window size
 * {@code n} is passed to the constructor, and an array of size {@code n} is allocated to store the window values.
 * Values are added to the window with the {@link #update(double) update(..)} method. Once the window has been fully
 * initialized (after the first {@code n} values}, new values replace the oldest value in the window.
 * <p>
 * The implementation uses {@link MeanVarianceSampler}; the calculations are based on the algorithm given in Knuth Vol
 * 2, p 232.
 */
public class MeanVarianceSlidingWindow {

	private final int windowSize;
	private final double[] window;
	private final MeanVarianceSampler sampler;
	private int k;

	/**
	 * Constructor with window size {@code n}. An array of size {@code n} is allocated to store the most recent
	 * {@code n} values passed to the {@code #update(double) update(..)} method.
	 * 
	 * @param n
	 *            the window size
	 */
	public MeanVarianceSlidingWindow(int n) {
		this.windowSize = n;
		this.window = new double[n];
		this.sampler = new MeanVarianceSampler();
	}

	/**
	 * Updates mean, variance and standard deviation with the new value. If the window has not yet been fully
	 * initialized (less than {@code n} values where {@code n} denotes the {@link #getWindowSize() window size}, it is
	 * added and the sample {@link #getCount() count} is incremented by one. Once the window has been fully initialized,
	 * the new values replaces the oldest value in the window and {@code count} remains constant (the same as the window
	 * size).
	 * <p>
	 * Calling this method involves a constant number of floating point operations.
	 * 
	 * @param x
	 *            the new value entering the sliding window
	 */
	public void update(double x) {
		final long cnt = getCount();
		if (cnt < windowSize) {
			sampler.add(x);
			window[k] = x;
		} else {
			final double r = window[k];
			sampler.replace(r, x);
			window[k] = x;
		}
		k = (k + 1) % windowSize;
	}

	/**
	 * Returns the first (oldest) value currently present in the sliding window. Returns 0 if no value has been added
	 * yet.
	 * 
	 * @return the first (oldest) value in the sliding window
	 */
	public double getFirst() {
		return getCount() < windowSize ? window[0] : window[k];
	}

	/**
	 * Returns the last (newest) value currently present in the sliding window. Returns 0 if no value has been added
	 * yet.
	 * 
	 * @return the last (newest) value in the sliding window
	 */
	public double getLast() {
		return window[(k - 1 + windowSize) % windowSize];
	}

	/**
	 * Returns the mean value of the sample represented by this sliding window. Returns 0 if the sample count is zero.
	 * <p>
	 * The method returns the calculated value and returns immediately.
	 * 
	 * @return the mean value of the sample
	 */
	public double getMean() {
		return sampler.getMean();
	}

	/**
	 * Calculates the mean value of the sample represented by this sliding window and returns it. The standard method to
	 * calculate mean values is used. Returns 0 if the sample count is zero.
	 * <p>
	 * Every method call recalculates the mean value, a linear operation involving a constant multiple of the sample
	 * {@link #getCount() count}.
	 * 
	 * @return the mean value of the sample freshly calculated using the standard algorithm
	 */
	public double calculateMeanSimple() {
		final long cnt = getCount();
		double sum = 0;
		for (int i = 0; i < cnt; i++) {
			sum += window[i];
		}
		return sum / cnt;
	}

	/**
	 * Returns the variance of the sample represented by this sliding window (using the {@code (n-1)} method). Returns 0
	 * if the sample count is zero, and Inf or NaN if count is 1.
	 * <p>
	 * The method is based on calculated values and returns almost immediately (involves a simple division).
	 * 
	 * @return the variance of the sample
	 */
	public double getVariance() {
		return sampler.getVariance();
	}

	/**
	 * Calculates the variance of the sample represented by this sliding window and returns it. The standard
	 * {@code (n-1} method is used to calculate variance. Returns 0 if the sample count is zero, and Inf or NaN if count
	 * is 1.
	 * <p>
	 * Every method call recalculates the variance, a linear operation involving a constant multiple of the sample
	 * {@link #getCount() count}.
	 * 
	 * @return the variance of the sample freshly calculated using the {@code (n-1)} method
	 */
	public double calculateVarianceSimple() {
		final long cnt = getCount();
		final double mean = calculateMeanSimple();
		double sum = 0;
		for (int i = 0; i < cnt; i++) {
			final double delta = window[i] - mean;
			sum += delta * delta;
		}
		return sum / (cnt - 1);
	}

	/**
	 * Returns the standard deviation of the sample represented by this sliding window (using the {@code (n-1)} method).
	 * Returns 0 if the sample count is zero, and Inf or NaN if count is 1.
	 * <p>
	 * The method is based on calculated values and returns almost immediately (involves a square root and division
	 * operation).
	 * 
	 * @return the standard deviation of the sample
	 */
	public double getStdDev() {
		return sampler.getStdDev();
	}

	/**
	 * Calculates the standard deviation of the sample represented by this sliding window and returns it. The standard
	 * {@code (n-1} method is used to calculate standard deviation. Returns 0 if the sample count is zero, and Inf or
	 * NaN if count is 1.
	 * <p>
	 * Every method call recalculates the standard deviation, a linear operation involving a constant multiple of the
	 * sample {@link #getCount() count}.
	 * 
	 * @return the standard deviation of the sample freshly calculated using the {@code (n-1)} method
	 */
	public double calculateStdDevSimple() {
		return Math.sqrt(calculateVarianceSimple());
	}

	/**
	 * Returns the number of values in the sample, usually the same as the {@link #getWindowSize() window size} but less
	 * if the window is not yet fully initialized.
	 * 
	 * @return the number of values in the sample
	 */
	public long getCount() {
		return sampler.getCount();
	}

	/**
	 * Returns the window size, the value that was passed to the constructor.
	 * 
	 * @return the window size
	 */
	public int getWindowSize() {
		return windowSize;
	}

	/**
	 * Resets this window to its initial state. The sample count is 0 after this operation.
	 */
	public void reset() {
		if (getCount() > 0) {
			Arrays.fill(window, 0);
			k = 0;
			sampler.reset();
		}
	}

	/**
	 * Returns a string representation of this sliding window showing size, count, mean, variance and standard
	 * deviation.
	 * 
	 * @return a string representation of this sliding window
	 */
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[size=" + windowSize + ",count=" + getCount() + ",mean=" + getMean()
				+ ",var=" + getVariance() + ",std=" + getStdDev() + "]";
	}
}
