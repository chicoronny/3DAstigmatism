package org.micromanager.AstigPlugin.tools;

import java.util.ArrayList;
import java.util.Random;

/**
 * A generic, re-usable histogram class that supports partial aggregations.
 * The algorithm is a heuristic adapted from the following paper:
 * Yael Ben-Haim and Elad Tom-Tov, "A streaming parallel decision tree algorithm",
 * J. Machine Learning Research 11 (2010), pp. 849--872. Although there are no approximation
 * guarantees, it appears to work well with adequate data and a large (e.g., 20-80) number
 * of histogram bins.
 */
public class NumericHistogram {
  /**
   * The Coord class defines a histogram bin, which is just an (x,y) pair.
   */
  static class Coord implements Comparable<Object> {
    double x;
    double y;

    public int compareTo(Object other) {
      return Double.compare(x, ((Coord) other).x);
    }
  };

  // Class variables
  private int nbins;
  private int nusedbins;
  private ArrayList<Coord> bins;
  private Random prng;

  /**
   * Creates a new histogram object. Note that the allocate() or merge()
   * method must be called before the histogram can be used.
   */
  public NumericHistogram() {
    nbins = 0;
    nusedbins = 0;
    bins = null;

    // init the RNG for breaking ties in histogram merging. A fixed seed is specified here
    // to aid testing, but can be eliminated to use a time-based seed (which would
    // make the algorithm non-deterministic).
    prng = new Random(System.currentTimeMillis());
  }

  /**
   * Resets a histogram object to its initial state. allocate() or merge() must be
   * called again before use.
   */
  public void reset() {
    bins = null;
    nbins = nusedbins = 0;
  }

  /**
   * Returns the number of bins currently being used by the histogram.
   */
  public int getUsedBins() {
    return nusedbins;
  }

  /**
   * Returns true if this histogram object has been initialized by calling merge()
   * or allocate().
   */
  public boolean isReady() {
    return nbins != 0;
  }

  /**
   * Returns a particular histogram bin.
   */
  public Coord getBin(int b) {
    return bins.get(b);
  }

  /**
   * Sets the number of histogram bins to use for approximating data.
   *
   * @param num_bins Number of non-uniform-width histogram bins to use
   */
  public void allocate(int num_bins) {
    nbins = num_bins;
    bins = new ArrayList<Coord>(num_bins);
    nusedbins = 0;
  }

  
  /**
   * Adds a new data point to the histogram approximation. Make sure you have
   * called either allocate() first. This method implements Algorithm #1
   * from Ben-Haim and Tom-Tov, "A Streaming Parallel Decision Tree Algorithm", JMLR 2010.
   *
   * @param v The data point to add to the histogram approximation.
   */
  public void add(double v) {
    // Binary search to find the closest bucket that v should go into.
    // 'bin' should be interpreted as the bin to shift right in order to accommodate
    // v. As a result, bin is in the range [0,N], where N means that the value v is
    // greater than all the N bins currently in the histogram. It is also possible that
    // a bucket centered at 'v' already exists, so this must be checked in the next step.
    int bin = 0;
    for(int l=0, r=nusedbins; l < r; ) {
      bin = (l+r)/2;
      if (bins.get(bin).x > v) {
        r = bin;
      } else {
        if (bins.get(bin).x < v) {
          l = ++bin;
        } else {
          break; // break loop on equal comparator
        }
      }
    }

    // If we found an exact bin match for value v, then just increment that bin's count.
    // Otherwise, we need to insert a new bin and trim the resulting histogram back to size.
    // A possible optimization here might be to set some threshold under which 'v' is just
    // assumed to be equal to the closest bin -- if fabs(v-bins[bin].x) < THRESHOLD, then
    // just increment 'bin'. This is not done now because we don't want to make any
    // assumptions about the range of numeric data being analyzed.
    if (bin < nusedbins && bins.get(bin).x == v) {
      bins.get(bin).y++;
    } else {
      Coord newBin = new Coord();
      newBin.x = v;
      newBin.y = 1;
      bins.add(bin, newBin);

      // Trim the bins down to the correct number of bins.
      if (++nusedbins > nbins) {
        trim();
      }
    }

  }

