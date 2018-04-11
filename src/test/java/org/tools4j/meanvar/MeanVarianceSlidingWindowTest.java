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

import org.junit.Assert;
import org.junit.Test;

/**
 * Simple use case and unit test for {@link MeanVarianceSlidingWindow}.
 */
public class MeanVarianceSlidingWindowTest {
	
	private static final double TOLERANCE = 1e-16;

	@Test
	public void shouldGetWindowSize() {
		final int windowSize = 3;
		final MeanVarianceSlidingWindow win = new MeanVarianceSlidingWindow(windowSize);
		Assert.assertEquals("window size", windowSize, win.getWindowSize());
	}

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
	public void shouldCalculateVarianceUnbiased() {
		final int windowSize = 3;
		final MeanVarianceSlidingWindow win = new MeanVarianceSlidingWindow(windowSize);

		Assert.assertEquals("unexpected Var", 0.0, win.getVarianceUnbiased(), TOLERANCE);

		win.update(1);
		Assert.assertFalse("unexpected Var", isFinite(win.getVarianceUnbiased()));

		win.update(2);
		Assert.assertEquals("unexpected Var", 0.5, win.getVarianceUnbiased(), TOLERANCE);
		
		win.update(3);
		Assert.assertEquals("unexpected Var", 1.0, win.getVarianceUnbiased(), TOLERANCE);
		
		//window is full
		
		//1 drops out now
		win.update(4);
		Assert.assertEquals("unexpected Var", 1.0, win.getVarianceUnbiased(), TOLERANCE);

		//2 drops out now
		win.update(5);
		Assert.assertEquals("unexpected Var", 1.0, win.getVarianceUnbiased(), TOLERANCE);

		//3 drops out now
		win.update(-4.5);
		Assert.assertEquals("unexpected Var", 27.25, win.getVarianceUnbiased(), TOLERANCE);

		//4 drops out now
		win.update(-0.5);
		Assert.assertEquals("unexpected Var", 22.75, win.getVarianceUnbiased(), TOLERANCE);
	}

	@Test
	public void shouldCalculateVariance() {
		final int windowSize = 3;
		final MeanVarianceSlidingWindow win = new MeanVarianceSlidingWindow(windowSize);

		Assert.assertFalse("unexpected Biased Var", isFinite(win.getVariance()));

		win.update(1);
		Assert.assertEquals("unexpected Biased Var", 0, win.getVariance(), TOLERANCE);

		win.update(2);
		Assert.assertEquals("unexpected Biased Var", 0.25, win.getVariance(), TOLERANCE);
		
		win.update(3);
		Assert.assertEquals("unexpected Biased Var", 2.0/3, win.getVariance(), TOLERANCE);
		
		//window is full
		
		//1 drops out now
		win.update(4);
		Assert.assertEquals("unexpected Biased Var", 2.0/3, win.getVariance(), TOLERANCE);

		//2 drops out now
		win.update(5);
		Assert.assertEquals("unexpected Biased Var", 2.0/3, win.getVariance(), TOLERANCE);

		//3 drops out now
		win.update(-4.5);
		Assert.assertEquals("unexpected Biased Var", 18.16666666666666667, win.getVariance(), TOLERANCE);

		//4 drops out now
		win.update(-0.5);
		Assert.assertEquals("unexpected Biased Var", 15.16666666666666667, win.getVariance(), TOLERANCE);
	}

	@Test
	public void shouldCalculateStdDevUnbiased() {
		final int windowSize = 3;
		final MeanVarianceSlidingWindow win = new MeanVarianceSlidingWindow(windowSize);

		Assert.assertEquals("unexpected StdDev", 0, win.getStdDevUnbiased(), TOLERANCE);

		win.update(1);
		Assert.assertFalse("unexpected StdDev", isFinite(win.getStdDevUnbiased()));

		win.update(2);
		Assert.assertEquals("unexpected StdDev", Math.sqrt(0.5), win.getStdDevUnbiased(), TOLERANCE);
		
		win.update(3);
		Assert.assertEquals("unexpected StdDev", 1.0, win.getStdDevUnbiased(), TOLERANCE);
		
		//window is full
		
		//1 drops out now
		win.update(4);
		Assert.assertEquals("unexpected StdDev", 1.0, win.getStdDevUnbiased(), TOLERANCE);

		//2 drops out now
		win.update(5);
		Assert.assertEquals("unexpected StdDev", 1.0, win.getStdDevUnbiased(), TOLERANCE);

		//3 drops out now
		win.update(-4.5);
		Assert.assertEquals("unexpected StdDev", Math.sqrt(27.25), win.getStdDevUnbiased(), TOLERANCE);

		//4 drops out now
		win.update(-0.5);
		Assert.assertEquals("unexpected StdDev", Math.sqrt(22.75), win.getStdDevUnbiased(), TOLERANCE);
	}

