package org.micromanager;

import ij.ImagePlus;
import ij.plugin.FolderOpener;

import org.data.Calibration;
import org.main.Pipeline;

public class StartLocalization {
	public static Calibration calib;
	public static ImagePlus loc_im;
	
	public static void main(String[] args) {
		System.out.println("Run localization");
		calib = new Calibration();
        calib.readCSV("C:/Users/Ries/Documents/zs2_cal.csv");

    	loc_im = FolderOpener.open("C:/Users/Ries/Documents/3D");

        Thread t = new Thread() {
	        public void run() {
	        	long startTime = System.nanoTime();

	    		Pipeline p = new Pipeline(calib, 5, "centroid", loc_im.getStack(), true);
	    		p.run();
	    		
	    		long endTime = System.nanoTime();

	        	long duration = (endTime - startTime)/1000000 ;
	        	System.out.println("Duration: "+duration);
	    		
	    		p.saveCSV("C:/Users/Ries/Documents/result");
	        }
	    };
	    t.start();
		
        
	}
}
