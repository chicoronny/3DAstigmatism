package org.fitter;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import ij.gui.Roi;
import ij.process.ImageProcessor;

public class WeightedCentroidFitter {

	private ImageProcessor ip;
	private Roi roi;
	private double beta;
	private double alpha;


	public WeightedCentroidFitter(final ImageProcessor ip, final Roi roi, final double alpha, final double beta) {
		this.ip=ip;
		this.roi=roi;
		this.alpha=alpha;
		this.beta=beta;
	}

	
	public double[] fit(){
		double[] centroid = new double[4];
		int rwidth = (int) roi.getFloatWidth();
		int rheight = (int) roi.getFloatHeight();
		int xstart = (int) roi.getXBase();
		int ystart = (int) roi.getYBase();
		Map<Integer,Integer> values = new HashMap<Integer,Integer>();
		Map<Integer,Integer> sorted = new TreeMap<Integer,Integer>(new ValueComparator(values));
		Map<Integer,Double> weighted = new HashMap<Integer, Double>();
		
		ip.resetRoi();
		ip.setRoi(roi);
		double mean = ip.getStatistics().mean;
		int theta = (int) Math.round(beta * mean);
		int it = 0, s = 0, n = 0;
		double sum=0, stdx=0, stdy=0;
		
		for(int i=xstart;i<rwidth+xstart;i++){
			for(int j=ystart;j<rheight+ystart;j++){
				s = ip.get(i, j) - theta;
				s = s > 0 ? s : 0;
				values.put(it++,s);
			}
		}
		sorted.putAll(values);
		
		Iterator<Entry<Integer, Integer>> iter = sorted.entrySet().iterator();
		while (iter.hasNext()){
			Entry<Integer, Integer> entry = iter.next();
			double wv = entry.getValue() * Math.exp(-n++/(alpha*mean));
			weighted.put(entry.getKey(),wv);
			sum += wv;
		}
		
		it = 0;
		for(int i=xstart;i<rwidth+xstart;i++){
			for(int j=ystart;j<rheight+ystart;j++){
				centroid[0] += i*weighted.get(it);
				centroid[1] += j*weighted.get(it);
				it++;
			}
		}
		
		centroid[0] /= sum;
		centroid[1] /= sum;
		it = 0;
		
		for(int i=xstart;i<rwidth+xstart;i++){
			for(int j=ystart;j<rheight+ystart;j++){
				stdx += weighted.get(it)*(i-centroid[0])*(i-centroid[0]);
				stdy += weighted.get(it)*(j-centroid[1])*(j-centroid[1]);
				it++;
			}
		}

		stdx /= sum;
		stdy /= sum;
		stdx = Math.sqrt(stdx);
		stdy = Math.sqrt(stdy);
		
		centroid[2] = stdx;
		centroid[3] = stdy;
		
		return centroid;
	}
	
	class ValueComparator implements Comparator<Integer> {
		 
		Map<Integer, Integer> map;
	 
		public ValueComparator(Map<Integer, Integer> map) {
			this.map = map;
		}

		@Override
		public int compare(Integer a, Integer b) {
			if (map.get(a) >= map.get(b)) {
	            return -1;
	        } else {
	            return 1;
	        } // returning 0 would merge keys 
		}
	 
		
	}
}
