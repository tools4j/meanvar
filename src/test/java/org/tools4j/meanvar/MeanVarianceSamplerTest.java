/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2017 tools4j.org (Marco Terzer)
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
 * Simple use case and unit test for {@link MeanVarianceSampler}.
 */
public class MeanVarianceSamplerTest {
	
	private static final double TOLERANCE = 1e-16;
	
	@Test
	public void shouldCalculateMean() {
		final MeanVarianceSampler sampler = new MeanVarianceSampler();
		sampler.add(1);
		sampler.add(2);
		sampler.add(3);
		sampler.add(6);
		Assert.assertEquals("unexpected Mean", 3, sampler.getMean(), TOLERANCE);
	}

	@Test
	public void shouldCalculateVarianceUnbiased() {
		final MeanVarianceSampler sampler = new MeanVarianceSampler();
		sampler.add(1);
		sampler.add(2);
		sampler.add(3);
		sampler.add(6);
		Assert.assertEquals("unexpected Var", 4.666666666666667, sampler.getVarianceUnbiased(), TOLERANCE);
	}
	
	@Test
	public void shouldCalculateStdDevUnbiased() {
		final MeanVarianceSampler sampler = new MeanVarianceSampler();
		sampler.add(1);
		sampler.add(2);
		sampler.add(3);
		sampler.add(6);
		Assert.assertEquals("unexpected StdDev", 2.160246899469287, sampler.getStdDevUnbiased(), TOLERANCE);
	}
	
	@Test
	public void shouldUpdateCount() {
		final int n = 100;
		final MeanVarianceSampler sampler = new MeanVarianceSampler();
		for (int count = 0; count < n; count++) {
			Assert.assertEquals("Unexpected count", count, sampler.getCount());
			sampler.add(Math.random());
		}
		Assert.assertEquals("Unexpected count", n, sampler.getCount());
	}

	@Test
	public void shouldReset() {
		final MeanVarianceSampler sampler = new MeanVarianceSampler();
		sampler.add(1);
		sampler.add(2);
		sampler.add(3);
		sampler.add(6);
		Assert.assertEquals("unexpected count before reset", 4, sampler.getCount());

		sampler.reset();
		Assert.assertEquals("unexpected count", 0, sampler.getCount());
		Assert.assertEquals("unexpected mean", 0, sampler.getMean(), 0);
		Assert.assertEquals("unexpected variance", Double.NaN, sampler.getVariance(), 0);
	}

	@Test
	public void shouldCombine() {
		final MeanVarianceSampler sampler1 = new MeanVarianceSampler();
		final MeanVarianceSampler sampler2 = new MeanVarianceSampler();
		sampler1.add(1);
		sampler1.add(2);
		sampler1.add(3);
		sampler2.add(2);
		sampler2.add(3);
		sampler2.add(4);

		final MeanVarianceSampler combined = sampler1.clone();
		Assert.assertEquals("clone should be equal", sampler1, combined);
		combined.combine(sampler2);
		Assert.assertEquals("unexpected count", 6, combined.getCount());
		Assert.assertEquals("unexpected mean", 2.5, combined.getMean(), TOLERANCE);
		Assert.assertEquals("unexpected variance", 0.91666666666666667, combined.getVariance(), TOLERANCE);
	}
}
