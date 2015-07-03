package org.calibration;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.plugin.FolderOpener;
import ij.plugin.ImageCalculator;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;

import org.data.Peak;
import org.filters.MedianFilter;
import org.filters.NMS;
import org.fitter.CentroidFitter;
import org.fitter.LSQFitter;

public class Localizator {

	private NMS nms;
	
	String file_path;
	ImagePlus im;
	int nSlice;
	int roi_size = 10;
	int width, height;
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
    		width = im.getWidth();
    		height = im.getHeight();
        }
	}
	

	public void medianFilter(int sFilter, int nFrames) {
		ShortProcessor sp = new ShortProcessor(width,height);
		for(int i=0;i<height;i++){
			for(int j=0;j<width;j++){
				sp.set(j, i, filterPixel(sFilter,nFrames,j,i));
			}
		}
		
		ImagePlus imMedian = new ImagePlus("median filtered",sp);
		ImageCalculator calcul = new ImageCalculator(); 
 	    calcul.run("Substract", im, imMedian);
 	    //imMedian.show();
	}
	
	public int filterPixel(int sFilter, int nFrames, int j0, int i0){
		
		ImageStack is = im.getStack();
		ImageProcessor ip;
		//int dim = sFilter*sFilter*nFrames;
		//int[] imSample = new int[dim];
		List<Integer> imSample = new ArrayList<Integer>();
		MedianFilter<Integer> mf = new MedianFilter<Integer>();
		int w = (sFilter-1)/2;
		
		int ii,jj;
		
		for(int k=0;k<nFrames;k++){
			ip = is.getProcessor(k+1);
			for(int i=0;i<sFilter;i++){
				for(int j=0;j<sFilter;j++){
					ii = i0+i-w;
					jj = j0+j-w;
					if(ii<0){
						ii = -ii;
					} else if(ii>=height){
						ii = 2*height-1-ii;
					}
					if(jj<0){
						jj = -jj;
					} else if(jj>=width){
						jj = 2*width-1-jj;
					}
					
					//imSample[(k*sFilter+i)*sFilter+j] = ip.get(jj,ii); 
					imSample.add(ip.get(jj,ii));
				}
			}
		}
		
		return mf.fastmedian(imSample);
	}
	
	public void runNMS(){
		nms = new NMS();
		ImageStack is = im.getStack();
		ImageProcessor ip;
		for(int i=0;i<1;i++){																															////////// stack length
			ip = is.getProcessor(i+1);
			nms.run(i+1,ip, 5, 300, ip.getStatistics().mean+3*ip.getStatistics().stdDev,false);													
			peaks.addAll(nms.getPeaks());
			
		}
	}
	
	public boolean isOutside(int x, int y){
		if(x<0 || x>=width || y<0 || y>=height){
			return true;
		} 
		return false;
	}
	
	public void fitImages(){
		LSQFitter lsq = new LSQFitter();

		double[] results = new double[4];
		ImageStack is = im.getStack();
		Peak p;
		int xstart, ystart,xend,yend;

		double[][] execution = new double[23][3];
		double[][] wx = new double[23][3];
		double[][] wy = new double[23][3];
		
		int max=23;
		
		for(int i=0;i<max;i++){
			System.out.println(i);
			
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
			
			// comparison
			long startTime, endTime, duration;
			startTime = System.nanoTime();
			results = CentroidFitter.fitCentroidandWidth(is.getProcessor(p.getSlice()), new Roi(xstart, ystart, xend-xstart, yend-ystart),(int) (is.getProcessor(p.getSlice()).getStatistics().mean+3*is.getProcessor(p.getSlice()).getStatistics().stdDev));
			endTime = System.nanoTime();
			duration = (endTime - startTime)/1000000; 
			//System.out.println(duration);
			//System.out.println("Centroid: "+results[0]+" "+results[1]+" "+results[2]+" "+results[3]);

			execution[i][0] = duration;
			wx[i][0] = results[2];
			wy[i][0] = results[3]; 
			
			
			startTime = System.nanoTime();
			results= lsq.fit1DGsingle(is.getProcessor(p.getSlice()), new Roi(xstart, ystart, xend-xstart, yend-ystart), 100000, 100000);
			endTime = System.nanoTime();
			duration = (endTime - startTime)/1000000; 
			//System.out.println(duration);
			//System.out.println("1D LSQ: "+results[0]+" "+results[1]+" "+results[2]+" "+results[3]);
			
			execution[i][1] = duration;
			wx[i][1] = results[2];
			wy[i][1] = results[3]; 
			
			startTime = System.nanoTime();
			results= lsq.fit2Dsingle(is.getProcessor(p.getSlice()), new Roi(xstart, ystart, xend-xstart, yend-ystart), 100000, 100000);
			endTime = System.nanoTime();
			duration = (endTime - startTime)/1000000; 
			//System.out.println(duration);
			//System.out.println("2D LSQ: "+results[0]+" "+results[1]+" "+results[2]+" "+results[3]);
			
			execution[i][2] = duration;
			wx[i][2] = results[2];
			wy[i][2] = results[3]; 
			
			p.set((int)results[0], (int) results[1], results[2], results[3]);
		}
		
		System.out.println("--------------------------------- Execution");
		for(int i=0;i<max;i++){
				System.out.println(execution[i][0]+"  "+execution[i][1]+"  "+execution[i][2]);
		}
		
		System.out.println("--------------------------------- Wx");
		for(int i=0;i<max;i++){
			System.out.println(wx[i][0]+"  "+wx[i][1]+"  "+wx[i][2]);
		}
		
		System.out.println("--------------------------------- Wy");
		for(int i=0;i<max;i++){
			System.out.println(wy[i][0]+"  "+wy[i][1]+"  "+wy[i][2]);
		}
		
	}
	
	public void showResults(){
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

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	