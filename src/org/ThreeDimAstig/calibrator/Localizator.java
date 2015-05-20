package org.calibration;

import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.gui.StackWindow;
import ij.plugin.FolderOpener;
import ij.process.ShortProcessor;

import org.data.Peak;
import org.filters.NMS;
import org.fitter.CentroidFitter;
import org.img.RoiCutter;

public class Localizator {

	private NMS nms;
	private RoiCutter rc;
	
	String file_path;
	ImagePlus im;
	int nSlice;
	int roi_size = 10;

	ArrayList<Peak> peaks;
	
	public Localizator(){
		peaks = new ArrayList<Peak>();
	}
	
	public void loadImages(String path){
    	JFileChooser fc = new JFileChooser(path);
    	fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

    	
    	int returnVal = fc.showOpenDialog(new JFrame());
    	 
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            java.io.File file = fc.getSelectedFile();
            file_path = file.getAbsolutePath();
            im = FolderOpener.open(file_path);
            nSlice = im.getNSlices();
        }
	}
	
	public void runNMS(){
		nms = new NMS();
		ImageStack is = im.getStack();
		for(int i=0;i<1;i++){
			nms.run(i+1,is.getProcessor(i+1), 10, 100, 400, false);														////// need autothreshold, or maybe from input
			peaks.addAll(nms.getPeaks());
		}
		System.out.println(peaks.size());
	}
	
	public void fitImages(){
		CentroidFitter cf = new CentroidFitter();
		double[] results = new double[4];
		ImageStack is = im.getStack();
		Peak p;
		int xstart, ystart,xend,yend,width, height;
		width = is.getWidth();
		height = is.getHeight();
		for(int i=0;i<peaks.size();i++){
			p = peaks.get(i);
			xstart = p.getX()-roi_size;
			ystart = p.getY()-roi_size;
			xend = p.getX()+roi_size;
			yend = p.getY()+roi_size;
			
			// Boundaries
			if(xstart<0){
				xstart = 0;
			}
			if(ystart < 0){
				ystart = 0;
			}
			if(xend >= width){
				xend = width-1;
			}
			if(yend >= height){
				yend = height-1;
			}
			
			results = cf.fitCentroidandWidth(is.getProcessor(p.getSlice()), new Roi(xstart, ystart, xend-xstart, yend-ystart));
			p.set((int)results[0], (int) results[1], results[2], results[3]);
		}
	}
	
	public void showResults(){
		int width, height;
		width = im.getWidth();
		height = im.getHeight();
		
		ShortProcessor sp = new ShortProcessor(width,height);
		Peak p;
		int x,y,sx,sy;
		for(int i=0;i<peaks.size();i++){
			p = peaks.get(i);
			x = p.getX();
			y = p.getY();
			sx = (int) p.getSX()/2;
			sy = (int) p.getSY()/2;
			for(int s1 = x-sx;s1<=x+sx;s1++){
				if(s1>=0 && s1<width){
					sp.set(s1, y, 1000);
				}
			}
			for(int s2 = y-sy;s2<=y+sy;s2++){
				if(s2>=0 && s2<height){
					sp.set(x, s2, 1000);
				}
			}
		}
		
		ImagePlus im_loc = new ImagePlus("results",sp);
		im_loc.show();
	}
}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	