  /**
   * Trims a histogram down to 'nbins' bins by iteratively merging the closest bins.
   * If two pairs of bins are equally close to each other, decide uniformly at random which
   * pair to merge, based on a PRNG.
   */
  private void trim() {
    while(nusedbins > nbins) {
      // Find the closest pair of bins in terms of x coordinates. Break ties randomly.
      double smallestdiff = bins.get(1).x - bins.get(0).x;
      int smallestdiffloc = 0, smallestdiffcount = 1;
      for(int i = 1; i < nusedbins-1; i++) {
        double diff = bins.get(i+1).x - bins.get(i).x;
        if(diff < smallestdiff)  {
          smallestdiff = diff;
          smallestdiffloc = i;
          smallestdiffcount = 1;
        } else {
          if(diff == smallestdiff && prng.nextDouble() <= (1.0/++smallestdiffcount) ) {
            smallestdiffloc = i;
          }
        }
      }

      // Merge the two closest bins into their average x location, weighted by their heights.
      // The height of the new bin is the sum of the heights of the old bins.
      // double d = bins[smallestdiffloc].y + bins[smallestdiffloc+1].y;
      // bins[smallestdiffloc].x *= bins[smallestdiffloc].y / d;
      // bins[smallestdiffloc].x += bins[smallestdiffloc+1].x / d *
      //   bins[smallestdiffloc+1].y;
      // bins[smallestdiffloc].y = d;

      double d = bins.get(smallestdiffloc).y + bins.get(smallestdiffloc+1).y;
      Coord smallestdiffbin = bins.get(smallestdiffloc);
      smallestdiffbin.x *= smallestdiffbin.y / d;
      smallestdiffbin.x += bins.get(smallestdiffloc+1).x / d * bins.get(smallestdiffloc+1).y;
      smallestdiffbin.y = d;
      // Shift the remaining bins left one position
      bins.remove(smallestdiffloc+1);
      nusedbins--;
    }
  }

  /**
   * Gets an approximate quantile value from the current histogram. Some popular
   * quantiles are 0.5 (median), 0.95, and 0.98.
   *
   * @param q The requested quantile, must be strictly within the range (0,1).
   * @return The quantile value.
   */
  public double quantile(double q) {
    assert(bins != null && nusedbins > 0 && nbins > 0);
    double sum = 0, csum = 0;
    int b;
    for(b = 0; b < nusedbins; b++)  {
      sum += bins.get(b).y;
    }
    for(b = 0; b < nusedbins; b++) {
      csum += bins.get(b).y;
      if(csum / sum >= q) {
        if(b == 0) {
          return bins.get(b).x;
        }

        csum -= bins.get(b).y;
        double r = bins.get(b-1).x +
          (q*sum - csum) * (bins.get(b).x - bins.get(b-1).x)/(bins.get(b).y);
        return r;
      }
    }
    return -1; // for Xlint, code will never reach here
  }
  
  public int getNBins( final int size, final int minBinNumber, final int maxBinNumber ){
		final double q1 = quantile(0.25);
		final double q3 = quantile(0.75);
		final double iqr = q3 - q1;
		final double binWidth = 2 * iqr * Math.pow( size, -0.333 );

		final double range = quantile(1)-quantile(0);
		int nBin = ( int ) ( range / binWidth + 1 );
		if ( nBin > maxBinNumber ){
			nBin = maxBinNumber;
		}
		else if ( nBin < minBinNumber ){
			nBin = minBinNumber;
		}
		return nBin;
	}
  
  public int[] getCounts(){
  	int[] counts = new int[nbins];
  	if (nusedbins < 2)
		counts[0]=(int) getBin(0).y;
  	else	
  		for (int i=0 ; i<nbins;i++)
  			counts[i] = (int) getBin(i).y;

  	return counts;
  }

 }