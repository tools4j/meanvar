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

/**
 * Utility to incrementally calculate mean, variance and standard deviation of a sample. Sample points can be
 * {@link #add(double) added}, {@link #remove(double) removed} or {@link #replace(double, double) replaced}.
 * <p>
 * The implementation is based on the algorithm given in Knuth Vol 2, p 232.
 */
public class MeanVarianceSampler {
	private long count;
	private double mean, s;

	/**
	 * Adds the value {@code x} to the sample. The sample count is incremented by one by this operation,
	 * 
	 * @param x
	 *            the value to remove
	 */
	public void add(double x) {
		count++;
		final double delta = x - mean;
		mean = mean + delta / count;
		s += delta * (x - mean);
	}

	/**
	 * Removes the value {@code x} currently present in this sample. The sample count is decremented by one by this
	 * operation.
	 * 
	 * @param x
	 *            the value to remove
	 */
	public void remove(double x) {
		if (count == 0) {
			throw new IllegalStateException("sample is empty");
		}
		final double deltaOld = x - mean;
		final double countMinus1 = count - 1;
		mean = count / countMinus1 * mean - x / countMinus1;
		final double deltaNew = x - mean;
		s -= deltaOld * deltaNew;
		// s = s - count / countMinus1 * deltaOld * deltaOld;
		count--;
	}

	/**
	 * Replaces the value {@code x} currently present in this sample with the new value {@code y}. In a sliding window,
	 * {@code x} is the value that drops out and {@code y} is the new value entering the window. The sample count
	 * remains constant with this operation.
	 * 
	 * @param x
	 *            the value to remove
	 * @param y
	 *            the value to add
	 */
	public void replace(double x, double y) {
		if (count == 0) {
			throw new IllegalStateException("sample is empty");
		}
		final double deltaYX = y - x;
		final double deltaX = x - mean;
		final double deltaY = y - mean;
		mean = mean + deltaYX / count;
		final double deltaYp = y - mean;
		final long countMinus1 = count - 1;
		s = s - count * (deltaX * deltaX - deltaY * deltaYp) / countMinus1 - (deltaYX * deltaYp) / countMinus1;
		// s = s - (count * deltaX * deltaX - count * deltaY * deltaYp) / countMinus1 - (deltaYX * deltaYp) /
		// countMinus1;
	}

	/**
	 * Returns the mean value of the sample. Returns 0 if the sample count is zero.
	 * <p>
	 * The method returns the calculated value and returns immediately.
	 * 
	 * @return the mean value of the sample
	 */
	public double getMean() {
		return mean;
	}

	/**
	 * Returns the variance of the sample using the {@code (n-1)} method. Returns 0 if the sample count is zero, and Inf
	 * or NaN if count is 1.
	 * <p>
	 * The method is based on calculated values and returns almost immediately (involves a simple division).
	 * 
	 * @return the variance of the sample (bias corrected)
	 */
	public double getVariance() {
		return count > 0 ? s / (count - 1) : 0;// yes, this returns Inf if count==1
	}

	/**
	 * Returns the variance of the sample using the {@code (n)} method. Returns NaN if count is 0.
	 * <p>
	 * The method is based on calculated values and returns almost immediately (involves a simple division).
	 * 
	 * @return the biased variance of the sample
	 */
	public double getVarianceBiased() {
		return s / count;// yes, this returns NaN if count==0
	}

	/**
	 * Returns the standard deviation of the sample using the {@code (n-1)} method. Returns 0 if the sample
	 * count is zero, and Inf or NaN if count is 1.
	 * <p>
	 * The method is based on calculated values and returns almost immediately (involves a square root and division
	 * operation).
	 * 
	 * @return the standard deviation of the sample (bias corrected)
	 */
	public double getStdDev() {
		return Math.sqrt(getVariance());
	}

	/**
	 * Returns the standard deviation of the sample using the {@code (n)} method. Returns NaN if count is 0.
	 * <p>
	 * The method is based on calculated values and returns almost immediately (involves a square root and division
	 * operation).
	 * 
	 * @return the biased standard deviation of the sample
	 */
	public double getStdDevBiased() {
		return Math.sqrt(getVarianceBiased());
	}

	/**
	 * Returns the number of values in the sample.
	 * 
	 * @return the number of values in the sample
	 */
	public long getCount() {
		return count;
	}

	/**
	 * Resets this sampler to its initial state. The sample count is 0 after this operation.
	 */
	public void reset() {
		count = 0;
		mean = 0;
		s = 0;
	}

	/**
	 * Returns a string representation of this sampler showing count, mean, variance and standard deviation.
	 * 
	 * @return a string representation of this sampler.
	 */
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[count=" + count + ",mean=" + mean + ",var=" + getVariance() + ",std="
				+ getStdDev() + "]";
	}

}
