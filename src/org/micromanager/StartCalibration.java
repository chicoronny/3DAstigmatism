package org.micromanager;

import javax.swing.SwingUtilities;

import org.swing.PluginFrame;

public class StartCalibration {

	public static void main(String[] args) {
	
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
