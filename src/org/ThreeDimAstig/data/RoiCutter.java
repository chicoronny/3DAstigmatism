package org.img;

import ij.ImagePlus;
import ij.process.ImageProcessor;  
import ij.process.ShortProcessor;

public class RoiCutter {

	
	public ImageProcessor cutoutROI(ImagePlus im, int x, int y, int dim){
		
		int len = (dim-1)/2;
		ShortProcessor imp = new ShortProcessor(dim, dim);
		
		for(int i=x-len;i<=x+len;i++){
			for(int j=y-len;j<=y+len;j++){
				if(!(i<0 || j<0 || i>=im.getHeight() || j>=im.getWidth())){
					imp.set(j, i, im.getPixel(j, i)[0]);
				}
			}
		}
		return imp;	
	}
	
	
}
