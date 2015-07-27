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
    	//loc_im = FolderOpener.open("C:/Users/Ries/Documents/zs2");
        loc_im = FolderOpener.open("C:/Users/Ries/Desktop/ImagineOptic/Data/Timelaps/data/3DMT");

        Thread t = new Thread() {
	        public void run() {
	    		Pipeline p = new Pipeline(calib, 5, "2DG", loc_im.duplicate(), true);
	    		p.run();
	    		p.saveTXT("C:/Users/Ries/Documents/result");
	        }
	    };
	    t.start();
		
        
	}
}
