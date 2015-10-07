# tools4j-meanvar
Tiny Java utility to incrementally calculate Mean and Standard Deviation with a numerically stable algorithm. 

Contains a simple utility class to incrementally calculate moving average and moving
standard deviation of a data series. 

#### More Information
* [MeanVarianceSampler.java](https://github.com/tools4j/meanvar/blob/master/src/main/java/org/tools4j/meanvar/MeanVarianceSampler.java): Utility to add, remove or replace values in a running calculation of mean and variance
* * [MeanVarianceSlidingWindow.java](https://github.com/tools4j/meanvar/blob/master/src/main/java/org/tools4j/meanvar/MeanVarianceSlidingWindow.java): A sliding window of a fixed length to calculate moving average and moving standard deviation of a data series
