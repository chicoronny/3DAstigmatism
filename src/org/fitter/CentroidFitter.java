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
		//System.out.println(centroid[0]);
		//System.out.println(centroid[1]);
		
		return centroid;
	}
	
	public double[] fitCentroid(ImageProcessor ip_, Roi roi){
		double[] centroid = new double[2];
		int rwidth = (int) roi.getFloatWidth();
		int rheight = (int) roi.getFloatHeight();
		int xstart = (int) roi.getXBase();
		int ystart = (int) roi.getYBase();
		
		// Copy the ImageProcessor and carry on threshold
		ImageProcessor ip = ip_.duplicate();
		
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
		//System.out.println(centroid[0]);
		//System.out.println(centroid[1]);
		
		return centroid;
	}
	
	public double[] fitCentroidandWidth(ImageProcessor ip_, Roi roi, int threshold){
		double[] centroid = new double[4];
		int rwidth = (int) roi.getFloatWidth();
		int rheight = (int) roi.getFloatHeight();
		int xstart = (int) roi.getXBase();
		int ystart = (int) roi.getYBase();
		
		// Copy the ImageProcessor and carry on threshold
		ImageProcessor ip = ip_.duplicate();
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

		// From quickpalm
  /*      double xstd = 0; // stddev x
        double ystd = 0; // stddev y
        double xlstd = 0; // stddev left x
        double xrstd = 0; // stddev right x
        double ylstd = 0; // stddev left y
        double yrstd = 0; // stddev right y
        int xlsum = 0; // left pixel sum
        int xrsum = 0; // right pixel sum
        int ylsum = 0; // left pixel sum
        int yrsum = 0; // right pixel sum
        double sxdev = 0;
        double sydev = 0;
        // get the axial std    
        for (int i=xstart;i<rwidth+xstart;i++){
                for (int j=ystart;j<rheight+ystart;j++){
                        s=ip.get(i, j); 
                        if (s>thrsh){
                                sxdev = (i-centroid[0])*s;
                                sydev = (j-centroid[1])*s;
                                if ((sxdev)<0)
                                {
                                        xlstd+=-sxdev;
                                        xlsum+=s;
                                }
                                else
                                {
                                        xrstd+=sxdev;
                                        xrsum+=s;
                                }
                                if ((sydev)<0)
                                {
                                        ylstd+=-sydev;
                                        ylsum+=s;
                                }
                                else
                                {
                                        yrstd+=sydev;
                                        yrsum+=s;
                                }
                                xstd+=Math.abs(sxdev);
                                ystd+=Math.abs(sydev);
                        }
                }
        }
        xstd/=sum;
        ystd/=sum;
        xlstd/=xlsum;
        xrstd/=xrsum;
        ylstd/=ylsum;
        yrstd/=yrsum;

		centroid[2] = xlstd+xrstd;
		centroid[3] = ylstd+yrstd;*/
		
		double[] Ivaly,Ivalx;
		Ivaly = new double[rheight];
		Ivalx = new double[rwidth];
		
		for(int i=0;i<rheight;i++){
			Ivaly[i] = 0;
			for(int j=0;j<rwidth;j++){
				Ivaly[i] += ip.get(xstart+j,ystart+i)/rheight;
			}

			//System.out.println(Ivaly[i]);
		}
		
		System.out.println("---");
		for(int i=0;i<rwidth;i++){
			Ivalx[i] = 0;
			for(int j=0;j<rheight;j++){
				Ivalx[i] += ip.get(xstart+i,ystart+j)/rwidth;
			}
			
			//System.out.println(Ivalx[i]);
		}

		double sumx=0, stdx=0;
		for(int i=0;i<rwidth;i++){
			sumx += Ivalx[i];
			stdx += Ivalx[i]*(xstart+i-centroid[0])*(xstart+i-centroid[0]);
		}
		stdx /= sumx;
		stdx = Math.sqrt(stdx);
		centroid[2] = stdx;

		double sumy=0, stdy=0;
		for(int i=0;i<rheight;i++){
			sumy += Ivaly[i];
			stdy += Ivaly[i]*(ystart+i-centroid[1])*(ystart+i-centroid[1]);
		}
		stdy /= sumy;
		stdy = Math.sqrt(stdy);
		centroid[3] = stdy;

		
		return centroid;
	}

}