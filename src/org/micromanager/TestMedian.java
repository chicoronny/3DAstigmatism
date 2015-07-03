package org.micromanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.filters.MedianFilter;

public class TestMedian {

	public static void main(String[] args) {
		List<Integer> values = new ArrayList<Integer>();
		Random rand = new Random();
		rand.setSeed(12345);
		
		for (int i=0; i<1e+5; i++)
		    values.add(rand.nextInt(10000));
		
		List<Integer> compValues = new ArrayList<Integer>(values);
		Collections.sort(compValues);
		
		List<Integer> selValues = new ArrayList<Integer>(values);
		
		MedianFilter<Integer> mf = new MedianFilter<Integer>();
		Integer median = mf.fastmedian(values);
		Integer selMedian = mf.select(selValues, values.size()/ 2);
		System.out.println("FastMedian:" + median);
		System.out.println("QuickSelect:" + selMedian);
		System.out.println("ExactMedian:" + compValues.get(values.size()/ 2));
	}

}
