/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2018 tools4j.org (Marco Terzer)
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

import java.io.Serializable;
import java.util.Arrays;

/**
 * Utility to calculate mean, variance and standard deviation of a sample based on a sliding window. The window size
 * {@code n} is passed to the constructor, and an array of size {@code n} is allocated to store the window values.
 * Values are added to the window with the {@link #update(double) update(..)} method. Once the window has been fully
 * initialized (after the first {@code n} values}, new values replace the oldest value in the window.
 * <p>
 * The implementation uses {@link MeanVarianceSampler}; the calculations are based on Welford’s Algorithm given in Knuth
 * Vol 2, p 232.
 * <p>
 * This class is <i>NOT</i> thread safe. 
 */
public class MeanVarianceSlidingWindow implements Cloneable, Serializable {

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
		this.window = new double[n];
		this.sampler = new MeanVarianceSampler();
	}

	/**
	 * Constructor used by {@link #clone()} method.
	 *
	 * @param toCopy
	 *            the sliding window to clone
	 */
	protected MeanVarianceSlidingWindow(MeanVarianceSlidingWindow toCopy) {
		this.window = toCopy.window.clone();
		this.sampler = toCopy.sampler.clone();
		this.k = toCopy.k;
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
		if (cnt < window.length) {
			sampler.add(x);
			window[k] = x;
		} else {
			final double r = window[k];
			sampler.replace(r, x);
			window[k] = x;
		}
		k = (k + 1) % window.length;
	}

	/**
	 * Returns the first (oldest) value currently present in the sliding window. Returns 0 if no value has been added
	 * yet.
	 * 
	 * @return the first (oldest) value in the sliding window
	 */
	public double getFirst() {
		return getCount() < window.length ? window[0] : window[k];
	}

	/**
	 * Returns the last (newest) value currently present in the sliding window. Returns 0 if no value has been added
	 * yet.
	 * 
	 * @return the last (newest) value in the sliding window
	 */
	public double getLast() {
		return window[(k - 1 + window.length) % window.length];
	}

	/**
	 * Returns the i-th (oldest) value currently present in the sliding window. Returns {@link #getFirst()} for
	 * {@code i=0} and {@link #getLast()} for {@code i=(window.count-1)}. Throws an exception if {@code i < 0} or if
	 * {@code i >= window.count}.
	 * 
	 * @param i
	 *            the zero-based index, must be in [0...(window.length-1)]
	 * @return the first (oldest) value in the sliding window
	 * @throws IllegalArgumentException
	 *             if {@code i < 0} or if {@code i >= window.count}.
	 * @see #getCount()
	 */
	public double get(int i) {
		final long count = getCount();
		if (i < 0 || i >= count) {
			throw new IndexOutOfBoundsException(
					"index out of bounds: " + i + " not in " + (count == 0 ? "[]" : "[0.." + (count - 1) + "]"));
		}
		return count < window.length ? window[i] : window[(i + k) % window.length];
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
	 * Calculates mean and variance by creating a new sampler and adding all values values in the window to the sampler.
	 * This method is slow but known to be numerically stable and hence may may be used as a reference to compare with
	 * mean/variance values computed directly by this sliding window.
	 * <p>
	 * Every method call recalculates the sampler, which is a liner operation involving a constant multiple of
	 * {@code min(windowLength, count)}.
	 * 
	 * @return a new sampler with mean and variance calculated by adding all window values to the sampler
	 */
	public MeanVarianceSampler calculateMeanVariance() {
		final MeanVarianceSampler sampler = new MeanVarianceSampler();
		final int cnt = (int) Math.min(window.length, getCount());
		for (int i = 0; i < cnt; i++) {
			sampler.add(window[i]);
		}
		return sampler;
	}

	/**
	 * Returns the variance of the sample represented by this sliding window (using the {@code (n-1)} method). Returns 0
	 * if the sample count is zero, and Inf or NaN if count is 1.
	 * <p>
	 * The method is based on calculated values and returns almost immediately (involves a simple division).
	 * 
	 * @return the variance of the sample (bias corrected)
	 */
	public double getVarianceUnbiased() {
		return sampler.getVarianceUnbiased();
	}

	/**
	 * Returns the variance of the sample represented by this sliding window (using the {@code (n)} method). Returns NaN
	 * if the sample count is zero.
	 * <p>
	 * The method is based on calculated values and returns almost immediately (involves a simple division).
	 * 
	 * @return the biased variance of the sample
	 */
	public double getVariance() {
		return sampler.getVariance();
	}

	/**
	 * Returns the standard deviation of the sample represented by this sliding window (using the {@code (n-1)} method).
	 * Returns 0 if the sample count is zero, and Inf or NaN if count is 1.
	 * <p>
	 * The method is based on calculated values and returns almost immediately (involves a square root and division
	 * operation).
	 * 
	 * @return the standard deviation of the sample (bias corrected)
	 */
	public double getStdDevUnbiased() {
		return sampler.getStdDevUnbiased();
	}

	/**
	 * Returns the standard deviation of the sample represented by this sliding window (using the biased {@code (n)}
	 * method). Returns NaN if the sample count is zero.
	 * <p>
	 * The method is based on calculated values and returns almost immediately (involves a square root and division
	 * operation).
	 * 
	 * @return the biased standard deviation of the sample
	 */
	public double getStdDev() {
		return sampler.getStdDev();
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
		return window.length;
	}

	/**
	 * Resets this window to its initial state. The sample count is 0 after this operation.
	 */
	public void reset() {
		final int end = (int) Math.min(window.length, getCount());
		Arrays.fill(window, 0, end, 0d);
		k = 0;
		sampler.reset();
	}


	/**
	 * Returns a clone of this sliding window.
	 *
	 * @return a clone initialised with the state of this current sliding window.
	 */
	public MeanVarianceSlidingWindow clone() {
		return new MeanVarianceSlidingWindow(this);
	}

	/**
	 * Returns true if the specified object is a sliding window in the exact same
	 * state as this sliding window.
	 * <p>
	 * Note that this method is not very efficient since it inspects all elements
	 * of the window.
	 *
	 * @param o the object to be compared with
	 * @return true if o is another sliding window with exactly the same state
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		final MeanVarianceSlidingWindow that = (MeanVarianceSlidingWindow) o;

		if (k != that.k) return false;
		if (!Arrays.equals(window, that.window)) return false;
		return sampler.equals(that.sampler);

	}

	/**
	 * Returns a hash code based on this sliding window's state.
	 * <p>
	 * Note that this method is not very efficient since it inspects all elements
	 * of the window.
	 *
	 * @return hash code based on sampler state.
	 */
	@Override
	public int hashCode() {
		int result = Arrays.hashCode(window);
		result = 31 * result + sampler.hashCode();
		result = 31 * result + k;
		return result;
	}

	/**
	 * Returns a string representation of this sliding window showing size, count, mean, variance and standard
	 * deviation.
	 * 
	 * @return a string representation of this sliding window
	 */
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[length=" + window.length + ",count=" + getCount() + ",mean=" + getMean()
				+ ",var=" + getVariance() + ",std=" + getStdDev() + "]";
	}
}
