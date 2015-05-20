package org.ThreeDimAstig.micromanager;

import ij.IJ;
import ij.ImagePlus;

import javax.swing.SwingUtilities;

import org.ThreeDimAstig.swing.PluginFrame;

public class StartStandAlone {

public static void main(String[] args) {
	
	// start ImageJ
	//new ImageJ();

	// open the Clown sample
	ImagePlus image = IJ.openImage("/Users/ronny/Documents/storm/z calibration 03_segment.tif");
	image.show();
	
	SwingUtilities.invokeLater(new Runnable() {
        public void run() {
            displayJFrame();
        }
    });
		
	}

	protected static void displayJFrame() {
		PluginFrame frame = new PluginFrame();
	    frame.setVisible(true);
	}

}