	@Test
	public void shouldCalculateStdDev() {
		final int windowSize = 3;
		final MeanVarianceSlidingWindow win = new MeanVarianceSlidingWindow(windowSize);

		Assert.assertTrue("unexpected StdDev: " + win.getStdDev(), Double.isNaN(win.getStdDev()));

		win.update(1);
		Assert.assertEquals("unexpected StdDev", 0, win.getStdDev(), TOLERANCE);

		win.update(2);
		Assert.assertEquals("unexpected StdDev", Math.sqrt(0.25), win.getStdDev(), TOLERANCE);
		
		win.update(3);
		Assert.assertEquals("unexpected StdDev", Math.sqrt(2.0/3), win.getStdDev(), TOLERANCE);
		
		//window is full
		
		//1 drops out now
		win.update(4);
		Assert.assertEquals("unexpected StdDev", Math.sqrt(2.0/3), win.getStdDev(), TOLERANCE);

		//2 drops out now
		win.update(5);
		Assert.assertEquals("unexpected StdDev", Math.sqrt(2.0/3), win.getStdDev(), TOLERANCE);

		//3 drops out now
		win.update(-4.5);
		Assert.assertEquals("unexpected StdDev", Math.sqrt(18.16666666666666667), win.getStdDev(), TOLERANCE);

		//4 drops out now
		win.update(-0.5);
		Assert.assertEquals("unexpected StdDev", Math.sqrt(15.16666666666666667), win.getStdDev(), TOLERANCE);
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

	@Test(expected = IndexOutOfBoundsException.class)
	public void shouldNotGetWhenEmpty() {
		final int windowSize = 3;
		final MeanVarianceSlidingWindow win = new MeanVarianceSlidingWindow(windowSize);
		win.get(0);
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void shouldNotGetNegativeIndex() {
		final int windowSize = 3;
		final MeanVarianceSlidingWindow win = new MeanVarianceSlidingWindow(windowSize);
		win.update(1);
		win.update(2);
		win.update(3);
		win.get(-1);
	}

	@Test
	public void shouldClone() {
		final int windowSize = 3;
		final MeanVarianceSlidingWindow win = new MeanVarianceSlidingWindow(windowSize);
		win.update(1);
		win.update(2);
		win.update(3);
		final MeanVarianceSlidingWindow win2 = win.clone();
		Assert.assertNotSame("clones not same object", win, win2);
		Assert.assertEquals("clones are equal objects", win, win2);
	}

	@Test
	public void shouldReset() {
		final int windowSize = 3;
		final MeanVarianceSlidingWindow win1 = new MeanVarianceSlidingWindow(windowSize);
		final MeanVarianceSlidingWindow win2 = new MeanVarianceSlidingWindow(windowSize);
		win1.update(1);
		win1.update(2);
		win1.update(3);
		Assert.assertEquals("count is 3", 3, win1.getCount());
		win1.reset();
		Assert.assertEquals("count is 0", 0, win1.getCount());
		Assert.assertEquals("win1 same as empty one", win1, win2);
	}

	@Test
	public void shouldHashAndEqual() {
		final MeanVarianceSlidingWindow win3a = new MeanVarianceSlidingWindow(3);
		final MeanVarianceSlidingWindow win3b = new MeanVarianceSlidingWindow(3);
		final MeanVarianceSlidingWindow win4 = new MeanVarianceSlidingWindow(4);
		Assert.assertEquals("empty same length windows should be equal", win3a, win3b);
		Assert.assertEquals("empty same length windows should have same hash", win3a.hashCode(), win3b.hashCode());
		Assert.assertNotEquals("empty different length windows should not be equal", win3a, win4);
		win3a.update(1);
		win3b.update(1);
		Assert.assertEquals("1-element windows should be equal", win3a, win3b);
		Assert.assertEquals("1-element windows should have same hash", win3a.hashCode(), win3b.hashCode());
		win3a.update(2);
		win3a.update(3);
		win3b.update(2);
		win3b.update(3);
		Assert.assertEquals("3-element windows should be equal", win3a, win3b);
		Assert.assertEquals("3-element windows should have same hash", win3a.hashCode(), win3b.hashCode());
		win3b.update(10);
		Assert.assertNotEquals("windows should not be equal", win3a, win3b);
	}

	@Test
	public void shouldToString() {
		final int windowSize = 3;
		final MeanVarianceSlidingWindow win = new MeanVarianceSlidingWindow(windowSize);
		Assert.assertEquals("empty window string", "MeanVarianceSlidingWindow[length=3,count=0,mean=0.0,var=NaN,std=NaN]", win.toString());
		win.update(1);
		Assert.assertEquals("1-element samplers string", "MeanVarianceSlidingWindow[length=3,count=1,mean=1.0,var=0.0,std=0.0]", win.toString());
		win.update(1);
		win.update(4);
		win.update(4);
		Assert.assertEquals("empty samplers string", "MeanVarianceSlidingWindow[length=3,count=3,mean=3.0,var=2.0,std=1.4142135623730951]", win.toString());
	}

    private static boolean isFinite(double d) {
        return Math.abs(d) <= Double.MAX_VALUE;
    }
}
