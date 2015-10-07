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

import org.junit.Assert;
import org.junit.Test;

/**
 * Simple use case and unit test for {@link MeanVarianceSlidingWindow}.
 */
public class MeanVarianceSlidingWindowTest {
	
	private static final double TOLERANCE = 1e-16;

	@Test
	public void shouldCalculateMean() {
		final int windowSize = 3;
		final MeanVarianceSlidingWindow win = new MeanVarianceSlidingWindow(windowSize);

		win.update(1);
		Assert.assertEquals("unexpected Mean", 1, win.getMean(), TOLERANCE);

		win.update(2);
		Assert.assertEquals("unexpected Mean", 1.5, win.getMean(), TOLERANCE);
		
		win.update(3);
		Assert.assertEquals("unexpected Mean", 2, win.getMean(), TOLERANCE);
		
		//window is full
		
		//1 drops out now
		win.update(4);
		Assert.assertEquals("unexpected Mean", 3, win.getMean(), TOLERANCE);

		//2 drops out now
		win.update(5);
		Assert.assertEquals("unexpected Mean", 4, win.getMean(), TOLERANCE);

		//3 drops out now
		win.update(-4.5);
		Assert.assertEquals("unexpected Mean", 1.5, win.getMean(), TOLERANCE);

		//4 drops out now
		win.update(-0.5);
		Assert.assertEquals("unexpected Mean", 0, win.getMean(), TOLERANCE);
	}

	@Test
	public void shouldCalculateVar() {
		final int windowSize = 3;
		final MeanVarianceSlidingWindow win = new MeanVarianceSlidingWindow(windowSize);

		Assert.assertEquals("unexpected Var", 0.0, win.getVariance(), TOLERANCE);

		win.update(1);
		Assert.assertFalse("unexpected Var", isFinite(win.getVariance()));

		win.update(2);
		Assert.assertEquals("unexpected Var", 0.5, win.getVariance(), TOLERANCE);
		
		win.update(3);
		Assert.assertEquals("unexpected Var", 1.0, win.getVariance(), TOLERANCE);
		
		//window is full
		
		//1 drops out now
		win.update(4);
		Assert.assertEquals("unexpected Var", 1.0, win.getVariance(), TOLERANCE);

		//2 drops out now
		win.update(5);
		Assert.assertEquals("unexpected Var", 1.0, win.getVariance(), TOLERANCE);

		//3 drops out now
		win.update(-4.5);
		Assert.assertEquals("unexpected Var", 27.25, win.getVariance(), TOLERANCE);

		//4 drops out now
		win.update(-0.5);
		Assert.assertEquals("unexpected Var", 22.75, win.getVariance(), TOLERANCE);
	}

	@Test
	public void shouldCalculateVarBiased() {
		final int windowSize = 3;
		final MeanVarianceSlidingWindow win = new MeanVarianceSlidingWindow(windowSize);

		Assert.assertFalse("unexpected Biased Var", isFinite(win.getVarianceBiased()));

		win.update(1);
		Assert.assertEquals("unexpected Biased Var", 0, win.getVarianceBiased(), TOLERANCE);

		win.update(2);
		Assert.assertEquals("unexpected Biased Var", 0.25, win.getVarianceBiased(), TOLERANCE);
		
		win.update(3);
		Assert.assertEquals("unexpected Biased Var", 2.0/3, win.getVarianceBiased(), TOLERANCE);
		
		//window is full
		
		//1 drops out now
		win.update(4);
		Assert.assertEquals("unexpected Biased Var", 2.0/3, win.getVarianceBiased(), TOLERANCE);

		//2 drops out now
		win.update(5);
		Assert.assertEquals("unexpected Biased Var", 2.0/3, win.getVarianceBiased(), TOLERANCE);

		//3 drops out now
		win.update(-4.5);
		Assert.assertEquals("unexpected Biased Var", 18.16666666666666667, win.getVarianceBiased(), TOLERANCE);

		//4 drops out now
		win.update(-0.5);
		Assert.assertEquals("unexpected Biased Var", 15.16666666666666667, win.getVarianceBiased(), TOLERANCE);
	}

	@Test
	public void shouldCalculateStdDev() {
		final int windowSize = 3;
		final MeanVarianceSlidingWindow win = new MeanVarianceSlidingWindow(windowSize);

		Assert.assertEquals("unexpected StdDev", 0, win.getStdDev(), TOLERANCE);

		win.update(1);
		Assert.assertFalse("unexpected StdDev", isFinite(win.getStdDev()));

		win.update(2);
		Assert.assertEquals("unexpected StdDev", Math.sqrt(0.5), win.getStdDev(), TOLERANCE);
		
		win.update(3);
		Assert.assertEquals("unexpected StdDev", 1.0, win.getStdDev(), TOLERANCE);
		
		//window is full
		
		//1 drops out now
		win.update(4);
		Assert.assertEquals("unexpected StdDev", 1.0, win.getStdDev(), TOLERANCE);

		//2 drops out now
		win.update(5);
		Assert.assertEquals("unexpected StdDev", 1.0, win.getStdDev(), TOLERANCE);

		//3 drops out now
		win.update(-4.5);
		Assert.assertEquals("unexpected StdDev", Math.sqrt(27.25), win.getStdDev(), TOLERANCE);

		//4 drops out now
		win.update(-0.5);
		Assert.assertEquals("unexpected StdDev", Math.sqrt(22.75), win.getStdDev(), TOLERANCE);
	}

