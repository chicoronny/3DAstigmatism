package org.fitter;

import ij.gui.Roi;
import ij.process.ImageProcessor;


/////////////////////////////////////////////////
///
///	Partially inspired from QuickPalm
///

public class CentroidFitter {

	public double[] fitThreshold(ImageProcessor ip_, Roi roi){
		double[] centroid = new double[2];
		int rwidth = (int) roi.getFloatWidth();
		int rheight = (int) roi.getFloatHeight();
		int xstart = (int) roi.getXBase();
		int ystart = (int) roi.getYBase();
		
		// Copy the ImageProcessor and carry on threshold
		ImageProcessor ip = ip_.duplicate();
		ip.setRoi(roi);
		ip.autoThreshold();
		
		// Find centroid
		double sum = 0;
		for(int i=ystart;i<rheight+ystart;i++){
			for(int j=xstart;j<rwidth+xstart;j++){
				if(ip.get(j, i)>0){
					centroid[0] += j;
					centroid[1] += i;
					sum ++;
				}
			}
		}
		centroid[0] = centroid[0]/sum;
		centroid[1] = centroid[1]/sum; 
		
		return centroid;
	}
	
	public double[] fitCentroid(ImageProcessor ip, Roi roi){
		double[] centroid = new double[2];
		int rwidth = (int) roi.getFloatWidth();
		int rheight = (int) roi.getFloatHeight();
		int xstart = (int) roi.getXBase();
		int ystart = (int) roi.getYBase();
		
		// Find centroid
		double sum = 0;
		for(int i=ystart;i<rheight+ystart;i++){
			for(int j=xstart;j<rwidth+xstart;j++){
				if(ip.get(j, i)>0){
					centroid[0] += j*ip.get(j, i);
					centroid[1] += i*ip.get(j, i);
					sum += ip.get(j, i);
				}
			}
		}
		centroid[0] = centroid[0]/sum;
		centroid[1] = centroid[1]/sum; 

		return centroid;
	}
	
	public static double[] fitCentroidandWidth(ImageProcessor ip, Roi roi, int threshold){
		double[] centroid = new double[4];
		int rwidth = (int) roi.getFloatWidth();
		int rheight = (int) roi.getFloatHeight();
		int xstart = (int) roi.getXBase();
		int ystart = (int) roi.getYBase();
		
		int thrsh = threshold;
		
		// Find centroid and widths
		int s = 0;
		double sum = 0;
		for(int i=xstart;i<rwidth+xstart;i++){
			for(int j=ystart;j<rheight+ystart;j++){
				s = ip.get(i, j);
				if(s>thrsh){
					centroid[0] += i*s;
					centroid[1] += j*s;
					sum += s;
				}
			}
		}
		centroid[0] = centroid[0]/sum;
		centroid[1] = centroid[1]/sum; 
		
		double[] Ivaly,Ivalx;
		Ivaly = new double[rheight];
		Ivalx = new double[rwidth];
		
		for(int i=0;i<rheight;i++){
			Ivaly[i] = 0;
			for(int j=0;j<rwidth;j++){
				Ivaly[i] += ip.get(xstart+j,ystart+i)/rheight;
			}
		}
		
		for(int i=0;i<rwidth;i++){
			Ivalx[i] = 0;
			for(int j=0;j<rheight;j++){
				Ivalx[i] += ip.get(xstart+i,ystart+j)/rwidth;
			}
		}

		double sumx=0, stdx=0;
		for(int i=0;i<rwidth;i++){
			sumx += Ivalx[i];
			stdx += Ivalx[i]*(xstart+i-centroid[0])*(xstart+i-centroid[0]);
		}
		stdx /= sumx;
		stdx = Math.sqrt(stdx)*.5;
		centroid[2] = stdx;

		double sumy=0, stdy=0;
		for(int i=0;i<rheight;i++){
			sumy += Ivaly[i];
			stdy += Ivaly[i]*(ystart+i-centroid[1])*(ystart+i-centroid[1]);
		}
		stdy /= sumy;
		stdy = Math.sqrt(stdy)*.5;
		centroid[3] = stdy;

		
		return centroid;
	}

}