/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2021 tools4j.org (Marco Terzer)
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

/**
 * Utility to incrementally calculate mean, variance and standard deviation of a sample. Sample points can be
 * {@link #add(double) added}, {@link #remove(double) removed} or {@link #replace(double, double) replaced}.
 * <p>
 * The implementation is based on Welford’s Algorithm given in Knuth Vol 2, p 232.
 * <p>
 * This class is <i>NOT</i> thread safe. 
 */
public class MeanVarianceSampler implements Cloneable, Serializable {

	private long count;
	private double mean, s;

	/**
	 * Adds the value {@code x} to the sample. The sample count is incremented by one by this operation,
	 * 
	 * @param x
	 *            the value to add
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
	public double getVarianceUnbiased() {
		return count > 0 ? s / (count - 1) : 0;// yes, this returns Inf if count==1
	}

	/**
	 * Returns the variance of the sample using the {@code (n)} method. Returns NaN if count is 0.
	 * <p>
	 * The method is based on calculated values and returns almost immediately (involves a simple division).
	 * 
	 * @return the biased variance of the sample
	 */
	public double getVariance() {
		return s / count;// yes, this returns NaN if count==0
	}

	/**
	 * Returns the standard deviation of the sample using the {@code (n-1)} method. Returns 0 if the sample count is
	 * zero, and Inf or NaN if count is 1.
	 * <p>
	 * The method is based on calculated values and returns almost immediately (involves a square root and division
	 * operation).
	 * 
	 * @return the standard deviation of the sample (bias corrected)
	 */
	public double getStdDevUnbiased() {
		return Math.sqrt(getVarianceUnbiased());
	}

	/**
	 * Returns the standard deviation of the sample using the {@code (n)} method. Returns NaN if count is 0.
	 * <p>
	 * The method is based on calculated values and returns almost immediately (involves a square root and division
	 * operation).
	 * 
	 * @return the biased standard deviation of the sample
	 */
	public double getStdDev() {
		return Math.sqrt(getVariance());
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
	 * Combines this sampler with the specified other sampler. After the operation, this sampler
	 * reflects the combined mean, variance and standard deviation.
	 * <p>
	 * Combining samplers is sometimes useful e.g. if separate parts of a statistic are collected
	 * on separate threads. The combine operation (maybe after calling clone) can be used to
	 * calculate the combined statistics with a lower frequency (e.g. every 1000 data points).
	 *
	 * @param with the sampler with which this sampler is combined
     */
	public void combine(final MeanVarianceSampler with) {
		//e.g. see https://en.wikipedia.org/wiki/Standard_deviation#Combining_standard_deviations
		final long n1 = this.count;
		final long n2 = with.count;
		final double m1 = this.mean;
		final double m2 = with.mean;
		final double s1 = this.s;
		final double s2 = with.s;
		final long n = n1 + n2;
		final double m = (n1 * m1 + n2 * m2) / n;
		final double s = s1 + s2 + n1 * m1 * m1 + n2 * m2 * m2 - n * m * m;
		this.count = n;
		this.mean = m;
		this.s = s;
	}

	/**
	 * Returns a clone of this sampler.
	 *
	 * @return a clone initialised with the state of this current sampler.
     */
	public MeanVarianceSampler clone() {
		try {
			return (MeanVarianceSampler)super.clone();
		} catch (final CloneNotSupportedException e) {
			throw new RuntimeException("Should be Cloneable", e);
		}
	}

	/**
	 * Returns true if the specified object is a sampler in the exact same
	 * state as this sampler.
	 *
	 * @param o the object to be compared with
	 * @return true if o is another sampler with exactly the same state
     */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		final MeanVarianceSampler that = (MeanVarianceSampler) o;

		if (count != that.count) return false;
		if (Double.compare(that.mean, mean) != 0) return false;
		return Double.compare(that.s, s) == 0;

	}

	/**
	 * Returns a hash code based on this sampler's state.
	 *
	 * @return hash code based on sampler state.
     */
	@Override
	public int hashCode() {
		int result;
		long temp;
		result = (int) (count ^ (count >>> 32));
		temp = Double.doubleToLongBits(mean);
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(s);
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		return result;
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
