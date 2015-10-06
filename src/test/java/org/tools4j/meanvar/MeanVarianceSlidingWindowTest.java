package org.tools4j.meanvar;

import java.util.Random;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

/**
 * Test for {@link MeanVarianceSlidingWindow} and {@link MeanVarianceSampler}.
 */
public class MeanVarianceSlidingWindowTest {
	
	private static final Random RND = new Random();
	
	@Rule
	public TestName name = new TestName();
	
	private static final boolean PRINT = true;
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
				final double refMean = winReplace.calculateMeanSimple();
				final double refStd = winReplace.calculateStdDevSimple();
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
		assertIsLess("Replace should be better than add/remove to calculate Mean", winReplaceMeanErr, sampleAddRemoveMeanErr);
		assertIsLess("Replace should be better than remove/add to calculate Mean", winReplaceMeanErr, sampleRemoveAddMeanErr);
		assertIsLess("Replace should be better than add/remove to calculate StdDev", winReplaceStdErr, sampleAddRemoveStdErr);
		assertIsLess("Replace should be better than remove/add to calculate StdDev", winReplaceStdErr, sampleRemoveAddStdErr);
		
	}
	
	private static void assertIsLess(String msg, double value1, double value2) {
		Assert.assertTrue(msg + " (expected: " + value1 + " < " + value2 + ")", value1 < value2);
		
	}
}
