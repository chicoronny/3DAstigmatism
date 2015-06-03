package org.swing;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;

  
@SuppressWarnings("serial")
public class PluginFrame extends JFrame{

	private CalibrationPanel cp;
	//private LocalizationPanel lc;
	
	public PluginFrame() {
        initComponents();
    }

    private void initComponents() {
    	/// Calibration
    	cp = new CalibrationPanel();
    	
    	this.add(cp);
    	
    	this.setPreferredSize(new Dimension(300,400));
    	
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width/2-150, dim.height/2-200);
    	
    	pack();
    
    	/// Analysis
    /*	Localizator lc = new Localizator();
    	lc.loadImages("C:/Users/Ries/Desktop/ImagineOptic/Data/Timelaps/data");
    	lc.medianFilter(5,10);
    	lc.runNMS();
    	lc.fitImages();
    */	//lc.showResults();
    }       		
	
                           
}
