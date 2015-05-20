package org.swing;

import javax.swing.JProgressBar;

/**
* Link JProgressBar to non swing classes (Frame or Panel).
* 
*/
public class ProgressDisplay {

	JProgressBar progress;
	double length;
	
	public ProgressDisplay(JProgressBar pg, int max){													//// int, double, have to check that no mistake arises from bad conversion
		this.progress = pg;
		this.length = max;
	}
	
	public void updateProgress(int value){
		progress.setValue(scaling(value)+1);
		progress.updateUI();
	}

	private int scaling(double value) {
		
		return (int) (100.* (double) (value+1)/ (double) length);
	}
	
}
