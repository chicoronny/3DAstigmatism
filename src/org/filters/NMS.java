package org.filters;


import java.io.PrintWriter;
import java.util.ArrayList;

import org.data.Peak;

import ij.ImagePlus;
import ij.gui.ImageWindow;
import ij.process.ImageProcessor;

public class NMS {
	ImageProcessor imp;
	ImagePlus im_, imtemp_;
	ImageWindow iw;
	int width_, height_;
	int n_,max_num,nSlice_;
	double cutoff_;
	boolean display_;
	boolean win = false;
	
	PrintWriter writer;
	ArrayList<Peak> peaks;
	
	public NMS(){
		peaks = new ArrayList<Peak>();
	}
	
	public void run(int nSlice, ImageProcessor im, int n, int max, double cutoff, boolean display){
		nSlice_ = nSlice;
		width_ = im.getWidth();
		height_ = im.getHeight();
		imp = im;
		n_ = n;
		max_num = max;
		cutoff_ = cutoff;
		display_ = display;
		peaks.clear();
		
		process();
	}
	
	public int getN(){
		int N=0;
		
		if(!(peaks == null)){
			N = peaks.size();
		}
		
		return N;
	}
	
	public Peak getPeakN(int num){
		return peaks.get(num);
	}
	
	public ArrayList<Peak> getPeaks(){
		return peaks;
	}
	
	public void process(){
		int i,j,ii,jj,ll,kk;
		int mi,mj;
		boolean failed=false;
	
		for(i=0;i<=width_-1-n_;i+=n_+1){	// Loop over (n+1)x(n+1)
			for(j=0;j<=height_-1-n_;j+=n_+1){
				mi = i;
				mj = j;
				for(ii=i;ii<=i+n_;ii++){	
					for(jj=j;jj<=j+n_;jj++){
						if(imp.get(ii,jj) > imp.get(mi,mj)){	
							mi = ii;
							mj = jj;
						}
					}
				}
				failed = false;
				
				Outer:
				for(ll=mi-n_;ll<=mi+n_;ll++){	
					for(kk=mj-n_;kk<=mj+n_;kk++){
						if((ll<i || ll>i+n_) || (kk<j || kk>j+n_)){
							if(ll<width_ && ll>0 && kk<height_ && kk>0){		
								if(imp.get(ll,kk)>imp.get(mi,mj) ){
									failed = true;
									break Outer;
								}
							}
						}
					}
				}
				if(!failed && peaks.size()<max_num){
					if(imp.get(mi,mj) > cutoff_){
						peaks.add(new Peak(nSlice_, mi, mj, imp.get(mi,mj)));
					}
				}
			}			
		}	
		
		// Display NMS result on an image
		if(display_){																										//display_
			imtemp_ = new ImagePlus();
			//im_.show();
			imtemp_.hide();
			
			imp = im_.getProcessor();
			int max = (int) imp.getMax();
			for(ll=0;ll<width_;ll++){
				for(kk=0;kk<height_;kk++){
					imp.set(ll, kk, max-imp.get(ll, kk));
				}
			}
			int x,y;


			for(int l=0;l<peaks.size();l++){ 

				x =peaks.get(l).getX();
				y= peaks.get(l).getY();

				for(ll=0;ll<10;ll++){
					
					if(x+ll<width_ && x-ll>0 && y+10<height_ && y-10>0){
						imp.set(x+ll, y+10, 0);
						imp.set(x-ll, y-10, 0);
						imp.set(x+ll, y-10, 0);
						imp.set(x-ll, y+10, 0);

					}
					if(x+10<width_ && x-10>0 && y+ll<height_ && y-ll>0){
						imp.set(x+10, y+ll, 0);
						imp.set(x-10, y-ll, 0);
						imp.set(x+10, y-ll, 0);
						imp.set(x-10, y+ll, 0);
					}
				}
				 
				imp.set(x, y, 5000);
			}

			imtemp_.setProcessor(imp.duplicate());
			/*imtemp_.setProcessor(imp.duplicate());
			  if(!win){
				imtemp_.show();
				win = true;
			}
			imtemp_.update()*/
			imtemp_.show();

		}
		//writer.close();
	}
}