	@Test
	public void shouldCalculateStdDevBiased() {
		final int windowSize = 3;
		final MeanVarianceSlidingWindow win = new MeanVarianceSlidingWindow(windowSize);

		Assert.assertTrue("unexpected StdDev: " + win.getStdDevBiased(), Double.isNaN(win.getStdDevBiased()));

		win.update(1);
		Assert.assertEquals("unexpected StdDev", 0, win.getStdDevBiased(), TOLERANCE);

		win.update(2);
		Assert.assertEquals("unexpected StdDev", Math.sqrt(0.25), win.getStdDevBiased(), TOLERANCE);
		
		win.update(3);
		Assert.assertEquals("unexpected StdDev", Math.sqrt(2.0/3), win.getStdDevBiased(), TOLERANCE);
		
		//window is full
		
		//1 drops out now
		win.update(4);
		Assert.assertEquals("unexpected StdDev", Math.sqrt(2.0/3), win.getStdDevBiased(), TOLERANCE);

		//2 drops out now
		win.update(5);
		Assert.assertEquals("unexpected StdDev", Math.sqrt(2.0/3), win.getStdDevBiased(), TOLERANCE);

		//3 drops out now
		win.update(-4.5);
		Assert.assertEquals("unexpected StdDev", Math.sqrt(18.16666666666666667), win.getStdDevBiased(), TOLERANCE);

		//4 drops out now
		win.update(-0.5);
		Assert.assertEquals("unexpected StdDev", Math.sqrt(15.16666666666666667), win.getStdDevBiased(), TOLERANCE);
	}

	@Test
	public void shouldGetFirstLastIth() {
		double[] vals;
		final int windowSize = 3;
		final MeanVarianceSlidingWindow win = new MeanVarianceSlidingWindow(windowSize);

		Assert.assertEquals("unexpected first", 0, win.getFirst(), 0.0);
		Assert.assertEquals("unexpected last", 0, win.getLast(), 0.0);
		
		win.update(1);
		Assert.assertEquals("unexpected first", 1, win.getFirst(), 0.0);
		Assert.assertEquals("unexpected last", 1, win.getLast(), 0.0);
		vals = new double[]{1.0};
		for (int i = 0; i < vals.length; i++) {
			Assert.assertEquals("unexpected " + i + "-th", vals[i], win.get(i), 0.0);
		}

		win.update(2);
		Assert.assertEquals("unexpected first", 1, win.getFirst(), 0.0);
		Assert.assertEquals("unexpected last", 2, win.getLast(), 0.0);
		vals = new double[]{1.0, 2.0};
		for (int i = 0; i < vals.length; i++) {
			Assert.assertEquals("unexpected " + i + "-th", vals[i], win.get(i), 0.0);
		}
		
		win.update(3);
		Assert.assertEquals("unexpected first", 1, win.getFirst(), 0.0);
		Assert.assertEquals("unexpected last", 3, win.getLast(), 0.0);
		vals = new double[]{1.0, 2.0, 3.0};
		for (int i = 0; i < vals.length; i++) {
			Assert.assertEquals("unexpected " + i + "-th", vals[i], win.get(i), 0.0);
		}
		
		//window is full
		
		//1 drops out now
		win.update(4);
		Assert.assertEquals("unexpected first", 2, win.getFirst(), 0.0);
		Assert.assertEquals("unexpected last", 4, win.getLast(), 0.0);
		vals = new double[]{2.0, 3.0, 4.0};
		for (int i = 0; i < vals.length; i++) {
			Assert.assertEquals("unexpected " + i + "-th", vals[i], win.get(i), 0.0);
		}

		//2 drops out now
		win.update(5);
		Assert.assertEquals("unexpected first", 3, win.getFirst(), 0.0);
		Assert.assertEquals("unexpected last", 5, win.getLast(), 0.0);
		vals = new double[]{3.0, 4.0, 5.0};
		for (int i = 0; i < vals.length; i++) {
			Assert.assertEquals("unexpected " + i + "-th", vals[i], win.get(i), 0.0);
		}

		//3 drops out now
		win.update(-4.5);
		Assert.assertEquals("unexpected first", 4, win.getFirst(), 0.0);
		Assert.assertEquals("unexpected last", -4.5, win.getLast(), 0.0);
		vals = new double[]{4.0, 5.0, -4.5};
		for (int i = 0; i < vals.length; i++) {
			Assert.assertEquals("unexpected " + i + "-th", vals[i], win.get(i), 0.0);
		}

		//4 drops out now
		win.update(-0.5);
		Assert.assertEquals("unexpected first", 5, win.getFirst(), 0.0);
		Assert.assertEquals("unexpected last", -0.5, win.getLast(), 0.0);
		vals = new double[]{5.0, -4.5, -0.5};
		for (int i = 0; i < vals.length; i++) {
			Assert.assertEquals("unexpected " + i + "-th", vals[i], win.get(i), 0.0);
		}
	}

    private static boolean isFinite(double d) {
        return Math.abs(d) <= Double.MAX_VALUE;
    }
}
