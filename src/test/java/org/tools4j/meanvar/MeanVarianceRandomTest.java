/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2016 tools4j.org (Marco Terzer)
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

import java.util.Random;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

/**
 * Random data test for {@link MeanVarianceSlidingWindow} and {@link MeanVarianceSampler}.
 * Prints some statistics for different ways to calculate mean and variance.
 */
public class MeanVarianceRandomTest {
	
	private static final Random RND = new Random();
	
	@Rule
	public TestName name = new TestName();
	
	private static final boolean PRINT = true;
	private static final boolean ASSERT = false;
	private static final int WINDOW_SIZE = 20;
//	final int N = 10000000;
	private static final int N = 1000000;

	private static interface Rnd {
		double random();
	}
	private static final Rnd RND_UNIFORM_0_1 = new Rnd() {
		public double random() {
			return RND.nextDouble();
		}
	};
	private static final Rnd RND_UNIFORM_PLUS_MINUS_1 = new Rnd() {
		public double random() {
			return RND.nextBoolean() ? RND.nextDouble() : -RND.nextDouble();
		}
	};
	private static final Rnd RND_GAUSSIAN = new Rnd() {
		public double random() {
			return RND.nextGaussian();
		}
	};

	@Test
	public void testRandomUniform_0_1() {
		runTest(name.getMethodName(), RND_UNIFORM_0_1);
	}
	@Test
	public void testRandomUniform_PlusMinusOne() {
		runTest(name.getMethodName(), RND_UNIFORM_PLUS_MINUS_1);
	}
	@Test
	public void testRandomGaussian() {
		runTest(name.getMethodName(), RND_GAUSSIAN);
	}
	
	private static void runTest(final String testName, final Rnd rnd) {
		final MeanVarianceSlidingWindow winReplace = new MeanVarianceSlidingWindow(WINDOW_SIZE);
		final MeanVarianceSampler sampleAddRemove = new MeanVarianceSampler();
		final MeanVarianceSampler sampleRemoveAdd = new MeanVarianceSampler();
		double winReplaceMeanErr, sampleAddRemoveMeanErr, sampleRemoveAddMeanErr;
		double winReplaceStdErr, sampleAddRemoveStdErr, sampleRemoveAddStdErr;
		winReplaceMeanErr = sampleAddRemoveMeanErr = sampleRemoveAddMeanErr = 0;
		winReplaceStdErr = sampleAddRemoveStdErr = sampleRemoveAddStdErr = 0;
		for (int i = 0; i < N; i++) {
			final double x = rnd.random();
			final double r = winReplace.getFirst();
			winReplace.update(x);
			sampleAddRemove.add(x);
			if (i >= WINDOW_SIZE) sampleAddRemove.remove(r);
			if (i >= WINDOW_SIZE) sampleRemoveAdd.remove(r);
			sampleRemoveAdd.add(x);

			if (i >= WINDOW_SIZE && i < N - WINDOW_SIZE) {
				final MeanVarianceSampler ref = winReplace.calculateMeanVariance();
				final double refMean = ref.getMean();
				final double refStd = ref.getStdDev();
				winReplaceMeanErr = Math.max(winReplaceMeanErr, Math.abs(winReplace.getMean() - refMean));
				winReplaceStdErr = Math.max(winReplaceStdErr, Math.abs(winReplace.getStdDev() - refStd));
				sampleAddRemoveMeanErr = Math.max(sampleAddRemoveMeanErr, Math.abs(sampleAddRemove.getMean() - refMean));
				sampleAddRemoveStdErr = Math.max(sampleAddRemoveStdErr, Math.abs(sampleAddRemove.getStdDev() - refStd));
				sampleRemoveAddMeanErr = Math.max(sampleRemoveAddMeanErr, Math.abs(sampleRemoveAdd.getMean() - refMean));
				sampleRemoveAddStdErr = Math.max(sampleRemoveAddStdErr, Math.abs(sampleRemoveAdd.getStdDev() - refStd));
			}
		}
		if (PRINT) {
			System.out.println();
			System.out.println(testName);
			System.out.println("...        Replace method        \t Add/Remove method    \t Remove/Add method");
			System.out.println("...Mean:   " + winReplaceMeanErr + "\t " + sampleAddRemoveMeanErr + "\t " + sampleRemoveAddMeanErr);
			System.out.println("...StdDev: " + winReplaceStdErr + "\t " + sampleAddRemoveStdErr + "\t " + sampleRemoveAddStdErr);
		}
		if (ASSERT) {
			assertIsLess("Replace should be better than add/remove to calculate Mean", winReplaceMeanErr, sampleAddRemoveMeanErr);
			assertIsLess("Replace should be better than remove/add to calculate Mean", winReplaceMeanErr, sampleRemoveAddMeanErr);
			assertIsLess("Replace should be better than add/remove to calculate StdDev", winReplaceStdErr, sampleAddRemoveStdErr);
			assertIsLess("Replace should be better than remove/add to calculate StdDev", winReplaceStdErr, sampleRemoveAddStdErr);
		}
	}
	
	private static void assertIsLess(String msg, double value1, double value2) {
		Assert.assertTrue(msg + " (expected: " + value1 + " < " + value2 + ")", value1 < value2);
		
	}
}
